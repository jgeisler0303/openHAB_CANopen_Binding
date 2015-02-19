package org.openhab.binding.canopen.internal;

public interface CANMessageReceivedListener {

	public void messageReceived(int canID, boolean rtr, byte[] data);
	
}