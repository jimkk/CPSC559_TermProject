import java.io.*;
import java.util.*;


public class GameManager {

	private int pot = 0;
	private int currentBetCall = 0;
	private int playerCount = 0;
	private int playerID = 0;
	private int turn = -1;
	private int bigBlindAmount = 100;
	private int smallBlindAmount = 50;
	private boolean gameOn = false;
	private boolean gameStart = true;

	private Deck deck;
	private Card[] communityCards = new Card[5];
	private LinkedPlayerList playerList = new LinkedPlayerList();

	// When all players have joined, the game manager will deal the cards and assign the first
	// player in the player linked list, the turn. Big and small blinds will also be assigned at
	// this point



	public void beginRound(){
		/**
		 * Need to have a state-machine to manage the turns required of each player 
		 */

		//rotatePlayers();		 

		// Set the blinds and the turns
		setBlinds();

		// Deal the cards
		deal();

		// Subtract the big and small blind values from their respective player's stacks
		// and add them to the pot
		subtractBlinds();

		// Traverse through player list, prompting each player for their turns
		/*for(int i = 0; i < playerCount; i++){
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
			while (player.getDoneTurn() == false && player.getTurn() == true ){
				
				// Now we need to notify the player that it is their turn
				// Setting the flag below, lets the serverthread know that it's now
				// that player's turn, and the serverthread will notify them accordingly
				player.setBeginTurn(true);
				
				
				if(player.getDoneTurn() == true && player.getTurn() == true) {
					// Set the next player's turn to true
					player.setTurn(false);
					player.setDoneTurn(false);
					player.nextPlayer.setTurn(true);
				}
			}
		}*/

		//set various flags 

	}

	public void setBlinds(){
		/**
		 * Set Big and small blinds for each round
		 */

		for(int i = 0; i < playerCount; i++){
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
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

	// Deal cards
	public void deal(){	
		deck = new Deck();

		for(int i = 0; i < playerCount; i++){
			PlayerNode player = getPlayerList().findPlayerByIndex(i);
			player.setHand(deck.Draw(), deck.Draw());
			System.out.print(".");
		}
		gameOn = true;
		turn = 0;
	}
	// Assign turn
	// Flip a card
	// Add bet to pot
	// force display the cards
	// remove pot from table and award to winning player

	public int addPlayerToGame(int stack, int playerPort, String ipAddress) {
		/**
		 * Add a player to the game
		 */
		if(playerCount == 6) {
			return -1;
		}
		playerCount++;
		playerID++;
		this.getPlayerList().addPlayers(playerCount, playerID, stack, playerPort, ipAddress);

		if(playerCount > 2){
			beginRound();
		}

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

	public void rotatePlayers(){
		PlayerNode currentPlayer = getPlayerList().findPlayerByIndex(0);
		currentPlayer.setTurn(true);
		System.out.println("Current player's port: " + currentPlayer.port);
	}

	public boolean checkTurn (int playerPort){
		/**
		 * Check if it's the player's turn. If not, return false.
		 */
		PlayerNode player = getPlayerList().findPlayerByPort(playerPort);

		if(player.getTurn() == false) return false;
		else return true; 
	}

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

	public boolean isGameOn(){
		return gameOn;
	}


	public LinkedPlayerList getPlayerList(){
		return playerList;
	}	

	public int getPot(){
		return pot;
	}

	public int getTurn(){
		return turn;
	}

	public int getPlayerCount(){
		return playerCount;
	}
}
