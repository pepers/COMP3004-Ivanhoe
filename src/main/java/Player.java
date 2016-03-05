package main.java;

/*
 * info about one player
 */

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String username;
	int handSize, displayScore = 0;
	private ArrayList<Card> display;
	private ArrayList<Card> hand;
	int ready = 0; 
	boolean inTournament = false;
	boolean isTurn = false;
	private ArrayList<Token> tokens = new ArrayList<Token>();  // tokens won for tournament wins
	private int id;
	
	// possible tokens to win
	public enum Token {purple, red, blue, yellow, green}
	
	
	public Player(String u, int id){
		username = u;
		display = new ArrayList<Card>();
		hand = new ArrayList<Card>();
		tokens = new ArrayList<Token>();
		this.id = id;
	}
	
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
	 * check if player is in tournament
	 */
	public boolean inTournament() {
		return this.inTournament;
	}
	
	/*
	 * toggle player in tournament
	 */
	public void toggleTnmt() {
		this.inTournament = !this.inTournament;
	}

	/*
	 * toggle whether the player is ready to start a game or not
	 */
	public boolean toggleReady() {
		if(ready == 0){
			ready = 1;
			return true;
		}else if(ready == 1){
			ready = 0;
			return false;
		}
		return false;
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
	 * return the player's display score
	 */
	public int getScore(String type) {
		if(type.equals("green")){
			return this.display.size();
		}else{
			return this.displayScore;
		}
	}
	
	/*
	 * add a card to the player's hand
	 */
	public int addToHand(Card c){
		hand.add(c);
		handSize++;
		return hand.size();
	}
	public int removeFromHand(Card c){
		hand.remove(c);
		handSize--;
		return hand.size();
	}
	
	public int addToDisplay(Card c){
		display.add(c);
		DisplayCard d = (DisplayCard) c;
		displayScore += d.getValue();
		return display.size();
	}
	
	public int removeFromDisplay(Card c){
		display.remove(c);
		return display.size();
	}
	/*
	 * give token to player for tournament win
	 * return false if failed to give token to player (already has that colour)
	 */
	public boolean giveToken (Token token) {
		for (Token t: tokens) {
			if (token == t) { return false; } // player already has token
		}
		tokens.add(token); 
		return true;
	}
	
	/*
	 * list player's tokens
	 */
	public String listTokens() {
		boolean empty = true; 
		
		for (Token t: tokens) {
			if (t != null) {
				empty = false;
				break;
			}
		}
				
		if (empty) {
			return "no tokens";
		} else {
			return String.join(", ", tokens.toString());
		}
	}

	/*
	 * get a card from the player's hand
	 * return null if card not found
	 */
	public Card getCard(String sub) {
		for (Card c : hand){
			if(c.toString().equalsIgnoreCase(sub)){
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
	
	/*
	 * prints the player's display
	 */
	public boolean printDisplay(String type) {
		if (!(inTournament)) { // not in tournament
			System.out.println(username + " is not in this tournament.\n");
			return true;
		}
		if (display.isEmpty()) { return false; } // empty display
		System.out.println(username + "'s Display (" + getScore(type) + "): ");
		for (Card c: display) {
			System.out.println(c.toString());
		}
		System.out.println("");
		return true;
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return username;
	}

	public boolean hasValidDisplayCard(String colour) {
		for (Card c : hand){
			if (c instanceof DisplayCard){
				if(((DisplayCard) c).getColour().equals(colour) || colour.equals("none")){
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Card> getDisplay() {
		return display;
	}
	
	@Override
	public boolean equals(Object o){
		if (this.id == ((Player) o).id){
			return true;
		}
		if (this.username.equals(((Player) o).username)){
			return true;
		}
		return false;
	}

	public void setTurn() {
		isTurn = true;
	}
}
