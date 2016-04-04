import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;


/**
 * Processes communications between a client and the game server that they are connected to.
 */
public class ServerThread implements Runnable{

	private transient Socket clientSocket;
	private transient Socket gameServerSocket;
	private transient HashMap<Integer, Socket> servers;

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
	private boolean isRecovery = false;

	private int clientID;
	private int gameIndex;

	private static String message;

	public ServerThread(Socket socket, int clientID, HashMap<Integer, Socket> servers){
		this.clientSocket = socket;
		this.clientID = clientID;
		this.servers = servers;
		gameServerChosen = false;
		message = "";
	}
	
	public ServerThread(Socket socket, int clientID, HashMap<Integer, Socket> servers, boolean isRecovery){
		this.clientSocket = socket;
		this.clientID = clientID;
		this.servers = servers;
		gameServerChosen = false;
		message = "";
		this.isRecovery = isRecovery;
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
				gameIndex = 1;
				//Scanner scanner = new Scanner(System.in);
				//System.out.print("Which game number would you like to join?: ");
				//gameIndex = scanner.nextInt();


				do{
					gameServerSocket = servers.get(gameIndex);
					gameIndex++;
					if(gameIndex > 100){
						System.err.println("Can't find a valid game server");
						System.exit(-1);
					}
				} while (gameServerSocket == null);
				gameServerChosen = true;
				if(gameServerSocket == null){
					System.err.println("Something went wrong, the GameServer chosen does not exist");
					System.exit(-1);
				}
			}
			gameIndex--;

			bufGameIn = new BufferedInputStream(gameServerSocket.getInputStream());
			gameIn = new InputStreamReader(bufGameIn);
			bufGameOut = new BufferedOutputStream(gameServerSocket.getOutputStream());
			gameOut = new OutputStreamWriter(bufGameOut);

			int stack = 1000; //TODO Custom stack
			if(!isRecovery){
				gameOut.write(clientID + " addplayer " + stack + "\n");
				gameOut.flush();
				System.out.printf("Client %d added to game %d\n", clientID, gameIndex);
			}

			while(!isDone){
				try{
					if(in.ready()){
						String messageIn = IOUtilities.read(in);
						if(!messageIn.equals("ping")){
							System.out.printf("Message from %d: %s\n", clientID, messageIn);
							gameOut.write(clientID + " " + messageIn + "\n");
							gameOut.flush();
						}
					}
					if(gameIn.ready()){
						String newMessage = IOUtilities.read(gameIn);
						if(!newMessage.equals("ping")){
							
							System.out.println(newMessage);
							
							while(!message.equals("")){
								System.out.printf("Waiting for \"%s\" to be removed\n", message);
								Thread.sleep(100);
							}
							message = newMessage;
							System.out.printf("Message from game server: \"%s\"\n", message);
							int ID = Integer.parseInt(message.split(" ")[0]);
							if(ID == clientID){
								out.write(message.substring(message.indexOf(" ")+1) + "\n");
								out.flush();
								message = "";
							}
						}
					}
					if(!message.equals("")){
						int ID = Integer.parseInt(message.split(" ")[0]);
						if(ID == clientID){
							out.write(message.substring(message.indexOf(" ")+1) + "\n");
							out.flush();
							message = "";
						}
					}

				} catch(SocketException e){
					Socket socket = servers.remove(gameIndex);
					servers.put(-gameIndex, socket);
					while(servers.get(gameIndex) == null);
					gameServerSocket = servers.get(gameIndex);
				} catch(Exception e){e.printStackTrace(); isDone = true;}

				Thread.sleep(100);
			}

		} catch (Exception e) {e.printStackTrace();}

	}

}
