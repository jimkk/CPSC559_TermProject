import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;

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
	private long timeSincePing = 0;

	private BufferedInputStream bufIn;
	private InputStreamReader in;
	private BufferedOutputStream bufOut;
	private OutputStreamWriter out;

	private int numGames = 1;
	private ArrayList<GameThread> games = new ArrayList<GameThread>();
	private static volatile HashMap<Integer, String> messageInboxes = new HashMap<Integer, String>();

	private static String backupAddress = "localhost";
	private static int backupPort = 5432;
	private static boolean backupExists = false;

	public GameServer(String address, int port){
		serverManagerAddress = address;
		serverManagerPort = port;
	}

	public GameServer(String address, int port, int numGames){
		serverManagerAddress = address;
		serverManagerPort = port;
		this.numGames = numGames;
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

		try{	
			bufIn = new BufferedInputStream(serverManagerSocket.getInputStream());
			in = new InputStreamReader(bufIn);
			bufOut = new BufferedOutputStream(serverManagerSocket.getOutputStream());
			out = new OutputStreamWriter(bufOut);

			out.write("addGameServer " + numGames + "\n");
			out.flush();

			while(!in.ready()){
				Thread.sleep(100);
			}
			String firstMessage = IOUtilities.read(in);
			String [] firstMessageParts = firstMessage.split(" ");
			int gameIDs = Integer.parseInt(firstMessageParts[1]);
			for(int i = 0; i < numGames; i++){
				messageInboxes.put(gameIDs, new String(""));
				GameThread gt;
				if(backupExists){
					gt = new GameThread(serverManagerSocket, new Socket(backupAddress, backupPort), gameIDs, messageInboxes);
				} else {
					gt = new GameThread(serverManagerSocket, gameIDs, messageInboxes);
				}
				games.add(gt);
				new Thread(gt).start();
				gameIDs++;
			}

			while(!isDone){
				try{
					if(in.ready()){
						String message = IOUtilities.read(in);
						if(message.split(" ")[0].equals("restoregame")){
							System.out.println("Restoring from backup");
							String [] messageParts = message.split(" ");
							int gameID = Integer.parseInt(messageParts[1]);
							Gson gson = new GsonBuilder().create();
							String startContents = IOUtilities.rebuildString(messageParts, 2, messageParts.length);
							GameManager game = gson.fromJson(startContents, GameManager.class);
							messageInboxes.put(gameID, new String(""));
							if(backupExists){
								new Thread(new GameThread(serverManagerSocket, new Socket(backupAddress, backupPort), gameID, messageInboxes, game)).start();
							} else {
								new Thread(new GameThread(serverManagerSocket, gameID, messageInboxes, game)).start();
							}

						} else {
							String [] messageParts = message.split(" ");
							int gameID = Integer.parseInt(messageParts[0]);
							System.out.printf("Received message for game %d: ", gameID);
							if(messageInboxes.containsKey(gameID)){
								messageInboxes.put(gameID, IOUtilities.rebuildString(messageParts, 1, messageParts.length));
							} else {
								System.out.printf("ERROR: Received message for a game that this server do not handle (ID = %d)\n", gameID);
							}
							System.out.printf("%s\n", messageInboxes.get(gameID));
						}
					}
					if(new Date().getTime() - timeSincePing > 3000){
						out.write("ping\n");
						out.flush();
						timeSincePing = new Date().getTime();
					}
					Thread.sleep(10);
				} catch (SocketException sockete){
					System.out.print("Server down...Attempting to reconnect...");
					reconnect();
					out.write("recoverGame " + (gameIDs - numGames) + " " + numGames + "\n");
					out.flush();
					System.out.println("DONE");
				}
			}
		} catch (Exception e){e.printStackTrace(); isDone = true;}
	}

	private void reconnect(){
		try{
			serverManagerSocket.close();
			while(true){
				try{
					serverManagerSocket = new Socket(serverManagerAddress, serverManagerPort);
					break;
				} catch (ConnectException conEx){
					System.out.print(".");
					Thread.sleep(2000);
				}
			}
			bufIn = new BufferedInputStream(serverManagerSocket.getInputStream());
			in = new InputStreamReader(bufIn);
			bufOut = new BufferedOutputStream(serverManagerSocket.getOutputStream());
			out = new OutputStreamWriter(bufOut);
			for(GameThread game : games){
				game.setSocket(serverManagerSocket);
			}
		} catch (Exception e) {e.printStackTrace(); System.exit(-1);}
	}

	public static void main(String[] args) {	
		if(args.length == 1){
			String address = args[0];
			int port = Server.SERVERPORT;
			try{
				new Socket(GameServer.backupAddress, GameServer.backupPort).close();
				GameServer.backupExists = true;
			} catch (Exception e){
				System.out.println("WARNING: No backup server");
			}
			new GameServer(address, port).run();
		}
		if(args.length == 2){
			String address = args[0];
			int port = Server.SERVERPORT;
			int numGames = Integer.parseInt(args[1]);
			try{
				new Socket(GameServer.backupAddress, GameServer.backupPort).close();
				GameServer.backupExists = true;
			} catch (Exception e){
				System.out.println("WARNING: No backup server");
			}
			new GameServer(address, port, numGames).run();
		}
		if(args.length == 3){
			String address = args[0];
			int port = Server.SERVERPORT;
			int numGames = Integer.parseInt(args[1]);
			GameServer.backupAddress = args[2];
			try{
				new Socket(GameServer.backupAddress, GameServer.backupPort).close();
				GameServer.backupExists = true;
			} catch (Exception e){
				System.out.println("WARNING: No backup server");
			}
			new GameServer(address, port, numGames).run();
		}
	}

}
