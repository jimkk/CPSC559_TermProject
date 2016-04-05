import java.net.*;
import java.io.*;

/**
 * The class from the server that will set up a connection to a backup server
 * if there is one and then will listen for client connections and create new
 * GameThread threads to manage that client.
 */
public class GameServer {

	private boolean isDone = false;
	private int port = 7777;
	private ServerSocket serverSocket;
	
	//Server Manager stuff
	private Socket serverManagerSocket;
	private String serverManagerAddress;
	private int serverManagerPort;

	public GameServer(String address, int port){
		serverManagerAddress = address;
		serverManagerPort = port;
	}

	/**
	 * The main function that will loop while checking for clients attempting to connect and create GameThreads for them.
	 */
	private void run(){

		try{
			serverManagerSocket = new Socket(serverManagerAddress, serverManagerPort);
		} catch (Exception e) {
			System.err.println("ERROR: Failed to connect to server manager");
			System.exit(-1);
		}
		
		//TODO Thread this. And therefore move message receiving into this class with the threads
		//reading a string variable containing messages that may pertain to them (processing and
		//remove the messages if they do) similar to ServerThread.
		//new GameThread(serverManagerSocket, game).run();
		new Thread(new GameThread(serverManagerSocket)).start();

		//TODO Create a stream reader that will process the messages and pass them to their 
		//respective GameThreads. It's either that or have the GameThreads process the stream
		//and then share the data between themselves

	}

	public static void main(String[] args) {	
		if(args.length == 1){
			String address = args[0];
			int port = Server.SERVERPORT;
			new GameServer(address, port).run();
		}
		if(args.length == 2){
			String address = args[0];
			int port = Integer.parseInt(args[1]);
			new GameServer(address, port).run();
		}
	}

}
