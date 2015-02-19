package org.openhab.binding.canopen.internal;

public class ConnectionFactory {
	
	private static boolean testMode = false; 
	
	static {
		try {
			String useTestmode = System.getProperty("socketcan.testmode");
			testMode = Boolean.parseBoolean(useTestmode);
		} catch (Exception e) {
			testMode = false;
		}
	}

	public static ISocketConnection createConnection(String interfaceId) {
		if (testMode) {
			return new TestingSocketConnection();
		} else {
			return new SocketConnection(interfaceId);
		}
	}
	
}
