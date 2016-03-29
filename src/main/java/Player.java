package main.java;

import java.awt.Color;

/*
 * info about one player
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Player implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String username;
	private int handSize = 0;
	private ArrayList<Card> hand;
	private Display display = new Display();
	
	private boolean inTournament = false;
	private boolean isStunned = false;
	private boolean addedToDisplay = false;  // if stunned, can only add one card to Display each turn
	private boolean isShielded = false;
	private ArrayList<Token> tokens = new ArrayList<Token>();  // tokens won for tournament wins
	private int id;
	private Color playerColor = Color.black;
	
	public boolean isTurn = false;
	public int ready = 0;
	
	public Color getColor(){return this.playerColor;}
	public int getHandSize(){return this.handSize;}
	public boolean getShielded(){return this.isShielded;}
	public void setShielded(boolean shield) { this.isShielded = shield; } 
	public boolean getStunned(){return this.isStunned;}
	public void setStunned(boolean stunned) { this.isStunned = stunned; } 
	public boolean getAddedToDisplay() { return this.addedToDisplay; }
	public void setAddedToDisplay(boolean added) { this.addedToDisplay = added; }
	public boolean getParticipation(){return this.inTournament;}
	public ArrayList<Card> getHand () {	return this.hand; }
	public int getId() { return this.id;	}
	public String getName() { return this.username; }
	public Display getDisplay() { return this.display; }

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
		this(u);
		this.id = id;
	}
	
	public Player(String u){
		Random r = new Random();
		playerColor = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
		username = u;
		hand = new ArrayList<Card>();
		tokens = new ArrayList<Token>();
	}

	public void reset(){
		hand = new ArrayList<Card>();
		tokens = new ArrayList<Token>();
		handSize = 0;
		inTournament = false;
		isTurn = false;
		ready = 0;
		display.clear();
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
	 * add a card to the player's hand
	 */
	public int addToHand(Card c){
		if (hand.add(c)) {
			handSize++;
		}
		return hand.size();
	}
	
	/*
	 * add a card to the player's display
	 */
	public int addToDisplay(Card c){
		if(c instanceof DisplayCard){
			display.add((DisplayCard)c);
		}else{
			return -1;
		}
		return display.size();
	}
	
	/*
	 * remove card from player's hand
	 */
	public int removeFromHand(Card c){
		if (hand.remove(c)) {
			handSize--;
		}
		return hand.size();
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
	
	/*
	 * remove a token
	 */
	public boolean removeToken (Token token) {
		if (tokens.size() < 1) { return false; }
		for (Token t : tokens) {
			if (t.equals(token)) {
				tokens.remove(t);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * return the number of tokens the player has earned
	 */
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
	 * checks if there is at least one card of a specific colour, or 
	 * supporter card in hand
	 */
	public boolean hasValidDisplayCard(Colour colour) {
		for (Card c : hand){
			if (c instanceof DisplayCard){
				if(((DisplayCard) c).getColour().equals(colour) || 
						colour.equals(Colour.c.NONE)){
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o){
		if (o == null){return false;}
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
	public ArrayList<Token> getTokens() {
		return tokens;
	}
}
