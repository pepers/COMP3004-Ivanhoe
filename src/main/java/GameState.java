package main.java;

import java.util.ArrayList;
import java.util.Iterator;

public class GameState{
	Server server = null;
	
	//Game stuff
	ArrayList<Player> players;
	Deck deck;
	int numPlayers;
	Tournament t = null;
	
	public GameState(Server s){
		//set up player array
		players = new ArrayList<Player>();
		Iterator<ServerThread> i = s.clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if(s.clients.get(t).ready){
				players.add(s.clients.get(t));
			}
		}
		
		numPlayers = players.size();
		deck = new Deck();
		deck.initialize();
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
