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
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);
			//Scanner userIn = new Scanner(System.in);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			StringBuffer buffer = new StringBuffer();

			System.out.print("Enter Command: ");

			while(!isDone){
				if(in.ready()){
					buffer = read(in);
					String messageType;
					if(buffer.indexOf(" ") != -1){
						messageType = buffer.substring(0, buffer.indexOf(" "));
					} else {
						messageType = buffer.toString();
					}

					switch(messageType){
						case("message"):
							String message = buffer.substring(buffer.indexOf(" "));
							System.out.printf("\rServer says: %s\n", message);
							System.out.print("Enter Command: ");
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
							buffer = read(in);
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

	public static void main(String [] args){
		if(args.length == 2){

			Client client = new Client();

			client.address = args[0];
			client.port = Integer.parseInt(args[1]);

			client.run();
		}
	}

}




