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
		try{
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);

			if(playerCount >= 2){
				System.out.println("New client rejected due to game being full");
				out.write("full");
				out.flush();
				Thread.sleep(5000);
				return;
			}

			System.out.printf("New Client Connected, IP=%s, Port=%d\n", socket.getInetAddress(), socket.getPort());
			playerCount++;
			playerList.addPlayers(playerCount, playerID, turn, folded, hand);
			playerList.displayGameState();

			StringBuffer buffer = new StringBuffer();
			String messageType = "";

			while(!isDone){
				if(in.ready()){
					buffer = read(in);
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
							out.write("message Card dealt: " + deal + "\n");
							out.flush();
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
						case(""):
							break;
						default:
							System.out.println("ERROR: Unknown Message Type");
							System.out.println("\t" + buffer);
							System.exit(-1);
							break;
					}
				}
				Thread.sleep(10);
			}

		} catch(Exception e) {e.printStackTrace();}

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

}
