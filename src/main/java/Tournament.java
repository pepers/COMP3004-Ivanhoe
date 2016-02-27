package main.java;

import java.io.Serializable;

import main.resources.MedievalNames;

public class Tournament implements Serializable{

	private static final long serialVersionUID = 1L;
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
