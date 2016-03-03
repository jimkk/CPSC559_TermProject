import java.io.*;
import java.net.*;

public class BackupManager implements Runnable{

	private boolean isDone = false;
	private int gameChecksum = -1;
	private ObjectOutputStream out;
	GameManager game;

	public BackupManager(ObjectOutputStream out, GameManager game){
		this.out = out;
		this.game = game;
	}

	public void run(){
		while(!isDone){
			if(gameChecksum != game.hashCode()){
				System.out.printf("Game State Changed. %x\n", gameChecksum);
				try{
					out.writeObject(game);
					out.flush();
				} catch (Exception e) {e.printStackTrace();}
				gameChecksum = game.hashCode();
			}
		}
	}
}
