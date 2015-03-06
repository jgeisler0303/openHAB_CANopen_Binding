package org.openhab.binding.canopen.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.canopen.internal.CANopenSDOMessage.SDOTransactionType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.entropia.can.CanSocket.CanId;

public class SDODeviceManager extends ArrayList<CANOpenItemConfig> implements CANMessageReceivedListener{
	private static final long serialVersionUID = 1775924765752669957L;
	private static final Logger logger = 
			LoggerFactory.getLogger(CANOpenBinding.class);

	private BlockingQueue<SDOTransaction> queue= new LinkedBlockingQueue<SDOTransaction>();
	private SDOTransactionHandler handler= null; // multiples possible, if device can handle multiple requests simultaneously
	private CANOpenBinding binding;
	private int sdoResponseTimeout;

	
	public SDODeviceManager(CANOpenBinding binding, int sdoResponseTimeout) {
		super();
		this.binding = binding;
		this.sdoResponseTimeout = sdoResponseTimeout;
	}

	public void requestSDORead(CANOpenItemConfig config) {
//		logger.debug("queing SDO read request for " + config);
		queue.add(new SDOTransaction(config, SDOTransactionType.UPLOAD, null, true));
	}
	
	public void writeSDO(CANOpenItemConfig config, BigDecimal val, boolean command) {
//		logger.debug("queing SDO write request for " + config);
		queue.add(new SDOTransaction(config, SDOTransactionType.DOWNLOAD, val, command));
	}
	
	public boolean removeItemName(String itemName) {
		ListIterator<CANOpenItemConfig> iter= listIterator();
		while(iter.hasNext()) {
			CANOpenItemConfig config= iter.next();
			if(config.getItemName().equals(itemName)) {
				config.setSDOManager(null);
				iter.remove();
				if(size()==0)
					stopProcessing();
					
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean add(CANOpenItemConfig c) {
//		logger.debug("adding " + c + " to SDO transaction manager");
		c.setSDOManager(this);
		if(c.sdoReadAtInit()) {
//			logger.debug("sending SDO initial read request"); 
			requestSDORead(c);			
		}
		
		if(size()==0)
			startProcessing();
		
		return super.add(c);
	}

	private void startProcessing() {
//		logger.debug("Staring SDO transaction handler");
		if(handler==null)
			handler= new SDOTransactionHandler();
		
		if(!handler.isAlive())
			handler.start();
	}

	private void stopProcessing() {
		if(handler!=null && handler.isAlive()) {
			handler.terminate();
			handler= null;
		}
	}

	@Override
	public void messageReceived(int canID, boolean rtr, byte[] data, ISocketConnection iface) {
		if(handler==null) {
			logger.error("Received SDO response but don't have a handler");
			return;
		}
		if(handler.currentTransaction==null) {
			logger.error("Received SDO response but don't have pending transaction");
			return;
		}
		
		if(handler.currentTransaction.config.expectingSDOResponse()) {
			if(handler.currentTransaction.config.sdoMessage.isExpectedCANopenObject(data)) {
				State s= handler.currentTransaction.config.sdoToState(data, handler.currentTransaction.val);
				if(s!=null && handler.currentTransaction.confirm) {
					binding.postUpdate(handler.currentTransaction.config.getItemName(), s);
				} else
					logger.debug("no confirmation sent for " + handler.currentTransaction.config.getItemName());
				
			} else
				logger.warn("Received SDO response doesn't match expected OD entry");
				
		} else
			logger.warn("Received SDO response but not expecting one");
	}

	public void refresh() {
		for(CANOpenItemConfig c: this)
			if(c.sdoReadRefresh())
				requestSDORead(c);
	}

	public void setSdoResponseTimeout(int sdoResponseTimeout) {
		this.sdoResponseTimeout = sdoResponseTimeout;
	}

	private class SDOTransaction {
		private BigDecimal val;
		private CANOpenItemConfig config;
		private SDOTransactionType type;
		private boolean confirm;
		
		public String toString() {
			String odEntry= "index " + String.format("%04X#", config.sdoMessage.getIndex()) + "sub" + String.format("%02X#", config.sdoMessage.getSubIndex());
			if(type==SDOTransactionType.DOWNLOAD || type==SDOTransactionType.SEG_DOWNLOAD)
				return type + " of " + val + " to " + odEntry;
			else
				return type + " from " + odEntry;
		}
		
		public SDOTransaction(CANOpenItemConfig config,	SDOTransactionType type, BigDecimal val, boolean confirm) {
			super();
			this.config = config;
			this.type = type;
			this.val = val;
			this.confirm = confirm;
		}
		
		public boolean sendRequest() {
			synchronized (config) {
				while(config.expectingSDOResponse())
					try {
						config.wait();  // TODO in case the time out does, for some reason, not trigger this will wait for ever
					} catch (InterruptedException e) {
					}				
			}
			
			switch(type) {
			case DOWNLOAD:
				if(!config.sdoMessage.sdoDownloadInitiateRequest(val)) {
					logger.warn("Failed composing SDO download request");
					return false;
				}
				break;
				
			case UPLOAD:				
				config.sdoMessage.sdoUploadInitiateRequest();
				
				break;
				
			case SEG_DOWNLOAD:
			case SEG_UPLOAD:
			case NONE:
			default:
				return false;
				
			}

			String canInterfaceId= config.getCanInterfaceId();

			ISocketConnection connection = CANOpenActivator.getConnection(canInterfaceId);
			try {
				connection.open();
			} catch (Exception e) {
				logger.error("Error opening the connection " + canInterfaceId);
			}
			config.expectSDO(type);
			connection.send(new CanId(config.sdoMessage.getCanId()), config.sdoMessage.getData());

			return true;
		}
		
	}
	
	private class SDOTransactionHandler extends Thread {
		private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);	
		private boolean active= true;
		private SDOTransaction currentTransaction= null;
		private SDOTimeOut sdoTimeOut= new SDOTimeOut();
		
		@Override
		public void run() {
			while(active) {
				try {
					currentTransaction= queue.take();
					logger.debug("processing SDO request " + currentTransaction);
					if(currentTransaction.sendRequest())
						executor.schedule(sdoTimeOut, sdoResponseTimeout, TimeUnit.MILLISECONDS);
					
					// wait until either a timeout occurs or the response is received
					synchronized (currentTransaction.config) {
						while(currentTransaction.config.expectingSDOResponse()) currentTransaction.config.wait();
						// TODO in case the time out does, for some reason, not trigger this will wait for ever			
					}
					currentTransaction= null;
				} catch (InterruptedException e) {
				}
			}
		}

		public void terminate() {
			active= false;
		}
		
		private class SDOTimeOut implements Runnable {		
			public void run() {
				if(currentTransaction.config.getExpectedSDOResponse()==SDOTransactionType.DOWNLOAD)
					logger.error("Failed to receive response to " + currentTransaction);
				if(currentTransaction.config.getExpectedSDOResponse()==SDOTransactionType.UPLOAD)
					logger.error("Failed to receive response to " + currentTransaction);
	
				currentTransaction.config.expectSDO(SDOTransactionType.NONE);
			}
		}
	}

}
