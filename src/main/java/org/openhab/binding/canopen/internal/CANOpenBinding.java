/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canopen.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openhab.binding.canopen.CANOpenBindingProvider;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.entropia.can.CanSocket.CanId;
	

/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 * 
 * @author Jens Geisler
 * @since 1.7.0
 */
public class CANOpenBinding extends AbstractActiveBinding<CANOpenBindingProvider> implements ManagedService, CANMessageReceivedListener {
	// TODO refactor protocol types into different Bindings
	
	private static final Logger logger = 
		LoggerFactory.getLogger(CANOpenBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
	 * method and must not be accessed anymore once the deactivate() method was called or before activate()
	 * was called.
	 */
	private BundleContext bundleContext;

	
	/**
	 * used to store events that we have sent ourselves; we need to remember them for not reacting to them
	 */
	private List<String> ignoreEventList = new ArrayList<String>();

	/** 
	 * the refresh interval which is used to poll values from the CANOpen
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;
	private boolean autoStartAll= false;
	private Set<Integer> autoStartNodes= new HashSet<Integer>();

	private int sdoResponseTimeout= 1000;
	private Set<String> syncInterfaces= new HashSet<String>();
	private int syncMaxVal= 0;
	private int syncVal= 0;
	// TODO timestamp protocol
	
	private Map<Integer, LinkedList<CANOpenItemConfig>> pdoConfigMap= new ConcurrentHashMap<Integer, LinkedList<CANOpenItemConfig>>();
	private Map<String, Integer> itemPDOMap= new ConcurrentHashMap<String, Integer>();
	
	private Map<Integer, CANOpenItemConfig> nmtConfigMap= new ConcurrentHashMap<Integer, CANOpenItemConfig>();
	
	private Map<Integer, SDODeviceManager> sdoDeviceManagerMap= new ConcurrentHashMap<Integer, SDODeviceManager>();

	
	public void bindingChanged(BindingProvider provider, String itemName) {
		super.bindingChanged(provider, itemName);
		
		// register as listener!
		
		if(!((CANOpenBindingProvider) provider).providesBindingFor(itemName)) { // Item was removed
//			logger.debug("removing item " + itemName);
			// TODO provide for removal of unused sockets
			// remove PDO
			Integer pdoId= itemPDOMap.get(itemName);
			if(pdoId!=null) {
				LinkedList<CANOpenItemConfig> pdoList= pdoConfigMap.get(pdoId);
				if(pdoList!=null) {
					ListIterator<CANOpenItemConfig> iterator= pdoList.listIterator();
					while(iterator.hasNext()) {
						if(itemName.equals(iterator.next().getItemName()))
							iterator.remove();
					}
				}
				itemPDOMap.remove(itemName);
			}
			// remove NMT
			Iterator<CANOpenItemConfig> configsIterator= nmtConfigMap.values().iterator();
			while(configsIterator.hasNext()) {
				if(itemName.equals(configsIterator.next().getItemName()))
					configsIterator.remove();
			}

			// remove SDOs
			for(SDODeviceManager manager: sdoDeviceManagerMap.values()) {
				if(manager.removeItemName(itemName)) break;
			}

		} else {
			CANOpenItemConfig itemConfig = ((CANOpenBindingProvider) provider).getItemConfig(itemName);
			ISocketConnection conn= null;
			try {
				conn = CANOpenActivator.getConnection(itemConfig.getCanInterfaceId());
				conn.addMessageReceivedListener(this);
				conn.open();
			} catch (Exception e) {
				logger.error("Error adding listener to or opening socket " + itemConfig.getCanInterfaceId() + ": " + e);
			}
			
			if(conn!=null) {
				initializeItem(conn, itemConfig);
			}
			
			// add PDO
			if(itemConfig.providesTxPDO()) {
				LinkedList<CANOpenItemConfig> pdoList= pdoConfigMap.get(itemConfig.getPDOId());
				if(pdoList==null) {
					pdoList= new LinkedList<CANOpenItemConfig>();
					pdoConfigMap.put(itemConfig.getPDOId(), pdoList);
				}
				pdoList.add(itemConfig);
				itemPDOMap.put(itemName, itemConfig.getPDOId());
			}
			
			// add NMT
			if(itemConfig.providesNMT()) {
				nmtConfigMap.put(itemConfig.getDeviceID(), itemConfig);
			}
			
			// add SDO
			if(itemConfig.providesSDO()) {
				SDODeviceManager manager= sdoDeviceManagerMap.get(itemConfig.getDeviceID());
				if(manager==null) {
					manager= new SDODeviceManager(this, sdoResponseTimeout);
					sdoDeviceManagerMap.put(itemConfig.getDeviceID(), manager);
				}
				manager.add(itemConfig);
			}
			
			logger.debug("added item config " + itemConfig);
		}
	}
	
	private void initializeItem(ISocketConnection conn, CANOpenItemConfig itemConfig) {
	}

	public void allBindingsChanged(BindingProvider provider) {
		super.allBindingsChanged(provider);
		
		CANOpenBindingProvider prov = (CANOpenBindingProvider) provider;
		for (String itemName : prov.getItemNames())
			bindingChanged(prov, itemName);
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		logger.trace("Received command (item='{}', command='{}')", itemName, command.toString());
		if (!isEcho(itemName, command)) {
			for (CANOpenBindingProvider provider : providers) {
				if (provider.providesBindingFor(itemName)) {
					CANOpenItemConfig config = provider.getItemConfig(itemName);
				
					config.sendCommand(command);
					return;
				}
			}
		}
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		logger.debug("Received update (item='{}', state='{}')", itemName, newState.toString());
		if (!isEcho(itemName, newState)) {
			for (CANOpenBindingProvider provider : providers) {
				if (provider.providesBindingFor(itemName)) {
					CANOpenItemConfig config = provider.getItemConfig(itemName);
				
					config.sendState(newState);
					return;
				}
			}		
		} else
			logger.debug("Not sending echo to bus");
	}
		
	private boolean isEcho(String itemName, Type type) {
		String ignoreEventListKey = itemName + type.toString();
		if (ignoreEventList.contains(ignoreEventListKey)) {
			ignoreEventList.remove(ignoreEventListKey);
			logger.trace("We received this event (item='{}', state='{}') from KNX, so we don't send it back again -> ignore!", itemName, type.toString());
			return true;
		}
		else {
			return false;
		}
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {
			
			// to override the default refresh interval one has to add a 
			// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
			refreshInterval= 60000;
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}
			
			sdoResponseTimeout= 1000;
			String sdoResponseTimeoutString = (String) config.get("sdo_timeout");
			if (StringUtils.isNotBlank(sdoResponseTimeoutString)) {
				sdoResponseTimeout = Integer.parseInt(sdoResponseTimeoutString);
			}
			
			autoStartNodes.clear();
			autoStartAll= false;
			String autoStartString = (String) config.get("auto_start_nodes");
			if (StringUtils.isNotBlank(autoStartString)) {
				String[] nodes = autoStartString.split(",");
				for(String node: nodes) {
					if(node.trim().toLowerCase().equals("all"))
						autoStartAll= true;
					
					try {
						autoStartNodes.add(Integer.decode(node));
					} catch (NumberFormatException e) {
					}
				}
			}
			
			syncInterfaces.clear();
			String syncInterfaceString = (String) config.get("sync_master_for");
			if (StringUtils.isNotBlank(syncInterfaceString)) {
				if(syncInterfaceString.contains(","))
					syncInterfaces.addAll(Arrays.asList(syncInterfaceString.split("\\s*,\\s*")));
				else
					syncInterfaces.add(syncInterfaceString.trim());					
				logger.debug("Sync master for: " + syncInterfaces);
			}

			syncMaxVal= 0;
			String syncMaxValString = (String) config.get("sync_max_val");
			if (StringUtils.isNotBlank(syncMaxValString)) {
				try {
					syncMaxVal = Integer.parseInt(syncMaxValString);
					if(syncMaxVal>255) syncMaxVal= 255;
					if(syncMaxVal<0) syncMaxVal= 0;
				} catch (NumberFormatException e) {
					logger.error("Could not parse sync_max_val from string " + syncMaxValString);
				}				
			}
			
			setProperlyConfigured(true);
		}
	}

	@Override
	public void messageReceived(int canID, boolean rtr, byte[] data, ISocketConnection canInterface) {
		// logger.debug("Message received: = " + Util.canMessageToSting(canID, data));
		if(!rtr) {
			// Process NMT
			if((canID & ~0x7F)==0x700) { // NMT error control (bootup and heart beat)
				int deviceID= canID & 0x7F;
				if((autoStartAll || autoStartNodes.contains(new Integer(deviceID))))
					if(data.length>0 && data[0]!=5) {
						byte[] msg= new byte[2];
						msg[0]= 1; // C= start command
						msg[1]= (byte)(deviceID); // node id
						canInterface.send(new CanId(0), msg);
					}
				
				CANOpenItemConfig config= nmtConfigMap.get(deviceID);
				if(config!=null) {
					String itemName= config.getItemName();
					State s= config.nmtToState(data);
					addIgnoreEvent(itemName, s);
					eventPublisher.postUpdate(itemName, s);
				}
				
				return;
			}
			// Process SDO
			if((canID & ~0x7F)==0x580) {
				if(data==null || data.length!=8) {
					logger.error("Received SDO message with less than 8 data bytes: " + Util.canMessageToSting(canID, data));
					return;
				}
				
				int deviceID= canID & 0x7F;
				SDODeviceManager manager= sdoDeviceManagerMap.get(deviceID);
				if(manager!=null) {
					manager.messageReceived(canID, rtr, data, canInterface);
				} else
					logger.warn("Couldn't find SDO handler for device " + deviceID);
				
				return;
			} else {
			// process PDO
				LinkedList<CANOpenItemConfig> pdoList= pdoConfigMap.get(canID);
				if(pdoList!=null) {
					ListIterator<CANOpenItemConfig> iterator= pdoList.listIterator();
					while(iterator.hasNext()) {
						CANOpenItemConfig config= iterator.next();
						String itemName= config.getItemName();
						State s= config.pdoToState(data);
						addIgnoreEvent(itemName, s);
						eventPublisher.postUpdate(itemName, s); 
					}
				}
			}
		}
	}

	/**
	 * Called by the SCR to activate the component with its configuration read from CAS
	 * 
	 * @param bundleContext BundleContext of the Bundle that defines this component
	 * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;

		// the configuration is guaranteed not to be null, because the component definition has the
		// configuration-policy set to require. If set to 'optional' then the configuration may be null
		logger.debug("Entering activate");
		modified(configuration);

		setProperlyConfigured(true);
	}
	
	/**
	 * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
	 * @param configuration Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// to override the default refresh interval one has to add a 
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		refreshInterval= 60000;
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}

		String sdoResponseTimeoutString = (String) configuration.get("sdo_timeout");
		sdoResponseTimeout= 1000;
		if (StringUtils.isNotBlank(sdoResponseTimeoutString)) {
			sdoResponseTimeout = Integer.parseInt(sdoResponseTimeoutString);
		}

		autoStartNodes.clear();
		autoStartAll= false;
		String autoStartString = (String) configuration.get("auto_start_nodes");
		if (StringUtils.isNotBlank(autoStartString)) {
			String[] nodes = autoStartString.split(",");
			for(String node: nodes) {
				if(node.trim().toLowerCase().equals("all"))
					autoStartAll= true;
				
				try {
					autoStartNodes.add(Integer.decode(node));
				} catch (NumberFormatException e) {
				}
			}
		}

		syncInterfaces.clear();
		String syncInterfaceString = (String) configuration.get("sync_master_for");
		if (StringUtils.isNotBlank(syncInterfaceString)) {
			if(syncInterfaceString.contains(","))
				syncInterfaces.addAll(Arrays.asList(syncInterfaceString.split("\\s*,\\s*")));
			else
				syncInterfaces.add(syncInterfaceString.trim());
			
			logger.debug("Sync master for: " + syncInterfaces);
		}

		syncMaxVal= 0;
		String syncMaxValString = (String) configuration.get("sync_max_val");
		if (StringUtils.isNotBlank(syncMaxValString)) {
			try {
				syncMaxVal = Integer.parseInt(syncMaxValString);
				if(syncMaxVal>255) syncMaxVal= 255;
				if(syncMaxVal<0) syncMaxVal= 0;
			} catch (NumberFormatException e) {
				logger.error("Could not parse sync_max_val from string " + syncMaxValString);
			}				
		}
		
	}
	
	/**
	 * Called by the SCR to deactivate the component when either the configuration is removed or
	 * mandatory references are no longer satisfied or the component has simply been stopped.
	 * @param reason Reason code for the deactivation:<br>
	 * <ul>
	 * <li> 0 – Unspecified
     * <li> 1 – The component was disabled
     * <li> 2 – A reference became unsatisfied
     * <li> 3 – A configuration was changed
     * <li> 4 – A configuration was deleted
     * <li> 5 – The component was disposed
     * <li> 6 – The bundle was stopped
     * </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and 
		// should be reset when activating this binding again
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "CANOpen Service";
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		for(SDODeviceManager manager: sdoDeviceManagerMap.values()) {
			manager.refresh();
		}
		if(syncInterfaces.size()>0) {
			byte[] data= null;
			if(syncMaxVal>0) {
				if(++syncVal>=syncMaxVal) syncVal= 0;
				data= new byte[] {(byte)syncVal};
			}
			for(String syncInterfaceId: syncInterfaces) {
					ISocketConnection connection = CANOpenActivator.getConnection(syncInterfaceId);
				try {
					connection.open(); // TODO open always necessary ?
				} catch (Exception e) {
					logger.error("Error opening the connection " + syncInterfaceId);
				}
				connection.send(new CanId(0x80), data);
			}
		}
	}
	
	public void postUpdate(String itemName, State s) {
		logger.debug("Publishing update to " + itemName + " data: " + s);
		addIgnoreEvent(itemName, s);
		eventPublisher.postUpdate(itemName, s);
	}
	
	public void addIgnoreEvent(String itemName, State s) {
		ignoreEventList.add(itemName + s.toString());
	}
}