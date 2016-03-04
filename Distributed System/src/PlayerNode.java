import java.util.LinkedList;
import java.io.*;

public class PlayerNode implements Serializable{
	
	static int numberOfPlayers = 0;
	
	int playerNumber;
	int playerID;
	int stack;
	int currentBetAmount;
	int betPerTurn;
	int port;
	boolean turn = false;
	boolean folded = false;
	boolean bigBlind = false;
	boolean littleBlind = false;
	boolean allIn = false;
	String ipAddress;
	Card[] hand;
	PlayerNode nextPlayer;
	
	PlayerNode (int playerNumber, int playerID, int stack, int port, String ipAddress){
		
		this.playerNumber = playerNumber;
		this.playerID = playerID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.stack = stack;
		this.currentBetAmount = 0;
		this.betPerTurn = 0;
		this.turn = false;
		this.folded = false;
		this.bigBlind = false;
		this.littleBlind = false;
		this.allIn = false;
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

	public int getStack(){
		return stack;
	}

	public void bet(int amount){
		currentBetAmount += amount;
		betPerTurn += amount;
		stack -= amount;
	}
}
