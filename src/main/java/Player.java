package main.java;

import java.util.ArrayList;

public class Player {
	
	String username;
	int handSize, displayScore;
	ArrayList<Card> display;
	
	public Player(String u){
		username = u;
		display = new ArrayList<Card>();
	}

	public void setName(String u){
		username = u;
	}
}
