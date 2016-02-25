import java.util.LinkedList;
import java.io.*;


// Referenced the following videos on linked lists:
// https://www.youtube.com/watch?v=pBaZl9B448g
// https://www.youtube.com/watch?v=SMuL7ld3r5M
public class LinkedPlayerList {

	static PlayerNode currentPlayer;
	static PlayerNode temp;
	static PlayerNode rootPlayer;
	
	public void addPlayers (int playerNumber, int playerID, boolean turn, boolean folded, int port, Card[] hand){
		PlayerNode player = new PlayerNode(playerNumber, playerID, turn, folded, port, hand);
		
		if(rootPlayer == null){
			
			rootPlayer = player;
			rootPlayer.turn = true;
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
							  int	  port,
							  Card[]    hand,
							  int     after){
		
		PlayerNode player = new PlayerNode(playerNumber, playerID, turn, folded, port, hand);
		
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
	
	public int findPlayerbyPort(int playerPort){
		currentPlayer = rootPlayer;
		do{
			if (currentPlayer.port == playerPort){
				break;
			}
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != rootPlayer);
		
		return currentPlayer.playerNumber;
	}
	
	public void displayGameState() {
	// Print the game state on the Server side
		currentPlayer = rootPlayer;
		boolean arrow = false;
		
		do{
			//System.out.print((arrow) ? " --> |" + currentPlayer.playerNumber + "\t|" : "|" + currentPlayer.playerNumber + "\t|");
			System.out.print((arrow) ? "\t|\n" : "");
			System.out.print((arrow) ? "\tV\n" : "");
			System.out.println("ID   |" + currentPlayer.playerID + "\t\t|");
			System.out.println("#    |" + currentPlayer.playerNumber + "\t\t|");
			System.out.println("Turn |" + currentPlayer.turn + "\t|");
			System.out.println("Port |" + currentPlayer.port + "\t|");
			
			arrow = true;
			
			currentPlayer = currentPlayer.nextPlayer;
			
		}while(currentPlayer != rootPlayer);
		System.out.println();
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
	
	public static void main(String[] args){
		
		LinkedPlayerList playerList = new LinkedPlayerList();
		
		
	}
	
}
