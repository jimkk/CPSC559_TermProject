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

public class GameThread {


	private int randomCardNumber;

	private boolean isDone = false;
	private boolean folded = false;
	private boolean handSent = false;
	private boolean turnSent = false;

	private static String serverMessage = "";
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

			StringBuffer buffer = new StringBuffer();
			String messageType = "";

			while(!isDone){



				/*if(game.getPlayerList().findPlayerByPort(playerPort).getTurn() 
				  && !turnSent){
				  out.write("message It's your turn!\n");
				  out.flush();
				  turnSent = true;
				  }*/

				if(game.getPlayerCount() > 2){
					game.beginRound();
				}
				
				if(game.isGameOn() && !handSent){
					for(int i = 0; i < game.getPlayerCount(); i++){
						PlayerNode player = game.getPlayerList().findPlayerByIndex(i);
						Card [] hand = player.getHand();
						int playerID = player.getPlayerID();
						sendMessage(out, playerID, "Game Started!");
						sendMessage(out, playerID, "message Hand: " + hand[0] + " " + hand[1] + "\n");
						//out.write("message Game Started!\n");
						//out.write("message Hand: " + hand[0] + " " + hand[1] + "\n");
						//out.flush();
					}
					handSent = true;
				}

				// game play
				// begin round
				//playerPort = socket.getPort();
				//player = game.getPlayerList().findPlayerByPort(playerPort);
				/*if (player.getBeginTurn() == true && turnSent == false) {
				  out.write("message It's now your turn...\n");
				  out.write("message You can either bet, call, or fold\n");
				  out.flush();
				  player.setBeginTurn(false);
				  turnSent = true;

				  }*/
				// notify player of their turn

				// notifyPlayer();
				if(in.ready()){
					int playerID;
					String [] messageParts;
					String contents;

					buffer = read(in);

					//if(buffer.indexOf(" ") != -1){
					//messageType = buffer.substring(0, buffer.indexOf(" "));
					messageParts = buffer.toString().split(" ");
					playerID = Integer.parseInt(messageParts[0]);
					messageType = messageParts[1];
					contents = rebuildString(messageParts, 2, messageParts.length);
					//TODO Rebuild the rest
					//} else {
					//	messageType = buffer.toString();
					//}


					// Check if it is that player's turn; if not reply accordingly
					// Note: only check if the player has requested something that cannot
					// be done if it is not his or her turn; i.e. betting, folding, calling


					switch(messageType){
						case("addplayer"):
							int stack = Integer.parseInt(messageParts[2]);
							System.out.println("ADDING NEW PLAYER");
							game.addPlayerToGame(stack, playerID);
							System.out.println("playerCount: " + game.getPlayerCount());
						case("checkTurn"):
							checkTurn(playerID);
							break;
						case("bet"):
							int betAmount = Integer.parseInt(contents);
							bet(betAmount, playerID);
							break;
						case("call"):
							call(playerID);
							break;
						case("fold"):
							fold(playerID);
							break;
						case("deal"):
							if (game.checkTurn(playerID) == false){
								 sendMessage(out, playerID, "It is not your turn");
								 break;
							}
							deal(playerID);
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
							if (debug == false) break;
							else game.getPlayerList().displayGameState();
							break;
						case("close"):

							System.out.printf("Player %d has left the game\n", playerID);
							// return the playerID
							game.removePlayerFromGame(playerID);
							isDone = true;
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
				Thread.sleep(10);
			}
		} catch(SocketException e) {
			System.err.println("Lost connection to client");
			//TODO Remove player from game
		} catch(Exception e) {e.printStackTrace();}

	}

	/**
	 * Reads in a message from the stream, stopping on a -1 or a newline character
	 * @param in The stream to read from
	 * @return StringBuffer The received message
	 */
	private StringBuffer read(InputStreamReader in){
		try{
			StringBuffer buffer = new StringBuffer();
			int c;
			while((c = in.read()) != -1){
				if(c == (int) '\n'){
					break;
				}
				buffer.append((char) c);
			}
			return buffer;
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}

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
				game.bet(playerID, betAmount);
				PlayerNode player = game.getPlayerList().findPlayerByID(playerID);
				int stack = player.getStack();
				System.out.println("This player's new stack total: " + stack);
				player.setTurn(false);
				System.out.println("No longer this players turn. Confirmation: " + player.getTurn());
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
		} catch (IOException e){e.printStackTrace();}
	}

	private void deal(int playerID){
		randomCardNumber = rand.nextInt(52) + 1;
		String deal = String.valueOf(randomCardNumber);
		System.out.printf("Dealt card from %s: %s\n", socket.getInetAddress(), deal);
		System.out.printf("Assigning card to player: %s\n", playerID);
		//attempt at integrating Card class to deal command
		//String randomSuit = determineCardSuit(randomCardNumber);

		//int finalCardValue = determineCardValue(randomCardNumber, randomSuit);
		Card randCard = new Card(randomCardNumber);														
		System.out.println("This is the card: " + randCard);

		sendMessage(out, playerID, "message Card dealt: " + randCard + "\n");
		//out.write("message Card dealt: " + randCard + "\n");
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
