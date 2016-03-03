import java.util.LinkedList;
import java.io.*;

public class PlayerNode implements Serializable{
	
	static int numberOfPlayers = 0;
	
	int playerNumber;
	int playerID;
	int stack;
	int currentBetAmount;
	int betPerTurn;
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
		this.betPerTurn = 0;
		this.turn = false;
		this.folded = false;
		this.port = port;
		this.hand = new Card[2];
		numberOfPlayers++;
		
	}

	public void setHand(Card c1, Card c2){
		hand[0] = c1;
		hand[1] = c2;
	}

	public Card[] getHand(){
		return hand;
	}

	public void bet(int amount){
		currentBetAmount += amount;
		betPerTurn += amount;
		stack -= amount;
	}
}
