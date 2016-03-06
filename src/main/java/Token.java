package main.java;

import java.io.Serializable;

public class Token implements Serializable {
	private static final long serialVersionUID = 1L;

	private String colour;		//one of red, blue, green, yellow, purple
	private String origin;		//the location or festival the token came from

	public Token(String colour, String origin) {
		this.colour = colour;
		this.origin = origin;
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
		return String.valueOf(Character.toUpperCase(colour.charAt(0))) + colour.substring(1) + " token from " + origin;
	}
}
