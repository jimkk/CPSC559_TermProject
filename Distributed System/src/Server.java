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
	public static final int SYNCPORT = 7774;

	private boolean isDone = false;
	private static boolean isBackup = false;
	public static boolean changesMade = true;

	private int port;
	private int type;
	private ServerSocket serverSocket;

	//Backup stuff
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;
	private Socket backupServer;

	private static HashMap<Integer, Socket> servers;
	private static ArrayList<Socket> backupServers;
	private volatile int nextGameID = 1;
	private volatile int nextClientID = 1;

	private static ArrayList<Integer> backupIDs;
	private static HashMap<Integer, String> backups;

	public Server(int port, int type){
		this.port = port;
		this.type = type;
		servers = new HashMap<Integer, Socket>();
		backupServers = new ArrayList<Socket>();
		backupIDs = new ArrayList<Integer>();
		backups = new HashMap<Integer, String>();
	}


	/**
	 * The main function that will loop while checking for clients attempting to connect and create ServerThreads for them.
	 */
	public void run(){

		//Socket Initialization
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

		changesMade = true;
		//Connection loop
		while(!isDone){
			try{
				Socket clientSocket = serverSocket.accept();
				if(type == SERVER){
					System.out.println("Server added to list");
					BufferedOutputStream bufOut = new BufferedOutputStream(clientSocket.getOutputStream());
					OutputStreamWriter out = new OutputStreamWriter(bufOut);
					System.out.printf("There are currently %d backups\n", backups.size());
					if(backups.size() > 0){
						System.out.println("New server is being repurposed as crashed server");
						int gameID = backupIDs.remove(0);
						System.out.printf("Game ID: %d\n", gameID);
						servers.put(gameID, clientSocket);
						out.write("gameid " + gameID + " " + backups.remove(gameID) + "\n");

						out.flush();
					} else {
						servers.put(nextGameID, clientSocket);
						out.write("gameid " + nextGameID++ + "\n");
						out.flush();
					}
				} else if (type == CLIENT){
					new Thread(new ServerThread(clientSocket, nextClientID++, servers)).start();
				} else if (type == BACKUP){
					backupServers.add(clientSocket);
					System.out.println("Backup server added to list");
				}

				changesMade = true;

			} catch (SocketTimeoutException e){
				Iterator it = servers.keySet().iterator();
				while(it.hasNext()){
					int key = (int) it.next();
					if(key < 0){
						System.out.println("Found crashed server!");
						int gameID = -key;
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

								String backupMessage = IOUtilities.read(backupIn);
								String [] messageParts = backupMessage.split(" ");
								int receivedGameID = Integer.parseInt(messageParts[1]);
								String contents = IOUtilities.rebuildString(messageParts, 2, messageParts.length);
								if(gameID == receivedGameID){
									System.out.printf("Received backup for game %d: \"%s\"\n", gameID, contents);
									backupIDs.add(gameID);
									backups.put(gameID, contents);	
									servers.remove(key);
								}

								
							} catch (Exception e2){e2.printStackTrace();}
						} else {
							System.out.println("Sadly there are no backup servers right now :(");
							servers.remove(key);
						}

						changesMade = true;
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
	
	/**
	 * Recieved backups from a main server and then takes over for it in cases where
	 * the main server goes down.
	 * @param serverIP The IP address of the main server
	 */
	private void backupRun(String serverIP){
		boolean mainServerDown = false;

		try{
			Socket syncSocket = new Socket(serverIP, SYNCPORT);
			BufferedInputStream bufIn = new BufferedInputStream(syncSocket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			System.out.println("Connected to main server!");
			//TODO Received backups from the main server
			while(!mainServerDown){
			}
		} catch (Exception e){e.printStackTrace();}
		//TODO Become the main server in the case where the main server goes down
		//TODO Start the other threads required for server
	}
	

	public HashMap<Integer, Socket> getServersInfo(){
		return servers;
	}

	public int getNextGameID(){
		return nextGameID;
	}

	public int getNextClientID(){
		return nextClientID;
	}

	public ArrayList<Socket> getBackupServers(){
		return backupServers;
	}

	public ArrayList<Integer> getBackupIDs(){
		return backupIDs;
	}

	public HashMap<Integer, String> getBackupsInfo(){
		return backups;
	}
	
	
	public static void main(String[] args) {
		if(args.length == 2 && args[0].equals("-b")){
			System.out.println("Running as backup server");
			String serverIP = args[1];
			new Server(serverPort, Server.SERVER).backupRun(serverIP);
		}
		else{
			new Thread(new Server(clientPort, Server.CLIENT)).start();
			new Thread(new Server(backupPort, Server.BACKUP)).start();
			Server server = new Server(serverPort, Server.SERVER);
			new Thread(new ServerSync(server)).start();
			server.run();
		}
	}

}
