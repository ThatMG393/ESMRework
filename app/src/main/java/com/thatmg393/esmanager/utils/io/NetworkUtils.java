package com.thatmg393.esmanager.utils.io;

import java.io.IOException;
import java.net.ServerSocket;

public class NetworkUtils {
	public static int generateRandomPort() {
		ServerSocket tmp = null;
		
		try {
			tmp = new ServerSocket(0);
			return tmp.getLocalPort();
		} catch (IOException ioe) {
			return 0;
		} finally {
			try { if (tmp != null) { tmp.close(); } }
			catch (IOException ioe) { tmp = null; }
		}
	}
}
