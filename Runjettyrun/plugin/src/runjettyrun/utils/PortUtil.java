package runjettyrun.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class PortUtil {

	/**
	 * a random avaiable port between start and end
	 * @param start
	 * @param end
	 * @return
	 */
	public static int findAAvailablePort(int start,int end){

		int finalPort = -1;
		for( int i = 0 ; i< 20 ; ++i){
			int port = start +  (int) (Math.random()* (end-start)) ;
			if(available(port)){
				finalPort = port;
				break;
			}
		}

		return finalPort;
	}

	public static boolean available(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}

}
