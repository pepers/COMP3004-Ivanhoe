package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class GameState implements Serializable{
	
	private static final long serialVersionUID = 1L;	
	//Game stuff
	Deck deck;
	Tournament tnmt = null;
	ArrayList<Player> players;
	int numPlayers;
	int highScore = 0;
	
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
			if (p.getName().equals(name)) { return p; }
		}
		return null; // player doesn't exist, return null
	}

	public boolean setTurn (Player player) {
		if (getPlayer(player.getName()) == null) { return false; } // player not in game
		for (Player p: players) {
			if (p == player) { // found player 
				p.isTurn = true; 
			} else {
				p.isTurn = false;
			}
		}
		return true;
	}
	
	public Player getNext(){
		
		ArrayList<Player> temp;
		if(tnmt != null){
			temp = new ArrayList<Player>();
			for (Player p : players){
				if(p.inTournament){
					temp.add(p);
				}
			}
		}else{
			temp = players;
		}
		for (int i = 0; i<temp.size(); i++){
			Player p = temp.get(i);
			if(p.isTurn){
				return temp.get((i+1)%temp.size());
			}
		}
		return players.get(new Random().nextInt(players.size()));
	}

	public int addDisplay(Player player, Card card) {
		return getPlayer(player.getName()).addToDisplay(card);
	}
	
	public int removeDisplay(Player player, Card card) {
		return getPlayer(player.getName()).removeFromDisplay(card);
	}
	
	public int addHand(Player player, Card card) {
		return getPlayer(player.getName()).addToHand(card);
	}
	
	public int removeHand(Player player, Card card) {
		return getPlayer(player.getName()).removeFromHand(card);
	}
	
	public String getTournamentColor(){
		if(tnmt == null){
			return "none";
		}else{
			return tnmt.colour;
		}
	}
	
	public ArrayList<Player> getTournamentParticipants(){
		ArrayList<Player> members = new ArrayList<Player>();
		for (Player p:players){
			if (p.inTournament) members.add(p);
		}
		return members;
	}
}
