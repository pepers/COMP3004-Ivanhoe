package main.java;

import java.io.Serializable;
import java.util.ArrayList;

import main.resources.MedievalNames;

public class Tournament implements Serializable{

	private static final long serialVersionUID = 1L;
	
	// possible card colours (none is for squires and maidens)
	public enum Colour {
		none, purple, red, blue, yellow, green
	}
	
	String name;               // tournament name
	int turns;
	public String colour;      // colour of tournament
	
	public Tournament(String colour) {
		name = MedievalNames.genTournament();
		this.colour = colour;
	}
}
