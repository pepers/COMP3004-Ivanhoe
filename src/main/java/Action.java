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
		Trace.getInstance().test(this, this.getClass().getSimpleName() + ": " + this.message);
	}
	
	public String getMessage() {
		return message;
	}
}

/*
 * draws a card from the deck
 */
class DrawCard implements Action {

	private static final long serialVersionUID = 1L;
	
	public DrawCard () {
		Trace.getInstance().test(this, this.getClass().getSimpleName());
	}
}

/*
 * lists the other players in the game
 */
class List implements Action {
	private static final long serialVersionUID = 1L;
	
	public List () {
		Trace.getInstance().test(this, this.getClass().getSimpleName());
	}
}

/*
 * tells when player is ready to start a game
 */
class Ready implements Action {
	private static final long serialVersionUID = 1L;
	
	public Ready () {
		Trace.getInstance().test(this, this.getClass().getSimpleName());
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
		Trace.getInstance().test(this, this.getClass().getSimpleName() + ": " + this.name);
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
		Trace.getInstance().test(this, this.getClass().getSimpleName());
	}
	
}