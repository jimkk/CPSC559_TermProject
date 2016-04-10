import java.util.LinkedList;
import java.io.*;

public class PlayerNode {

	//int numberOfPlayers = 0;

	int playerNumber;
	int playerID;
	int stack;
	int currentBetAmount;
	int betPerTurn;
	int vote;
	boolean turn = false;
	boolean folded = false;
	boolean bigBlind = false;
	boolean smallBlind = false;
	boolean allIn = false;
	boolean doneTurn = false;
	boolean beginTurn = false;
	Card[] hand;
	PlayerNode nextPlayer;

	PlayerNode (int playerNumber, int playerID, int stack){

		this.playerNumber = playerNumber;
		this.playerID = playerID;
		this.stack = stack;
		this.currentBetAmount = 0;
		this.betPerTurn = 0;
		this.vote = -1;
		this.turn = false;
		this.folded = false;
		this.bigBlind = false;
		this.smallBlind = false;
		this.allIn = false;
		this.doneTurn = false;
		this.beginTurn = false;
		this.hand = new Card[2];
		//numberOfPlayers++;

	}

	public int getPlayerID(){
		return playerID;
	}

	public void setHand(Card c1, Card c2){
		hand[0] = c1;
		hand[1] = c2;
	}

	public void setBigBlind(boolean b){
		bigBlind = b;
	}

	public void setSmallBlind(boolean b){
		smallBlind = b;
	}

	public Card[] getHand(){
		return hand;
	}

	public int getStack(){
		return stack;
	}

	public void setBeginTurn(boolean val){
		beginTurn = val;
	}
	
	public boolean getBeginTurn(){
		return beginTurn;
	}
	
	public void setDoneTurn(boolean val){
		doneTurn = val;
	}
	
	public boolean getDoneTurn(){
		return doneTurn;
	}
	
	public void setTurn(boolean val){
		turn = val;
	}

	public boolean getTurn(){
		return turn;
	}

	public void bet(int amount){
		currentBetAmount += amount;
		betPerTurn += amount;
		stack -= amount;
	}
	
	public void setFolded(boolean b){
		folded = b;
	}
	
	public boolean getFolded(){
		return folded;
	}
	
	public int getCurrentBetAmount(){
		return currentBetAmount;
	}
	
	public void setVote(int i){
		vote = i;
	}
	
	public int getVote(){
		return vote;
	}
	
	public void setStack(int i){
		stack = i;
	}
	
}
