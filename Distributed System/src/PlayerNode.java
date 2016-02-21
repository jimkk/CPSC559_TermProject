import java.util.LinkedList;

public class PlayerNode {
	
	static int numberOfPlayers = 0;
	
	int playerNumber;
	boolean turn = false;
	boolean folded = false;
	Card hand;
	PlayerNode nextPlayer;
	
	PlayerNode (int playerNumber, boolean turn, boolean folded, Card hand){
		
		this.playerNumber = playerNumber;
		this.turn = turn;
		this.folded = folded;
		this.hand = hand;
		numberOfPlayers++;
		
	}
}
