package org.openhab.binding.canopen.internal;

public class Util {

	public static String formatData(byte[] data) {
		if (data == null) {
			return "[null]";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < data.length; i++) {
			sb.append(Integer.toHexString(data[i]));
			if(i<data.length-1)
				sb.append(" ");				
		}
		sb.append("]");
		
		return sb.toString();
	}
}
