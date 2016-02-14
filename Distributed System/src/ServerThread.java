import java.net.*;
import java.lang.*;
import java.io.*;

public class ServerThread implements Runnable{

	private Socket socket;
	private boolean isDone = false;

	public ServerThread(Socket socket){
		this.socket = socket;
	}

	public void run(){
		System.out.printf("New Client Connected, IP=%s, Port=%d\n", socket.getInetAddress(), socket.getPort());

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
