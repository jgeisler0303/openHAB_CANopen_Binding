package org.openhab.binding.canopen.internal;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.entropia.can.CanSocket.CanId;

public class CANOpenItemConfig implements BindingConfig {
	private static final Logger logger = 
			LoggerFactory.getLogger(CANOpenItemConfig.class);
	
    private static final Map<String, Integer> CANopen_Types;
    static
    {
    	CANopen_Types = new HashMap<String, Integer>();
		CANopen_Types.put("boolean",          0x01);
		CANopen_Types.put("int8",             0x02);
		CANopen_Types.put("int16",            0x03);
		CANopen_Types.put("int32",            0x04);
		CANopen_Types.put("uint8",            0x05);
		CANopen_Types.put("uint16",           0x06);
		CANopen_Types.put("uint32",           0x07);
		CANopen_Types.put("real32",           0x08);
		CANopen_Types.put("visible_string",   0x09);
		CANopen_Types.put("octet_string",     0x0A);
		CANopen_Types.put("unicode_string",   0x0B);
		CANopen_Types.put("time_of_day",      0x0C);
		CANopen_Types.put("time_difference",  0x0D);
	
		CANopen_Types.put("domain",           0x0F);
		CANopen_Types.put("int24",            0x10);
		CANopen_Types.put("real64",           0x11);
		CANopen_Types.put("int40",            0x12);
		CANopen_Types.put("int48",            0x13);
		CANopen_Types.put("int56",            0x14);
		CANopen_Types.put("int64",            0x15);
		CANopen_Types.put("uint24",           0x16);
		CANopen_Types.put("uint40",           0x18);
		CANopen_Types.put("uint48",           0x19);
		CANopen_Types.put("uint56",           0x1A);
		CANopen_Types.put("uint64",           0x1B);
	
		CANopen_Types.put("pdo_communication_parameter", 0x20);
		CANopen_Types.put("pdo_mapping",                 0x21);
		CANopen_Types.put("sdo_parameter",               0x22);
		CANopen_Types.put("identity",                    0x23);
	}
//  from ocera.rtcan.CanOpen.ODNode
//    public final static DataTypeDef dataTypeDefs[] = {
//        new DataTypeDef("UNKNOWN", -1), new DataTypeDef("BOOLEAN", 1),
//        new DataTypeDef("INTEGER8", 1), new DataTypeDef("INTEGER16", 2), new DataTypeDef("INTEGER32", 4),
//        new DataTypeDef("UNSIGNED8", 1), new DataTypeDef("UNSIGNED16", 2), new DataTypeDef("UNSIGNED32", 4),
//        new DataTypeDef("REAL32", 4),
//        new DataTypeDef("VISIBLE_STRING", 0), new DataTypeDef("OCTET_STRING", 0), new DataTypeDef("UNICODE_STRING", 0),
//        new DataTypeDef("TIME_OF_DAY", -1), new DataTypeDef("TIME_DIFERENCE", -1),
//        new DataTypeDef("reserved", -1),
//        new DataTypeDef("DOMAIN", -1),
//        new DataTypeDef("INTEGER24", 3),
//        new DataTypeDef("REAL64", 8),
//        new DataTypeDef("INTEGER40", 5), new DataTypeDef("INTEGER48", 6), new DataTypeDef("INTEGER56", 7), new DataTypeDef("INTEGER64", 8),
//        new DataTypeDef("UNSIGNED24", 3),
//        new DataTypeDef("reserved", -1),
//        new DataTypeDef("UNSIGNED40", 5), new DataTypeDef("UNSIGNED48", 6), new DataTypeDef("UNSIGNED56", 7), new DataTypeDef("UNSIGNED64", 8),
//        new DataTypeDef("reserved", -1), new DataTypeDef("reserved", -1), new DataTypeDef("reserved", -1), new DataTypeDef("reserved", -1),
//        new DataTypeDef("PDO_COMMUNICATION_PARAMETER", -1), new DataTypeDef("PDO_MAPPING", -1),
//        new DataTypeDef("SDO_PARAMETER", -1),
//        new DataTypeDef("IDENTITY", -1)
//   };

    private static final String canInterfaceIdProp= "if";
    private static final String dataTypeProp= "type";
    private static final String deviceIDProp= "id";
    private static final String txCOB_IDProp= "txpdo";
    private static final String txDataOffsetProp= "txofs";
    private static final String rxCOD_IDProp= "rxpdo";
    private static final String odIndexProp= "odidx";
    private static final String odSubIndexProp= "odsubidx";

    private String canInterfaceId= null;
	private String itemName;
	private int dataType= -1;
	private int deviceID= -1;
	private int txCOB_ID= -1;
	private int txDataOffset= -1;
	private int rxCOD_ID= -1;
	private int odIndex= -1;
	private int odSubIndex= -1;
	
	public boolean providesTxPDO= false; 
	public boolean providesRxPDO= false; 
	public boolean providesSDO= false; 
	
	
	public CANOpenItemConfig(Item item, String bindingConfig) throws BindingConfigParseException {
		itemName = item.getName();
		
		String[] props = bindingConfig.split(",");
		
		for(String prop: props) {
			String[] parts= prop.split(":");
			if(parts.length!=2) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + " at property " + prop);
			
			if(parts[0].trim().equalsIgnoreCase(canInterfaceIdProp))
				canInterfaceId= parts[1].trim();
			else if(parts[0].trim().equalsIgnoreCase(deviceIDProp))
				deviceID= parseIntProperty(parts[1].trim(), deviceIDProp, 127, itemName);
			else if(parts[0].trim().equalsIgnoreCase(rxCOD_IDProp))
				rxCOD_ID= parseIntProperty(parts[1].trim(), rxCOD_IDProp, 2047, itemName);
			else if(parts[0].trim().equalsIgnoreCase(txCOB_IDProp))
				txCOB_ID= parseIntProperty(parts[1].trim(), txCOB_IDProp, 2047, itemName);
			else if(parts[0].trim().equalsIgnoreCase(txDataOffsetProp))
				txDataOffset= parseIntProperty(parts[1].trim(), txDataOffsetProp, 7, itemName);
			else if(parts[0].trim().equalsIgnoreCase(odIndexProp))
				odIndex= parseIntProperty(parts[1].trim(), odIndexProp, 65535, itemName);
			else if(parts[0].trim().equalsIgnoreCase(odSubIndexProp))
				odSubIndex= parseIntProperty(parts[1].trim(), odSubIndexProp, 255, itemName);
			else if(parts[0].trim().equalsIgnoreCase(dataTypeProp)) {
				Integer dataType_= CANopen_Types.get(parts[1].trim());
				if(dataType_==null) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". Unknown data type: " + parts[1]);
				if(dataType_!=CANopen_Types.get("real32")) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". Currently only data type real32 supported");
				dataType= dataType_;
			}
			
			if(canInterfaceId==null) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". No interface provided");
			
			if(odIndex>=0 && odSubIndex>=0 && dataType>=0) providesSDO= true;
			if(txCOB_ID>=0 && txDataOffset>=0 && dataType>=0) providesTxPDO= true;
			if(rxCOD_ID>=0 && dataType>=0) providesRxPDO= true;
		}
	}

	private int parseIntProperty(String value, String prop, int maxRange, String item) throws BindingConfigParseException {
		int val;
		try {
			val= Integer.decode(value).intValue();
		} catch (NumberFormatException nfe) {
			throw new BindingConfigParseException("Error parsing binding configuration for item " + item + ". Couldn't parse " + value + " in property " + prop);
		}
		if(val<0 || val>maxRange) throw new BindingConfigParseException("Error parsing binding configuration for item " + item + ". Property " + prop + " out of range");
		return val;
	}
	
	public String getCanInterfaceId() {
		return canInterfaceId;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public void sendCommand(Command cmd, Logger logger) {
		if (cmd instanceof DecimalType) {
			CanId canId= null;
			byte[] data= new byte[0];
			if(providesRxPDO) {
				canId= new CanId(rxCOD_ID);
				data= new byte[4];
				ByteBuffer bb = ByteBuffer.wrap(data);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				bb.putFloat(((DecimalType)cmd).floatValue());
			} else if(providesSDO) {
				
			}
			
			if(canId!=null) {
				ISocketConnection connection = CANOpenActivator.getConnection(canInterfaceId);
				try {
					connection.open();
				} catch (Exception e) {
					logger.error("Error opening the connection " + canInterfaceId);
				}
				connection.send(canId, data);
			}
		}
//		else if (cmd instanceof IncreaseDecreaseType) {
//			return true;
//		}
//		OnOffType
//		OpenClosedType
	}

	public void sendState(State state, Logger logger) {
		if (state instanceof DecimalType)
			sendCommand((DecimalType)state, logger);
	}

	public State pdoToState(byte[] data) {
		if (data != null && dataType>=0 && txCOB_ID>=0 && txDataOffset>=0) {
			ByteBuffer bb = ByteBuffer.wrap(data);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			try {
				float val= bb.getFloat(txDataOffset);
//				logger.debug("Parsed PDO data " + val + " from " + data + "@" + txDataOffset);
				return new DecimalType(new BigDecimal(val));
			} catch (Exception e) {
//				logger.debug("Error converting PDO. " + e);
				return null;
			}
		}
//		logger.debug("No converting PDO " + data);
		return null;
	}

	public String toString() {
		return itemName+"["+
				"if:" + canInterfaceId + "," +
				"type:" + dataType + "," +
				"id:" + deviceID + "," +
				"txcob:0x" + Integer.toString(txCOB_ID, 16).toUpperCase() + "," +
				"txofs:" + txDataOffset + "," +
				"rxcob:0x" + Integer.toString(rxCOD_ID, 16).toUpperCase() + "," +
				"odidx:0x" + Integer.toString(odIndex, 16).toUpperCase() + "," +
				"odsubidx:" + odSubIndex + "]";
	}

	public int getPDOId() {
		return txCOB_ID;
	}

}
