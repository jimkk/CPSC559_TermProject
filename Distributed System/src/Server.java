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
	public static final int BACKUP = 2;

	private static int clientPort = 7777;
	private static int serverPort = 7776;
	private static int backupPort = 7775;

	private boolean isDone = false;
	private int port;
	private int type;
	private ServerSocket serverSocket;

	//Backup stuff
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;
	private Socket backupServer;
	private OutputStreamWriter out = null;

	//private static ArrayList<Socket> servers;
	private static HashMap<Integer, Socket> servers;
	private static ArrayList<Socket> backupServers;
	private int nextGameID = 1;
	private int nextClientID = 1;

	public Server(int port, int type){
		this.port = port;
		this.type = type;
		servers = new HashMap<Integer, Socket>();
		backupServers = new ArrayList<Socket>();
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

		try{
			serverSocket.setSoTimeout(1000);
		} catch (Exception e){e.printStackTrace();}

		while(!isDone){
			try{
				Socket clientSocket = serverSocket.accept();
				if(type == SERVER){
					servers.put(nextGameID, clientSocket);
					System.out.println("Server added to list");

					BufferedOutputStream bufOut = new BufferedOutputStream(clientSocket.getOutputStream());
					OutputStreamWriter out = new OutputStreamWriter(bufOut);
					out.write("gameid " + nextGameID++ + "\n");
					out.flush();
				} else if (type == CLIENT){
					new Thread(new ServerThread(clientSocket, nextClientID++, servers)).start();
				} else if (type == BACKUP){
					backupServers.add(clientSocket);
					System.out.println("Backup server added to list");
				}



			} catch (SocketTimeoutException e){
				Iterator it = servers.keySet().iterator();
				while(it.hasNext()){
					int key = (int) it.next();
					if(key < 0){
						System.out.println("Found crashed server!");
						int gameID = -key;
						Socket socket = servers.remove(key);
						if(backupServers.size() > 0){
							Socket backupSocket = backupServers.get(0);
							try{
								BufferedOutputStream bufBackupOut = new BufferedOutputStream(backupSocket.getOutputStream());
								OutputStreamWriter backupOut = new OutputStreamWriter(bufBackupOut);
								BufferedInputStream bufBackupIn = new BufferedInputStream(backupSocket.getInputStream());
								InputStreamReader backupIn = new InputStreamReader(bufBackupIn);

								backupOut.write("backup_request " + gameID + "\f");
								backupOut.flush();

								while(!backupIn.ready());

								String backupMessage = read(backupIn).toString();
								System.out.printf("Received backup: \"%s\"\n", backupMessage);
								
								
							} catch (Exception e2){e2.printStackTrace();}
						} else {
							System.out.println("Sadly there are no backup servers right now :(");
						}

					}
				}
			} catch (Exception e){
				if(type == SERVER){
					System.out.println("Error accepting server\n");
				} else if (type == CLIENT){
					System.out.println("Error accepting client\n");
				}
				e.printStackTrace();
			}
			try{
			Thread.sleep(10);
			} catch (Exception e) {e.printStackTrace();}
		}
	};

	private StringBuffer read(InputStreamReader in){
		try{
			StringBuffer buffer = new StringBuffer();
			int c;
			while((c = in.read()) != -1){
				if(c == (int) '\f'){
					break;
				}
				buffer.append((char) c);
			}
			return buffer;
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}


	/**
	 * This function will check for a backup server and connect to it if it exists.
	 */

	public static void main(String[] args) {
		new Thread(new Server(clientPort, Server.CLIENT)).start();
		new Thread(new Server(backupPort, Server.BACKUP)).start();
		new Server(serverPort, Server.SERVER).run();
	}

}
