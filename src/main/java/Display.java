package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Display implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ArrayList<DisplayCard> display; // keep track of cards in display
	private int score = 0;                  // score of the cards in display
	
	public Display() {
		this.display = new ArrayList<DisplayCard>();
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
		return display.get(i);
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
		if (this.display.remove(c)) {
			this.score -= c.getValue();
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * remove cards with a certain value
	 */
	public boolean removeValue (int value) {
		if (this.display.isEmpty()) { return false; }
		for (Iterator<DisplayCard> iterator = this.display.iterator(); iterator.hasNext();) {
		    DisplayCard card = iterator.next();
		    if (card.getValue() == value) {
		        iterator.remove();
		        this.score -= card.getValue();
		    }
		}
		return true;
	}

	/*
	 * remove the last played card on the display
	 */
	public boolean removeLast() {
		if (this.display.size() > 0) {
			DisplayCard d = (DisplayCard) this.display.get(this.display.size()-1);
			if (this.display.remove(this.display.size()-1) != null) {
				this.score -= d.getValue();
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
	 * determines if there are cards in the display of a specific colour
	 */
	public boolean hasColour (Colour colour) {
		if (this.display.isEmpty()) { return false; }
		for (Card c : this.display) {
			if (((DisplayCard) c).getColour().equals(colour)) {
				return true;
			}
		}
		return false;
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
