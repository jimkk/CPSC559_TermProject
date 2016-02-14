import java.net.*;
import java.io.*;

public class Server {

	private boolean isDone = false;
	private int port = 7777;
	private ServerSocket serverSocket;

	private void run(){
		
		try{
			serverSocket = new ServerSocket(port);
		} catch (Exception e){
			System.out.println("Failed to initialize server socket");
			e.printStackTrace();
			System.exit(-1);
		}
		
		while(!isDone){
			try{
				Socket clientSocket = serverSocket.accept();

				new Thread(new ServerThread(clientSocket)).start();
			} catch (Exception e){
				System.out.println("Error accepting client\n");
				e.printStackTrace();
			}
		}
	};

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

}
