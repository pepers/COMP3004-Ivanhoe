package main.java;

import java.io.Serializable;

public class Token implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String colour;
	String origin;
	
	public Token(String colour, String origin){
		this.colour = colour;
		this.origin = origin;
	}
	
	public String toString(){
		return String.valueOf(Character.toUpperCase(colour.charAt(0))) + colour.substring(1) + " token from " + origin;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Token){
			if (this.colour.equals(((Token) o).colour)){
				return true;
			}
		}
		return false;
	}
	
}
