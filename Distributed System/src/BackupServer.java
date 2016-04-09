import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.Scanner;
import com.google.gson.*;


/** 
 * This class is the main process for backups from the game server. It listens
 * on the specified port for any backups sent to it in JSON format.
 */

public class BackupServer implements Runnable{

	private static final int MAINSERVER = 0;
	private static final int GAMESERVER = 1;

	private static final int serverManagerPort = 7775;
	private static String serverManagerAddress;
	private static final int GAMESERVERPORT = 5432;

	private ServerSocket serverSocket;
	private Socket socket;
	private boolean isDone = false;
	private GameManager game;
	private int type;

	public BackupServer(String address){
		this.type = MAINSERVER;
		this.serverManagerAddress = address;
	}

	public BackupServer(Socket socket){
		this.type = GAMESERVER;
		this.socket = socket;
	}


	/** 
	 * This is the main process that continuously loops, listening for backups and
	 * processing and storing any that are received.
	 */
	public void run(){
		if(type == MAINSERVER){
			mainServer();
		} else if (type == GAMESERVER){
			gameServer();
		}
	}

	private void mainServer(){
		try{
			socket = new Socket(serverManagerAddress, serverManagerPort);

			BufferedInputStream bufManagerIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader managerIn = new InputStreamReader(bufManagerIn);
			BufferedOutputStream bufManagerOut = new BufferedOutputStream(socket.getOutputStream());
			OutputStreamWriter managerOut = new OutputStreamWriter(bufManagerOut);

			while(!isDone){
				if(managerIn.ready()){
					String managerMessage = IOUtilities.read(managerIn);
					int gameID = Integer.parseInt(managerMessage.split(" ")[1]);
					System.out.printf("Recovering %d from backup\n", gameID);
					String restoredString = restoreBackup(gameID);
					managerOut.write("backup_response " + gameID + " " + restoredString + "\n");
					managerOut.flush();
					System.out.println("Sent backup!");
				}
				Thread.sleep(10);
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}

	private void gameServer(){
		try{
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);


			Gson gson = new GsonBuilder().create();

			while(!isDone){
				if(in.ready()){
					String message = IOUtilities.read(in);
					String [] messageParts = message.split(" ");
					int gameID = Integer.parseInt(messageParts[1]);
					String backup = IOUtilities.rebuildString(messageParts, 2, messageParts.length);

					System.out.printf("%s: Received backup from server for game %d\n", new Date(), gameID);

					FileWriter fw = new FileWriter("backup" + gameID + ".bck");
					fw.write(backup);
					fw.close();

					game = gson.fromJson(backup, GameManager.class);
				}
				Thread.sleep(100);
			}
		} catch(NullPointerException e){
			System.out.println("--------------------------");
			System.err.println("Lost connection to GameServer."); 
			System.out.println("--------------------------");
		}
		catch (Exception e) {e.printStackTrace();}
	}

	/**
	 *Restore a backup.
	 *@param gameID Integer representating the gameID.
	 */
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

	public static void main (String [] args){
		if(args.length == 1){
			boolean isDone = false;
			new Thread(new BackupServer(args[0])).start();
			try{
				ServerSocket server = new ServerSocket(BackupServer.GAMESERVERPORT);
				while(!isDone){
					Socket socket = server.accept();
					new Thread(new BackupServer(socket)).start();
				}
			} catch (IOException e){e.printStackTrace();}

		}
	}


}
