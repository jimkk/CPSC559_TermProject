import java.io.*;
import java.util.*;

/**
 * This class maintains the game state as well as containing the logic for most of the actions possible in a game.
 */

public class GameManager implements Runnable {
	
	private volatile int gameID;
	private volatile int pot = 0;
	private volatile int currentBetCall = 0;
	private volatile int playerCount = 0;
	private volatile int playerID = 0;
	private volatile int turn = -1;
	private volatile int bigBlindAmount = 100;
	private volatile int smallBlindAmount = 50;
	
	private volatile int currentPlayerIDTurn;
	private volatile int currentPlayerBetAmount;
	
	private volatile int counter = 0;
	private volatile boolean gameOn = false;
	private volatile boolean gameStart = true;
	private volatile boolean handDealt = false;
	private volatile boolean currentPlayerBeginTurn = false;
	private volatile boolean currentPlayerDoneTurn = false;
	private volatile boolean currentPlayerTurn = false;
	private volatile boolean currentPlayerBetFlag = false;
	private volatile boolean turnSent = false;
	
	
	private volatile Deck deck;
	private volatile Card[] communityCards = new Card[5];
	private volatile LinkedPlayerList playerList = new LinkedPlayerList();



	// When all players have joined, the game manager will deal the cards and assign the first
	// player in the player linked list, the turn. Big and small blinds will also be assigned at
	// this point


	/**
	 * Triggers the beginning of a round. The blinds are collected and the players are dealt their cards.
	 */
	public void beginRound(){
		/**
		 * Need to have a state-machine to manage the turns required of each player 
		 */
		//rotatePlayers();		 
		// Set the blinds and the turns
		setBlinds();

		// Deal the cards
		deal();
		handDealt = true;
		

		// Subtract the big and small blind values from their respective player's stacks
		// and add them to the pot
		subtractBlinds();

		// Traverse through player list, prompting each player for their turns
		for(int i = 0; i < playerCount; i++){
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
			
			setCurrentPlayerIDTurn(player.playerID);
			setCurrentPlayerTurn(true);
			setCurrentPlayerDoneTurn(false);
			setTurnSent(false);
			
			System.out.println("Current player's turn " + player.playerNumber);
			System.out.println("Turn: " + currentPlayerTurn);
			System.out.println("DoneTurn: " + currentPlayerDoneTurn);
			System.out.println("sentTurn: " + turnSent);
			
			System.out.println("Begin Turn State:");
			this.getPlayerList().displayGameState();
			while (getCurrentPlayerDoneTurn() == false && getCurrentPlayerTurn() == true ){
				
				//System.out.println("Counter: " + counter);
				// Now we need to notify the player that it is their turn
				// Setting the flag below, lets the serverthread know that it's now
				// that player's turn, and the serverthread will notify them accordingly
				//player.setBeginTurn(true);
				setCurrentPlayerBeginTurn(true);
				
				
				if (getCurrentPlayerBetFlag() == true && getCurrentPlayerBeginTurn() == true) {
					setCurrentPlayerBeginTurn(false);
					System.out.println("Player: " + getCurrentPlayerIDTurn() + " has chosed to bet: " + getCurrentPlayerBetAmount());
					bet(getCurrentPlayerIDTurn(), getCurrentPlayerBetAmount());
					setCurrentPlayerBetFlag(false);
					System.out.println("CurrentPlayerBetFlag set to: " + getCurrentPlayerBetFlag());
					System.out.println("CurrentPlayerBetFlag set to: " + currentPlayerBetFlag);
					setCurrentPlayerDoneTurn(true);
					System.out.println("CurrentPlayerDoneTurn set to: " + currentPlayerDoneTurn);
				}
				
				if(getCurrentPlayerDoneTurn() == true && player.getTurn() == true) {
					// Set the next player's turn to true
					setCurrentPlayerTurn(false);
					player.setTurn(false);
					//player.setDoneTurn(false);
					player.nextPlayer.setTurn(true);
				}
				
			}
			
			System.out.println("\nEnd Turn State:");
			this.getPlayerList().displayGameState();
			
		}

		//set various flags 

	}

	/**
	 * Sets the flags for which players just bet the large and small blinds.
	 */
	public void setBlinds(){
		/**
		 * Set Big and small blinds for each round
		 */
		
		for(int i = 0; i < playerCount; i++){
			// pass in i+1 so that the index matches up with the player number
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
			//System.out.println("index: " + i);
			//System.out.println("player.playerNumber: " + player.playerNumber);
			if(player.playerNumber == 1 && player.folded == false && gameStart == true){
				// initial state of the game
				player.bigBlind = true;
				player.turn = true;
				player.nextPlayer.smallBlind = true;
				gameStart = false;
			}
			else if (player.bigBlind == true){
				player.bigBlind = false;
				player.nextPlayer.bigBlind = true;
				player.nextPlayer.turn = true;
				player.nextPlayer.nextPlayer.smallBlind = true;
			}
		}
	} 
	
	/**
	 * Collects the blinds from the players that are responsible for them.
	 */
	public void subtractBlinds(){
		/**
		 * Method to subtract the blinds from the respective players' stacks	
		 */
		for(int i = 0; i < playerCount; i++){
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
			if(player.bigBlind == true){
				player.stack -= bigBlindAmount;
				// notify the player of the changes using the player's port to send the message
			}
			if(player.smallBlind == true){
				player.stack -= smallBlindAmount;
				// notify the player of the changes using the player's port to send the message
			}
			//else, we do nothing...
		}

	}

	// traverse the list of players by playerNumber and prompt each player for their turn

	/**
	 * Deals each players their hands.
	 */
	public void deal(){	
		deck = new Deck();
	
		for(int i = 0; i < playerCount; i++){
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
			player.setHand(deck.Draw(), deck.Draw());
			System.out.print(".");
		}
		
		gameOn = true;
		handDealt = true;
		turn = 0;
	}
	// Assign turn
	// Flip a card
	// Add bet to pot
	// force display the cards
	// remove pot from table and award to winning player

	/**
	 * Adds a player to the game.
	 * @param stack The stack starting value for the new player.
	 * @param playerID The ID of the new player 
	 * @return int - Returns 0 on the player being added to the game and -1 if the game is at capacity
	 */	
	public int addPlayerToGame(int stack, int playerID) {
		/**
		 * Add a player to the game
		 */
		if(playerCount == 6) {
			return -1;
		}
		playerCount++;
		getPlayerList().addPlayers(playerCount, playerID, stack);

		//this.getPlayerList().displayGameState();
		
		/*if(playerCount > 2){
			beginRound();
		}*/

		return 0;
	}

	/**
	 * Removes a player from the game.
	 * @param playerID The ID of the player to be removed.
	 * @return int Returns 0 on the player being successfully removed, -1 if there are no players in the game.
	 */
	public int removePlayerFromGame(int playerID)
	{
		if (playerCount == 0){
			return -1;
		}
		playerCount--;
		getPlayerList().deletePlayer(playerID);

		return 0;
	}

	//TODO Add javadoc comment because I don't understand this comment
	public void rotatePlayers(){
		PlayerNode currentPlayer = getPlayerList().findPlayerByIndex(0);
		currentPlayer.setTurn(true);
		System.out.println("Current player's ID: " + currentPlayer.playerID);
	}

	/**
	 * Checks if it is the specified players turn
	 * @param playerID The player to check
	 * @return boolean - True if it is the player's turn, false otherwise
	 */
	public boolean checkTurn (int playerID){
		/**
		 * Check if it's the player's turn. If not, return false.
		 */
		PlayerNode player = getPlayerList().findPlayerByID(playerID);

		if(player.getTurn() == false) return false;
		else return true; 
	}

	/**
	 * Processes a player's bet.
	 * @param playerID The ID of the player that is betting
	 * @param amount The amount to be bet
	 */
	public void bet(int playerID, int amount){
		// check if the player has enough to bet what they asked to
		// if so, then add that money to pot and subtract it from the
		// player's stack

		PlayerNode player = getPlayerList().findPlayerByID(playerID);

		if(amount < player.stack){
			// If the bet is greater than the currentBetCall, then set the
			// bet to the new currentBetCall
			if((player.betPerTurn + amount) > currentBetCall){		//Raise
				currentBetCall = (player.betPerTurn + amount);
				player.bet(amount);
				pot += amount;
			}	
			// Check if bet is equal to the currentBetCall, if so continue on
			else if((player.betPerTurn + amount) == currentBetCall){ 	//Call
				player.bet(amount);
				pot += amount;
			} else if (amount == player.stack) { //All in
				player.bet(amount);
				pot += amount;
			} else {
				;//TODO Illegal bet
			}
		}
		// Keep track of how much each player bets, for each round of betting

		// Will have to record each player's bet in their 'playerNode', setting the
		// amount accordingly

	}
	
	public void fold(int playerID){
		PlayerNode player = getPlayerList().findPlayerByID(playerID);
		player.setFolded(true);
		
	}

	/**
	 * Check if the game is currently on.
	 * @return boolean - True if the game is on, False if the game is not on.
	 */
	public boolean isGameOn(){
		return gameOn;
	}

	public void setGameOn(boolean b){
		this.gameOn = b;
	}
	
	/**
	 * Gets the list of players
	 * @return LinkedPlayerList - The list of players in the game.
	 */
	public LinkedPlayerList getPlayerList(){
		return playerList;
	}	

	/**
	 * Returns the value of the pot.
	 * @return int - The value of the pot.
	 */
	public int getPot(){
		return pot;
	}

	/**
	 * Returns the turn value.
	 * @return int
	 */
	public int getTurn(){
		return turn;
	}

	/**
	 * Return the handDealt value
	 * @return boolean
	 */
	public boolean getHandDealt(){
		return handDealt;
	}
	
	/**
	 * Returns the number of players in the game.
	 * @return int - The number of players.
	 */
	public int getPlayerCount(){
		return playerCount;
	}
	
	public void setPlayerCountPlusOne (int i){
		this.playerCount += i;
	}

	public void run() {
		// TODO Auto-generated method stub
		beginRound();
	}
	
	public void setGameID(int ID){
		gameID = ID;
	}

	public int getGameID(){
		return gameID;
	}
	
	/**
	 * Call to set the currentPlayerIDTurn
	 * @param ID
	 */
	public void setCurrentPlayerIDTurn(int ID){
		currentPlayerIDTurn = ID;
	}
	
	/**
	 * Call to get the currentPlayerIDTurn
	 * @return
	 */
	public int getCurrentPlayerIDTurn(){
		return currentPlayerIDTurn;
	}
	
	public void setCurrentPlayerBeginTurn(boolean b){
		currentPlayerBeginTurn = b;
	}
	
	public boolean getCurrentPlayerBeginTurn(){
		return currentPlayerBeginTurn;
	}
	
	public void setCurrentPlayerDoneTurn(boolean b){
		currentPlayerDoneTurn = b;
	}
	
	public boolean getCurrentPlayerDoneTurn(){
		return currentPlayerDoneTurn;
	}
	
	public void setCurrentPlayerTurn(boolean b){
		currentPlayerTurn = b;
	}
	
	public boolean getCurrentPlayerTurn(){
		return currentPlayerTurn;
	}
	
	public void setCurrentPlayerBetFlag(boolean b){
		currentPlayerBetFlag = b;
	}
	
	public boolean getCurrentPlayerBetFlag(){
		return currentPlayerBetFlag;
	}
	
	public void setCurrentPlayerBetAmount(int i){
		currentPlayerBetAmount = i;
	}
	
	public int getCurrentPlayerBetAmount(){
		return currentPlayerBetAmount;
	}
	
	public void setTurnSent(boolean b){
		turnSent = b;
	}
	
	public boolean getTurnSent(){
		return turnSent;
	}
	
}
