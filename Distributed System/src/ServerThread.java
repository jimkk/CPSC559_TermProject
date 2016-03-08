import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

public class ServerThread implements Runnable{

	
	private int randomCardNumber;
	
	private int playerPort;
	private int returnCode;
	private String playerIP;
	
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

	public ServerThread(Socket socket, GameManager game){
		this.socket = socket;
		this.game = game;

		//this.game.setTurnToCurrentPlayer(playerPort);
	}

	public void run(){
		try{
			//if (game == null) {
			//	game = new GameManager();
			//}

						

			int playerNumber;
			int playerID;
			
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream());
			InputStreamReader in = new InputStreamReader(bufIn);
			BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
			OutputStreamWriter out = new OutputStreamWriter(bufOut);

			//if(playerCount >= 2){
			//	System.out.println("New client rejected due to game being full");
			//	out.write("full");
			//	out.flush();
			//	Thread.sleep(5000);
			//	return;
			//}

			playerPort = socket.getPort();

			

			playerIP = socket.getInetAddress().getHostAddress();
			returnCode = game.addPlayerToGame(1000, playerPort, playerIP);		//TODO Set custom stack amount
			if (returnCode == -1) {
				out.write("Game is full; connection denied");
				out.flush();
				socket.close();
				
			}
			System.out.printf("New Client Connected, IP=%s, Port=%d\n", socket.getInetAddress(), socket.getPort());
			game.getPlayerList().displayGameState();
			  

			StringBuffer buffer = new StringBuffer();
			String messageType = "";

			while(!isDone){

				if(game.getPlayerList().findPlayerByPort(playerPort).getTurn() && !turnSent){
					out.write("message It's your turn!\n");
					out.flush();
					turnSent = true;
				}
				
				if(game.isGameOn() && !handSent){
					Card [] hand = game.getPlayerList().findPlayerByPort(playerPort).getHand();
					out.write("message Game Started!\n");
					out.write("message Hand: " + hand[0] + " " + hand[1] + "\n");
					out.flush();
					handSent = true;
				}
				
				if(in.ready()){
					buffer = read(in);
					if(buffer.indexOf(" ") != -1){
						messageType = buffer.substring(0, buffer.indexOf(" "));
					} else {
						messageType = buffer.toString();
					}
					
					// Check if it is that player's turn; if not reply accordingly
					// Note: only check if the player has requested something that cannot
					// be done if it is not his or her turn; i.e. betting, folding, calling
					playerPort = socket.getPort();
					
					switch(messageType){
									case("checkTurn"):
			            	if (game.checkTurn(playerPort) == false) {
			            		System.out.println("Currently not this player's turn.");
			            		
			            		out.write("It's not your turn");
			            		out.flush();
			            		
			            		break;
			            	}
										else
											System.out.println("It is your turn.");
										break;
			            case("bet"):
			            	if (game.checkTurn(playerPort) == false) {
			            		System.out.println("Currently not this player's turn; cannot place bet yet");
			            		
			            		out.write("It's not your turn, you cannot place a bet yet.");
			            		out.flush();
			            		
			            		break;
			            	}
			            	String betAmount = buffer.substring(buffer.indexOf(" "));
			       			System.out.printf("Bet amount from %s: %s\n", socket.getInetAddress(), betAmount);
			
			       			playerID = game.getPlayerList().findPlayerByPort(socket.getPort(), "Player ID");
			       			game.bet(playerID, Integer.parseInt(betAmount.trim()));
									PlayerNode player = game.getPlayerList().findPlayerByPort(socket.getPort());
									int stack = player.getStack();
									System.out.println("This player's new stack total: " + stack);
									player.setTurn(false);
									System.out.println("No longer this players turn. Confirmation: " + player.getTurn());
							break;
			            case("call"):
			            	if (game.checkTurn(playerPort) == false) {
			            		System.out.println("Currently not this player's turn; cannot call another player's bet");
			            		
			            		out.write("It's not your turn, you cannot call another player's bet yet");
			            		out.flush();
			            		
			            		break;
			            	}
			            	break;
			            case("fold"):
			            	if (game.checkTurn(playerPort) == false) {
			            		System.out.println("Currently not this player's turn; cannot fold until it is");
			            		
			            		out.write("It's not your turn, you cannot fold until it is");
			            		out.flush();
			            		
			            		break;
			            	}
			            	break;
						case("deal"):
							if (game.checkTurn(playerPort) == false) break;
							//String deal = buffer.substring(buffer.indexOf(" "));
							randomCardNumber = rand.nextInt(52) + 1;
							String deal = String.valueOf(randomCardNumber);
							System.out.printf("Dealt card from %s: %s\n", socket.getInetAddress(), deal);
							playerNumber = game.getPlayerList().findPlayerByPort(socket.getPort(), "Player Number");
							System.out.printf("Assigning card to player: %s\n", playerNumber);
							//attempt at integrating Card class to deal command
							String randomSuit = determineCardSuit(randomCardNumber);

							int finalCardValue = determineCardValue(randomCardNumber, randomSuit);
							Card randCard = new Card(randomSuit, finalCardValue);
              

							//Card randCard = new Card(randomSuit, randomCardNumber);
							//System.out.println("This is the card: " + randCard.getSuit() + randCard.getValue());
							System.out.println("This is the card: " + randCard + " with random number: " + randomCardNumber);


							out.write("message Card dealt: " + randCard + "\n");
							out.flush();
							break;
						case("message"):
							String message = buffer.substring(buffer.indexOf(" "));
							System.out.printf("Message from %s: %s\n", socket.getInetAddress(), message);
							break;
						case("set_message_request"):
							if(!serverMessageLock){
								serverMessageLock = true;
								key = rand.nextInt(10000);
								out.write("set_message_request_granted " + Integer.toString(key) + "\n");
								out.flush();
							} else {
								out.write("set_message_request_denied\n");
								out.flush();
							}
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
							if(!serverMessage.equals("")){
								out.write("message " + serverMessage + "\n");
								out.flush();
							} else {
								out.write("message No server message is set\n");
								out.flush();
							}
							break;
						case("display game"):
							if (debug == false) break;
							else game.getPlayerList().displayGameState();
						case("close"):
							
							System.out.println("Socket closed at client's request");
							// return the playerID
							playerID = game.getPlayerList().findPlayerByPort(socket.getPort(), "Player ID");
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

		} catch(Exception e) {e.printStackTrace();}

	}

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

}
