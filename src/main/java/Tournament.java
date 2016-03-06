package main.java;

import java.io.Serializable;
import main.resources.MedievalNames;

public class Tournament implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String context;			// Location, festival of tournament
	private String name;            // Name of Tournament
	private String colour;    		// Display colour of tournament
	
	public Tournament(String colour){
		context = MedievalNames.genContext();
		this.name = MedievalNames.genTrinket(context);
		this.colour = colour;
	}
	
	//getters
	public String getColour () {return this.colour;}
	public String getContext (){return context;}
	public String getName (){return name;}
	
	//setters
	public void setColour (String colour) {this.colour = colour;}
}
