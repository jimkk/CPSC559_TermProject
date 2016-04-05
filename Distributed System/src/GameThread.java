import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;

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
	private String backupServerAddress = "localhost";
	private int backupServerPort = 5432;

	BufferedOutputStream bufOut;
	OutputStreamWriter out;

	public GameThread(Socket socket, int gameID, HashMap<Integer, String> inboxes){
		this.socket = socket;
		this.gameID = gameID;
		this.inboxes = inboxes;
	}

	/**
	 * The main function that runs in a loop and and will manage communications for the client and GameManager.
	 */
	public void run(){
		game = new GameManager();
		System.out.println("Number of players: " + game.getPlayerCount());
		game.setGameID(gameID);
		System.out.printf("Game ID: %d\n", gameID);
		try{
			bufOut = new BufferedOutputStream(socket.getOutputStream());
			out = new OutputStreamWriter(bufOut);
		} catch (Exception e){e.printStackTrace();}
		//TODO Fix this.
		/*
		   if(startMessageParts.length > 2){
		   System.out.println("Restoring from backup");
		   Gson gson = new GsonBuilder().create();
		   String startContents = IOUtilities.rebuildString(startMessageParts, 2, startMessageParts.length);
		   game = gson.fromJson(startContents, GameManager.class);
		   }
		   */

		while(!isDone){
			try{			
				if(game.getPlayerCount() > 2 && !game.isGameOn()){
					// Start game
					game.setGameOn(true);
					game.initializeCommunityCards();
					// Set blinds
					game.setBlinds();
					game.subtractBlinds();
					// Deal cards and send them to their respective players
					game.deal();
					sendCards();

					// Round 1
					System.out.println("===============================================");
					System.out.println("Starting round 1");
					beginRound();
					// Check if all folded
					// Check if all player bets are equal


					// Round 2
					System.out.println("===============================================");
					System.out.println("Starting round 2");
					game.flop();
					sendCommunityCards();
					beginRound();
					// Check if all folded
					// Check if all player bets are equal

					System.out.println("===============================================");
					System.out.println("Starting round 3");
					// Round 3
					game.turn();
					sendCommunityCards();
					beginRound();
					// Check if all folded
					// Check if all player bets are equal

					System.out.println("===============================================");
					System.out.println("Starting round 4");
					// Round 4
					game.river();
					sendCommunityCards();
					beginRound();
					// Check if all folded
					// Check if all player bets are equal

					// Resolve winner 
					// Reset game

					//new Thread(game).start();
				}
				readMessage();
				if(new Date().getTime() - timeSincePing > 3000){
					out.write("ping\n");
					out.flush();
					timeSincePing = new Date().getTime();
				}
				Thread.sleep(10);
			} catch(SocketException e) {
				System.err.println("Lost connection to server. Reconnecting...");
				try{
					reconnect();
				} catch (Exception reconnecte) {
					reconnecte.printStackTrace();
					isDone = true;			
				}
			} catch(Exception e) {e.printStackTrace();}
		}

	}

	public String buildCommunityCards(){
		Card [] communityCards = game.getCommunityCards();
		String comCards = "";
		int x = 0;
		while (communityCards[x] != null && x < communityCards.length) {
			comCards += communityCards[x] + " ";
			x++;
		}

		return comCards;
	}

	public void sendCommunityCards(){
		System.out.println("Displaying community cards to players");
		for(int i = 0; i < game.getPlayerCount(); i++){
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			int playerID = player.getPlayerID();
			String cardList = buildCommunityCards();
			sendMessage(out, playerID, "Community cards: " + cardList);
		}
	}

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
		for(int i = 0; i < game.getPlayerCount(); i++){
			readMessage();
			// Step 1:
			// Find the current player's turn 
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);


			// Note that we skip players who have folded

			game.setCurrentPlayerIDTurn(player.playerID);
			game.setCurrentPlayerTurn(true);
			game.setCurrentPlayerDoneTurn(false);
			game.setTurnSent(false);

			System.out.println("Current player's turn " + player.playerNumber);
			System.out.println("Turn: " + game.getCurrentPlayerTurn());
			System.out.println("DoneTurn: " + game.getCurrentPlayerDoneTurn());
			System.out.println("sentTurn: " + turnSent);

			System.out.println("Begin Turn State:");
			game.getPlayerList().displayGameState();

			// Step 2:
			// Wait on that player's response/move for their turn
			// all the while, processing other player's messages
			while (game.getCurrentPlayerDoneTurn() == false && game.getCurrentPlayerTurn() == true){
				readMessage();
				game.setCurrentPlayerBeginTurn(true);

				// Notify the player that it is their turn
				if (game.getCurrentPlayerBeginTurn() == true && game.getTurnSent() == false) {
					sendMessage(out, game.getCurrentPlayerIDTurn(), "It's now your turn...");
					sendMessage(out, game.getCurrentPlayerIDTurn(), "You can either bet, call, check, or fold");
					//game.setCurrentPlayerBeginTurn(false);
					game.setTurnSent(true);

				}

				// Step 3:
				// process all incoming messages from all clients.
				// Only the client, whose current turn it is, can actually
				// complete a turn. 
				readMessage();

				if (game.getCurrentPlayerBetFlag() == true && game.getCurrentPlayerBeginTurn() == true) {
					game.setCurrentPlayerBeginTurn(false);
					System.out.println("Player: " + game.getCurrentPlayerIDTurn() + " has chosen to bet: " + game.getCurrentPlayerBetAmount());
					// I believe this is already called in the 'bet' switch case
					//bet(game.getCurrentPlayerIDTurn(), game.getCurrentPlayerBetAmount());
					game.setCurrentPlayerBetFlag(false);
					System.out.println("CurrentPlayerBetFlag set to: " + game.getCurrentPlayerBetFlag());
					System.out.println("CurrentPlayerBetFlag set to: " + game.getCurrentPlayerBetFlag());
					game.setCurrentPlayerDoneTurn(true);
					System.out.println("CurrentPlayerDoneTurn set to: " + game.getCurrentPlayerDoneTurn());
				}

				if(game.getCurrentPlayerDoneTurn() == true && player.getTurn() == true) {
					// Set the next player's turn to true
					game.setCurrentPlayerTurn(false);
					player.setTurn(false);
					player.nextPlayer.setTurn(true);
				}
				readMessage();
			}
			readMessage();

			System.out.println("\nEnd Turn State:");
			game.getPlayerList().displayGameState();
		}
	}
	/**
	 * Read inputs from the Clients and branch according to the messageType
	 * Note: those player's who make a request reqruiring it to be their turn
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
					System.out.println("New player added");
					game.addPlayerToGame(stack, playerID);
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
				case("message"):
					System.out.printf("Message from %s: %s\n", playerID, contents);
					break;
				case("displayGame"):
					game.getPlayerList().displayGameState();
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

	private void seeHand(int playerID){
		PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
		Card[] hand = player.getHand();
		if(hand[0] == null){
			sendMessage(out, playerID, "The hands have not been dealt yet.");
		} else {
			sendMessage(out, playerID, "Hand: " + hand[0] + " " + hand[1]);
		}
	}

	//message switch methods
	private void bet(int betAmount, int playerID){

		try{
			if (game.checkTurn(playerID) == false) {
				System.out.println("Currently not this player's turn; cannot place bet yet");

				sendMessage(out, playerID, "It's not your turn, you cannot place a bet yet.");
				out.flush();
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
			}
		} catch (IOException e) {e.printStackTrace();}
	}

	private void checkTurn(int playerID){
		try{
			if (game.checkTurn(playerID) == false) {
				System.out.println("Currently not this player's turn.");
				sendMessage(out, playerID, "It's not your turn");
				out.flush();
			}
			else
				System.out.println("It is your turn.");
		} catch (IOException e) {e.printStackTrace();}
	}

	private void checkStack(int playerID){
		try{
			PlayerNode player = game.getPlayerList().findPlayerByIndex(playerID);
			System.out.println("This players current stack: " + player.getStack());
			sendMessage(out, playerID, "This players current stack: " + player.getStack());
			out.flush();
		} catch (IOException e) {e.printStackTrace();}
	}

	private void call(int playerID){
		try{
			if (game.checkTurn(playerID) == false) {
				System.out.println("Currently not this player's turn; cannot call another player's bet");
				sendMessage(out, playerID, "It's not your turn, you cannot call another player's bet yet");
				out.flush();
			}
		} catch (IOException e){e.printStackTrace();}
	}

	private void fold(int playerID){
		try{
			if (game.checkTurn(playerID) == false) {
				System.out.println("Currently not this player's turn; cannot fold until it is");
				sendMessage(out, playerID, "It's not your turn, you cannot fold until it is");
				out.flush();
			}
			else {
				System.out.println("Player: " + playerID + "has chosen to fold his/her hand...");
				game.fold(playerID);
				PlayerNode player = game.getPlayerList().findPlayerByIndex(playerID);
				System.out.println("Player: " + playerID + "folded = " +  player.getFolded());
				game.setCurrentPlayerDoneTurn(true);
				sendMessage(out, playerID, "You have chosen to fold your hand for this round");
			}
		} catch (IOException e){e.printStackTrace();}
	}

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

	private void reconnect(){
		try{
			socket.close();
			ServerSocket reconnectSocket = new ServerSocket(socket.getLocalPort());
			socket = reconnectSocket.accept();
			reconnectSocket.close();
			bufOut = new BufferedOutputStream(socket.getOutputStream());
			out = new OutputStreamWriter(bufOut);
		} catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * This function will check for a backup server and connect to it if it exists.
	 */
	private void setUpBackup(){
		try{
			Socket backupServer = new Socket(backupServerAddress, backupServerPort);
			BufferedOutputStream bufOut = new BufferedOutputStream(backupServer.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);
			new Thread(new BackupManager(out, game)).start();
		} catch (Exception e){
			System.out.println("WARNING: Unable to connect to backup server");
		}
	}

}
