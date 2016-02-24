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
 *  sets the Client's user name
 */
class SetName implements Action {
	
	private static final long serialVersionUID = 1L;
	private String name;  // user's name
	
	public SetName(String name) {
		this.name = name;
		Trace.getInstance().test(this, "new name: " + this.name);
	}
	
	public String getName() {
		return name;
	}
}
	
/*
 * draws a card from the deck
 */
class DrawCard implements Action {

	private static final long serialVersionUID = 1L;
	
	public DrawCard () {
		Trace.getInstance().test(this, "drawing card");
	}
}

/*
 * tells when player is ready to start a game
 */
class Ready implements Action {

	private static final long serialVersionUID = 1L;
	
	public Ready () {
		Trace.getInstance().test(this, "Client is ready");
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
	}
	
	public String getMessage() {
		return message;
	}
}
