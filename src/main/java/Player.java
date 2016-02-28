package main.java;

/*
 * info about one player
 */

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String username;
	int handSize, displayScore;
	ArrayList<Card> display;
	ArrayList<Card> hand;
	int ready = 0; 
	boolean inTournament = false;
	
	public Player(String u){
		username = u;
		display = new ArrayList<Card>();
		hand = new ArrayList<Card>();
	}

	/*
	 * Set the player's name
	 */
	public void setName(String u){
		username = u;
	}

	/*
	 * toggle whether the player is ready to start a game or not
	 */
	public void toggleReady() {
		if(ready == 0){ready = 1;}else
		if(ready == 1){ready = 0;}
	}
	
	/*
	 * return a string representation of the player's ready state
	 */
	public String getReadyState() {
		switch (ready) {
			case 0: return "waiting";
			case 1: return "ready";
			case 2: return "in game";
			default: return "unknown";
		}
	}
	
	/*
	 * add a card to the player's hand
	 */
	public int addHand(Card c){
		hand.add(c);
		return hand.size();
	}

	/*
	 * get a card from the player's hand
	 * return null if card not found
	 */
	public Card getCard(String sub) {
		for (Card c : hand){
			if(c.toString().equals(sub)){
				return c;
			}
		}
		return null;
	}
	
	/*
	 * returns cards in hand
	 */
	public ArrayList<Card> getHand () {
		return hand;
	}
	
}
