package main.java;

public class ActionCard extends Card {

	private static final long serialVersionUID = 1L;
	private String name = null; // name of card
	private String description = null;
	
	// possible action cards:
	private String[] actionName = {
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
	
	private String[] actionDescription = {
			"Each player may only keep one card of each value in his or her display. All other cards with the same value is discarded.",
			"Target opponent discards all purple cards from his or her display.",
			"Change the tournament color from red, blue or yellow to a different one of these colors.",
			"Identify the lowest value throughout all displays. All players must discard all cards of this value from their displays.",
			"Identify the highest value throughout all displays. All players must discard all cards of this value from their displays.",
			"All players discard all supporters from their displays.",
			"Discard any one card from an opponent's display.",
			"Change the tournament color from red, blue or yellow to green.",
			"(You may play this card at any time as long as you are in a tournament.) Cancel all effects of an action card that was just played.",
			"Take a random card from an opponent's hand and add it to your own hand (without revealing the card).",
			"Each opponent must the discard their last card from their displays.",
			"Exchange a card in front of you and one in front of an opponent. This may include SHIELD and STUNNED cards.",
			"Return a card from your own display back into your hand.",
			"Take the last card of an opponent's display and add it to your own display.",
			"(Play this card face-up in front of you.) As long as you are shielded, action cards have no effect on your display.",
			"(Play this card face-up in front of an opponent in the tournament.) As long as that player is stunned, he or she may only add one card to his or her display each turn.",
			"Change the tournament color from purple to red, blue, or yellow."
	};
	
	
	public ActionCard (String name) {
		for (int i=0; i<actionName.length; i++){

			if (actionName[i].equalsIgnoreCase(name)) {
				this.name = actionName[i];
				this.description = actionDescription[i];
			} 
		}
	}
	
	/* 
	 * get description of the card's game rules
	 */
	public String getDescription(){
		return description;
	}
	
	/*
	 * return name of action card
	 * returns null if invalid action card
	 */
	@Override
	public String toString() {
		return this.name;
	}
	
	/*
	 * return tooltip of action card
	 * returns null if invalid action card
	 */
	@Override
	public String toToolTip() {
		return "<html><p width=\"180\">" +this.name + "<BR/><BR/>" + this.description+"</p></html>";
	}

	public boolean hasTargets() {
		switch(name){
		case("Adapt"):
			return false;
		case("Charge"):
			return false;
		case("Countercharge"):
			return false;
		case("Outmaneuver"):
			return false;
		case("Disgrace"):
			return false;
		case("Shield"):
			return false;
		case("Ivanhoe"):
			return false;
		default:
			return true;
		}
	}

	@Override
	public Colour getColour() {
		return new Colour(Colour.c.NONE);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ActionCard other = (ActionCard) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
