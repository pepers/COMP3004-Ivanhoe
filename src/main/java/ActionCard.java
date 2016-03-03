package main.java;

public class ActionCard extends Card {

	private static final long serialVersionUID = 1L;
	private String name = null; // name of card
	
	// possible action cards:
	private String[] action = {
			"Adapt",
			"Break Lance",
			"Change Weapon",
			"Charge",
			"Countercharge",
			"Disgrace",
			"Dodge",
			"Drop Weapon",
			"Ivanhoe",
			"Knock Down",
			"Outmaneuver",
			"Outwit",
			"Retreat",
			"Riposte",
			"Shield",
			"Stunned",
			"Unhorse"
	};
	
	
	public ActionCard (String name) {
		for (String actionName: action) {
			if (actionName.equalsIgnoreCase(name)) {
				this.name = actionName;
			} 
		}
	}
	
	/*
	 * return name of action card
	 * returns null if invalid action card
	 */
	@Override
	public String toString() {
		return this.name;
	}
}
