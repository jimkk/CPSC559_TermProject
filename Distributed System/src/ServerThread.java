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

	public ServerThread(Socket socket, ArrayList<Socket> servers){
		this.clientSocket = socket;
		gameServerChosen = false;
		this.servers = servers;
	}

	public void run(){

		try{

		bufIn = new BufferedInputStream(clientSocket.getInputStream());
		in = new InputStreamReader(bufIn);
		bufOut = new BufferedOutputStream(clientSocket.getOutputStream());
		out = new OutputStreamWriter(bufOut);


		out.write("message Welcome to the Poker server!");
		out.flush();
		while(!gameServerChosen){
			if(servers.size() == 0){
				Thread.sleep(1000);
				continue;
			}
			//TODO Ask which game server to join
			//For now, it's the first one
			gameServerSocket = servers.get(0);
		}

		bufGameIn = new BufferedInputStream(gameServerSocket.getInputStream());
		gameIn = new InputStreamReader(bufGameIn);
		bufGameOut = new BufferedOutputStream(gameServerSocket.getOutputStream());
		gameOut = new OutputStreamWriter(bufGameOut);

		while(!isDone){
			try{
				if(in.ready()){
					gameOut.write(read(in).toString());
				}
				if(gameIn.ready()){
					out.write(read(gameIn).toString());
				}
			} catch(Exception e){e.printStackTrace(); isDone = true;}
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
