/**
 *	Class that sends updates to the backup Server in case of the main Server going down.
 *
 */

public class ServerSync implements Runnable{

	private Server server;
	private boolean isDone = false;

	public ServerSync(Server server){
		this.server = server;
	}

	public void run(){
		while(!isDone){
			if(Server.changesMade){
				//TODO Send update
				Server.changesMade = false;
			}
			try{
				Thread.sleep(100);
			} catch (Exception et) {et.printStackTrace();}
		}

	}

}
