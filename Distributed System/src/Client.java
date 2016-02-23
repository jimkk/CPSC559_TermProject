import java.net.*;
import java.util.*;
import java.io.*;


public class Client{

	private String address;
	private int port;
	private Socket socket;
	private boolean isDone = false;
	private boolean isTurn = false;
	//Random rand = new Random();
	//int randomCardNumber;


	public void run(){
		try{
			socket = new Socket(address, port);
			System.out.printf("Connected to %s on port %d\n", socket.getInetAddress(), socket.getPort());
			BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);
			Scanner in = new Scanner(System.in);
			while(!isDone){
				System.out.print("Enter Command: ");
				String input = in.nextLine();

				switch(input){
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
						String message = in.nextLine();
						out.write("message " + message + '\n');
						break;
					case("close"):
						System.out.println("Socket closed");
						isDone = true;
						out.write("close\n");
						break;
					case("destroy"):
						//TODO
						break;
					default:
						System.out.println("ERROR: Unknown Message Type");
						break;
				}
				out.flush();

			}
		} catch (Exception e){
			System.out.printf("Failed to connect to server at %s on port %d\n", socket.getInetAddress(), socket.getPort());
			e.printStackTrace();
		}
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

			


