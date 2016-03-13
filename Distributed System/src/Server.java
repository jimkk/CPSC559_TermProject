import java.net.*;
import java.io.*;
import java.util.*;

/**
 * The class from the server that will set up a connection to a backup server
 * if there is one and then will listen for client connections and create new
 * ServerThread threads to manage that client.
 */
public class Server implements Runnable{

	public static final int SERVER = 0;
	public static final int CLIENT = 1;
	
	private static int clientPort = 7777;
	private static int serverPort = 7776;


	private boolean isDone = false;
	private int port;
	private int type;
	private ServerSocket serverSocket;

	//Backup stuff
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;
	private Socket backupServer;
	private OutputStreamWriter out = null;

	private volatile ArrayList<Socket> servers;

	public Server(int port, int type){
		this.port = port;
		this.type = type;
		servers = new ArrayList<Socket>();
	}


	/**
	 * The main function that will loop while checking for clients attempting to connect and create ServerThreads for them.
	 */
	public void run(){

		try{
			serverSocket = new ServerSocket(port);
		} catch (Exception e){
			System.out.println("Failed to initialize server socket");
			e.printStackTrace();
			System.exit(-1);
		}

		while(!isDone){
			try{
				Socket clientSocket = serverSocket.accept();
				if(type == SERVER){
					servers.add(clientSocket);
					System.out.println("Server added to list");
				} else if (type == CLIENT){
					new Thread(new ServerThread(clientSocket, servers)).start();
				}
			} catch (Exception e){
				if(type == SERVER){
					System.out.println("Error accepting server\n");
				} else if (type == CLIENT){
					System.out.println("Error accepting client\n");
				}
				e.printStackTrace();
			}
		}
	};

	/**
	 * This function will check for a backup server and connect to it if it exists.
	 */

	public static void main(String[] args) {
		new Thread(new Server(clientPort, Server.CLIENT)).start();
		new Server(serverPort, Server.SERVER).run();
	}

}
