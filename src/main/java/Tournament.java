package main.java;

import java.io.Serializable;
import main.resources.MedievalNames;

public class Tournament implements Serializable{

	private static final long serialVersionUID = 1L;
	
	// possible tournament colours
	public enum Colour {
		purple, red, blue, yellow, green
	}
	String context;
	String name;               // tournament name
	int turns;
	private String colour;     // colour of tournament
	
	public Tournament(String colour){
		context = MedievalNames.genContext();
		name = MedievalNames.genTrinket(context);
		this.colour = colour;
	}
	
	/*
	 * get colour of tournament
	 */
	public String getColour () {
		return this.colour;
	}

	public String getContext() {
		return context;
	}
	
	
}
