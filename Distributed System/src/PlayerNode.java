import java.util.LinkedList;

public class PlayerNode {
	
	static int numberOfPlayers = 0;
	
	int playerNumber;
	int playerID; 
	boolean turn = false;
	boolean folded = false;
	int port;
	Card[] hand;
	PlayerNode nextPlayer;
	
	PlayerNode (int playerNumber, int playerID, boolean turn, boolean folded, int port, Card[] hand){
		
		this.playerNumber = playerNumber;
		this.playerID = playerID;
		this.turn = turn;
		this.folded = folded;
		this.port = port;
		this.hand = hand;
		numberOfPlayers++;
		
	}
}
