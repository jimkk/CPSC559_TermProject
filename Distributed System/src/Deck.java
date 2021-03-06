import java.util.*;

/**
 *	This class represents a deck of cards.
 */
public class Deck {	

	private ArrayList<Card> deck;

	public Deck(){
		deck = new ArrayList<Card>();
		generateDeck();
	}
	
	/**
	 * Generate a deck of cards that are randomly sorted.
	 */
	private void generateDeck(){
		Random rd = new Random();

		ArrayList<Card> sortedDeck = new ArrayList<Card>();
		for(int i = 0; i < 4; i++){
			for(int j = 1; j <= 13; j++){
				sortedDeck.add(new Card(Card.SUITS[i], j));
			}
		}
		for(int i = 0; i < 52; i++){
			int index = rd.nextInt(52-i);
			deck.add(sortedDeck.remove(index));
		}
	}

	/**
	 * Draws one card from the deck and removes it from the deck.
	 * @return Card The card on the top of the deck
	 */
	public Card Draw(){
		if(deck.size() == 0){
			return null;
		}
		Card card = deck.remove(0);
		return card;
	}

}
