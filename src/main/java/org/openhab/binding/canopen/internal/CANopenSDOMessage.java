/**
 * 
 */
package org.openhab.binding.canopen.internal;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.openhab.binding.canopen.internal.CANOpenItemConfig.CANOpenDataType;
import org.slf4j.Logger;

/**
 * adapted from https://github.com/smay4finger/canopen-toolbox
 * 
 * @author jgeisler
 *
 */
public class CANopenSDOMessage {
    public enum SDOTransactionType {
    	NONE("no pending transaction"),UPLOAD("SDO upload (read)"), DOWNLOAD("SDO download (write)"), SEG_DOWNLOAD("segmented SDO download (write)"), SEG_UPLOAD("segmented SDO upload (read)");
    	private String string;
    	private SDOTransactionType(String name) {string = name;}
    	public String toString() { return string; }
    };

	
    private static final Map<Integer, String> sdoErrorStrings;
    static
    {
    	sdoErrorStrings = new HashMap<Integer, String>();
    	
	    sdoErrorStrings.put(0x05030000, "Toggle bit not alternated.");
	    sdoErrorStrings.put(0x05040000, "SDO protocol timed out.");
	    sdoErrorStrings.put(0x05040001, "Client/server command specifier not valid or unknown.");
	    sdoErrorStrings.put(0x05040002, "Invalid block size (block mode only).");
	    sdoErrorStrings.put(0x05040003, "Invalid sequence number (block mode only).");
	    sdoErrorStrings.put(0x05040004, "CRC error (block mode only).");
	    sdoErrorStrings.put(0x05040005, "Out of memory.");
	    sdoErrorStrings.put(0x06010000, "Unsupported access to an object.");
	    sdoErrorStrings.put(0x06010001, "Attempt to read a write only object.");
	    sdoErrorStrings.put(0x06010002, "Attempt to write a read only object.");
	    sdoErrorStrings.put(0x06020000, "Object does not exist in the object dictionary.");
	    sdoErrorStrings.put(0x06040041, "Object cannot be mapped to the PDO.");
	    sdoErrorStrings.put(0x06040042, "The number and length of the objects to be mapped would exceed PDO length.");
	    sdoErrorStrings.put(0x06040043, "General parameter incompatibility reason.");
	    sdoErrorStrings.put(0x06040047, "General internal incompatibility in the device.");
	    sdoErrorStrings.put(0x06060000, "Access failed due to an hardware error.");
	    sdoErrorStrings.put(0x06070010, "Data type does not match, length of service parameter does not match");
	    sdoErrorStrings.put(0x06070012, "Data type does not match, length of service parameter too high");
	    sdoErrorStrings.put(0x06070013, "Data type does not match, length of service parameter too low");
	    sdoErrorStrings.put(0x06090011, "Sub-index does not exist.");
	    sdoErrorStrings.put(0x06090030, "Invalid value for parameter (download only).");
	    sdoErrorStrings.put(0x06090031, "Value of parameter written too high (download only).");
	    sdoErrorStrings.put(0x06090032, "Value of parameter written too low (download only).");
	    sdoErrorStrings.put(0x06090036, "Maximum value is less than minimum value.");
	    sdoErrorStrings.put(0x060A0023, "Resource not available: SDO connection");
	    sdoErrorStrings.put(0x08000000, "General error");
	    sdoErrorStrings.put(0x08000020, "Data cannot be transferred or stored to the application.");
	    sdoErrorStrings.put(0x08000021, "Data cannot be transferred or stored to the application because of local control.");
	    sdoErrorStrings.put(0x08000022, "Data cannot be transferred or stored to the application because of the present device state.");
	    sdoErrorStrings.put(0x08000023, "Object dictionary dynamic generation fails or no object dictionary is present (e.g. object dictionary is generated from file and generation fails because of an file error).");
	    sdoErrorStrings.put(0x08000024, "No data available");
	}
	
	Logger logger;
	
	private int nodeId;
	private int canId;
	private byte[] data;
	private int index;
	private int subIndex;
	private CANOpenDataType dataType;
	private String lastError= null;
	
	public CANopenSDOMessage(int nodeId, int index, int subIndex, CANOpenDataType dataType, Logger logger) {
		super();
		this.nodeId = nodeId;
		this.index = index;
		this.subIndex = subIndex;
		this.dataType = dataType;	
		this.logger = logger;	
	}

	private byte CS(int i) { // service is coded with three bits (command specifier)
	    return (byte) ((i & 0x7) << 5);
	}

	private byte E(int num) { // bit specifies whether an expedited or a non-expedited transfer is to be carried out
	    return (byte) ((num & 0x1) << 1);
	}

	private byte S(int num) { // bit indicates whether the size of the data to be transmitted is specified in the last four bytes
	    return (byte) ((num & 0x1) << 0);
	}

	private byte N(int num) { // bits of the protocol byte specify how many of these bytes are actually assigned
	    return (byte) ((num & 0x3) << 2);
	}

	private byte T(int t) {
	    return (byte) ((t & 0x1) << 4);
	}

	private byte cs() {
	    return (byte) (data[0] >> 5 & 0x7);
	}

	private byte n() {
	    if ( cs() == 0 ) {
	        return (byte) (data[0] >> 1 & 0x7);
	    } else {
	        return (byte) (data[0] >> 2 & 0x3);
	    }
	}

	private byte e() {
	    return (byte) (data[0] >> 1 & 0x1);
	}
	
	private byte s() {
	    return (byte) (data[0] >> 0 & 0x1);
	}

	private byte t() {
	    return (byte) (data[0] >> 4 & 0x1);
	}

	private byte c() {
	    return (byte) (data[0] >> 0 & 0x1);
	}

	private boolean isSDOConfirmation() {
	    return data.length == 8; // frame.can_id == 0x580 && node_id
	}

	public boolean isExpectedCANopenObject(byte[] data) {
		this.data= data;
		return isExpectedCANopenObject();
	}
	
	private boolean isExpectedCANopenObject() {
	    return data[1] == (index >> 0 & 0xFF)
	        && data[2] == (index >> 8 & 0xFF)
	        && data[3] == subIndex;
	}

	public boolean isUploadSegmentResponse(byte [] data) {
		this.data= data;
	    return cs() == 0;
	}

	public boolean isDownloadSegmentResponse(byte [] data) {
		this.data= data;
	    return cs() == 1;
	}

	public boolean isUploadInitiateResponse(byte [] data) {
		this.data= data;
	    return cs() == 2; // assume this was checked before: && isExpectedCANopenObject();
	}
	
	public int uploadInitiateResponseDataLength() {
		// this.data= data; assume this was done in isUploadInitiateResponse
		if(e()==0 && s()==1) {
			ByteBuffer bb = ByteBuffer.wrap(data, 4, 4);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			int n= bb.getInt();
			if(n<=4) {
				// this should not happen
				logger.warn("SDO upload initiate response for segmented transfer indicates less than 4 data bytes");
				n= 0;
			}
			return n;
			
			// commence with segmented upload
		} else if(e()==1 && s()==1) {
			return 4-n();
			// retrieve data
		} else if(e()==1 && s()==0) {
//          // d contains unspecified number of bytes to be uploaded
			return -1;
			
		} else
			return 0;
	}


	public boolean isDownloadInitiateResponse(byte [] data) {
		this.data= data;
	    return cs() == 3; // assume this was checked before:  && isExpectedCANopenObject();
	}

	public boolean isAbortTransferRequest(byte [] data) {
		this.data= data;
		
		boolean isAbort= cs() == 4; // assume this was checked before:  && isExpectedCANopenObject();
		if(isAbort) {
			ByteBuffer bb = ByteBuffer.wrap(data, 4, 4);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			int errorCode= bb.getInt();
			lastError= getSDOErrorString(errorCode);
		}
	    return isAbort;
	}

	private String getSDOErrorString(int code)  {
		String s= sdoErrorStrings.get(code);
		if(s==null) s= "Unknown error code: "+ code;
		return s;
	}
	
	public void sdoAbortTransfer(int abortCode) {
	    canId = 0x600 + nodeId;
	    
	    data= new byte[8];
	    data[0] = CS(4);
	    data[1] = (byte) (index >> 8 & 0xFF);
	    data[2] = (byte) (index >> 0 & 0xFF);
	    data[3] = (byte) subIndex;
	    data[4] = (byte) (abortCode >> 0 & 0xFF);
	    data[5] = (byte) (abortCode >> 8 & 0xFF);
	    data[6] = (byte) (abortCode >> 16 & 0xFF);
	    data[7] = (byte) (abortCode >> 24 & 0xFF);

	}

	public boolean sdoDownloadInitiateRequest(BigDecimal val) {
		canId = 0x600 + nodeId;

		data= new byte[8];
	    data[0] = CS(1);
	    data[1] = (byte) (index >> 0 & 0xFF);
	    data[2] = (byte) (index >> 8 & 0xFF);
	    data[3] = (byte) subIndex;
	    
	    if(!dataType.putData(data, val, 4))
	    	return false;
	    
	    switch (dataType.getSize()) {
	    case 4:
	        data[0] |= E(1) | S(1) | N(0);
	        break;
	    case 3:
	        data[0] |= E(1) | S(1) | N(1);
	        break;
	    case 2:
	        data[0] |= E(1) | S(1) | N(2);
	        break;
	    case 1:
	        data[0] |= E(1) | S(1) | N(3);
	        break;

//	    case SDO_TYPE_UNSPECIFIED:
//	        data[0] |= E(1) | S(0);
//	        break;
	    }
	    return true;
	}

	public void sdoUploadInitiateRequest() {
	    canId = 0x600 + nodeId;
	    
	    data= new byte[8];
	    data[0] = CS(2);
	    data[1] = (byte) (index >> 0 & 0xFF);
	    data[2] = (byte) (index >> 8 & 0xFF);
	    data[3] = (byte) subIndex;
	}

	public void sdoUploadSegmentRequest(int toggle) {
	    canId = 0x600 + nodeId;
	    
	    data= new byte[8];
	    data[0]= (byte) (CS(3) | T(toggle));
	}

	public int getCanId() {
		return canId;
	}

	public byte[] getData() {
		return data;
	}

	public String getLastError() {
		return lastError;
	}

	public int getIndex() {
		return index;
	}

	public int getSubIndex() {
		return subIndex;
	}

}
