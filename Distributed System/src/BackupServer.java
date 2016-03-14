import java.net.*;
import java.io.*;
import java.util.Date;
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

				message = read(in).toString();
				System.out.printf("%s: Received backup from server\n", new Date());
				
				FileWriter fw = new FileWriter("backups.bck");
				fw.write(message);
				fw.close();
				
				//System.out.println(message);
				game = gson.fromJson(message, GameManager.class);
				if(game.getPlayerCount() > 0){
					list = game.getPlayerList();
					list.findPlayerByIndex(list.getCount()-1).nextPlayer = 
						list.findPlayerByIndex(0);
				}
			}
		} catch(NullPointerException e){
			System.out.println("--------------------------");
			System.err.println("Lost connection to server."); 
			System.out.println("--------------------------");
			System.exit(-1);
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

	public static void main (String [] args){
		BackupServer bs = new BackupServer();
		bs.run();
	}


}
