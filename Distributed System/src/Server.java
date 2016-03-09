import java.net.*;
import java.io.*;

public class Server {

	private boolean isDone = false;
	private int port = 7777;
	private ServerSocket serverSocket;

	//Backup stuff
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;
	private Socket backupServer;
	private OutputStreamWriter out = null;


	GameManager game = new GameManager();
	private int gameChecksum = 0;

	private void run(){

		try{
			backupServer = new Socket(backupServerAddress, backupServerPort);
			BufferedOutputStream bufOut = new BufferedOutputStream(backupServer.getOutputStream());
			//out = new ObjectOutputStream(bufOut);
			out = new OutputStreamWriter(bufOut);
			new Thread(new BackupManager(out, game)).start();
		} catch (Exception e){
			System.out.println("WARNING: Unable to connect to backup server");
		}



		try{
			serverSocket = new ServerSocket(port);
		} catch (Exception e){
			System.out.println("Failed to initialize server socket");
			e.printStackTrace();
			System.exit(-1);
		}

		while(!isDone){
			try{
				//if (ServerThread.playerCount <= 6){
				Socket clientSocket = serverSocket.accept();

				new Thread(new ServerThread(clientSocket, game)).start();
				//}
			} catch (Exception e){
				System.out.println("Error accepting client\n");
				e.printStackTrace();
			}
		}
	};

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

}
