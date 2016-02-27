package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class GameState implements Serializable{
	
	private static final long serialVersionUID = 1L;	
	//Game stuff
	Deck deck;
	Tournament tnmt = null;
	ArrayList<Player> players;
	int numPlayers;
	
	public GameState(){
		players = new ArrayList<Player>();
		deck = new Deck();
		deck.initialize();
	}
	
	public void addPlayer(Player p){
		players.add(p);
		numPlayers++;
	}
}
