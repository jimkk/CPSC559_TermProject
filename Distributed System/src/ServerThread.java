import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

public class ServerThread implements Runnable{

	private Socket socket;
	private boolean isDone = false;
	public static int playerCount = 0;
	private static int playerID = 0;
	private boolean turn = false;
	private boolean folded = false;
	Random rand = new Random();
	int randomCardNumber;
	
	Card[] hand;
	LinkedPlayerList playerList = new LinkedPlayerList();

	public ServerThread(Socket socket){
		this.socket = socket;
	}

	public void run(){
		System.out.printf("New Client Connected, IP=%s, Port=%d\n", socket.getInetAddress(), socket.getPort());
		playerCount++;
		playerList.addPlayers(playerCount, playerID, turn, folded, hand);
		playerList.displayGameState();
		
		try{
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);

			while(!isDone){

				StringBuffer buffer = new StringBuffer();
				int c;
				while((c = in.read()) != (int)'\n'){
					buffer.append((char) c);
				}

				String messageType;
				if(buffer.indexOf(" ") != -1){
					messageType = buffer.substring(0, buffer.indexOf(" "));
				} else {
					messageType = buffer.toString();
				}

				switch(messageType){
					case("deal"):
						//String deal = buffer.substring(buffer.indexOf(" "));
						randomCardNumber = rand.nextInt(52) + 1;
						String deal = String.valueOf(randomCardNumber);
						System.out.printf("Dealt card from %s: %s\n", socket.getInetAddress(), deal);
						break;
					case("message"):
						String message = buffer.substring(buffer.indexOf(" "));
						System.out.printf("Message from %s: %s\n", socket.getInetAddress(), message);
						break;
					case("close"):
						System.out.println("Socket closed at client's request");
						isDone = true;
						break;
					case("destroy"):
						System.out.println("Server shut down at client's request");
						//TODO
						break;
					default:
						System.out.println("ERROR: Unknown Message Type");
						System.out.println("\t" + buffer);
						System.exit(-1);
						break;
				}
			}

		} catch(Exception e) {e.printStackTrace();}

	}

}
