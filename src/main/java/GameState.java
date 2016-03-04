package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class GameState implements Serializable{
	
	private static final long serialVersionUID = 1L;	
	//Game stuff
	Deck deck;
	Tournament tnmt = null;
	private String lastColour = null;
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
	
	/*
	 * get a player by their id
	 */
	public Player getPlayer (int id) {
		for (Player p: players) {
			if (p.getId() == id) { return p; }
		}
		return null; // player doesn't exist, return null
	}
	
	/*
	 * get a player by their player object
	 */
	public Player getPlayer (Player player) {
		for (Player p: players) {
			if (p.equals(player)) { return p; }
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
	
	
	public Tournament getTournament(){
		return tnmt;
	}
	public void startTournament(Tournament t){
		tnmt = t;
		for (Player p : players) {
			p.inTournament = true;
		}
		setLastColour(t.getColour());
	}
	/*
	 * get colour of current tournament
	 */
	public String getTournamentColour(){
		if(tnmt == null){
			return "none";
		}else{
			return tnmt.getColour();
		}
	}
	
	/*
	 * get colour of last tournament
	 */
	public String getLastColour() {
		if (this.lastColour == null) {
			return "none";
		} else {
			return lastColour.toString();
		}
	}
	
	/*
	 * set colour of last tournament
	 */
	public void setLastColour(String colour) {
		this.lastColour = colour;
	}
	
	public ArrayList<Player> getTournamentParticipants(){
		ArrayList<Player> members = new ArrayList<Player>();
		for (Player p:players){
			if (p.inTournament) members.add(p);
		}
		return members;
	}
	
	public void endTournament(){
		tnmt = null;
		highScore = 0;
		for (Player p:players){
			p.inTournament = false;
			p.displayScore = 0;
			p.getDisplay().clear();
		}
	}
}
