package main.java;

import main.resources.MedievalNames;

public class Tournament {
	// possible card colours (none is for squires and maidens)
	public enum Colour {
		none, purple, red, blue, yellow, green
	}
	
	String name;
	int turns;
	
	public Tournament(String colour){
		name = MedievalNames.genTournament();
	}
}
