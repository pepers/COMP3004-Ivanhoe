package main.java;

import java.io.Serializable;
import main.resources.MedievalNames;

public class Tournament implements Serializable{

	private static final long serialVersionUID = 1L;
	
	// possible tournament colours
	public enum Colour {
		purple, red, blue, yellow, green
	}
	
	String name;               // tournament name
	int turns;
	public String colour;      // colour of tournament
	
	public Tournament(String colour) {
		name = MedievalNames.genTournament();
		this.colour = colour;
	}
}
