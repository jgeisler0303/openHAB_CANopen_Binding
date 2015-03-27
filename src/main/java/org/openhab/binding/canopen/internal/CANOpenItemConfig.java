package org.openhab.binding.canopen.internal;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.openhab.binding.canopen.internal.CANopenSDOMessage.SDOTransactionType;
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
	/**
	 * @author jgeisler
	 *
	 */
	public static class CANOpenDataType {
		public static final int CANopenTypeBoolean= 0x01;
		public static final int CANopenTypeInt8= 0x02;
		public static final int CANopenTypeInt16= 0x03;
		public static final int CANopenTypeInt32= 0x04;
		public static final int CANopenTypeUint8= 0x05;
		public static final int CANopenTypeUint16= 0x06;
		public static final int CANopenTypeUint32= 0x07;
		public static final int CANopenTypeReal32= 0x08;
		public static final int CANopenTypeVisible_string= 0x09;
		public static final int CANopenTypeOctet_string= 0x0A;
		public static final int CANopenTypeUnicode_string= 0x0B;
		public static final int CANopenTypeTime_of_day= 0x0C;
		public static final int CANopenTypeTime_difference= 0x0D;

		public static final int CANopenTypeDomain= 0x0F;
		public static final int CANopenTypeInt24= 0x10;
		public static final int CANopenTypeReal64= 0x11;
		public static final int CANopenTypeInt40= 0x12;
		public static final int CANopenTypeInt48= 0x13;
		public static final int CANopenTypeInt56= 0x14;
		public static final int CANopenTypeInt64= 0x15;
		public static final int CANopenTypeUint24= 0x16;
		public static final int CANopenTypeUint40= 0x18;
		public static final int CANopenTypeUint48= 0x19;
		public static final int CANopenTypeUint56= 0x1A;
		public static final int CANopenTypeUint64= 0x1B;

		public static final int CANopenTypePDO_communication_parameter= 0x20;
		public static final int CANopenTypePDO_mapping= 0x21;
		public static final int CANopenTypeSDO_parameter= 0x22;
		public static final int CANopenTypeIdentity= 0x23;
		
		private int id;
		private int size;
		private boolean signed;
		private boolean real;
		private final BigDecimal minVal;
		private final BigDecimal maxVal;
		private String name;
		
		private boolean supported;
		/**
		 * @param id
		 * @param size
		 * @param putMethod
		 */
		public CANOpenDataType(int id) {
			super();
			this.id = id;
//	        new DataTypeDef("UNKNOWN", -1), new DataTypeDef("BOOLEAN", 1),
//	        new DataTypeDef("INTEGER8", 1), new DataTypeDef("INTEGER16", 2), new DataTypeDef("INTEGER32", 4),
//	        new DataTypeDef("UNSIGNED8", 1), new DataTypeDef("UNSIGNED16", 2), new DataTypeDef("UNSIGNED32", 4),
//	        new DataTypeDef("REAL32", 4),
//	        new DataTypeDef("VISIBLE_STRING", 0), new DataTypeDef("OCTET_STRING", 0), new DataTypeDef("UNICODE_STRING", 0),
//	        new DataTypeDef("TIME_OF_DAY", -1), new DataTypeDef("TIME_DIFERENCE", -1),
//	        new DataTypeDef("reserved", -1),
//	        new DataTypeDef("DOMAIN", -1),
//	        new DataTypeDef("INTEGER24", 3),
//	        new DataTypeDef("REAL64", 8),
//	        new DataTypeDef("INTEGER40", 5), new DataTypeDef("INTEGER48", 6), new DataTypeDef("INTEGER56", 7), new DataTypeDef("INTEGER64", 8),
//	        new DataTypeDef("UNSIGNED24", 3),
//	        new DataTypeDef("reserved", -1),
//	        new DataTypeDef("UNSIGNED40", 5), new DataTypeDef("UNSIGNED48", 6), new DataTypeDef("UNSIGNED56", 7), new DataTypeDef("UNSIGNED64", 8),
//	        new DataTypeDef("reserved", -1), new DataTypeDef("reserved", -1), new DataTypeDef("reserved", -1), new DataTypeDef("reserved", -1),
//	        new DataTypeDef("PDO_COMMUNICATION_PARAMETER", -1), new DataTypeDef("PDO_MAPPING", -1),
//	        new DataTypeDef("SDO_PARAMETER", -1),
//	        new DataTypeDef("IDENTITY", -1)
			switch(id) {
				case CANopenTypeBoolean:
					size= 1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= true;
					name= "boolean";
					break;
				case CANopenTypeInt8:
					size= 1;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(7).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(7).negate();
					supported= true;
					name= "int8";
					break;
				case CANopenTypeInt16:
					size= 2;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(15).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(15).negate();
					supported= true;
					name= "int16";
					break;
				case CANopenTypeInt32:
					size= 4;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(31).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(31).negate();
					supported= true;
					name= "int32";
					break;
				case CANopenTypeUint8:
					size= 1;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(8).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint8";
					break;
				case CANopenTypeUint16:
					size= 2;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(16).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint16";
					break;
				case CANopenTypeUint32:
					size= 4;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(32).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint32";
					break;
				case CANopenTypeReal32:
					size= 4;
					signed= true;
					real= true;
					minVal= null;
					maxVal= null;
					supported= true;
					name= "real32";
					break;
				case CANopenTypeVisible_string: // TODO support Strings
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "visible string";
					break;
				case CANopenTypeOctet_string:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "octet string";
					break;
				case CANopenTypeUnicode_string:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "unicode string";
					break;
				case CANopenTypeTime_of_day:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "time of day";
					break;
				case CANopenTypeTime_difference:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "time difference";
					break;

				case CANopenTypeDomain:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "domain";
					break;
					
				case CANopenTypeInt24:
					size= 3;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(23).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(23).negate();
					supported= true;
					name= "int24";
					break;
				case CANopenTypeReal64:
					size= 8;
					signed= true;
					real= true;
					minVal= null;
					maxVal= null;
					supported= true;
					name= "real64";
					break;
				case CANopenTypeInt40:
					size= 5;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(39).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(39).negate();
					supported= true;
					name= "40";
					break;
				case CANopenTypeInt48:
					size= 6;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(47).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(47).negate();
					supported= true;
					name= "int48";
					break;
				case CANopenTypeInt56:
					size= 7;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(55).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(55).negate();
					supported= true;
					name= "int56";
					break;
				case CANopenTypeInt64:
					size= 8;
					signed= true;
					real= false;
					maxVal= new BigDecimal((int)2).pow(63).subtract(BigDecimal.ONE);
					minVal= new BigDecimal((int)2).pow(63).negate();
					supported= true;
					name= "int64";
					break;
				case CANopenTypeUint24:
					size= 3;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(24).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint24";
					break;
				case CANopenTypeUint40:
					size= 5;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(40).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint40";
					break;
				case CANopenTypeUint48:
					size= 6;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(48).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint48";
					break;
				case CANopenTypeUint56:
					size= 7;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(56).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= true;
					name= "uint56";
					break;
				case CANopenTypeUint64:
					size= 8;
					signed= false;
					real= false;
					maxVal= new BigDecimal((int)2).pow(64).subtract(BigDecimal.ONE);
					minVal= BigDecimal.ZERO;
					supported= false; // unfortunately not supported by java long
					name= "uint64";
					break;

				case CANopenTypePDO_communication_parameter:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "pdo comm parameter";
					break;
				case CANopenTypePDO_mapping:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "pdo mapping";
					break;
				case CANopenTypeSDO_parameter:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "sdo parameter";
					break;
				case CANopenTypeIdentity:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "identity";
					break;
				default:
					size= -1;
					signed= false;
					real= false;
					minVal= null;
					maxVal= null;
					supported= false;
					name= "undefined";			
			}
		}
		
		public byte[] packData(BigDecimal val) {
			return packData(val, 0);
		}
		
		public byte[] packData(BigDecimal val, int ofs) {
			if(!supported) {
				logger.warn("Trying to pack unsupported data type " + id);
				return null;
			}
			
			byte[] buf= new byte[ofs+size];
			if(putData(buf, val, ofs))
				return buf;
			else
				return null;
		}

		public boolean putData(byte[] buf, BigDecimal val, int ofs) {
			if(!supported) {
				logger.warn("Trying to pack unsupported data type " + id);
				return false;
			}
			
			ByteBuffer bb = ByteBuffer.wrap(buf, ofs, buf.length-ofs);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			if(real) {
				if(size==4)
					bb.putFloat(val.floatValue());
				else
					bb.putDouble(val.doubleValue());
			} else if(id==CANopenTypeBoolean) {
				byte b;
				if(val.compareTo(BigDecimal.ZERO)!=0)
					b= 1;
				else
					b= 0;
				bb.put(ofs, b);
			} else {
				if(val.compareTo(minVal)<0) {
					logger.warn("Trying to pack " + val + " is less than min value of data type " + id );
					return false;
				}
				if(val.compareTo(maxVal)>0) {
					logger.warn("Trying to pack " + val + " is greater than max value of data type " + id );
					return false;
				}
				byte[] b;
				if(size>3) {
					b= new byte[8];
					ByteBuffer bb_= ByteBuffer.wrap(b);
					bb_.order(ByteOrder.LITTLE_ENDIAN);
					bb_.putLong(val.longValue());
				} else if(size>1) {
					b= new byte[4];
					ByteBuffer bb_= ByteBuffer.wrap(b);
					bb_.order(ByteOrder.LITTLE_ENDIAN);
					bb_.putInt(val.intValue());
				} else {
					b= new byte[2];
					ByteBuffer bb_= ByteBuffer.wrap(b);
					bb_.order(ByteOrder.LITTLE_ENDIAN);
					bb_.putShort(val.shortValue());
				}
//				logger.debug("  intermediate buffer was: " + Util.byteArrayToString(b));
				bb.put(b, 0, size);
			}
			return true;
		}
		
		public BigDecimal getData(byte[] buf, int ofs) {
			if(!supported) {
				logger.warn("Trying to pack unsupported data type " + id);
				return null;
			}
			
			try {
				ByteBuffer bb = ByteBuffer.wrap(buf, ofs, buf.length-ofs);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				if(real) {
					if(size==4)
						return new BigDecimal(bb.getFloat());
					else
						return new BigDecimal(bb.getDouble());
				} else if(id==CANopenTypeBoolean) {
					byte b= bb.get();
					if(b==0)
						return BigDecimal.ZERO;
					else
						return BigDecimal.ONE;
				} else {
					byte[] b;
					if(size>3) {
						b= extendBuffer(buf, ofs, 8);
						bb= ByteBuffer.wrap(b);
						bb.order(ByteOrder.LITTLE_ENDIAN);
//						logger.debug("  extracting long (signed:"+signed +") from intermediate buffer: " + Util.byteArrayToString(b));
						return new BigDecimal(bb.getLong());
					} else if(size>1) {
						b= extendBuffer(buf, ofs, 4);
						bb= ByteBuffer.wrap(b);
						bb.order(ByteOrder.LITTLE_ENDIAN);
//						logger.debug("  extracting int (signed:"+signed +") from intermediate buffer: " + Util.byteArrayToString(b));
						return new BigDecimal(bb.getInt());
					} else {
						b= extendBuffer(buf, ofs, 2);
						bb= ByteBuffer.wrap(b);
						bb.order(ByteOrder.LITTLE_ENDIAN);
//						logger.debug("  extracting short (signed:"+signed +") from intermediate buffer: " + Util.byteArrayToString(b));
						return new BigDecimal(bb.getShort());
					}
				}
			} catch (Exception e) {
				logger.warn("Failed to decode data type \"" + name + "\" at offset " + ofs + " from " + Util.byteArrayToString(buf));

				return null;
			}
		}
		private byte[] extendBuffer(byte[] buf, int ofs, int bsize) {
			byte[] b= new byte[bsize];
			for(int i= 0; i<size; i++)
				b[i]= buf[i+ofs];

			for(int i= size; i<bsize; i++)
				if(signed && buf[size-1+ofs]<0)
					b[i]= -1;
				else
					b[i]= 0;
			
			return b;
		}
		
		public boolean isSupported() {
			return supported;
		}

		public int getSize() {
			return size;
		}
		
		public String toString() {
			return name;
		}
	}

	private static final Logger logger = 
			LoggerFactory.getLogger(CANOpenItemConfig.class);
	
    private static final Map<String, CANOpenDataType> CANopenTypes;
    static
    {
    	CANopenTypes = new HashMap<String, CANOpenDataType>();
		CANopenTypes.put("boolean", new CANOpenDataType(CANOpenDataType.CANopenTypeBoolean));
		CANopenTypes.put("int8", new CANOpenDataType(CANOpenDataType.CANopenTypeInt8));
		CANopenTypes.put("int16", new CANOpenDataType(CANOpenDataType.CANopenTypeInt16));
		CANopenTypes.put("int32", new CANOpenDataType(CANOpenDataType.CANopenTypeInt32));
		CANopenTypes.put("uint8", new CANOpenDataType(CANOpenDataType.CANopenTypeUint8));
		CANopenTypes.put("uint16", new CANOpenDataType(CANOpenDataType.CANopenTypeUint16));
		CANopenTypes.put("uint32", new CANOpenDataType(CANOpenDataType.CANopenTypeUint32));
		CANopenTypes.put("real32", new CANOpenDataType(CANOpenDataType.CANopenTypeReal32));
		CANopenTypes.put("visible_string", new CANOpenDataType(CANOpenDataType.CANopenTypeVisible_string));
		CANopenTypes.put("octet_string", new CANOpenDataType(CANOpenDataType.CANopenTypeOctet_string));
		CANopenTypes.put("unicode_string", new CANOpenDataType(CANOpenDataType.CANopenTypeUnicode_string));
		CANopenTypes.put("time_of_day", new CANOpenDataType(CANOpenDataType.CANopenTypeTime_of_day));
		CANopenTypes.put("time_difference", new CANOpenDataType(CANOpenDataType.CANopenTypeTime_difference));
	
		CANopenTypes.put("domain", new CANOpenDataType(CANOpenDataType.CANopenTypeDomain));
		CANopenTypes.put("int24", new CANOpenDataType(CANOpenDataType.CANopenTypeInt24));
		CANopenTypes.put("real64", new CANOpenDataType(CANOpenDataType.CANopenTypeReal64));
		CANopenTypes.put("int40", new CANOpenDataType(CANOpenDataType.CANopenTypeInt40));
		CANopenTypes.put("int48", new CANOpenDataType(CANOpenDataType.CANopenTypeInt48));
		CANopenTypes.put("int56", new CANOpenDataType(CANOpenDataType.CANopenTypeInt56));
		CANopenTypes.put("int64", new CANOpenDataType(CANOpenDataType.CANopenTypeInt64));
		CANopenTypes.put("uint24", new CANOpenDataType(CANOpenDataType.CANopenTypeUint24));
		CANopenTypes.put("uint40", new CANOpenDataType(CANOpenDataType.CANopenTypeUint40));
		CANopenTypes.put("uint48", new CANOpenDataType(CANOpenDataType.CANopenTypeUint48));
		CANopenTypes.put("uint56", new CANOpenDataType(CANOpenDataType.CANopenTypeUint56));
		CANopenTypes.put("uint64", new CANOpenDataType(CANOpenDataType.CANopenTypeUint64));
	
		CANopenTypes.put("pdo_communication_parameter", new CANOpenDataType(CANOpenDataType.CANopenTypePDO_communication_parameter));
		CANopenTypes.put("pdo_mapping", new CANOpenDataType(CANOpenDataType.CANopenTypePDO_mapping));
		CANopenTypes.put("sdo_parameter", new CANOpenDataType(CANOpenDataType.CANopenTypeSDO_parameter));
		CANopenTypes.put("identity", new CANOpenDataType(CANOpenDataType.CANopenTypeIdentity));
	}

    private static final String canInterfaceIdProp= "if";
    private static final String dataTypeProp= "type";
    private static final String deviceIDProp= "id";
    private static final String txCOB_IDProp= "txpdo";
    private static final String txDataOffsetProp= "txofs";
    private static final String rxCOD_IDProp= "rxpdo";
    private static final String odIndexProp= "odidx";
    private static final String odSubIndexProp= "odsubidx";
    private static final String nmtProp= "nmt";
    private static final String sdoInit= "sdoinit";
    private static final String sdoRefresh= "sdorefresh";
    
    private String canInterfaceId= null;
	private String itemName;
	private CANOpenDataType dataType= null;
	private int deviceID= -1;
	private int txCOB_ID= -1;	// TODO add RTR capability
	private int txDataOffset= -1;
	private int rxCOD_ID= -1;
	private int odIndex= -1;
	private int odSubIndex= -1;
	
	private SDOTransactionType expectedSDOResponse= SDOTransactionType.NONE;
	private boolean sdoInitRead= false;
	private boolean sdoRefreshRead= false;
	private SDODeviceManager sdoManager= null;
	public CANopenSDOMessage sdoMessage= null;
	
	private enum CANopenProtocolType {
		Undefined, TPDO, RPDO, SDO, NMT, ERROR // TODO Error protocol
	}
	
	private CANopenProtocolType protocolType= CANopenProtocolType.Undefined;
	
	public static final int TPDO1_ID= 0x180;
	
	public CANOpenItemConfig(Item item, String bindingConfig) throws BindingConfigParseException {
		itemName = item.getName();
		
		String[] props = bindingConfig.split(",");
		int txPDOnumber= 0; 
		int rxPDOnumber= 0;
		
		for(String prop: props) {
			if(!prop.contains(":")) {
				if(prop.trim().equalsIgnoreCase(nmtProp)) {
					protocolType= CANopenProtocolType.NMT;
					continue;
				} else if(prop.trim().equalsIgnoreCase(sdoInit)) {
					sdoInitRead= true;
				} else if(prop.trim().equalsIgnoreCase(sdoRefresh)) {
					sdoRefreshRead= true;
				}
			} else {
				String[] parts= prop.split(":");
				if(parts.length!=2) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + " at property " + prop);
				
				if(parts[0].trim().equalsIgnoreCase(canInterfaceIdProp))
					canInterfaceId= parts[1].trim();
				else if(parts[0].trim().equalsIgnoreCase(deviceIDProp))
					deviceID= parseIntProperty(parts[1].trim(), deviceIDProp, 127, itemName);
				else if(parts[0].trim().equalsIgnoreCase(rxCOD_IDProp))
					if(parts[1].trim().startsWith("#")) {
						rxPDOnumber= parseIntProperty(parts[1].trim().substring(1), rxCOD_IDProp + "#", 4, itemName);
						if(rxPDOnumber<1) throw new BindingConfigParseException("Error parsing binding configuration for item " + item + ". Property " + rxCOD_IDProp + "#" + " out of range");
					} else
						rxCOD_ID= parseIntProperty(parts[1].trim(), rxCOD_IDProp, 2047, itemName);
				else if(parts[0].trim().equalsIgnoreCase(txCOB_IDProp))
					if(parts[1].trim().startsWith("#")) {
						txPDOnumber= parseIntProperty(parts[1].trim().substring(1), txCOB_IDProp + "#", 4, itemName);
						if(txPDOnumber<1) throw new BindingConfigParseException("Error parsing binding configuration for item " + item + ". Property " + txCOB_IDProp + "#" + " out of range");
					} else
						txCOB_ID= parseIntProperty(parts[1].trim(), txCOB_IDProp, 2047, itemName);
				else if(parts[0].trim().equalsIgnoreCase(txDataOffsetProp))
					txDataOffset= parseIntProperty(parts[1].trim(), txDataOffsetProp, 7, itemName);
				else if(parts[0].trim().equalsIgnoreCase(odIndexProp))
					odIndex= parseIntProperty(parts[1].trim(), odIndexProp, 65535, itemName);
				else if(parts[0].trim().equalsIgnoreCase(odSubIndexProp))
					odSubIndex= parseIntProperty(parts[1].trim(), odSubIndexProp, 255, itemName);
				else if(parts[0].trim().equalsIgnoreCase(dataTypeProp)) {
					dataType= CANopenTypes.get(parts[1].trim());
					if(dataType==null) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". Unknown data type: " + parts[1]);
					if(!dataType.isSupported()) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". Currently only data type real32 supported");
				}
			}
		}
		
		if(canInterfaceId==null) throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". No interface provided");
		
		if(rxPDOnumber>0 && deviceID>=0)
			rxCOD_ID= 0x0200 + (rxPDOnumber-1)*0x0100 + deviceID;
		if(txPDOnumber>0 && deviceID>=0)
			txCOB_ID= 0x0180 + (rxPDOnumber-1)*0x0100 + deviceID;
			
		if(protocolType==CANopenProtocolType.NMT && deviceID<0)
			protocolType= CANopenProtocolType.Undefined;
		
		if(protocolType==CANopenProtocolType.Undefined && deviceID>=0 && odIndex>=0 && odSubIndex>=0 && dataType!=null) {
			if(dataType.size>4)  throw new BindingConfigParseException("Error parsing binding configuration for item " + itemName + ". Only expedit SDOs currently supported");
			protocolType= CANopenProtocolType.SDO;
			sdoMessage= new CANopenSDOMessage(deviceID, odIndex, odSubIndex, dataType, logger);
		}
		
		if(protocolType==CANopenProtocolType.Undefined && txCOB_ID>=0 && txDataOffset>=0 && dataType!=null) protocolType= CANopenProtocolType.TPDO;
		if(protocolType==CANopenProtocolType.Undefined && rxCOD_ID>=0 && dataType!=null) protocolType= CANopenProtocolType.RPDO;
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
	
	private void sendData(DecimalType d, boolean command) {
		CanId canId= null;
		byte[] data= null;
		
		if(protocolType==CANopenProtocolType.RPDO) {
			canId= new CanId(rxCOD_ID);
			data= dataType.packData(d.toBigDecimal());
			if(data==null)
				logger.warn("Failed composing PDO message");
		} else if(protocolType==CANopenProtocolType.SDO) {
			if(sdoManager!=null)
				sdoManager.writeSDO(this, d.toBigDecimal(), command);
		}
		
		if(canId!=null  && data!=null) {
			ISocketConnection connection = CANOpenActivator.getConnection(canInterfaceId);
			try {
				connection.open();
			} catch (Exception e) {
				logger.error("Error opening the connection " + canInterfaceId);
			}
			connection.send(canId, data); // TODO send using CANFrames ?
//			logger.debug("Sending number " + ((DecimalType)cmd).toBigDecimal() + " as " + Util.canMessageToSting(canId.getCanId_SFF(), data));
		}
	}
	
	public void sendCommand(Command cmd) {
		if (cmd instanceof DecimalType) {
			sendData((DecimalType)cmd, true);
		} else
			logger.debug("Received Command of type " + cmd.getClass().toString());
//		else if (cmd instanceof IncreaseDecreaseType) {
//			return true;
//		}
//		OnOffType
//		OpenClosedType
	}

	public void sendState(State state) {
		if (state instanceof DecimalType)
			sendData((DecimalType)state, false);
		else
			logger.debug("Received State of type " + state.getClass().toString());
	}

	public void expectSDO(SDOTransactionType type) {
		synchronized (this) {
			expectedSDOResponse= type;
			this.notify();			
		}
	}
	
	public State pdoToState(byte[] data) {
		if(!providesTxPDO())
			return null;
		
		BigDecimal val= dataType.getData(data, txDataOffset);
		if(val!=null) {
//			logger.debug("Parsed PDO " + dataType + " data " + val + " from " + Util.byteArrayToString(data) + "@" + txDataOffset);
			return new DecimalType(val);
		} else { 
			logger.warn("PDO parse error");
			return null;
		}
	}
	
	public State sdoToState(byte[] data, BigDecimal sendVal) {
		if(!providesSDO())
			return null;
		
		if(data==null || data.length!=8) {
			logger.debug("trying to decode SDO frame with data lenght != 8");
			return null;
		}
		
		State res= null;
		if(sdoMessage.isDownloadInitiateResponse(data)) {
			if(expectedSDOResponse!=SDOTransactionType.DOWNLOAD)
				logger.error("Received unexpected SDO write response: " + Util.byteArrayToString(data)); 
			else { // successful write
				res= new DecimalType(sendVal);
				logger.debug("received correct SDO write response");
				expectSDO(SDOTransactionType.NONE);				
			}
		} else if(sdoMessage.isUploadInitiateResponse(data)) {
			if(expectedSDOResponse!=SDOTransactionType.UPLOAD)
				logger.error("Received unexpected SDO write response: " + Util.byteArrayToString(data));
			else { // successful read
				int n= sdoMessage.uploadInitiateResponseDataLength();
				if(n>4 || n==0) {
					logger.warn("Received unexpected SDO read response for number of data bytes: " + n);
				} else if(n!=-1 && n!=dataType.getSize()) {
					logger.warn("Received unexpected SDO read response for number of data bytes: " + n + ", expecting " + dataType.getSize());
				} else {
					BigDecimal val= dataType.getData(data, 4);
					if(val!=null) {
						logger.debug("Parsed SDO data " + val + " from " + Util.byteArrayToString(data));
						res= new DecimalType(val);					
					} else { 
						logger.warn("SDO parse error");
					}
				}
				expectSDO(SDOTransactionType.NONE);	
			}
		} else if(sdoMessage.isDownloadSegmentResponse(data)) {
//			if(waitingForSDOResponse!=SDOResponseExpected.SEG_DOWNLOAD) not supported yet
			// TODO add segmented write 
			logger.error("Received unexpected SDO segmented write response: " + Util.byteArrayToString(data));

		} else if(sdoMessage.isUploadSegmentResponse(data)) {
			// TODO add segmented read 
//			if(waitingForSDOResponse!=SDOResponseExpected.SEG_UPLOAD) not supported yet
			logger.error("Received unexpected SDO segmented read response: " + Util.byteArrayToString(data));
//	        else if (is_upload_segment_response(confirmation)) {
//          dump_data_binary(confirmation, 1);
//          if ( c(confirmation) == 0 ) {
//              sdo_upload_segment_request(node_id, t(confirmation)+1);
//          } else {
//              printf("\n");
//              socketcan_close();
//              exit(EXIT_SUCCESS);
//          }
//      }

		} else if(sdoMessage.isAbortTransferRequest(data)) {
			if(expectedSDOResponse==SDOTransactionType.NONE) 
				logger.warn("Received unexpected SDO  error: " + sdoMessage.getLastError());
			else				
				logger.warn("SDO error: " + sdoMessage.getLastError());
			expectSDO(SDOTransactionType.NONE);	
		}
		
		return res;
	}

	public State nmtToState(byte[] data) {
		if(!providesNMT())
			return null;
		return new DecimalType(new BigDecimal(data[0]));
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(itemName+" [");
		sb.append("if:" + canInterfaceId + ",");
		sb.append("id:" + deviceID + ",");
		switch(protocolType) {
		case NMT:
			sb.append("nmt");
			break;
		case TPDO:
			sb.append("type:" + dataType + ",");
			sb.append("txcob:0x" + Integer.toString(txCOB_ID, 16).toUpperCase() + ",");
			sb.append("txofs:" + txDataOffset);
			break;
		case RPDO:
			sb.append("type:" + dataType + ",");
			sb.append("rxcob:0x" + Integer.toString(rxCOD_ID, 16).toUpperCase());
			break;
		case SDO:
			sb.append("type:" + dataType + ",");
			sb.append("odidx:0x" + Integer.toString(odIndex, 16).toUpperCase() + ",");
			sb.append("odsubidx:" + odSubIndex);
			break;
		case ERROR:
		case Undefined:
			sb.append("undefined protocol");
			break;			
		}
		sb.append("]");
		
		return sb.toString();
	}

	public int getPDOId() {
		return txCOB_ID;
	}

	public int getDeviceID() {
		return deviceID;
	}
	
	public SDOTransactionType getExpectedSDOResponse() {
		return expectedSDOResponse;
	}
	
	public boolean providesTxPDO() {
		return protocolType==CANopenProtocolType.TPDO;
	}
	
	public boolean providesNMT() {
		return protocolType==CANopenProtocolType.NMT;
	}
	
	public boolean providesSDO() {
		return protocolType==CANopenProtocolType.SDO;
	}

	public boolean sdoReadAtInit() {
		return sdoInitRead;
	}
	
	public boolean sdoReadRefresh() {
		return sdoRefreshRead;
	}
	
	public boolean expectingSDOResponse() {
		return expectedSDOResponse!=SDOTransactionType.NONE;
	}
	
	public void setSDOManager(SDODeviceManager m) {
		sdoManager= m;
	}
}
