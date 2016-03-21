package main.java;

import java.io.Serializable;

public class Token implements Serializable {
	private static final long serialVersionUID = 1L;

	private Colour colour; // red, blue, green, yellow, or purple
	private String origin; // the location or festival the token came from

	public Token(Colour colour, String origin) {
		if (colour.toString().equalsIgnoreCase("None")) {
			throw new IllegalArgumentException("Token must have a colour.");
		} else {
			this.colour = colour;
			this.origin = origin;
		}
	}

	//Equals: tokens are equal if they are the same color
	@Override
	public boolean equals(Object o) {
		if (o instanceof Token) {
			if (this.colour.equals(((Token) o).colour)) {
				return true;
			}
		}
		return false;
	}

	// Returns the colour of the token plus where the player got it
	public String toString() {
		return this.colour.toString() + " token from " + origin;
	}
	
	public Colour getColour() {
		return colour;
	}
	
	public String getOrigin() {
		return origin;
	}
}
