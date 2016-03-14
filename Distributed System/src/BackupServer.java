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

	private ServerSocket serverSocket;
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

			serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);


			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			while(!isDone){
				if(in.ready()){
					message = read(in).toString();
					System.out.printf("Message: \"%s\"\n", message);
					String [] messageParts = message.split(" ");
					int gameID = Integer.parseInt(messageParts[1]);
					String backup = rebuildString(messageParts, 2, messageParts.length);

					System.out.printf("%s: Received backup from server for game %d\n", new Date(), gameID);

					FileWriter fw = new FileWriter("backup" + gameID + ".bck");
					fw.write(backup);
					fw.close();

					//System.out.println(backup);
					game = gson.fromJson(backup, GameManager.class);
					if(game.getPlayerCount() > 0){
						list = game.getPlayerList();
						list.findPlayerByIndex(list.getCount()-1).nextPlayer = 
							list.findPlayerByIndex(0);
					}
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


	public void restoreBackup(){
		try{
			StringBuffer restoredMessage = new StringBuffer();
			int c;
			FileReader fr = new FileReader("backups.bck");
			

			while((c = fr.read()) != -1){
				restoredMessage.append((char) c);
			}
			
		} catch (Exception e) {e.printStackTrace();}

	private String rebuildString(String [] parts, int start, int end){
		StringBuffer buffer = new StringBuffer();
		for(int i = start; i < end; i++){
			buffer.append(parts[i]);
		}
		return buffer.toString();

	}

	public static void main (String [] args){
		BackupServer bs = new BackupServer();
		bs.run();
	}


}
