package org.openhab.binding.canopen.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.entropia.can.CanSocket.CanId;

public class TestingSocketConnection implements ISocketConnection {
	
	private static final Logger logger = LoggerFactory.getLogger(TestingSocketConnection.class);

	private List<CANMessageReceivedListener> listeners = new ArrayList<>();
	private byte[] data= new byte[8];
	
	private ReaderThread readingThread;
		
	@Override
	public void open() throws Exception {
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat((float) 32.1);
		bb.putFloat((float) 54.3);
		
		readingThread = new ReaderThread();
		readingThread.start();
		
		logger.debug("opened test connection");
	}

	@Override
	public void close() {
		readingThread.doStop();
	}

	@Override
	public void addMessageReceivedListener(CANMessageReceivedListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeMessageReceivedListener(CANMessageReceivedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void send(CanId canId, byte[] data) {
		logger.debug("sending " + canId.getCanId_SFF() + "#" + data);
	}
	
	private class ReaderThread extends Thread {
		
		private boolean run = true;
		
		public ReaderThread() {
			super("SocketCan reading thread");
		}
		
		public void run() {
			while (run) {
				notifyListeners(0x280 + 23, false, data);
				try {
					sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public void doStop() {
			run = false;
			interrupt();
		}
	}
	
	protected void notifyListeners(int canID, boolean rtr, byte[] data) {
		for (CANMessageReceivedListener listener : listeners) {
			try {
				listener.messageReceived(canID, rtr, data);
			} catch (Throwable t) {
				logger.error("Error in the listener for can frames!", t);
			}
		}
	}
}
