/**
 *	This class represents a card.
 */

public class Card{

	private int value;
	private String suit;

	public static final String [] SUITS = {"Hearts", "Diamonds", "Clubs", "Spades"};


	/**
	 * @param suit The suit of the card (Hearts, Diamonds, Clubs, or Spades)
	 * @param value The value of the card (1-13)
	 */
	public Card(String suit, int value){

		this.value = value;
		this.suit = suit;
	}

	/**
	 * Sets the value of this card object.
	 * @param suit The suit of the card (Hearts, Diamonds, Clubs, or Spades)
	 * @param value The value of the card (1-13)
	 */
	public void setValue(String suit, int value){
		this.value = value;
	}

	/**
	 * Returns the suit of this card.
	 * @return String
	 */
	public String getSuit(){
		return suit;
	}

	/**
	 * Returns the valueo of this card.
	 * @return int
	 */
	public int getValue(){
		return value;
	}

	/**
	 *	Returns the color of the current card.
	 *	@return String The color (red or black)
	 */
	public String getColor(){
		if(suit.equals("Hearts") || suit.equals("Diamonds"))
			return "red";
		else
			return "black";
	}

	/**
	 * Converts the current card to a string representation using a number value and a unicode suit character.
	 * @return String
	 */
	public String toString(){
		String numberValue;
		switch(value){
			case(14):
				numberValue = "A";
				break;
			case(11):
				numberValue = "J";
				break;
			case(12):
				numberValue = "Q";
				break;
			case(13):
				numberValue = "K";
				break;
			default:
				numberValue = Integer.toString(value);
				break;
		}

		return getUnicodeChar(suit) + " " + numberValue;
	}
	
	/**
	 * Returns the unicode character that corelates to the string passed to it.
	 * @param suit The string representation of the suit (Hearts, Diamonds, Spades, Clubs)
	 * @return String The unicode representation of the suit
	 */
	private String getUnicodeChar(String suit){
		String unicodeChar = "";

		switch(suit){
			case("Hearts"):
				unicodeChar = "♥";
				break;
			case("Diamonds"):
				unicodeChar = "♦";
				break;
			case("Spades"):
				unicodeChar = "♠";
				break;
			case("Clubs"):
				unicodeChar = "♣";
				break;
			default:
				unicodeChar = "UNKNOWN SUIT";
				break;
		}

		return unicodeChar;
	}
}
