package main.java;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable{
	
	private static final long serialVersionUID = 1L;	
	//Game stuff
	Deck deck;
	Tournament tnmt = null;
	private String lastColour = "none";
	ArrayList<Player> players;
	int turnIndex = 0;
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
	
	public void setTurnIndex(int i){
		turnIndex = i;
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
	
	public Player nextTurn(){
		if(tnmt == null){
			players.get(turnIndex).isTurn = false;
			turnIndex++;
			if(turnIndex >= players.size()){
				turnIndex = 0;
			}
		}else{
			do{
				players.get(turnIndex).isTurn = false;
				turnIndex++;
				if(turnIndex >= players.size()){
					turnIndex = 0;
				}
			}while(!players.get(turnIndex).inTournament);
		}
		players.get(turnIndex).isTurn=true;
		return players.get(turnIndex);
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
	public boolean startTournament(Tournament t){
		if(lastColour.equals(t.getColour())){
			System.out.println("A purple tournament was just played.");
			return false;
		}
		tnmt = t;
		for (Player p : players) {
			p.inTournament = true;
		}
		setLastColour(t.getColour());
		return true;
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
		lastColour = "none";
		highScore = 0;
		for (Player p:players){
			p.inTournament = false;
			p.displayScore = 0;
			p.getDisplay().clear();
		}
	}

	public boolean execute(ActionCard c) {
		switch(c.toString()){
			case "Outwit":
				System.out.println("played an outwit card");
				return true;
			default:
				return false;
		}
	}
}
