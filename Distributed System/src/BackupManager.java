import java.io.*;
import java.net.*;
import java.util.Date;
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

	public void run(){
		int turn;
		int pot;
		int playerCount;

		String message;

		try{
			//FileOutputStream fos = new FileOutputStream("serialize.tmp");
			//ObjectOutputStream oos = new ObjectOutputStream(fos);
			//out.writeObject(game);
			//out.flush();

			turn = game.getTurn();
			pot = game.getPot();
			playerCount = game.getPlayerCount();

			Gson gson = new GsonBuilder().create();
			message = gson.toJson(game);
			out.write("json_backup " + game.getGameID() + " " + message + "\n");
			out.flush();

			while(!isDone){
				System.out.println("checking...");
				if(turn != game.getTurn() || pot != game.getPot() ||
						playerCount != game.getPlayerList().getCount()){
					System.out.printf("Game State Changed.\n");

					//GSON
					LinkedPlayerList list = game.getPlayerList();
					list.findPlayerByIndex(list.getCount()-1).nextPlayer = null;
					System.out.println(game);
					message = gson.toJson(game);
					list.findPlayerByIndex(list.getCount()-1).nextPlayer = 
						list.findPlayerByIndex(0);

					out.write("json_backup " + game.getGameID() + " " + message + "\n");
					out.flush();
					System.out.println("Backup Send");
					//System.out.printf("Player Count: %d\n", game.getPlayerCount());
					//System.out.printf("Pot: %d\n", game.getPot());
					//System.out.printf("Turn: %d\n", game.getTurn());
					turn = game.getTurn();
					pot = game.getPot();
					playerCount = game.getPlayerList().getCount();
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {e.printStackTrace();}
	}
}
