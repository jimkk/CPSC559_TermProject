import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
import java.lang.reflect.*;

/**
 * The class from the server that will set up a connection to a backup server
 * if there is one and then will listen for client connections and create new
 * ServerThread threads to manage that client.
 */
public class Server implements Runnable{

	public static final int SERVER = 0;
	public static final int CLIENT = 1;
	public static final int BACKUP = 2;

	public static final int CLIENTPORT = 7777;
	public static final int SERVERPORT = 7776;
	public static final int BACKUPPORT = 7775;
	public static final int SYNCPORT = 7774;
	public static final int RECONNECTPORT = 7773;

	private boolean isDone = false;
	private static boolean isBackup = false;
	public static boolean changesMade = true;

	private int port;
	private int type;
	private transient ServerSocket serverSocket;

	//Backup stuff
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;

	private static HashMap<Integer, Socket> servers;
	private static ArrayList<Socket> backupServers;
	private volatile int nextGameID = 1;

	private static HashMap<Integer, Socket> clients;
	private volatile int nextClientID = 1;

	private static ArrayList<Integer> backupIDs;
	private static HashMap<Integer, String> backups;

	public Server(int port, int type){
		this.port = port;
		this.type = type;
		servers = new HashMap<Integer, Socket>();
		clients = new HashMap<Integer, Socket>();
		backupServers = new ArrayList<Socket>();
		backupIDs = new ArrayList<Integer>();
		backups = new HashMap<Integer, String>();
	}


	/**
	 * The main function that will loop while checking for clients attempting to connect and create ServerThreads for them.
	 */
	public void run(){

		//Socket Initialization
		while(serverSocket == null){
			try{
				serverSocket = new ServerSocket(port);
				//System.out.println("Server created");
				//System.out.printf("The IP is %s\n", serverSocket.getLocalSocketAddress());
			} catch (Exception e){
				System.out.println("Failed to initialize server socket...");
				try{
					Thread.sleep(3000);
				} catch (Exception te){te.printStackTrace();}
			}
		}

		try{
			serverSocket.setSoTimeout(1000);
		} catch (Exception e){e.printStackTrace();}

		changesMade = true;

		if(type == CLIENT){
			try{
				Thread.sleep(1000);
			} catch (Exception etc) {etc.printStackTrace();}
			if(clients.size() > 0){
				Iterator<Map.Entry<Integer, Socket>> clientIterator = clients.entrySet().iterator();
				while(clientIterator.hasNext()){
					Map.Entry<Integer, Socket> client = clientIterator.next();
					new Thread(new ServerThread(client.getValue(), client.getKey(), servers, true)).start();
				}
			}
		}
		//Connection loop
		while(!isDone){
			try{
				Socket clientSocket = serverSocket.accept();
				if(type == SERVER){
					System.out.println("Server added to list");
					BufferedOutputStream bufOut = new BufferedOutputStream(clientSocket.getOutputStream());
					OutputStreamWriter out = new OutputStreamWriter(bufOut);
					System.out.printf("There are currently %d backups to be restored\n", backups.size());
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
					clients.put(nextClientID, clientSocket);
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

								backupOut.write("backup_request " + gameID + "\n");
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
	@SuppressWarnings("unchecked")
	private void backupRun(String serverIP){
		boolean mainServerDown = false;
		Server server = null;
		String message = "";
		Gson gson = new GsonBuilder().serializeNulls()
			.excludeFieldsWithModifiers(Modifier.TRANSIENT)
			.registerTypeAdapter(Socket.class, new SocketDeserializer())
			.create();

		try{
			Socket syncSocket = new Socket(serverIP, SYNCPORT);
			BufferedInputStream bufIn = new BufferedInputStream(syncSocket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			BufferedOutputStream bufOut = new BufferedOutputStream(syncSocket.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);
			System.out.println("Connected to main server!");

			while(!mainServerDown){
				if(in.ready()){
					message = IOUtilities.read(in);	
					System.out.printf("%s: Backup Received\n", new Date());
				}else{
					try{
						out.write("ping\n");
						out.flush();
						Thread.sleep(1000);
					} catch(Exception e){
						System.err.println("Server is down. Taking over...");
						mainServerDown = true;
					}
				}
				Thread.sleep(100);
			}
			Thread.sleep(2000);
		} catch (SocketException se){
			System.err.println("Unable to connect to main server");
		} catch (Exception e){e.printStackTrace();}
		if(!message.equals("")){
			new Thread(new Server(CLIENTPORT, Server.CLIENT)).start();
			new Thread(new Server(BACKUPPORT, Server.BACKUP)).start();
			new Thread(new ServerSync(server)).start();
			server = gson.fromJson(message, Server.class);
			server.run();
		} else {
			System.out.println("ERROR: Did not receive any backups from server before it went down.");
			System.out.println("Exiting...");
			System.exit(0);
		}
	}

	/**
	 * Gets the servers
	 * @return HashMap<Integer, Socket>
	 */
	public HashMap<Integer, Socket> getServersInfo(){
		return servers;
	}
	/**
	 * Gets the clients
	 * @return HashMap<Integer, Socket>
	 */
	public HashMap<Integer, Socket> getClientsInfo(){
		return clients;
	}

	/**
	 * Gets the next game ID.
	 * @return int - the ID
	 */
	public int getNextGameID(){
		return nextGameID;
	}

	/**
	 * Gets the next client ID.
	 * @return int - the ID
	 */
	public int getNextClientID(){
		return nextClientID;
	}

	/**
	 * Gets the backup servers
	 * @return ArrayList<Socket>
	 */
	public ArrayList<Socket> getBackupServers(){
		return backupServers;
	}

	/**
	 * Gets the backup IDs
	 * @return ArrayList<Integer>
	 */
	public ArrayList<Integer> getBackupIDs(){
		return backupIDs;
	}

	/**
	 * Gets the backup info
	 * @return HashMap<Integer, String>
	 */
	public HashMap<Integer, String> getBackupsInfo(){
		return backups;
	}


	public static void main(String[] args) {
		if(args.length == 2 && args[0].equals("-b")){
			System.out.println("Running as backup server");
			String serverIP = args[1];
			new Server(SERVERPORT, Server.SERVER).backupRun(serverIP);
		}
		else{
			new Thread(new Server(CLIENTPORT, Server.CLIENT)).start();
			new Thread(new Server(BACKUPPORT, Server.BACKUP)).start();
			Server server = new Server(SERVERPORT, Server.SERVER);
			new Thread(new ServerSync(server)).start();
			server.run();
		}
	}

}
