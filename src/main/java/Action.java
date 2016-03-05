package main.java;
/*
 * Actions that the Client can perform:
 * 	SetName(String name) 
 */

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
		//Trace.getInstance().test(this, this.getAction() + ": " + this.message);
	}
	
	public String getMessage() {
		return message;
	}
}

class Prompt implements Action {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public Prompt (String message) {
		this.message = message;
		//Trace.getInstance().test(this, this.getAction() + ": " + this.message);
	}
	
	public String getMessage() {
		return message;
	}
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
	
	public Card getCard(){
		return c;
	}
	
}

class StartTournament implements Action {
	private static final long serialVersionUID = 1L;
	private Card c;
	private String s;
	
	public StartTournament(String s, Card c) {
		this.c = c;
		this.s = s;
		Trace.getInstance().test(this, this.getAction());
	}
	
	public Card getCard(){
		return c;
	}
	
	public String getColour(){
		return s;
	}
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
	private boolean init = false;
	
	public SetName(String name) {
		this.name = name;
		Trace.getInstance().test(this, this.getAction() + ": " + this.name);
	}
	
	public SetName(String name, boolean init) {
		this.name = name;
		this.init = init;
		Trace.getInstance().test(this, "new name: " + this.name);
	}
	
	public boolean isInit(){
		return init;
	}
	
	public String getName() {
		return name;
	}
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

