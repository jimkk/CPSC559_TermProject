public class Card{

	private int value;
	private String suit;

	public static final String [] SUITS = {"Hearts", "Diamonds", "Clubs", "Spades"};


	public Card(String suit, int value){
		
		this.value = value;
		this.suit = suit;
	}

	public void setValue(String suit, int value){
		this.value = value;
	}

	public String getSuit(){
		return suit;
	}

	public int getValue(){
		return value;
	}

	public String getColor(){
		if(suit.equals("Hearts") || suit.equals("Diamonds"))
			return "red";
		else
			return "black";
	}

	public String toString(){
		String numberValue;
		switch(value){
			case(1):
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

		return suit.substring(0,1) + numberValue;
	}
				
}
