import java.util.LinkedList;
import java.io.*;


// Referenced the following videos on linked lists:
// https://www.youtube.com/watch?v=pBaZl9B448g
// https://www.youtube.com/watch?v=SMuL7ld3r5M
public class LinkedPlayerList implements Serializable{

	static PlayerNode currentPlayer;
	static PlayerNode temp;
	static PlayerNode rootPlayer;
	
	public void addPlayers (int playerNumber, int playerID, int stack, int port, String ipAddress){
		PlayerNode player = new PlayerNode(playerNumber, playerID, stack, port, ipAddress);
		
		if(rootPlayer == null){
			
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
	}
	
	public void insertPlayer (int     playerNumber,
							  int     playerID,
							  int	  stack,
							  int	  port,
							  String  ipAddress,
							  int     after){
		
		PlayerNode player = new PlayerNode(playerNumber, playerID, stack, port, ipAddress);
		
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
	
	public int findPlayerByPort(int playerPort, String returnType){
		currentPlayer = rootPlayer;
		do{
			if (currentPlayer.port == playerPort){
				break;
			}
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != rootPlayer);
		
		if (returnType == "Player Number") 
			return currentPlayer.playerNumber;
		if (returnType == "Player ID")
			return currentPlayer.playerID;
		if (returnType == "Bet Amount")
			return currentPlayer.currentBetAmount;
		else return -1;
	}
	
	
	public PlayerNode findPlayerByPort(int playerPort){
		currentPlayer = rootPlayer;
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
	
	
	public PlayerNode findPlayerByID(int playerID){
		currentPlayer = rootPlayer;
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

		currentPlayer = rootPlayer;

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
		currentPlayer = rootPlayer;
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
			System.out.println("IP    |" + currentPlayer.ipAddress + "   |");
			System.out.println("Port  |" + currentPlayer.port + "\t   |");
			
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
	
	public static void main(String[] args){
		
		LinkedPlayerList playerList = new LinkedPlayerList();
		
		
	}
	
}
