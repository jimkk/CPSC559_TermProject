import java.io.*;
import java.net.*;
import java.util.Date;
import com.google.gson.*;

public class BackupManager implements Runnable{

	private boolean isDone = false;
	private OutputStreamWriter out;
	GameManager game;

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
			//FileWriter fw = new FileWriter("backup.json");
			//out.writeObject(game);
			//out.flush();

			turn = game.getTurn();
			pot = game.getPot();
			playerCount = game.getPlayerCount();

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			message = gson.toJson(game);
			out.write(message + "\f");
			out.flush();
			//fw.close();

			while(!isDone){
				if(turn != game.getTurn() || pot != game.getPot() ||
						playerCount != game.getPlayerList().getCount()){
					System.out.printf("Game State Changed.\n");

					//GSON
					//fw = new FileWriter("backup.json");
					LinkedPlayerList list = game.getPlayerList();
					list.findPlayerByIndex(list.getCount()-1).nextPlayer = null;
					message = gson.toJson(game);
					list.findPlayerByIndex(list.getCount()-1).nextPlayer = 
						list.findPlayerByIndex(0);
					//fw.close();

					out.write(message + "\f");
					out.flush();

					//out.writeObject(new Date());
					//out.writeObject(game);
					System.out.printf("Player Count: %d\n", game.getPlayerCount());
					System.out.printf("Pot: %d\n", game.getPot());
					System.out.printf("Turn: %d\n", game.getTurn());
					//oos.writeObject(game);
					turn = game.getTurn();
					pot = game.getPot();
					playerCount = game.getPlayerList().getCount();
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {e.printStackTrace();}
	}
}
