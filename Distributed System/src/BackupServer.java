import java.net.*;
import java.io.*;

public class BackupServer {

	private ServerSocket serverSocket;
	private int port = 5432;
	private boolean isDone = false;
	private GameManager game;


	public void run(){
		try{
			serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			ObjectInputStream in = new ObjectInputStream(bufIn);
			while(!isDone){
				game = (GameManager) in.readObject();
				System.out.println("Received backup from server");
				System.out.println(game.getPlayerCount());
				System.out.println(game.getPot());
				System.out.println(game.getTurn());
			}
		} catch (Exception e) {e.printStackTrace();}
	}

	public static void main (String [] args){
		BackupServer bs = new BackupServer();
		bs.run();
	}


}
