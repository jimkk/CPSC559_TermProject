import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;


/**
 * Processes communications between a client and the game server that they are connected to.
 */
public class ServerThread implements Runnable{

	private Socket clientSocket;
	private Socket gameServerSocket;
	private ArrayList<Socket> servers;
	
	private BufferedInputStream bufIn;
	private InputStreamReader in;
	private BufferedOutputStream bufOut;
	private OutputStreamWriter out;
	private BufferedInputStream bufGameIn;
	private InputStreamReader gameIn;
	private BufferedOutputStream bufGameOut;
	private OutputStreamWriter gameOut;

	private boolean gameServerChosen;
	private boolean isDone;

	private int clientID;

	public ServerThread(Socket socket, int clientID, ArrayList<Socket> servers){
		this.clientSocket = socket;
		this.clientID = clientID;
		this.servers = servers;
		gameServerChosen = false;
	}

	public void run(){

		try{

		bufIn = new BufferedInputStream(clientSocket.getInputStream());
		in = new InputStreamReader(bufIn);
		bufOut = new BufferedOutputStream(clientSocket.getOutputStream());
		out = new OutputStreamWriter(bufOut);

		out.write("message Welcome to the Poker server!\n");
		out.flush();
		while(!gameServerChosen){
			if(servers.size() == 0){
				System.out.println("Currently no active servers...");
				Thread.sleep(1000);
				continue;
			}
			//TODO Ask which game server to join
			//For now, it's the first one
			gameServerSocket = servers.get(0);
			gameServerChosen = true;
		}

		bufGameIn = new BufferedInputStream(gameServerSocket.getInputStream());
		gameIn = new InputStreamReader(bufGameIn);
		bufGameOut = new BufferedOutputStream(gameServerSocket.getOutputStream());
		gameOut = new OutputStreamWriter(bufGameOut);

		int stack = 1000; //TODO Custom stack
		gameOut.write(clientID + " addplayer " + stack + "\n");
		gameOut.flush();

		while(!isDone){
			try{
				if(in.ready()){
					String message = read(in).toString();
					System.out.printf("Message from %d: %s\n", clientID, message);
					gameOut.write(clientID + " " + message + "\n");
					gameOut.flush();
				}
				if(gameIn.ready()){
					String message = read(gameIn).toString();
					System.out.printf("Message from game server: \"%s\"\n", message);
					int ID = Integer.parseInt(message.split(" ")[0]);
					if(ID == clientID){
						out.write(message.substring(message.indexOf(" ")+1) + "\n");
						out.flush();
					}
				}
			} catch(SocketException e){
				System.err.println("Game server has crashed. Recovering...(but not really)");
			} catch(Exception e){e.printStackTrace(); isDone = true;}
			
			Thread.sleep(100);
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

}
