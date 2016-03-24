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

public class GameThread {


	private int randomCardNumber;
	
	private boolean isDone = false;
	private boolean folded = false;
	private boolean handSent = false;
	private boolean turnSent = false;

	private static String serverMessage = "";
	private static String buffer;
	private static String messageType;
	private static boolean serverMessageLock = false;
	private static int key = -1;
	private static boolean debug = true;

	// Instance variables
	private Socket socket;
	Random rand = new Random();
	Card[] hand;
	GameManager game;
	//LinkedPlayerList playerList = new LinkedPlayerList();

	BufferedInputStream bufIn;
	InputStreamReader in;
	BufferedOutputStream bufOut;
	OutputStreamWriter out;

	public GameThread(Socket socket, GameManager game){
		this.socket = socket;
		this.game = game;

		//this.game.setTurnToCurrentPlayer(playerPort);
	}

	/**
	 * The main function that runs in a loop and and will manage communications for the client and GameManager.
	 */
	public void run(){
		try{
			bufIn = new BufferedInputStream(socket.getInputStream());
			in = new InputStreamReader(bufIn);
			bufOut = new BufferedOutputStream(socket.getOutputStream());
			out = new OutputStreamWriter(bufOut);

			/*
			   returnCode = game.addPlayerToGame(1000, playerID);		//TODO Set custom stack amount
			   if (returnCode == -1) {
			   out.write("Game is full; connection denied");
			   out.flush();
			   socket.close();

			   }
			   System.out.printf("New Client Connected, IP=%s, Port=%d\n", socket.getInetAddress(), socket.getPort());
			   game.getPlayerList().displayGameState();
			   */
			
			String startMessage = IOUtilities.read(in);
			String [] startMessageParts = startMessage.split(" ");
			int gameID = Integer.parseInt(startMessageParts[1]);
			game.setGameID(gameID);
			String startContents = IOUtilities.rebuildString(startMessageParts, 2, startMessageParts.length);
			System.out.printf("Game ID: %d\n", gameID);

			if(startMessageParts.length > 2){
				System.out.println("Restoring from backup");
				Gson gson = new GsonBuilder().create();
				game = gson.fromJson(startContents, GameManager.class);
			}

			
			buffer = "";
			messageType = "";

			while(!isDone){
			


				/*if(game.getPlayerList().findPlayerByPort(playerPort).getTurn() 
				  && !turnSent){
				  out.write("message It's your turn!\n");
				  out.flush();
				  turnSent = true;
				  }*/

				
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
				readMessage(buffer, messageType);
				Thread.sleep(10);
			}
		} catch(SocketException e) {
			System.err.println("Lost connection to client");
			//TODO Remove player from game
		} catch(Exception e) {e.printStackTrace();}

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

				//out.write("message Game Started!\n");
				//out.write("message Hand: " + hand[0] + " " + hand[1] + "\n");
				//out.flush();
			}
			handSent = true;
		}
	}
	
	/**
	 * Reads in a message from the stream, stopping on a -1 or a newline character
	 * @param in The stream to read from
	 * @return StringBuffer The received message
	 */

	//TODO Move this method to the Card class
	private String determineCardSuit(int randCard){
		String foundSuit = "default suit";
		if(randCard > 0 && randCard < 14){
			foundSuit = "Spades";
		}
		if(randCard > 13 && randCard < 27){
			foundSuit = "Hearts";
		}
		if(randCard > 26 && randCard < 40){
			foundSuit = "Clubs";
		}
		if(randCard > 39 && randCard < 53){
			foundSuit = "Diamonds";
		}
		return foundSuit;

	}

	//TODO Move this method to the Card class
	private int determineCardValue(int randCard, String suit){
		int finalCardNumber = 0;
		if(suit == "Spades"){
			finalCardNumber = randCard + 1;
		}
		if(suit == "Hearts"){
			finalCardNumber = randCard - 12;
		}
		if(suit == "Clubs"){
			finalCardNumber = randCard - 25;
		}
		if(suit == "Diamonds"){
			finalCardNumber = randCard - 38;
		}
		return finalCardNumber;
	}  

	/**
	 * Starts a single round of the Poker Game
	 */
	private void beginRound(){
		// Begin the game loop
		// Loop through all players, and on each player, keep looping until their turn is
		// confirmed done, reading all inputs/messages from other players
		for(int i = 0; i < game.getPlayerCount(); i++){
			// Step 1:
			// Find the current player's turn 
			PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
			
			
			// Note that we skip players who have folded
			//if (player.getFolded() == true) continue;
			
			
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
				readMessage(buffer, messageType);
				
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
					//player.setDoneTurn(false);
					player.nextPlayer.setTurn(true);
				}
				
			}
			
			System.out.println("\nEnd Turn State:");
			game.getPlayerList().displayGameState();
		}
	}
	/**
	 * Read inputs from the Clients and branch according to the messageType
	 * Note: those player's who make a request reqruiring it to be their turn
	 *       and it's not their turn, are refused by the GameServer 
	 */
	private void readMessage(String buffer, String messageType){
		try{
			if(in.ready()){
				int playerID;
				String [] messageParts;
				String contents;
	
				buffer = IOUtilities.read(in);
	
	
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
						System.out.println("ADDING NEW PLAYER");
						game.addPlayerToGame(stack, playerID);
						System.out.println("playerCount: " + game.getPlayerCount());
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
						/*
						   case("set_message_request"):
						   setMessageRequest();
						   break;
						   case("set_message"):
						   String smessage = buffer.substring(buffer.indexOf(" ")+1);
						   String [] messageParts = smessage.split(" ");
						   int keyResponse = Integer.parseInt(messageParts[0]);
						   if(keyResponse == key){
						   StringBuffer smBuf = new StringBuffer();
						   for(int i = 1; i < messageParts.length; i++){
						   smBuf.append(messageParts[i]);
						   if(i != messageParts.length-1){
						   smBuf.append(" ");
						   }
						   }
						   serverMessage = smBuf.toString();
						   serverMessageLock = false;
						   key = -1;
						   out.write("message Message Set!\n");
						   out.flush();
						   System.out.printf("Server message set by %s: %s\n", socket.getInetAddress(), serverMessage);
						   } else {
						   out.write("message INVALID KEY\n");
						   out.flush();
						   }
	
						   break;
						   case("get_message"):
						   getMessage();
						   break;
						   */
					case("display game"):
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
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private void seeHand(int playerID){
		PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
		Card[] hand = player.getHand();
		sendMessage(out, playerID, "Hand: " + hand[0] + " " + hand[1]);
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
		} catch (Exception e){
			System.err.println("Error sending message to " + playerID);
			e.printStackTrace();
		}
	}


	private String rebuildString(String [] parts, int start, int end){
		StringBuffer buffer = new StringBuffer();
		for(int i = start; i < end; i++){
			buffer.append(parts[i]);
		}
		return buffer.toString();
	}

/*
	private void setMessageRequest(){
		try{
			if(!serverMessageLock){
				serverMessageLock = true;
				key = rand.nextInt(10000);
				sendMessage(out, playerID, "set_message_request_granted " + Integer.toString(key) + "\n");
				out.flush();
			} else {
				sendMessage(out, playerID, "set_message_request_denied\n");
				out.flush();
			}
		} catch (IOException e) {e.printStackTrace();}
	}

	private void getMessage(){
		try{
			if(!serverMessage.equals("")){
				sendMessage(out, playerID, "message " + serverMessage + "\n");
				out.flush();
			} else {
				sendMessage(out, playerID, "message No server message is set\n");
				out.flush();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
*/

}
