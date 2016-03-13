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

	//Backup stuff
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;
	private Socket backupServer;
	private OutputStreamWriter out = null;


	GameManager game = new GameManager();

	public GameServer(String address, int port){
		serverManagerAddress = address;
		serverManagerPort = port;
	}

	/**
	 * The main function that will loop while checking for clients attempting to connect and create GameThreads for them.
	 */
	private void run(){

		setUpBackup();

		try{
			serverManagerSocket = new Socket(serverManagerAddress, serverManagerPort);
		} catch (Exception e) {
			System.err.println("ERROR: Failed to connect to server manager");
			System.exit(-1);
		}
		
		new GameThread(serverManagerSocket, game).run();


		/*
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
				new Thread(new GameThread(clientSocket, game)).start();
			} catch (Exception e){
				System.out.println("Error accepting client\n");
				e.printStackTrace();
			}
		}
		*/
	};

	/**
	 * This function will check for a backup server and connect to it if it exists.
	 */
	private void setUpBackup(){
		try{
			backupServer = new Socket(backupServerAddress, backupServerPort);
			BufferedOutputStream bufOut = new BufferedOutputStream(backupServer.getOutputStream());
			//out = new ObjectOutputStream(bufOut);
			out = new OutputStreamWriter(bufOut);
			new Thread(new BackupManager(out, game)).start();
		} catch (Exception e){
			System.out.println("WARNING: Unable to connect to backup server");
		}
	}


	public static void main(String[] args) {	
		if(args.length == 2){
			String address = args[0];
			int port = Integer.parseInt(args[1]);
			new GameServer(address, port).run();
		}
	}

}
