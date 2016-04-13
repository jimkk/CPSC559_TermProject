import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;

/**
 *	BackupManager is a thread started by the Server class that continuously
 *	checks the game state for any changes and if there are changes then it will
 *	send a backup to the backup server.
 */

public class BackupManager implements Runnable{

	private boolean isDone = false;
	private OutputStreamWriter out;
	GameManager game;


	/**
	 * @param out This is the output stream to the backup server
	 * @param game This is the GameManager class that will be monitored for changes
	 */

	public BackupManager(OutputStreamWriter out, GameManager game){
		this.out = out;
		this.game = game;
	}

	/**
	 * Method that is called when a new thread is started. It contains the loop
	 * that runs, checking for game state changes and sending backups whenever it 
	 * detects a change.
	 */
	public void run(){
		int turn;
		int pot;
		int playerCount;

		String message;

		try{
			turn = game.getTurn();
			pot = game.getPot();
			playerCount = game.getPlayerCount();

			Gson gson = new GsonBuilder().create();
			message = gson.toJson(game);
			out.write("json_backup " + game.getGameID() + " " + message + "\n");
			out.flush();

			while(!isDone){
				if(turn != game.getTurn() || pot != game.getPot() ||
						playerCount != game.getPlayerList().getCount()){
					System.out.printf("Game State Changed.\n");

					//GSON
					LinkedPlayerList list = game.getPlayerList();
					if(list.getCount() > 0){
						list.findPlayerByIndex(list.getCount()-1).nextPlayer = null;
					}
					while(true){
						try{
							message = gson.toJson(game);
							break;
						} catch (ConcurrentModificationException cme){
							System.out.println("GSON concurrency issue");
							Thread.sleep(100);
						}
					}
					if(list.getCount() > 0){
						list.findPlayerByIndex(list.getCount()-1).nextPlayer = 
							list.findPlayerByIndex(0);
					}

					out.write("json_backup " + game.getGameID() + " " + message + "\n");
					out.flush();
					System.out.println("Backup Send");
					turn = game.getTurn();
					pot = game.getPot();
					playerCount = game.getPlayerList().getCount();
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {e.printStackTrace();}
	}
}
