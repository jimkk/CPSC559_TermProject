import java.util.LinkedList;
import java.io.*;
/**
 * This is the PlayerNode class which represents each player in the linked list
 * It contains various setters and getters for setting and retrieving various data about each player
 *
 */
public class PlayerNode {
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

	/**
	 * Gets the player's ID
	 * @return playerID
	 */
	public int getPlayerID(){
		return playerID;
	}

	/**
	 * Sets the player hand to a particular pair of cards
	 * @param c1
	 * @param c2
	 */
	public void setHand(Card c1, Card c2){
		hand[0] = c1;
		hand[1] = c2;
	}

	/**
	 * Sets the Big Blind flag for the player
	 * @param b
	 */
	public void setBigBlind(boolean b){
		bigBlind = b;
	}

	/**
	 * Sets the Small Blind flag for the player
	 * @param b
	 */
	public void setSmallBlind(boolean b){
		smallBlind = b;
	}
	
	/**
	 * Gets the Big Blind flag of the player
	 * @return bigBlind
	 */
	public boolean getBigBlind(){
		return bigBlind;
	}
	
	/**
	 * Gets the Small Blind flag of the player
	 * @return smallBlind
	 */
	public boolean getSmallBlind(){
		return smallBlind;
	}

	/**
	 * Gets a player's hand
	 * @return hand
	 */
	public Card[] getHand(){
		return hand;
	}

	/**
	 * Gets the player's stack amount
	 * @return
	 */
	public int getStack(){
		return stack;
	}

	/**
	 * Setst the beginTurn flag for a player
	 * @param val
	 */
	public void setBeginTurn(boolean val){
		beginTurn = val;
	}
	
	/**
	 * Gets the beginTurn flag for a player
	 * @return beginTurn
	 */
	public boolean getBeginTurn(){
		return beginTurn;
	}
	
	/**
	 * Sets the doneTurn flag for a player
	 * @param val
	 */
	public void setDoneTurn(boolean val){
		doneTurn = val;
	}
	
	/**
	 * Gets the doneTurn flag for a player
	 * @return doneTurn
	 */
	public boolean getDoneTurn(){
		return doneTurn;
	}
	
	/**
	 * Sets the turn of a player
	 * @param val
	 */
	public void setTurn(boolean val){
		turn = val;
	}

	/**
	 * Gets the turn flag of a player
	 * @return
	 */
	public boolean getTurn(){
		return turn;
	}

	/**
	 * Performs the bet for a player. Sets the appropriate amounts for that player
	 * @param amount
	 */
	public void bet(int amount){
		currentBetAmount += amount;
		betPerTurn += amount;
		stack -= amount;
	}
	
	/**
	 * Sets the folded flag for a player
	 * @param b
	 */
	public void setFolded(boolean b){
		folded = b;
	}
	
	/**
	 * Gets the folded flag for a player
	 * @return folded
	 */
	public boolean getFolded(){
		return folded;
	}
	
	/**
	 * Gets the currentBetAmount of a player
	 * @return currentBetAmount
	 */
	public int getCurrentBetAmount(){
		return currentBetAmount;
	}
	
	/**
	 * Sets the vote for a player
	 * @param i
	 */
	public void setVote(int i){
		vote = i;
	}
	
	/**
	 * Gets the vote value for a player
	 * @return vote
	 */
	public int getVote(){
		return vote;
	}
	
	/**
	 * Sets the stack value for a player
	 * @param i
	 */
	public void setStack(int i){
		stack = i;
	}
	
	
}
