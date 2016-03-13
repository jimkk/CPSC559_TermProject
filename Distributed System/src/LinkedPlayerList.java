import java.util.LinkedList;
import java.io.*;


// Referenced the following videos on linked lists:
// https://www.youtube.com/watch?v=pBaZl9B448g
// https://www.youtube.com/watch?v=SMuL7ld3r5M
public class LinkedPlayerList {

	//NOTE: I made these non-static, if it breaks something then change it back
	//PlayerNode currentPlayer;
	volatile PlayerNode temp;
	volatile PlayerNode rootPlayer;
	private volatile int count = 0;

	private volatile boolean writeLock = false;
	private volatile boolean readLock = false;

	public void addPlayers (int playerNumber, int playerID, int stack){
		PlayerNode player = new PlayerNode(playerNumber, playerID, stack);
		PlayerNode currentPlayer;
		count++;

		while(writeLock == true){
			System.out.println("waiting...");
			try{
				Thread.sleep(10);
			} catch (Exception e) {e.printStackTrace();}
		}

		writeLock = true;

		if(rootPlayer == null){
			System.out.println("Root set");

			rootPlayer = player;
			// ** could have the line directly below if we wanted to set player 1's turn to true
			// otherwise just do that when all player slots have been filled
			//rootPlayer.turn = true;
			//player.nextPlayer = null;
			rootPlayer.nextPlayer = rootPlayer;

		}
		else{

			currentPlayer = rootPlayer;
			//while(currentPlayer.nextPlayer != null){
			//	currentPlayer = currentPlayer.nextPlayer;

			//}
			while (currentPlayer.nextPlayer != rootPlayer){

				currentPlayer = currentPlayer.nextPlayer;

			}

			currentPlayer.nextPlayer = player;
			//player.nextPlayer = null;
			player.nextPlayer = rootPlayer;



		}
		writeLock = false;
	}

	public void insertPlayer (int     playerNumber,
			int     playerID,
			int	  stack,
			int     after){

		PlayerNode player = new PlayerNode(playerNumber, playerID, stack);
		count++;

		int ithPlayer = 1;

		PlayerNode currentPlayer = rootPlayer;

		while (after != ithPlayer){
			currentPlayer = currentPlayer.nextPlayer;
			ithPlayer++;

		}

		temp = currentPlayer.nextPlayer;
		currentPlayer.nextPlayer = player;
		player.nextPlayer = temp;

	}

	public void deletePlayer (int playerToBeDeleted) {

		int ithPlayer = 1;

		PlayerNode currentPlayer = rootPlayer;

		if(playerToBeDeleted == 1){

			temp = rootPlayer.nextPlayer;

			while(temp.nextPlayer != rootPlayer){
				temp = temp.nextPlayer;
			}

			temp.nextPlayer = temp.nextPlayer.nextPlayer;
			rootPlayer = currentPlayer.nextPlayer;
		}
		else{
			while(ithPlayer != playerToBeDeleted - 1){
				currentPlayer = currentPlayer.nextPlayer;
				ithPlayer++;
			}

			currentPlayer.nextPlayer = currentPlayer.nextPlayer;
		}

		// Decrease number of players
		//PlayerNode.numberOfPlayers--;
		count--;
	}
	/*
	public int findPlayerByPort(int playerPort, String returnType){	
		PlayerNode currentPlayer = rootPlayer;
		do{
			if (currentPlayer.port == playerPort){
				break;
			}
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != rootPlayer);

		int ret;

		if (returnType == "Player Number") 
			ret = currentPlayer.playerNumber;
		if (returnType == "Player ID")
			ret = currentPlayer.playerID;
		if (returnType == "Bet Amount")
			ret = currentPlayer.currentBetAmount;
		else ret = -1;

		return ret;
	}


	public PlayerNode findPlayerByPort(int playerPort){
		PlayerNode currentPlayer = rootPlayer;
		do{
			if (currentPlayer.port == playerPort){
				break;
			}
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != rootPlayer);

		if(currentPlayer.port != playerPort){
			return null;
		}
		return currentPlayer;	
	}
	*/

	public PlayerNode findPlayerByID(int playerID){
		PlayerNode currentPlayer = rootPlayer;
		do{
			if (currentPlayer.playerID == playerID){
				break;
			}
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != rootPlayer);

		return currentPlayer;
	}

	public PlayerNode findPlayerByIndex(int index){
		int ithplayer = 0;

		PlayerNode currentPlayer = rootPlayer;

		while(index != ithplayer){
			currentPlayer = currentPlayer.nextPlayer;
			ithplayer++;
		}

		return currentPlayer;
	}

	//Is this method really needed?	
	public void setPlayerBetAmount (int playerID, int betAmount){
		PlayerNode player;

		player = findPlayerByID(playerID);
		player.currentBetAmount = betAmount;		
	}

	public void displayGameState() {
		// Print the game state on the Server side
		PlayerNode currentPlayer = rootPlayer;
		boolean arrow = false;

		System.out.println("---------------------------");
		do{
			//System.out.print((arrow) ? " --> |" + currentPlayer.playerNumber + "\t|" : "|" + currentPlayer.playerNumber + "\t|");
			System.out.print((arrow) ? "\t|\n" : "");
			System.out.print((arrow) ? "\tV\n" : "");
			System.out.println("ID    |" + currentPlayer.playerID + "\t   |");
			System.out.println("#     |" + currentPlayer.playerNumber + "\t   |");
			System.out.println("Turn  |" + currentPlayer.turn + "\t   |");
			System.out.print("Blind |");
			System.out.println((currentPlayer.smallBlind) ? "Small Blind" + "\t	|" 
					: (currentPlayer.bigBlind) ? "Big Blind" + "\t	|" : "None");
			System.out.println("Stack |" + currentPlayer.stack + "\t   |");
			System.out.println("Folded|" + currentPlayer.folded + "\t   |");

			arrow = true;

			currentPlayer = currentPlayer.nextPlayer;

		}while(currentPlayer != rootPlayer);
		System.out.println("---------------------------");
		arrow = false;

		/*
		//Print ID
		do{
		System.out.print((arrow) ? " --> |" + currentPlayer.playerID + "\t|" : "|" + currentPlayer.playerID + "\t|");
		arrow = true;

		currentPlayer = currentPlayer.nextPlayer;

		}while(currentPlayer != rootPlayer);
		System.out.println();
		arrow = false;
		*/
		/*
		//print port
		do{
		System.out.print((arrow) ? " --> |" + currentPlayer.port + "\t|" : "|" + currentPlayer.port + "\t|");
		arrow = true;

		currentPlayer = currentPlayer.nextPlayer;

		}while(currentPlayer != rootPlayer);
		System.out.println();*/

	}

	public int getCount(){
		return count;
	}


	public static void main(String[] args){

		LinkedPlayerList playerList = new LinkedPlayerList();


	}

}
