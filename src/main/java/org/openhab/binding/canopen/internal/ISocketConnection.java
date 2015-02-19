package org.openhab.binding.canopen.internal;

import de.entropia.can.CanSocket.CanId;


public interface ISocketConnection {

	public void open() throws Exception;
	public void close();
	public void addMessageReceivedListener(CANMessageReceivedListener listener);
	public void removeMessageReceivedListener(CANMessageReceivedListener listener);
	public void send(CanId canId, byte[] data);
}
