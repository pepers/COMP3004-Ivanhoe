package main.java;

import java.util.ArrayList;

public class Player {
	
	String username;
	int handSize, displayScore;
	ArrayList<Card> display;
	boolean ready = false; 
	
	public Player(String u){
		username = u;
		display = new ArrayList<Card>();
	}

	public void setName(String u){
		username = u;
	}

	public void toggleReady() {
		ready = !ready;
	}
}
