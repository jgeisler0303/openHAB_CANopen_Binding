package org.openhab.binding.canopen.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO move to io.canbus bundle and add GenericCANBusInterface for unified access to socketcan, can4linux and slcan  
public class ConnectionFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(TestingSocketConnection.class);
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
			logger.debug("Creating connection for interface " + interfaceId);
			return new TestingSocketConnection();
		} else {
			return new SocketConnection(interfaceId);
		}
	}
	
}
