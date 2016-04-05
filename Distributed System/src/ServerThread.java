import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;


/**
 * Processes communications between a client and the game server that they are connected to.
 */
public class ServerThread implements Runnable{

	private transient Socket clientSocket;
	private transient Socket gameServerSocket;
	private transient HashMap<Integer, Socket> servers;

	private BufferedInputStream bufIn;
	private InputStreamReader in;
	private BufferedOutputStream bufOut;
	private OutputStreamWriter out;
	private BufferedInputStream bufGameIn;
	private InputStreamReader gameIn;
	private BufferedOutputStream bufGameOut;
	private OutputStreamWriter gameOut;

	private boolean gameServerChosen;
	private boolean isDone;
	private boolean isRecovery = false;

	private int clientID;
	private int gameIndex;

	private Server server;

	private static String message;
	private static boolean readingStream = false;

	/**
	 * @param socket The client's socket
	 * @param clientID The client's ID
	 * @param servers The list of available servers
	 */
	public ServerThread(Socket socket, int clientID, Server server){
		this.clientSocket = socket;
		this.clientID = clientID;
		this.servers = server.getServersInfo();
		this.server = server;
		gameServerChosen = false;
		message = "";
	}
	
	/**
	 * @param socket The client's socket
	 * @param clientID The client's ID
	 * @param servers The list of available servers
	 * @param isRecovery True if this is a new thread created during server recovery
	 */
	public ServerThread(Socket socket, int clientID, Server server, boolean isRecovery){
		this.clientSocket = socket;
		this.clientID = clientID;
		this.servers = server.getServersInfo();
		this.server = server;
		gameServerChosen = false;
		message = "";
		this.isRecovery = isRecovery;
	}


	public void run(){

		try{

			bufIn = new BufferedInputStream(clientSocket.getInputStream());
			in = new InputStreamReader(bufIn);
			bufOut = new BufferedOutputStream(clientSocket.getOutputStream());
			out = new OutputStreamWriter(bufOut);

			out.write("message Welcome to the Poker server!\n");
			out.flush();

			out.write("clientID " + clientID + "\n");
			out.flush();

			while(!gameServerChosen){
				if(servers.size() == 0){
					System.out.println("Currently no active servers...");
					try{
						out.write("message There are no active game servers...\n");
						out.flush();
					} catch (Exception e){
						System.out.printf("Client %d disconnected\n", clientID);
						return;
					}
					Thread.sleep(5000);
					continue;
				}
				//TODO Ask which game server to join
				//For now, it's the first one
				gameIndex = 1;
				//Scanner scanner = new Scanner(System.in);
				//System.out.print("Which game number would you like to join?: ");
				//gameIndex = scanner.nextInt();


				do{
					gameServerSocket = servers.get(gameIndex);
					gameIndex++;
					if(gameIndex > 100){
						System.err.println("Can't find a valid game server");
						System.exit(-1);
					}
				} while (gameServerSocket == null);
				gameServerChosen = true;
				if(gameServerSocket == null){
					System.err.println("Something went wrong, the GameServer chosen does not exist");
					System.exit(-1);
				}
			}
			gameIndex--;

			bufGameIn = new BufferedInputStream(gameServerSocket.getInputStream());
			gameIn = new InputStreamReader(bufGameIn);
			bufGameOut = new BufferedOutputStream(gameServerSocket.getOutputStream());
			gameOut = new OutputStreamWriter(bufGameOut);

			int stack = 1000; //TODO Custom stack
			if(isRecovery){
				while(true){
					String message = IOUtilities.read(in);
					if (message.split(" ")[0].equals("changeClientID")){
						int newID = Integer.parseInt(message.split(" ")[1]);
						System.out.printf("Client %d has reconnected\n");
						server.getClientsInfo().remove(newID);
						server.getClientsInfo().put(newID, clientSocket); 
						break;
					}
				}
			} else {
				gameOut.write(clientID + " addplayer " + stack + "\n");
				gameOut.flush();
				System.out.printf("Client %d added to game %d\n", clientID, gameIndex);
			}

			while(!isDone){
				try{
					if(in.ready()){
						String messageIn = IOUtilities.read(in);
						if(messageIn.equals("ping")){
							;
						} else if (messageIn.split(" ")[0].equals("changeClientID")){
							int newID = Integer.parseInt(messageIn.split(" ")[1]);
							System.out.printf("Client %d has requested that their ID be changed to %d\n");
							if(server.getClientsInfo().containsKey(newID)){
								System.out.println("Request denied. ID is already in use by a different client");
							} else {
								out.write("clientID " + newID + "\n");
								out.flush();
							}
						} else {
							System.out.printf("Message from %d: %s\n", clientID, messageIn);
							gameOut.write(clientID + " " + messageIn + "\n");
							gameOut.flush();
							if(messageIn.equals("close")){
								System.out.printf("Closing thread for client %d\n", clientID);
								isDone = true;
							}
						}
					}
					if(gameIn.ready()){
						while(readingStream){
							System.out.println("Waiting for reading stream access");
							Thread.sleep(10);
						}
						readingStream = true;
						if(!gameIn.ready()){
							readingStream = false;
						} else {
							System.out.printf("(%d)Read...", clientID);
							String newMessage = IOUtilities.read(gameIn);
							System.out.printf("(%d)Done.\n", clientID);
							readingStream = false;
							if(!newMessage.equals("ping")){

								System.out.println(newMessage);

								while(!message.equals("")){
									System.out.printf("Waiting for \"%s\" to be removed\n", message);
									Thread.sleep(100);
								}
								message = newMessage;
								System.out.printf("Message from game server: \"%s\"\n", message);
								System.out.println(message.substring(message.indexOf(" ")+1) + "\n");
								int ID = Integer.parseInt(message.split(" ")[0]);
								System.out.printf("ID = %d, clientID = %d\n", ID, clientID);
								if(ID == clientID){
									try{
										System.out.printf("Notifying Client (ID = %d, clientID = %d\n", ID, clientID);
										out.write(message.substring(message.indexOf(" ")+1) + "\n");
										out.flush();
									} catch (SocketException se) {
										System.err.println("Client disconnected.");
										isDone = true;
									}
									message = "";
								}
							}
						}
					}
					if(!message.equals("")){
						if (clientID == 1) System.out.printf("***** Thread %d *****\n\n", clientID);
						int ID = Integer.parseInt(message.split(" ")[0]);
						System.out.printf("ID = %d, clientID = %d (type2)\n", ID, clientID);
						if(ID == clientID){			
							try{
								out.write(message.substring(message.indexOf(" ")+1) + "\n");
								out.flush();
							} catch (SocketException se) {
								System.err.println("Client disconnected.");
								isDone = true;
							}
							message = "";
						}
					}

				} catch(SocketException e){
					Socket socket = servers.remove(gameIndex);
					servers.put(-gameIndex, socket);
					while(servers.get(gameIndex) == null);
					gameServerSocket = servers.get(gameIndex);
				} catch(Exception e){e.printStackTrace(); isDone = true;}

				Thread.sleep(100);
			}

		} catch (Exception e) {e.printStackTrace();}

	}

}
