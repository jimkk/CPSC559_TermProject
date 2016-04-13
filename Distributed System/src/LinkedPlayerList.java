import java.util.LinkedList;
import java.io.*;


/**
 * This is the Linked List class that contains the various linked list operations we use in our game
 *
 */
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

	/**
	 * Adds a new player to the linked list of players
	 * @param playerNumber
	 * @param playerID
	 * @param stack
	 */
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

			rootPlayer = player;

			rootPlayer.nextPlayer = rootPlayer;

		}
		else{

			currentPlayer = rootPlayer;
			
			while (currentPlayer.nextPlayer != rootPlayer){

				currentPlayer = currentPlayer.nextPlayer;

			}

			currentPlayer.nextPlayer = player;
			
			player.nextPlayer = rootPlayer;



		}
		writeLock = false;
	}
	/**
	 * Inserts a new player into the linked list of players; currently not used
	 * @param playerNumber
	 * @param playerID
	 * @param stack
	 * @param after
	 */
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

	/**
	 * Deletes a player from the linked list of players
	 * @param playerToBeDeleted
	 */
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
		count--;
	}
	
	/**
	 * Finds a particular player in the linked list by using that player's ID
	 * @param playerID
	 * @return currentPlayer
	 */
	public PlayerNode findPlayerByID(int playerID){
		if(rootPlayer == null){
			return null;
		}
		PlayerNode currentPlayer = rootPlayer;
		do{
			if (currentPlayer.playerID == playerID){
				break;
			}
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != rootPlayer);
		
		if(currentPlayer.playerID != playerID){
			return null;
		}
		return currentPlayer;
	}

	/**
	 * Finds a particular player by the player's index in the linked list
	 * (Player index differs from PlayerID)
	 * @param index
	 * @return currentPlayer
	 */
	public PlayerNode findPlayerByIndex(int index){
		int ithplayer = 0;

		PlayerNode currentPlayer = rootPlayer;

		while(index != ithplayer){
			currentPlayer = currentPlayer.nextPlayer;
			ithplayer++;
		}

		return currentPlayer;
	}

	/**
	 * Method to display the current Game State. We have used this for debugging purposes
	 * and it is not actually called by a client in the game...
	 */
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
			System.out.println((currentPlayer.smallBlind) ? "Small Blind" + " |" 
					: (currentPlayer.bigBlind) ? "Big Blind" + "   |" : "None" + "\t   |");
			System.out.println("Stack |" + currentPlayer.stack + "\t   |");
			System.out.println("Folded|" + currentPlayer.folded + "\t   |");

			arrow = true;

			currentPlayer = currentPlayer.nextPlayer;

		}while(currentPlayer != rootPlayer);
		System.out.println("---------------------------");
		arrow = false;

	}

	public int getCount(){
		return count;
	}

	public static void main(String[] args){

		LinkedPlayerList playerList = new LinkedPlayerList();


	}

}
