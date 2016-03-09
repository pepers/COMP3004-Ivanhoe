package main.java;

public class Colour {
	
	// possible card colours
	public enum c {NONE, PURPLE, RED, BLUE, YELLOW, GREEN}
	
	private Colour.c colour = c.NONE;  // the selected colour
	
	public Colour () {
		// empty constructor, colour is NONE by default
	}
	
	public Colour (Colour.c colour) {
		this.colour = colour;
	}
	
	public Colour.c get() { return this.colour; }               // return the colour
	public void set (Colour.c colour) { this.colour = colour; } // set the colour
	
	public String toString() { return colour.toString().toLowerCase(); } // colour as a string
	
	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (this.equals(obj)) return true;
		if (this.getClass() != obj.getClass()) return false;
		Colour col = (Colour) obj ;
		return this.colour.equals(col.get());
	}
}
