package org.openhab.binding.canopen.internal;

public class Util {

	public static String byteArrayToString(byte[] data) {
		if (data == null) {
			return "[]";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < data.length; i++) {
			sb.append(String.format("%02X ", data[i]));
			if(i<data.length-1)
				sb.append(" ");				
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	public static String canMessageToSting(int canID, byte[] data) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%04X#", canID));
		sb.append(byteArrayToString(data));
		return sb.toString();
	}
}
