package main.java;

import java.io.Serializable;
import java.util.ArrayList;

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
 * server gameplay messages
 */
class Info implements Action {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public Info (String message) {
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
	private ArrayList<Object> options;
	
	public Prompt (String message, ArrayList<Object> options) {
		this.message = message;
		this.options = options;
		Trace.getInstance().test(this, this.getAction());
	}
	public Prompt (String message) {
		this.message = message;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public String getMessage() {return message;}
	public ArrayList<Object> getOptions() { return options;}
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

class EndGame implements Action {

	private static final long serialVersionUID = 1L;
	private Player winner;
	
	public EndGame (Player winner) {
		this.winner = winner;
		Trace.getInstance().test(this, this.getAction());
	}
	public Player getWinner(){return winner;}
}

/*
 * when a player plays card 
 */
class Play implements Action {
	private static final long serialVersionUID = 1L;
	private Card c;
	private ArrayList<Colour> colours = null;
	private ArrayList<Player> opponents = null;
	private ArrayList<Card> cards = null; // (Outwit: given card = [0], taken card = [1])
	
	
	public Play(Card c) {
		this.c = c;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public Play(Card c, ArrayList<Colour> colours, ArrayList<Player> opponents, ArrayList<Card> cards) {
		this.c = c;
		this.colours = colours;
		this.opponents = opponents;
		this.cards = cards;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public Card getCard(){return this.c;}
	public ArrayList<Colour> getColours(){return this.colours;}
	public ArrayList<Player> getOpponents(){return this.opponents;}
	public ArrayList<Card> getCards(){return this.cards;}
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

