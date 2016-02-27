package main.java;

import java.util.ArrayList;

public class Player {
	
	String username;
	int handSize, displayScore;
	ArrayList<Card> display;
	ArrayList<Card> hand;
	int ready = 0; 
	boolean inTournament = false;
	
	public Player(String u){
		username = u;
		display = new ArrayList<Card>();
		hand = new ArrayList<Card>();
	}

	public void setName(String u){
		username = u;
	}

	public void toggleReady() {
		if(ready == 0){ready = 1;}else
		if(ready == 1){ready = 0;}
	}
	public int addHand(Card c){
		hand.add(c);
		return hand.size();
	}
	
}
