package main.java;

public class ActionCard extends Card {
	
	private String action;  // card name and action of the card
	
	public ActionCard (String action) {
		this.action = action;
	}
	public String toString(){
		return ("action card");
	}
}
