import java.util.LinkedList;
import java.io.*;


// Referenced the following videos on linked lists:
// https://www.youtube.com/watch?v=pBaZl9B448g
// https://www.youtube.com/watch?v=SMuL7ld3r5M
public class LinkedPlayerList {

	static PlayerNode currentPlayer;
	static PlayerNode temp;
	static PlayerNode rootPlayer;
	
	public void addPlayers (int playerNumber, int playerID, boolean turn, boolean folded, Card[] hand){
		PlayerNode player = new PlayerNode(playerNumber, playerID, turn, folded, hand);
		
		if(rootPlayer == null){
			
			rootPlayer = player;
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
	}
	
	public void insertPlayer (int     playerNumber,
							  int     playerID,
							  boolean turn,  
							  boolean folded,
							  Card[]    hand,
							  int     after){
		
		PlayerNode player = new PlayerNode(playerNumber, playerID, turn, folded, hand);
		
		int ithPlayer = 1;
		
		currentPlayer = rootPlayer;
		
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
		
		currentPlayer = rootPlayer;
		
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
		PlayerNode.numberOfPlayers--;
	}
	
	public void displayGameState() {
	// Print the game state on the Server side
		currentPlayer = rootPlayer;
		boolean arrow = false;
		
		do{
			System.out.print((arrow) ? " --> |" + currentPlayer.playerNumber + "|" : "|" + currentPlayer.playerNumber + "|");
			arrow = true;
			
			currentPlayer = currentPlayer.nextPlayer;
			
		}while(currentPlayer != rootPlayer);
		System.out.println();
		
	}
	
	public static void main(String[] args){
		
		LinkedPlayerList playerList = new LinkedPlayerList();
		
		
	}
	
}
