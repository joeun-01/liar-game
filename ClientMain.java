package mafia;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain {
	public static void main(String[] args) {
		try {
			InetAddress inetA = InetAddress.getLocalHost();
			String ip_str = inetA.toString();
			String ip = ip_str.substring(ip_str.indexOf("/") + 1);
			 
			ClientGui GUI = new ClientGui("127.0.0.1", 5592);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}