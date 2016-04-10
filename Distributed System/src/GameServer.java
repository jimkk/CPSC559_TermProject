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
	private static volatile HashMap<Integer, String> messageInboxes = new HashMap<Integer, String>();

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
				new Thread(new GameThread(serverManagerSocket, gameIDs, messageInboxes)).start();
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
						new Thread(new GameThread(serverManagerSocket, gameID, messageInboxes, game)).start();
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
					reconnect();
				}

			}
		} catch (Exception e){e.printStackTrace(); isDone = true;}
	}
	
	private void reconnect(){
		try{
			serverManagerSocket.close();
			ServerSocket reconnectSocket = new ServerSocket(serverManagerSocket.getLocalPort());
			serverManagerSocket = reconnectSocket.accept();
			reconnectSocket.close();
			bufIn = new BufferedInputStream(serverManagerSocket.getInputStream());
			in = new InputStreamReader(bufIn);
			bufOut = new BufferedOutputStream(serverManagerSocket.getOutputStream());
			out = new OutputStreamWriter(bufOut);
		} catch (Exception e) {e.printStackTrace();}
	}

	public static void main(String[] args) {	
		if(args.length == 1){
			String address = args[0];
			int port = Server.SERVERPORT;
			new GameServer(address, port).run();
		}
		if(args.length == 2){
			String address = args[0];
			int port = Server.SERVERPORT;
			int numGames = Integer.parseInt(args[1]);
			new GameServer(address, port, numGames).run();
		}
	}

}
