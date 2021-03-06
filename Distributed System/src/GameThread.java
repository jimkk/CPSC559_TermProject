import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * The main processing thread on the server side for communications with a
 * client. It will manage communications with the client and convey the
 * player's moves to the GameManager and sends and game related messages from
 * the server to the client.
 */

public class GameThread implements Runnable{


	private int randomCardNumber;

	private boolean isDone = false;
	private boolean folded = false;
	private boolean handSent = false;
	private boolean turnSent = false;

	private static boolean debug = true;

	private int gameID;
	private HashMap<Integer, String> inboxes;

	// Instance variables
	private Socket socket;
	Random rand = new Random();
	Card[] hand;
	GameManager game;
	long timeSincePing = new Date().getTime();

	//Backup
	private Socket backupServer;

	BufferedOutputStream bufOut;
	OutputStreamWriter out;

	public GameThread(Socket socket, int gameID, HashMap<Integer, String> inboxes){
		this.socket = socket;
		this.gameID = gameID;
		this.inboxes = inboxes;
		game = new GameManager();
	}

	public GameThread(Socket socket, Socket backupServer, int gameID, HashMap<Integer, String> inboxes){
		this.socket = socket;
		this.backupServer = backupServer;
		this.gameID = gameID;
		this.inboxes = inboxes;
		game = new GameManager();
	}

	public GameThread(Socket socket, int gameID, HashMap<Integer, String> inboxes, GameManager game){
		this.socket = socket;
		this.gameID = gameID;
		this.inboxes = inboxes;
		this.game = game; 
	}

	public GameThread(Socket socket, Socket backupServer, int gameID, HashMap<Integer, String> inboxes, GameManager game){
		this.socket = socket;
		this.backupServer = backupServer;
		this.gameID = gameID;
		this.inboxes = inboxes;
		this.game = game; 
	}
	/**
	 * The main function that runs in a loop and and will manage communications for the client and GameManager.
	 */
	public void run(){
		setUpBackup();
		System.out.println("Number of players: " + game.getPlayerCount());
		game.setGameID(gameID);
		System.out.printf("Game ID: %d\n", gameID);
		try{
			bufOut = new BufferedOutputStream(socket.getOutputStream());
			out = new OutputStreamWriter(bufOut);
		} catch (Exception e){e.printStackTrace();}

		while(!isDone){
			try{
				if(game.getPlayerCount() > 2 && !game.isGameOn()){
					System.out.println(game.isGameOn());
					// Start game
					game.setGameOn(true);
					game.initializeCommunityCards();
					// Set blinds
					
					game.getPlayerList().displayGameState();
					game.setBlinds();
					game.getPlayerList().displayGameState();
					game.subtractBlinds();
					
					// Deal cards and send them to their respective players
					game.deal();
					sendCards();
					sendBlindsNotification();

					// Round 1
					System.out.println("===============================================");
					System.out.println("Starting round 1");
					beginRound();
					// Check if all folded
					if (game.checkAllFolded())
					{
						System.out.println("Evaluating winner...");
						//call reset game
					}
					// Check if all player bets are equal
					checkEqualBets();


					// Round 2
					System.out.println("===============================================");
					System.out.println("Starting round 2");
					game.flop();
					sendCommunityCards();
					beginRound();
					// Check if all folded
					if (game.checkAllFolded())
					{
						System.out.println("Evaluating winner...");
						//call reset game
					}
					// Check if all player bets are equal
					checkEqualBets();

					System.out.println("===============================================");
					System.out.println("Starting round 3");
					// Round 3
					game.turn();
					sendCommunityCards();
					beginRound();
					// Check if all folded
					if (game.checkAllFolded())
					{
						System.out.println("Evaluating winner...");
						//call reset game
					}					
					// Check if all player bets are equal
					checkEqualBets();

					System.out.println("===============================================");
					System.out.println("Starting round 4");
					// Round 4
					game.river();
					sendCommunityCards();
					beginRound();
					// Check if all folded
					if (game.checkAllFolded())
					{
						System.out.println("Evaluating winner...");
						//call reset game
					}
					// Check if all player bets are equal
					checkEqualBets();

					// Resolve winner
					winner();
					
					// Reset game
					handSent = false;
					game.resetGame();

					//new Thread(game).start();
				}
				readMessage();
				Thread.sleep(10);
			} catch(Exception e) {e.printStackTrace(); isDone = true;}
		}

	}

	/**
	 * Helper method to build the Community Cards (comCards) list as a String
	 * @return String - comCards The community cards.
	 */
	public String buildCommunityCards(){
		Card [] communityCards = game.getCommunityCards();
		String comCards = "";
		int x = 0;
		while (x < communityCards.length) {
			if (communityCards[x] == null) break;
			comCards += communityCards[x] + " ";
			x++;
		}

		return comCards;
	}

	/**
	 * Sends Community Cards to all players
	 * @return None
	 */
	public void sendCommunityCards(){
		System.out.println("Displaying community cards to players");
		System.out.println(game.getPlayerCount());
		for(int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			int playerID = player.getPlayerID();
			String cardList = buildCommunityCards();
			sendMessage(out, playerID, "Community cards: " + cardList);
		}
	}

	/**
	 * Notify the players that they have been set as the small or big blinds
	 */
	public void sendBlindsNotification(){
		for(int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			int playerID = player.getPlayerID();
			if (player.getBigBlind() == true)
			{
				sendMessage(out, playerID, "You are the Big Blind for this game. Current amount subtracted from your stack: $" + game.getBigBlindAmount());
			}
			if (player.getSmallBlind() == true)
			{
				sendMessage(out, playerID, "You are the Small Blind for this game. Current amount subtracted from your stack: $" + game.getSmallBlindAmount());
			}
		}
	}
	
	/**
	 * Sends the cards to their respective players
	 * @return None	 
	 */
	public void sendCards(){
		if(game.isGameOn() && !handSent && game.getHandDealt()){
			System.out.println("Round has started; dealing cards to players");
			for(int i = 0; i < game.getPlayerCount(); i++){
				PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
				Card [] hand = player.getHand();
				int playerID = player.getPlayerID();
				sendMessage(out, playerID, "Game Started!");

				System.out.println("PlayerID: " + playerID + " Hand: " + hand[0] + " " + hand[1]);
				sendMessage(out, playerID, "Hand: " + hand[0] + " " + hand[1]);
			}
			handSent = true;
		}
	}

	/**
	 * Starts a single round of the Poker Game
	 */
	private void beginRound(){
		// Begin the game loop
		// Loop through all players, and on each player, keep looping until their turn is
		// confirmed done, reading all inputs/messages from other players
		LinkedPlayerList playerList = game.getPlayerList();
		
		// We have to initialize the startingPlayer to something or else the 
		// compiler gets all mad...
		PlayerNode startingPlayer = game.getPlayerList().rootPlayer;
		PlayerNode currentPlayer;
		for (int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			if (player.getBigBlind() == true){
				// When we find a player who is currently holding the Big Blind, we assign that player to be the
				// starting player
				startingPlayer = player;
				break;
			}
		}
		currentPlayer = startingPlayer;
		do{
			if (currentPlayer.getFolded() == true) {
				currentPlayer = currentPlayer.nextPlayer;
				continue;
			}
		
			// Step 1:
			// Find the current player's turn 
			// Note that we skip players who have folded

			game.setCurrentPlayerIDTurn(currentPlayer.playerID);
			game.setCurrentPlayerTurn(true);
			game.setCurrentPlayerDoneTurn(false);
			game.setTurnSent(false);

			System.out.println("Current player's turn " + currentPlayer.playerNumber);
			System.out.println("Turn: " + game.getCurrentPlayerTurn());
			System.out.println("DoneTurn: " + game.getCurrentPlayerDoneTurn());
			System.out.println("sentTurn: " + turnSent);

			System.out.println("Begin Turn State:");
			game.getPlayerList().displayGameState();

			// Step 2:
			// Wait on that player's response/move for their turn
			// all the while, processing other player's messages
			while (game.getCurrentPlayerDoneTurn() == false && game.getCurrentPlayerTurn() == true){				
				game.setCurrentPlayerBeginTurn(true);

				// Notify the player that it is their turn
				if (game.getCurrentPlayerBeginTurn() == true && game.getTurnSent() == false) {
					sendMessage(out, game.getCurrentPlayerIDTurn(), "It's now your turn...");
					sendMessage(out, game.getCurrentPlayerIDTurn(), "You can either bet, call, check, or fold");					
					game.setTurnSent(true);

				}

				// Step 3:
				// process all incoming messages from all clients.
				// Only the client, whose current turn it is, can actually
				// complete a turn. 
				
				readMessage();
				
				if (game.getCurrentPlayerBetFlag() == true && game.getCurrentPlayerBeginTurn() == true) {
					System.out.println("CurrentPlayerBetFlag set to: " + game.getCurrentPlayerBetFlag());
					game.setCurrentPlayerBeginTurn(false);
					System.out.println("Player: " + game.getCurrentPlayerIDTurn() + " has chosen to bet: " + game.getCurrentPlayerBetAmount());		
					game.setCurrentPlayerBetFlag(false);
					System.out.println("CurrentPlayerBetFlag set to: " + game.getCurrentPlayerBetFlag());
					System.out.println("CurrentPlayerBetFlag set to: " + game.getCurrentPlayerBetFlag());
					game.setCurrentPlayerDoneTurn(true);
					System.out.println("CurrentPlayerDoneTurn set to: " + game.getCurrentPlayerDoneTurn());
				}

				if(game.getCurrentPlayerDoneTurn() == true && currentPlayer.getTurn() == true) {
					// Set the next player's turn to true
					game.setCurrentPlayerTurn(false);
					currentPlayer.setTurn(false);
					
					PlayerNode potentialNextPlayer = currentPlayer.nextPlayer;
					do{
						if (potentialNextPlayer.getFolded() == false){
							potentialNextPlayer.setTurn(true);
							break;
						}
						potentialNextPlayer = potentialNextPlayer.nextPlayer;
					}while (potentialNextPlayer != currentPlayer);					
				}
				
			}			

			System.out.println("\nEnd Turn State:");
			game.getPlayerList().displayGameState();
			
			currentPlayer = currentPlayer.nextPlayer;
		}while(currentPlayer != startingPlayer);
	}
	/**
	 * Read inputs from the Clients and branch according to the messageType
	 * Note: those player's who make a request requiring it to be their turn
	 *       and it's not their turn, are refused by the GameServer 
	 */
	private void readMessage(){
		String buffer = "";
		String messageType = "";
		
		if(!inboxes.get(gameID).equals("")){
			int playerID;
			String [] messageParts;
			String contents;

			buffer = inboxes.get(gameID);
			inboxes.put(gameID, "");

			messageParts = buffer.toString().split(" ");
			playerID = Integer.parseInt(messageParts[0]);
			messageType = messageParts[1];
			contents = IOUtilities.rebuildString(messageParts, 2, messageParts.length);

			// Check if it is that player's turn; if not reply accordingly
			// Note: only check if the player has requested something that cannot
			// be done if it is not his or her turn; i.e. betting, folding, calling


			switch(messageType){
				case("addplayer"):
					int stack = Integer.parseInt(messageParts[2]);
					System.out.printf("New player added (ID = %d)\n", playerID);
					PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
					System.out.println(player);
					if(player == null){
						game.addPlayerToGame(stack, playerID);
					}
					System.out.printf("There are %d players currently in the game\n", game.getPlayerCount());
					break;
				case("checkTurn"):
					checkTurn(playerID);
					break;
				case("checkStack"):
					checkStack(playerID);
					break;
				case("seeHand"):
					seeHand(playerID);
					break;
				case("communityCards"):
					String cardList = buildCommunityCards();
					sendMessage(out, playerID, "Community Cards: " + cardList);
					break;
				case("bet"):
					int betAmount = Integer.parseInt(contents);
					bet(betAmount, playerID);
					break;
				case("call"):
					call(playerID);
					break;
				case("check"):
					System.out.println("Player: " + playerID + "checks");
					bet(0, playerID);
					break;
				case("fold"):
					fold(playerID);
					break;
				case("vote"):
					if (game.getStartVoting() == false){
						sendMessage(out, playerID, "You cannot place your votes for the winner yet; the game is still going");
						break;
					}
					int votedPlayer = Integer.parseInt(contents);
					game.setVotes(playerID, votedPlayer);
					System.out.printf("Player: " + playerID + "'s vote: " + votedPlayer + "\n");
					break;
				case("seePot"):
					sendMessage(out, playerID, "The current pot amount is: $" + game.getPot());
					break;
				case("message"):
					System.out.printf("Message from %s: %s\n", playerID, contents);
					break;
				case("displayGame"):
					game.getPlayerList().displayGameState();
					sendMessage(out, playerID, "          Current game ID: " + game.getGameID());
					sendMessage(out, playerID, "             Player count: " + game.getPlayerCount());
					sendMessage(out, playerID, "         Current bet call: " + game.getCurrentBetCall());
					sendMessage(out, playerID, " Current player ID's turn: " + game.getCurrentPlayerIDTurn());
					sendMessage(out, playerID, "Current player bet amount: " + game.getCurrentPlayerBetAmount());
					sendMessage(out, playerID, "             Current turn: " + game.getTurn());
					sendMessage(out, playerID, "              Current pot: " + game.getPot());
					sendMessage(out, playerID, "          Is the game on?: " + game.isGameOn());
					sendMessage(out, playerID, "          Community cards: " + buildCommunityCards());
					sendMessage(out, playerID, "                Your hand:");
					seeHand(playerID);
					sendMessage(out, playerID, "  Have all player folded?: " + game.checkAllFolded());
					sendMessage(out, playerID, "           Is it my turn?: " + game.checkTurn(playerID));
					break;
				case("close"):
	
					System.out.printf("Player %d has left the game\n", playerID);
					// return the playerID
					game.removePlayerFromGame(playerID);
					break;
				case("destroy"):
					System.out.println("Server shut down at client's request");
					//TODO
					break;
				case(""):
					break;
				default:
					System.out.println("ERROR: Unknown Message Type");
					System.out.println("\t" + buffer);
					System.exit(-1);
					break;
			}
		}
	}

	/**
	 * Does a lookup on the player's hand based on their playerID
	 * @param playerID
	 */
	private void seeHand(int playerID){
		PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
		Card[] hand = player.getHand();
		if(hand[0] == null){
			sendMessage(out, playerID, "The hands have not been dealt yet.");
		} else {
			sendMessage(out, playerID, "Hand: " + hand[0] + " " + hand[1]);
		}
	}
	
	/**
	 * Check to make sure that every player's bet amount is equivalent to the highest bet amount
	 */
	private void checkEqualBets()
	{
		List<Integer> sendTo = new ArrayList<>();
		PlayerNode player;
		int currentPlayerTurn = 0; 
		// Build the list of players who's current bet amount does not match the current call amount on the table
		for(int i = 0; i < game.getPlayerCount(); i++){
			player = game.getPlayerList().findPlayerByIndex(i);
			if (player.currentBetAmount < game.getCurrentBetCall()) sendTo.add(player.getPlayerID());
			if (player.getTurn() == true) {
				currentPlayerTurn = player.getPlayerID();
			}
		}
		for(int i = 0; i < sendTo.size(); i++){		
			int playerID = sendTo.get(i);
			sendMessage(out, playerID, "Your current bet amount is less than the call amount on the table. Please call or fold.");
			player = game.getPlayerList().findPlayerByID(playerID);
			
			player.setTurn(true);
			// loop on the player in question until they have matched the current call amount or folded their hand
			while(game.getPlayerList().findPlayerByID(playerID).getCurrentBetAmount() < game.getCurrentBetCall() 
					&& game.getPlayerList().findPlayerByID(playerID).getFolded() == false) {
				readMessage();
			}
			player.setTurn(false);
			
		}
		player = game.getPlayerList().findPlayerByID(currentPlayerTurn);
		player.setTurn(true);
		
		// We set the currenPlayerBetFlag to false because if we don't, the game
		// logic, once we get out of this checkEqualBets function, would skip that player
		// and we don't want that to happen.
		game.setCurrentPlayerBetFlag(false);
		// check to make sure all players in the original list of unequal bet amounts, now have an equal amount
		// if not, call checkEqualBets()
		for(int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode playerCheck = game.getPlayerList().findPlayerByIndex(i);
			if (playerCheck.currentBetAmount < game.getCurrentBetCall()
			    && game.getPlayerList().findPlayerByIndex(i).getFolded() == false) checkEqualBets(); 
		}
			
	}
	
	/**
	 * Checks if we have finished the voting phase for the winner
	 * @return
	 */
	private boolean doneVoting()
	{
		for(int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			//System.out.printf("Vote: " + player.getVote());
			if (player.getVote() == -1) return false;
		}
		return true;
	}
	
	/**
	 * Check all votes to make sure all players have voted a winner
	 * @return True if pass, False if failed
	 */
	private boolean checkVotes()
	{
		PlayerNode prevPlayer = game.getPlayerList().findPlayerByIndex(0);
		int previousVote = prevPlayer.getVote();
		for(int i = 1; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			if (player.getVote() != previousVote && player.getVote() != -1) return false;
		}
		return true;
	}
	
	/**
	 * Compute the winner
	 */
	private void winner()
	{
		game.setStartVoting(true);
		do{
			for(int i = 0; i < game.getPlayerCount(); i++){
				PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
				String msg = "GAME HAS COMPLETED!; commencing unanimous vote for the winner (not all votes may have been equal the first time)";
				sendMessage(out, player.getPlayerID(), msg);
			}
			sendCommunityCards();
			for (int i = 0; i < game.getPlayerCount(); i++) {
				PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
				int playerIDSend = player.getPlayerID();
				for (int j = 0; j < game.getPlayerCount(); j++) {
					PlayerNode otherPlayer = game.getPlayerList().findPlayerByIndex(j);
					Card [] hand = otherPlayer.getHand();
					int playerID = otherPlayer.getPlayerID();
					
					System.out.println("PlayerID: " + playerID + " Hand: " + hand[0] + " " + hand[1]);
					sendMessage(out, playerIDSend, "Player: " + playerID + "'s" + "Hand: " + hand[0] + " " + hand[1]);
				}
			}
			
			// Continuously check if we are done voting on the winner
			while (doneVoting() == false){
				readMessage();
			}
			game.getPlayerList().displayGameState();
			// When completed the voting, we check to make sure all votes equal
		}while(checkVotes() == false);	
		
		// Now, since all votes are unanimous, we can just take the first player's vote and use that to assign the winner
		PlayerNode player = game.getPlayerList().findPlayerByIndex(0);
		game.assignWinnings(player.getVote());
		sendMessage(out, player.getVote(), "You have WON! Your winnings are: $" + game.getPot());
	}

	/**
	 * Broadcast bets to all players who were not that player who made the bet
	 * @param bettingPlayerID
	 * @param betAmount
	 */
	private void sendBets(int bettingPlayerID, int betAmount){
		for (int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			if (player.getPlayerID() == bettingPlayerID) continue;
			else {
				sendMessage(out, player.getPlayerID(), "Player: " + bettingPlayerID + " has bet: $" + betAmount);
			}
		}
	}
	
	/**
	 * Process a player's bet amount
	 * @param betAmount
	 * @param playerID
	 */
	private void bet(int betAmount, int playerID){
		if (game.checkTurn(playerID) == false) {
			System.out.println("Currently not this player's turn; cannot place bet yet");

			sendMessage(out, playerID, "It's not your turn, you cannot place a bet yet.");
		}
		else {
			System.out.printf("Bet amount from %d: %s\n", playerID, betAmount);
			game.setCurrentPlayerBetFlag(true);
			game.setCurrentPlayerBetAmount(betAmount);
			game.bet(playerID, betAmount);
			PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
			int stack = player.getStack();
			System.out.println("This player's new stack total: " + stack);
			game.setCurrentPlayerDoneTurn(true);
			System.out.println("No longer this players turn. Confirmation: " + game.getCurrentPlayerDoneTurn());
			sendBets(playerID, betAmount);
		}
	}
	/**
	 * Checks to see if it is the requesting player's turn
	 * @param playerID Integer representing current player.
	 */
	private void checkTurn(int playerID){
		if (game.checkTurn(playerID) == false) {
			System.out.println("Currently not this player's turn.");
			sendMessage(out, playerID, "It's not your turn");				
		}
		else{
			System.out.println("It is this players turn.");
			sendMessage(out, playerID, "It's your turn!");	
		}
	}

	/**
	 * Allows the player to view their stack
	 * @param playerID Integer representing current player.
	 */
	private void checkStack(int playerID){		
		PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
		System.out.println("This players current stack: " + player.getStack());
		sendMessage(out, playerID, "This players current stack: " + player.getStack());
	}

	/**
	 * Method executed when a player has indicated they wish to call a previous bet
	 * @param playerID Integer representing current player.
	 */
	private void call(int playerID){
		if (game.checkTurn(playerID) == false) {
			System.out.println("Currently not this player's turn; cannot call another player's bet");
			sendMessage(out, playerID, "It's not your turn, you cannot call another player's bet yet");
		}
		else {
			bet(game.getCurrentBetCall(), playerID);
		}		
	}

	/**
	 * Fold this player's hand
	 * @param playerID Integer representing current player.
	 */
	private void fold(int playerID){
		if (game.checkTurn(playerID) == false) {
			System.out.println("Currently not this player's turn; cannot fold until it is");
			sendMessage(out, playerID, "It's not your turn, you cannot fold until it is");
		}
		else {
			System.out.println("Player: " + playerID + "has chosen to fold his/her hand...");
			game.fold(playerID);
			PlayerNode player = game.getPlayerList().findPlayerByIndex(playerID);
			System.out.println("Player: " + playerID + "folded = " +  player.getFolded());
			game.setCurrentPlayerDoneTurn(true);
			sendMessage(out, playerID, "You have chosen to fold your hand for this round");
		}		
	}

	/**
	 * Sends a message through the GameServer to the Main Server, where the Main Server will route that message
	 * to the corresponding player
	 * @param out OutputStreamWriter object.
	 * @param playerID Integer representing current player. 
	 * @param message String representing the message.
	*/
	private void sendMessage(OutputStreamWriter out, int playerID, String message){
		try{
			out.write(playerID + " message " + message + "\n");
			out.flush();
		} catch (SocketException se) {
			reconnect();
		} catch (Exception e){
			System.err.println("Error sending message to " + playerID);
			e.printStackTrace();
		}
	}

	/** 
	 * Will try and reconnect to the same IP address and port that it was
	 * originally connected to.  It will loop till it it able to successfully
	 * reconnect.
	 */
	private void reconnect(){
		try{
			while(true){
				try{
					bufOut = new BufferedOutputStream(socket.getOutputStream());
					out = new OutputStreamWriter(bufOut);
					break;
				} catch (SocketException socketE) {Thread.sleep(2000);}
			}
		} catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * This function will check for a backup server and connect to it if it exists.
	 */
	private void setUpBackup(){
		try{
			BufferedOutputStream bufOut = new BufferedOutputStream(backupServer.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);
			new Thread(new BackupManager(out, game)).start();
		} catch (Exception e){
			System.out.println("WARNING: Unable to connect to backup server");
		}
	}

	public void setSocket(Socket socket){
		this.socket = socket;
	}

}
