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
	private ArrayList<Card> hand;
	private Display display = new Display();
	
	private boolean inTournament = false;
	private boolean isStunned = false;
	private boolean isShielded = false;
	private ArrayList<Token> tokens = new ArrayList<Token>();  // tokens won for tournament wins
	private int id;
	
	public boolean isTurn = false;
	public int ready = 0;
	
	public int getHandSize(){return this.handSize;}
	public boolean getShielded(){return this.isShielded;}
	public boolean getStunned(){return this.isStunned;}
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
		username = u;
		hand = new ArrayList<Card>();
		tokens = new ArrayList<Token>();
		this.id = id;
	}
	
	public Player(String u){
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
}
