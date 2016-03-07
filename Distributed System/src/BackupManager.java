import java.io.*;
import java.net.*;

public class BackupManager implements Runnable{

	private boolean isDone = false;
	private ObjectOutputStream out;
	GameManager game;

	public BackupManager(ObjectOutputStream out, GameManager game){
		this.out = out;
		this.game = game;
	}

	public void run(){
		int turn;
		int pot;
		int playerCount;

		try{
			out.writeObject(game);
			out.flush();

			turn = game.getTurn();
			pot = game.getPot();
			playerCount = game.getPlayerCount();

			while(!isDone){
				if(turn != game.getTurn() || pot != game.getPot() ||
						playerCount != game.getPlayerList().getCount()){
					System.out.printf("Game State Changed.\n");
					System.out.println(playerCount);
					out.writeObject(game);
					out.flush();
					turn = game.getTurn();
					pot = game.getPot();
					playerCount = game.getPlayerList().getCount();
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {e.printStackTrace();}
	}
}
