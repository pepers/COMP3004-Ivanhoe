package main.java;

public class DisplayCard extends Card{

	
	private static final long serialVersionUID = 1L;
	private int value;      // point value of the card
	private Colour colour;  // colour of the card
	
	// possible card colours (none is for squires(2,3) and maidens(6))
	public enum Colour {none, purple, red, blue, yellow, green}
	
	public DisplayCard (int value, DisplayCard.Colour colour) {
		this.value = value;
		this.colour = colour;
	}
	
	public String toString(){
		// SQUIRE
		if ((this.colour.name().equals("none")) && ((this.value == 2) || (this.value == 3))) {
			return "squire:" + this.value;
			
		// MAIDEN
		} else if ((this.colour.name().equals("none")) && (this.value == 6)) {
			return "maiden:" + this.value;
			
		// COLOUR CARDS
		} else {
			return (this.colour.name() + ":" + this.value);
		}
	}

	public String getColour() {
		return colour.toString();
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
}
