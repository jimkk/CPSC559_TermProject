import java.util.LinkedList;

public class PlayerNode {
	
	static int numberOfPlayers = 0;
	
	int playerNumber;
	int playerID;
	int stack;
	int currentBetAmount;
	boolean turn = false;
	boolean folded = false;
	int port;
	Card[] hand;
	PlayerNode nextPlayer;
	
	PlayerNode (int playerNumber, int playerID, int port, int stack){
		
		this.stack = stack;
		this.playerNumber = playerNumber;
		this.playerID = playerID;
		this.currentBetAmount = 0;
		this.turn = false;
		this.folded = false;
		this.port = port;
		this.hand = null;
		numberOfPlayers++;
		
	}
}
