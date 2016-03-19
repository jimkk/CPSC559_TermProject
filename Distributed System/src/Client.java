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
						case("checkTurn"):
							System.out.println("Checking turn...");
							out.write("checkTurn " + '\n');
							break;
						case("bet"):
							System.out.print("Enter bet: ");
							String betAmount = br.readLine();
							out.write("bet " + betAmount + '\n');
							break;
						case("deal"):
							/* THis was the client side deal code
							   randomCardNumber = rand.nextInt(52) + 1;
							   System.out.println("Dealt card number: " +randomCardNumber);
							   String deal = String.valueOf(randomCardNumber);
							   out.write("deal " + deal + '\n');
							   */
							System.out.println("Dealing card.");
							out.write("deal " + '\n');
							break;
						case("message"):
							System.out.print("Enter message: ");
							String message = br.readLine();
							out.write("message " + message + '\n');
							break;
						case("set message"):
							out.write("set_message_request\n");
							out.flush();
							while(!in.ready()){;}
							buffer = IOUtilities.read(in);
							String messageType;
							if(buffer.indexOf(" ") != -1){
								messageType = buffer.substring(0, buffer.indexOf(" "));
							} else {
								messageType = buffer.toString();
							}
							if(messageType.equals("set_message_request_granted")){
								int key = Integer.parseInt(buffer.toString().split(" ")[1]);
								System.out.print("Enter message: ");
								String smessage = br.readLine();
								out.write("set_message " + Integer.toString(key) + " " + smessage + "\n");
								out.flush();
							} else {
								System.out.println("Request Denied\n");
							} 
							break;
						case("get message"):
							out.write("get_message\n");
							out.flush();
							break;
						case("close"):
							System.out.println("Socket closed");
							isDone = true;
							out.write("close\n");
							break;
						case("destroy"):
							//TODO
							break;
						case("display game"):
							out.write("display game\n");
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

				Thread.sleep(100);
			}
		} catch (Exception e){
			System.out.printf("Failed to connect to server at %s on port %d\n", socket.getInetAddress(), socket.getPort());
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Reads in a message from the stream, stopping on a -1 or a newline character
	 * @param in The stream to read from
	 * @return StringBuffer The received message
	 */

	private void message(String buffer){
		String message = buffer.substring(buffer.indexOf(" "));
		System.out.printf("\rServer says: %s\n", message);
		System.out.print("Enter Command: ");
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




