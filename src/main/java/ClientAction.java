package main.java;
/*
 * Actions that the Client can perform:
 * 	SetName(String name) 
 */

import java.io.Serializable;

import main.resources.Trace;

public interface ClientAction extends Serializable {
	
	// return the name of the action
	public default String getAction() {
		return this.getClass().getSimpleName();
	}
}

/*
 *  sets the Client's user name
 */
class SetName implements ClientAction {
	
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
class DrawCard implements ClientAction {

	private static final long serialVersionUID = 1L;
	
	public DrawCard () {
		Trace.getInstance().test(this, "drawing card");
	}
}

/*
 * tells when player is ready to start a game
 */
class Ready implements ClientAction {

	private static final long serialVersionUID = 1L;
	
	public Ready () {
		Trace.getInstance().test(this, "Client is ready");
	}
	
}