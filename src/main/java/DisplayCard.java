package main.java;

public class DisplayCard extends Card{

	
	private static final long serialVersionUID = 1L;
	private int value;      // point value of the card
	private Colour colour;  // colour of the card
	
	// possible card colours (none is for squires and maidens)
	public enum Colour {none, purple, red, blue, yellow, green}
	
	public DisplayCard (int value, DisplayCard.Colour colour) {
		this.value = value;
		this.colour = colour;
	}
	
	public String toString(){
		return (colour.name() + ":" + value);
	}

	public String getColour() {
		return colour.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		Card c = (Card) o;
		return c.toString().equals(this.toString());
	}
}
