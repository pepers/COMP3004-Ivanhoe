package main.java;

import java.io.Serializable;
import main.resources.MedievalNames;

public class Tournament implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String context;	// Location, festival of tournament
	private String name;    // Name of Tournament
	private Colour colour;  // Display colour of tournament
	
	public Tournament(Colour colour){
		if (colour.equals(new Colour(Colour.c.NONE))) {
			throw new IllegalArgumentException("Tournament must have a colour.");
		} else {
			context = MedievalNames.genContext();
			this.name = MedievalNames.genTrinket(context);
			this.colour = colour;
		}
	}
	
	//getters
	public Colour getColour () {return this.colour;}
	public String getContext (){return context;}
	public String getName (){return name;}
	
	//setters
	public boolean setColour (Colour colour) {
		if (colour.equals(new Colour(Colour.c.NONE))) {
			throw new IllegalArgumentException("Can't set Tournament colour to NONE");
		} else {
			this.colour = colour;
			return true;
		}
	}
}
