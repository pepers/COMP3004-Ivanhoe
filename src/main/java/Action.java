package main.java;

import java.io.Serializable;

import main.resources.Trace;

public interface Action extends Serializable {
	
	// return the name of the action
	public default String getAction() {
		return this.getClass().getSimpleName();
	}
}

/*
 * chat messages from Client
 */
class Chat implements Action {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public Chat (String message) {
		this.message = message;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public String getMessage() {return message;}
}
/*
 *  intervening chat that is evaluated immediately
 */
class Prompt implements Action {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public Prompt (String message) {
		this.message = message;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public String getMessage() {return message;}
}

/*
 * player ends their turn
 */
class EndTurn implements Action {

	private static final long serialVersionUID = 1L;
	
	public EndTurn () {
		Trace.getInstance().test(this, this.getAction());
	}
}

/*
 * when a player plays card 
 */
class Play implements Action {
	private static final long serialVersionUID = 1L;
	private Card c;
	
	public Play(Card c) {
		this.c = c;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public Card getCard(){return c;}
}

class StartTournament implements Action {
	private static final long serialVersionUID = 1L;
	private Card card;
	private Colour colour;
	
	public StartTournament(Colour colour, Card card) {
		this.card = card;
		this.colour = colour;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public Card getCard(){return card;}
	public Colour getColour(){return colour;}
}
/*
 * tells when player is ready to start a game
 */
class Ready implements Action {
	private static final long serialVersionUID = 1L;
	
	public Ready () {
		Trace.getInstance().test(this, this.getAction());
	}
}

/*
 *  sets the Client's user name
 */
class SetName implements Action {
	
	private static final long serialVersionUID = 1L;
	private String name;  // user's name
	
	public SetName(String name) {
		this.name = name;
		Trace.getInstance().test(this, this.getAction() + ": " + this.name);
	}
	
	public String getName() {return name;}
}

/*
 * tells when player wants to withdraw from tournament
 */
class Withdraw implements Action {
	private static final long serialVersionUID = 1L;
	
	public Withdraw() {
		Trace.getInstance().test(this, this.getAction());
	}
}

