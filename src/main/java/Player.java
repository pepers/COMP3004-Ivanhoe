package main.java;

/*
 * info about one player
 */

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String username;
	private int handSize = 0;
	private int displayScore = 0;
	private ArrayList<Card> display;
	private ArrayList<Card> hand;
	
	private boolean inTournament = false;
	private boolean isStunned = false;
	private boolean isShielded = false;
	private ArrayList<Token> tokens = new ArrayList<Token>();  // tokens won for tournament wins
	private int id;
	
	public boolean isTurn = false;
	public int ready = 0;
	
	public int getHandSize(){return handSize;}
	public int getDisplayScore(){return displayScore;}
	public boolean getShielded(){return isShielded;}
	public boolean getStunned(){return isStunned;}
	public boolean getParticipation(){return inTournament;}
	public void setParticipation(boolean b){inTournament = b;}
	public int hasToken(Token token){
		int count = 0;
		for (Token t: tokens){
			if (t.equals(token)){
				count++;
			}
		}
		return count;
	}
	
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
		tokens = new ArrayList<Token>();
	}

	public void clearDisplay(){
		display.clear();
		displayScore = 0;
	}
	
	public void reset(){
		display = new ArrayList<Card>();
		hand = new ArrayList<Card>();
		tokens = new ArrayList<Token>();
		handSize = 0;
		displayScore = 0;
		inTournament = false;
		isTurn = false;
		ready = 0;
	}
	
	/*
	 * Set the player's name
	 */
	public void setName(String u){
		username = u;
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
		if (hand.add(c)) {
			handSize++;
		}
		return hand.size();
	}
	public int removeFromHand(Card c){
		if (hand.remove(c)) {
			handSize--;
		}
		return hand.size();
	}
	
	public int addToDisplay(Card c){
		if (display.add(c)) {
			DisplayCard d = (DisplayCard) c;
			displayScore += d.getValue();
		}
		return display.size();
	}
	
	public int removeFromDisplay(Card c){
		if (display.remove(c)) {
			DisplayCard d = (DisplayCard) c;
			displayScore -= d.getValue();
		}
		return display.size();
	}
	
	/*
	 * remove the last played card on the display
	 */
	public void removeLastFromDisplay() {
		if (display.size() > 0) {
			DisplayCard d = (DisplayCard) display.get(display.size()-1);
			display.remove(display.size()-1);
			displayScore -= d.getValue();
		}
	}
	
	/*
	 * give token to player for tournament win
	 * return false if failed to give token to player (already has that colour)
	 */
	public boolean giveToken (Token token) {
		for (Token t: tokens) {
			if (token.equals(t)) { return false; } // player already has token
		}
		tokens.add(token); 
		return true;
	}
	
	public int getNumTokens() {
		return tokens.size();
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
	
	/*
	 * determines if there are cards in the display of a specific colour
	 */
	public boolean hasColourInDisplay (String colour) {
		for (Card c : display) {
			if (((DisplayCard) c).getColour().equals(colour)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * checks if the display has a specific card
	 */
	public boolean displayHasCard (Card c) {
		if (display.isEmpty()) { return false; }
		for (Card card : display) {
			if (card.toString().equals(c.toString())) { return true; }
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
