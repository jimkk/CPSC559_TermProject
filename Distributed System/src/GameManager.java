import java.io.*;
import java.util.*;


public class GameManager {
	
	private int pot = 0;
	private int currentBetCall = 0;
	private int playerCount = 0;
	private int playerID = 0;
	private int turn = -1;
	
	private Card[] communityCards = new Card[5];
	private LinkedPlayerList playerList = new LinkedPlayerList();

	// When all players have joined, the game manager will deal the cards and assign the first
	// player in the player linked list, the turn. Big and small blinds will also be assigned at
	// this point
	
	
	// Need to have a state-machine to manage the turns required of each player
	
	
	// traverse the list of players by playerNumber and prompt each player for their turn

	// Deal cards
	// Assign turn
	// Flip a card
	// Add bet to pot
	// force display the cards
	// remove pot from table and award to winning player
	
	public int addPlayerToGame(int playerPort, int stack) {
		if(playerCount == 6) {
			return -1;
		}
		playerCount++;
		playerID++;
		this.getPlayerList().addPlayers(playerCount, playerID, playerPort, stack);
		
		return 0;
	}
	
	public int removePlayerFromGame(int playerID)
	{
		if (playerCount == 0){
			return -1;
		}
		playerCount--;
		getPlayerList().deletePlayer(playerID);
		
		return 0;
	}
	
	public void bet(int playerID, int amount){
		// check if the player has enough to bet what they asked to
		// if so, then add that money to pot and subtract it from the
		// player's stack

		PlayerNode player = getPlayerList().findPlayerByID(playerID);

		if(amount < player.stack){
			// If the bet is greater than the currentBetCall, then set the
			// bet to the new currentBetCall
			if((betPerTurn + amount) > currentBetCall){		//Raise
				currentBetCall = (betPerTurn + amount);
				player.bet(amount);
			}	
			// Check if bet is equal to the currentBetCall, if so continue on
			else if((betPerTurn + amount) == currentBetCall){ 	//Call
				player.bet(amount);
			} else if (amount == player.stack) { //All in
				player.bet(amount);
			} else {
				;//TODO Illegal bet
			}
		// Keep track of how much each player bets, for each round of betting
		
		// Will have to record each player's bet in their 'playerNode', setting the
		// amount accordingly
		
	}
	
	
	public LinkedPlayerList getPlayerList(){
		return playerList;
	}
	
	
}
