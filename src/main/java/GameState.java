package main.java;

import java.io.Serializable;
import java.util.ArrayList;

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

	/*
	 * add a player to the current game
	 */
	public void addPlayer(Player p){
		players.add(p);
		numPlayers++;
	}
	
	
	/*
	 * get a player by their user name
	 */
	public Player getPlayer (String name) {
		for (Player p: players) {
			if (p.username == name) { return p; }
		}
		return null; // player doesn't exist, return null
	}
	
	/*
	 * set who's turn it is
	 */
	public boolean setTurn (Player player) {
		if (getPlayer(player.username) == null) { return false; } // player not in game
		for (Player p: players) {
			if (player.equals(p)) { // found player 
				p.isTurn = true; 
			} else {
				p.isTurn = false;
			}
		}
		return true;
	}
}
