package main.java;

import java.io.Serializable;

public class Colour implements Serializable {
	
	private static final long serialVersionUID = 1L;

	// possible card colours
	public enum c {NONE, PURPLE, RED, BLUE, YELLOW, GREEN}
	
	private Colour.c colour = c.NONE;  // the selected colour
	
	public Colour () {
		// empty constructor, colour is NONE by default
	}
	
	public Colour (Colour.c colour) {
		this.colour = colour;
	}
	
	// Warning: will throw error if parameter was not a colour
	public Colour (String strCol) {
		boolean colourAssigned = false;
		for (c col: c.values()) {
			if (col.toString().equalsIgnoreCase(strCol)) {
				this.colour = col;
				colourAssigned = true;
				break;
			}
		}
		if (colourAssigned == false) {
			throw new IllegalArgumentException("Colour not assigned from String:'" + strCol + "'");
		}
	}
	
	public Colour.c get() { return this.colour; }               // return the colour
	public void set (Colour.c colour) { this.colour = colour; } // set the colour
	
	/*
	 * return true if colour is NONE
	 */
	public boolean isNone() {
		if (colour.equals(c.NONE)) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * colour as a string, only first letter capitalized
	 */
	public String toString() { 
		String col = colour.toString();
		return col.substring(0, 1) + col.substring(1).toLowerCase();
	} 
	
	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (this.toString().equalsIgnoreCase(obj.toString())) return true;
		if (this.getClass() != obj.getClass()) return false;
		Colour col = (Colour) obj ;
		return this.colour.equals(col.get());
	}
}
