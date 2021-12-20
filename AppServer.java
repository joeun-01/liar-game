package mafia;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AppServer extends Application {

	//Application initialization
	@Override
	public void init() {
		super.init();
	}
	
	//Application execution/
	@Override
	public void start() {

		System.out.println("[ SERVER START ]");
		
		ServerSocket server = null;
		Socket clientSocket = null;
		try {
			//make server socket
			server = new ServerSocket(Application.port);
			
			//Manage the socket connected to the client
			while(true) {
				System.out.println(Application.getTime());
				System.out.println("Waiting for Client...");
				
				clientSocket = server.accept();
				
				System.out.println("Connected");
				/* 
				 * Create threads responsible for client transmission & reception
				 * then deliver a socket (cSocket)
				 * the number of Thread = the number of client
				 */
				Thread clientThread  = new Thread(new ServerHandler(clientSocket));
				clientThread.start();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}