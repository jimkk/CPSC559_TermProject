import java.net.*;
import java.io.*;
import java.util.Date;
import com.google.gson.*;

public class BackupServer {

	private ServerSocket serverSocket;
	private int port = 5432;
	private boolean isDone = false;
	private GameManager game;


	public void run(){
		try{
			String message;

			serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			//ObjectInputStream in = new ObjectInputStream(bufIn);
			InputStreamReader in = new InputStreamReader(bufIn);

			//game = (GameManager) in.readObject();
			message = read(in).toString();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			game = gson.fromJson(message, GameManager.class);

			System.out.println("Received backup from server");
			System.out.println(game.getPlayerCount());
			System.out.println(game.getPot());
			System.out.println(game.getTurn());
			while(!isDone){
				//Date date = (Date) in.readObject();
				//System.out.printf("Backup for %s\n", date);
				//game = (GameManager) in.readObject();

				message = read(in).toString();
				game = gson.fromJson(message, GameManager.class);
				System.out.println("Received backup from server");
				System.out.printf("Player Count: %d\n", game.getPlayerCount());
				System.out.printf("Pot: %d\n", game.getPot());
				System.out.printf("Turn: %d\n", game.getTurn());
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	private StringBuffer read(InputStreamReader in){
		try{
			StringBuffer buffer = new StringBuffer();
			int c;
			while((c = in.read()) != -1){
				if(c == (int) '\n'){
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
