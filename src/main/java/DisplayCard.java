package main.java;

public class DisplayCard extends Card{

	
	private static final long serialVersionUID = 1L;
	private int value;      // point value of the card
	private Colour colour;  // colour of the card (none is for squires(2,3) and maidens(6))
	
	public DisplayCard (int value, Colour colour) {
		this.value = value;
		this.colour = colour;
	}
	
	public Colour getColour() { 
		return this.colour; 
	}
	
	@Override
	public String toString(){
		// SQUIRE
		if ((this.colour.toString().equalsIgnoreCase("None")) && 
				((this.value == 2) || (this.value == 3))) {
			return "Squire:" + this.value;
			
		// MAIDEN
		} else if ((this.colour.toString().equalsIgnoreCase("None")) 
				&& (this.value == 6)) {
			return "Maiden:" + this.value;
			
		// COLOUR CARDS
		} else {
			return (this.colour.toString() + ":" + this.value);
		}
	}

	/*
	 * get the display value
	 */
	public int getValue () {
		return this.value;
	}
	
	@Override
	public boolean equals(Object o) {
		Card c = (Card) o;
		return c.toString().equals(this.toString());
	}
	
	@Override
	public String toToolTip(){
		return toString();
	}
}
