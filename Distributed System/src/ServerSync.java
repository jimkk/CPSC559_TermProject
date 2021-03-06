import java.util.*;
import java.io.*;
import java.net.*;
import com.google.gson.*;
import java.lang.reflect.*;


/**
 *	Class that sends updates to the backup Server in case of the main Server going down.
 *
 */

public class ServerSync implements Runnable{

	private Server server;
	private boolean isDone = false;
	private OutputStreamWriter out;

	/**
	 * @param server The server object to monitor for changes and back it up.
	 */
	public ServerSync(Server server){
		this.server = server;
	}

	/**
	 * The function that gets called when a thread of this class is run.
	 * It will monitor the server object for changes and send them to a backup server
	 * if one exists.
	 */
	public void run(){

		try{
			ServerSocket serverSocket = new ServerSocket(Server.SYNCPORT);
			Socket syncSocket = serverSocket.accept();
			BufferedOutputStream bufOut = new BufferedOutputStream(syncSocket.getOutputStream());
			out = new OutputStreamWriter(bufOut);
		} catch (Exception e) {e.printStackTrace();}

		while(!isDone){
			if(Server.changesMade){
				if(out == null){
					System.out.println("No sync server connected");
					printServerInfo();
				}
				else{
					sendServerInfo();
				}
				Server.changesMade = false;
			}
			try{
				Thread.sleep(100);
			} catch (Exception et) {et.printStackTrace();}
		}

	}
	
	/**
	 * Sends a json string representation of the server object.
	 */
	private void sendServerInfo(){
		try{
			Gson gson = new GsonBuilder().serializeNulls()
				.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
				.registerTypeAdapter(Socket.class, new SocketSerializer())	
				.create();
			gson.toJson(server, out);
			out.write("\n");
			out.flush();
		} catch (IOException e){
			System.err.println("Failed to send server info:");
			e.printStackTrace();
		}
	}

	/**
	 * Prints a human-readable version of the server object's data.
	 */
	private void printServerInfo(){
		System.out.println("Servers: " + Arrays.deepToString(toArrayFromSocket(server.getServersInfo())));
		System.out.println("Clients: " + Arrays.deepToString(toArrayFromSocket(server.getClientsInfo())));
		System.out.println("nextGameID: " + server.getNextGameID());
		System.out.println("nextClientID: " + server.getNextClientID());
		System.out.println("Backup Servers: " + server.getBackupServers());
		System.out.println("Backup IDs: " + server.getBackupIDs());
		System.out.println("Backups: " + Arrays.deepToString(toArrayFromString(server.getBackupsInfo())));
	}

	/**
	 * Creates a string array representation of a HashMap with socket values object for printing.
	 * @param HashMap<Integer,Socket> The HashMap to convert.
	 * @return String[][] the array representation of the HashMap passed to the
	 * method.
	 */	
	private String [][] toArrayFromSocket(HashMap<Integer, Socket> map){
		String [][] mapArray = new String[map.size()][3];
		Iterator it = map.keySet().iterator();
		int i = 0;
		while(it.hasNext()){
			int gameID = (int) it.next();
			Socket socket = map.get(gameID);
			mapArray[i][0] = Integer.toString(gameID);
			mapArray[i][1] = socket.getInetAddress().toString();
			mapArray[i][2] = Integer.toString(socket.getPort());
			i++;
		}
		return mapArray;
	}

	/**
	 * Creates a string array representation of a HashMap with String values.
	 * @param HashMap<Integer,String> The HashMap to convert.
	 * @return String[][] the array representation of the HashMap passed to the
	 * method.
	 */
	private String [][] toArrayFromString(HashMap<Integer, String> map){
		String [][] mapArray = new String[map.size()][2];
		Iterator it = map.keySet().iterator();
		int i = 0;
		while(it.hasNext()){
			int id = (int) it.next();
			mapArray[i][0] = Integer.toString(id);
			mapArray[i][1] = map.get(id); 
			i++;
		}
		return mapArray;
	}

}
