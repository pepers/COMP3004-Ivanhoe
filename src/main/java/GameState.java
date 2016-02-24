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
			if(s.clients.get(t).ready == 2){
				players.add(s.clients.get(t));
			}
		}
		
		server = s;
		numPlayers = players.size();
		deck = new Deck();
		deck.initialize();
	}
	
	public boolean evaluate(ActionWrapper action){
		if (action.object instanceof DrawCard) {
			int n = action.origin.addHand(deck.draw());
			server.broadcast(action.origin.username + " draws a card. (" + n + ")");
			return true;
		}
		return false;
	}
}
