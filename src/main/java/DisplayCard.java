package main.java;

public class DisplayCard extends Card{

	
	private static final long serialVersionUID = 1L;
	private int value;      // point value of the card
	private Colour colour;  // colour of the card (none is for squires(2,3) and maidens(6))
	
	public DisplayCard (int value, Colour colour) {
		this.value = value;
		this.colour = colour;
	}
	
	public DisplayCard(String text) {
		this.value = Integer.valueOf(text.split(":")[1]);
		String type = text.split(":")[0];
		if (type.equalsIgnoreCase("squire") || type.equalsIgnoreCase("maiden")){
			this.colour = new Colour(Colour.c.NONE);
		}else{
			this.colour = new Colour(Colour.c.valueOf(type.toUpperCase()));
		}
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
	public String toToolTip(){
		return toString();
	}	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisplayCard other = (DisplayCard) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (value != other.value)
			return false;
		return true;
	}
	
}
