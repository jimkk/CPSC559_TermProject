import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.Scanner;
import com.google.gson.*;


/** 
 * This class is the main process for backups from the game server. It listens
 * on the specified port for any backups sent to it in JSON format.
 */

public class BackupServer {

	private static final int serverManagerPort = 7775;
	private static String serverManagerAddress;

	private ServerSocket serverSocket;
	private Socket serverManagerSocket;
	private int port = 5432;
	private boolean isDone = false;
	private GameManager game;

	/** 
	 * This is the main process that continuously loops, listening for backups and
	 * processing and storing any that are received.
	 */
	public void run(){
		try{
			String message;
			LinkedPlayerList list;

			serverManagerSocket = new Socket(serverManagerAddress, serverManagerPort);

			serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			
			BufferedInputStream bufManagerIn = new BufferedInputStream(serverManagerSocket.getInputStream());
			InputStreamReader managerIn = new InputStreamReader(bufManagerIn);

			BufferedOutputStream bufManagerOut = new BufferedOutputStream(serverManagerSocket.getOutputStream());
			OutputStreamWriter managerOut = new OutputStreamWriter(bufManagerOut);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			while(!isDone){
				if(in.ready()){
					message = read(in).toString();
					String [] messageParts = message.split(" ");
					int gameID = Integer.parseInt(messageParts[1]);
					String backup = rebuildString(messageParts, 2, messageParts.length);

					System.out.printf("%s: Received backup from server for game %d\n", new Date(), gameID);

					FileWriter fw = new FileWriter("backup" + gameID + ".bck");
					fw.write(backup);
					fw.close();

					System.out.println(backup);
					game = gson.fromJson(backup, GameManager.class);
					System.out.printf("Number of players: %d\n", game.getPlayerCount());
					if(game.getPlayerCount() > 0){
						list = game.getPlayerList();
						list.findPlayerByIndex(list.getCount()-1).nextPlayer = 
							list.findPlayerByIndex(0);
					}
				}
				else if(managerIn.ready()){
					String managerMessage = read(managerIn).toString();
					int gameID = Integer.parseInt(managerMessage.split(" ")[1]);
					System.out.printf("Recovering %d from backup\n", gameID);
					//TODO Recover backup for gameID
					
					String restoredString = restoreBackup(gameID);
					managerOut.write("backup_response " + gameID + " " + restoredString + "\f");
					managerOut.flush();
					System.out.println("Sent backup!");
				}
			}
		} catch(NullPointerException e){
			System.out.println("--------------------------");
			System.err.println("Lost connection to GameServer. Please restart it to demonstrate Fault Tolerance."); 
			System.out.println("--------------------------");
			//System.exit(-1);
		}
		catch (Exception e) {e.printStackTrace();}
	}

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


	public String restoreBackup(int gameID){
		StringBuffer restoredMessage = new StringBuffer();
		try{
			System.out.println("In restoreBackup()");
			int c;
			FileReader fr = new FileReader("backup" + gameID +".bck");
			
			while((c = fr.read()) != -1){
				restoredMessage.append((char) c);
			}		
		} catch (Exception e) {e.printStackTrace();}
		return restoredMessage.toString();
	}

	private String rebuildString(String [] parts, int start, int end){
		StringBuffer buffer = new StringBuffer();
		for(int i = start; i < end; i++){
			buffer.append(parts[i]);
		}
		return buffer.toString();

	}

	public static void main (String [] args){
		if(args.length == 1){

			BackupServer bs = new BackupServer();
			bs.serverManagerAddress = args[0];
			bs.run();
		}
	}


}
