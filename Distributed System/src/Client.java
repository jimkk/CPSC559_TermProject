import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This class is the main process for a client in the game. It connects to the
 * server and will get input from the user and manage communications to and
 * from the game server.
 */
public class Client{

	private String address;
	private int port;
	private Socket socket;
	private boolean isDone = false;
	private boolean isTurn = false;
	//Random rand = new Random();
	//int randomCardNumber;

	private long timeSincePing = new Date().getTime();

	/**
	 * The main function for the client that loops while reading input from the user if there is any and receives and processes messages from the server.
	 */
	public void run(){
		try{
			socket = new Socket(address, port);
			System.out.printf("Connected to %s on port %d\n", socket.getInetAddress(), socket.getPort());
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);
			//Scanner userIn = new Scanner(System.in);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String buffer = "";

			System.out.print("Enter Command: ");

			while(!isDone){
				try{
					if(in.ready()){
						buffer = IOUtilities.read(in);
						String messageType;
						if(buffer.indexOf(" ") != -1){
							messageType = buffer.substring(0, buffer.indexOf(" "));
						} else {
							messageType = buffer.toString();
						}

						switch(messageType){
							case("message"):
								message(buffer);
								break;
							case("full"):
								System.out.println("Server is full. Exiting.");
								isDone = true;
								break;
							default:
								System.out.printf("UNKNOWN MESSAGE: %s\n", buffer);
								break;
						}
					}


					if(br.ready()){
						String input = br.readLine();

						switch(input){
							case("help"):
								System.out.println("\nPossible commands:\ncheckTurn \ncheckStack \ncommunityCards \nseeHand \nbet \nfold \ncheck message \nget message \ndisplayGame \nclose \n");
								break;
							case("checkTurn"):
								System.out.println("Checking turn...");
								out.write("checkTurn " + '\n');
								break;
							case("checkStack"):
								System.out.println("Checking stack...");
								out.write("checkStack" + '\n');
								break;
							case("communityCards"):
								System.out.println("Looking at community cards...");
								out.write("communityCards " + '\n');
								break;
							case("seeHand"):
								System.out.println("Looking at hand...");
								out.write("seeHand " + '\n');
								break;
							case("bet"):
								System.out.print("Enter bet: ");
								String betAmount = br.readLine();
								out.write("bet " + betAmount + '\n');
								break;
							case("fold"):
								System.out.println("You have chosen to fold...");
								out.write("fold " + '\n');
								break;
							case("check"):
								System.out.println("You have chosen to check; i.e. bet 0");
								out.write("check " + '\n');
								break;
							case("message"):
								System.out.print("Enter message: ");
								String message = br.readLine();
								out.write("message " + message + '\n');
								break;
							case("close"):
								System.out.println("Socket closed");
								isDone = true;
								out.write("close\n");
								break;
							case("displayGame"):
								out.write("displayGame\n");
								out.flush();
								break;
							default:
								System.out.println("ERROR: Unknown Message Type");
								break;
						}
						out.flush();

						if(!isDone){
							System.out.print("Enter Command: ");
						}
					}
					if(new Date().getTime() - timeSincePing > 3000){
						out.write("ping\n");
						out.flush();
						timeSincePing = new Date().getTime();
					}
					Thread.sleep(100);
				} catch (Exception e){
					System.out.println("\nLost connection to server. Attempting to reconnect.");
					try{
						socket.close();
						ServerSocket reconnectSocket = new ServerSocket(socket.getLocalPort());
						socket = reconnectSocket.accept();
						reconnectSocket.close();
						bufIn = new BufferedInputStream(socket.getInputStream());
						in = new InputStreamReader(bufIn);
						bufOut = new BufferedOutputStream(socket.getOutputStream());
						out = new OutputStreamWriter(bufOut);
						br = new BufferedReader(new InputStreamReader(System.in));
					} catch (Exception reconnecte) {
						reconnecte.printStackTrace();
						isDone = true;			
					}
				}
			}
		} catch (Exception e){
			System.out.printf("Failed to connect to server at %s on port %d\n", socket.getInetAddress(), socket.getPort());
			e.printStackTrace();
		}
	}

	/**
	 * Reads in a message from the stream, stopping on a -1 or a newline character
	 * @param in The stream to read from
	 * @return StringBuffer The received message
	 */

	private void message(String buffer){
		String message = buffer.substring(buffer.indexOf(" "));
		System.out.printf("\rServer says: %s\n", message);
		System.out.print("\nEnter Command (try 'help'): ");
	}

	public static void main(String [] args){
		if(args.length == 2){

			Client client = new Client();

			client.address = args[0];
			client.port = Integer.parseInt(args[1]);

			client.run();
		}
	}

}




