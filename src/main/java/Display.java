package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Display implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ArrayList<DisplayCard> display; // keep track of cards in display
	private int score = 0;                  // score of the cards in display
	
	public Display() {
		this.display = new ArrayList<DisplayCard>();
	}
	
	/*
	 * get elements
	 */
	public ArrayList<DisplayCard> elements() {
		return display;
	}
	
	/*
	 * get the number of cards in the display
	 */
	public int size() {
		return display.size();
	}
	
	/*
	 * get a card at a specific index
	 */
	public DisplayCard get(int i) {
		if (i < display.size()) {
			return display.get(i);
		} else {
			return null;
		}
	}
	
	/*
	 * get all cards of a specific value
	 */
	public ArrayList<DisplayCard> getAll(int value) {
		if (this.display.isEmpty()) { return null; }
		ArrayList<DisplayCard> cards = new ArrayList<DisplayCard>();
		for (DisplayCard card : this.display) {
			if (card.getValue() == value) {	cards.add(card); }
		}
		return cards;
	}
	
	/*
	 * get a card by name
	 */
	public DisplayCard get(String name) {
		DisplayCard dc = null;
		if (this.display.isEmpty()) { return dc; }
		for (DisplayCard card : this.display) {
			if (card.toString().equalsIgnoreCase(name)) { return card; }
		}
		return dc;		
	}
	
	/*
	 * get last card of display
	 */
	public DisplayCard getLast() {
		DisplayCard d = null;
		if (this.display.size() > 0) {
			d = (DisplayCard) this.display.get(this.display.size()-1);
		}
		return d;
	}
	
	/*
	 * add card to display
	 */
	public boolean add (DisplayCard c) {
		if (this.display.add(c)) {
			this.score += c.getValue();
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * remove card from display
	 */
	public boolean remove (DisplayCard c) {
		if (this.display.size() == 1) { return false; } // can't remove last card
		if (this.display.remove(c)) {
			this.score -= c.getValue();
			GameState.getDeck().discard(c);
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * remove all cards of a specific colour
	 */
	public boolean removeAll (Colour colour) {
		if (this.display.isEmpty()) { return false; }
		
		// can't remove last card of display
		if ((this.display.size() == 1) && (this.display.get(0).getColour().equals(colour))) {
			return false;
		}
		
		// determine how many cards will be removed
		int numToBeRemoved = 0;
		for (DisplayCard c : this.display) {
			if (c.getColour().equals(colour)) { numToBeRemoved += 1; }
		}
		boolean saveCard = false;
		if (numToBeRemoved == this.display.size()) { saveCard = true; }
		
		// remove all cards of colour
		for (Iterator<DisplayCard> iterator = this.display.iterator(); iterator.hasNext();) {
		    DisplayCard card = iterator.next();
		    if (card.getColour().equals(colour)) {
		    	// save the first card if all cards of display are to be removed
		    	if (!saveCard) {
		    		iterator.remove();
		    		this.score -= card.getValue();
		    		GameState.getDeck().discard(card);
		    	} else {
		    		saveCard = false;
		    	}
		    }
		}
		return true;
	}
	
	/*
	 * remove cards with a certain value
	 */
	public boolean removeValue (int value) {
		if (this.display.isEmpty()) { return false; }
		
		// can't remove last card of display
		if ((this.display.size() == 1) && (this.display.get(0).getValue() == value)) {
			return false;
		}
		
		// determine how many cards will be removed
		int numToBeRemoved = 0;
		for (DisplayCard c : this.display) {
			if (c.getValue() == value) { numToBeRemoved += 1; }
		}
		boolean saveCard = false;
		if (numToBeRemoved == this.display.size()) { saveCard = true; }
		
		for (Iterator<DisplayCard> iterator = this.display.iterator(); iterator.hasNext();) {
		    DisplayCard card = iterator.next();
		    if (card.getValue() == value) {
		    	// save the first card if all cards of display are to be removed
		    	if (!saveCard) {
		    		iterator.remove();
		    		this.score -= card.getValue();
		    		GameState.getDeck().discard(card);
		    	} else {
		    		saveCard = false;
		    	}
		    }
		}
		return true;
	}

	/*
	 * remove the last played card on the display
	 */
	public boolean removeLast() {
		if (this.display.size() > 1) { // can't remove last card if only one card
			DisplayCard d = (DisplayCard) this.display.get(this.display.size()-1);
			if (this.display.remove(this.display.size()-1) != null) {
				this.score -= d.getValue();
				GameState.getDeck().discard(d);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * get the display score
	 */
	public int score(Colour colour) {
		if (colour.equals(Colour.c.GREEN)) {
			return this.display.size();
		} else {
			return this.score;
		}
	}
	
	/*
	 * clear the display
	 */
	public void clear(){
		this.display.clear();
		this.score = 0;
	}

	/*
	 * get lowest value in cards
	 * (99 if empty)
	 */
	public int lowestValue() {
		int lowest = 99;
		if (this.display.isEmpty()) { return lowest; }
		for (DisplayCard c : display) {
			if (c.getValue() < lowest) {
				lowest = c.getValue();
			}
		}
		return lowest;
	}
	
	/*
	 * get highest value in cards
	 * (0 if empty)
	 */
	public int highestValue() {
		int highest = 0;
		if (this.display.isEmpty()) { return highest; }
		for (DisplayCard c : display) {
			if (c.getValue() > highest) {
				highest = c.getValue();
			}
		}
		return highest;
	}
	
	/*
	 * checks if there is a Maiden card in Display
	 */
	public boolean hasMaiden() {
		if (this.display.isEmpty()) { return false; }
		DisplayCard maiden = new DisplayCard(6, new Colour(Colour.c.NONE));
		for (DisplayCard card : this.display) {
			if (card.equals(maiden)) { return true; }
		}
		return false;
	}
	
	/*
	 * checks if the display has a specific card
	 */
	public boolean hasCard (DisplayCard c) {
		if (this.display.isEmpty()) { return false; }
		for (DisplayCard card : this.display) {
			if (card.equals(c)) { return true; }
		}
		return false;
	}
	
	/*
	 * checks if the display has at least one card of a specific colour
	 */
	public boolean hasColour(Colour colour) {
		if (this.display.isEmpty()) { return false; }
		for (DisplayCard card : this.display) {
			if (card.getColour().equals(colour)) { return true; }
		}
		return false;
	}
	
	/*
	 * returns a set of every value in display
	 */
	public Set<Integer> getValues() {
		if (this.display.isEmpty()) { return null; }
		Set<Integer> values = new HashSet<Integer>();
		for (DisplayCard card : this.display) {
			values.add(card.getValue());
		}
		return values;
	}
	
	/*
	 * prints the display
	 * uses colour of tournament for score
	 */
	public boolean print(Colour colour) {
		if (this.display.isEmpty()) { return false; } // empty display
		System.out.println("Display (" + score(colour) + "): ");
		for (Card c: this.display) {
			System.out.println(c.toString());
		}
		System.out.println("");
		return true;
	}

	
}
