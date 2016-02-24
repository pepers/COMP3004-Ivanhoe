package main.java;

import java.util.ArrayList;

public class GameState{
	Server server = null;
	
	//Game stuff
	ArrayList<Player> players;
	Deck deck;
	int numPlayers;
	Tournament t = null;
	
	public GameState(Server s){
		players = (ArrayList<Player>) s.clients.values();
		numPlayers = players.size();
		deck = new Deck();
	}
	
	public boolean evaluate(ActionWrapper action){
		if (action.object instanceof DrawCard) {
			action.origin.addHand(deck.draw());
			return true;
		}
		System.out.println("Game got something else");
		return false;
	}
}
