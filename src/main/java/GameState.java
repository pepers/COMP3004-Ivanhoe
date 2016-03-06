package main.java;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable{
	private static final long serialVersionUID = 1L;	
	
	private Deck deck;
	private Tournament tnmt = null;
	private String lastColour = "none";
	private ArrayList<Player> players;
	private int turnIndex = 0;
	private int numPlayers;
	private int highScore = 0;
	
	public Tournament getTournament(){return tnmt;}
	public int getHighScore(){return highScore;}
	public int getNumPlayers(){return numPlayers;}
	public ArrayList<Player> getPlayers(){return players;}
	
	public void setHighScore(int i){highScore = i;}
	public void setTurnIndex(int i){turnIndex = i;}
	public void setNumPlayers(int i){numPlayers = i;}
	
	public GameState(){
		players = new ArrayList<Player>();
		deck = new Deck();
		deck.initialize();
	}

	public Card drawFromDeck(){
		return deck.draw();
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
			}while(!players.get(turnIndex).getParticipation());
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
	
	public boolean startTournament(Tournament t){
		if(lastColour.equals(t.getColour())){
			System.out.println("A purple tournament was just played.");
			return false;
		}
		tnmt = t;
		for (Player p : players) {
			p.setParticipation(true);
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
			if (p.getParticipation()) members.add(p);
		}
		return members;
	}
	
	public void endTournament(){
		lastColour = tnmt.getColour();
		tnmt = null;
		highScore = 0;
		for (Player p:players){
			p.setParticipation(false);
			p.clearDisplay();
		}
	}
	
	/* 
	 * calculate the highscore of all participants in tournament
	 */
	private void calcHighScore() {
		ArrayList<Player> ps = getTournamentParticipants();
		if (ps == null) { return; }
		int high = 0;
		int score = 0;
		for (Player p: ps) {
			score = p.getScore(getTournamentColour());
			if (score > high) { high = score; }
		}
		this.highScore = high;
	}

	public boolean execute(ActionCard c) {
		switch(c.toString()){
			case "Drop Weapon":
				if ((tnmt.getColour().equalsIgnoreCase("red")) ||
						(tnmt.getColour().equalsIgnoreCase("blue")) ||
						(tnmt.getColour().equalsIgnoreCase("yellow"))) {
					System.out.println("Tournament colour changed from " + tnmt.getColour() + " to green.");
					tnmt.setColour("green");
				} else {
					System.out.println("Drop Weapon card has no effect.");
				}
				break;
			case "Outmaneuver":
				ArrayList<Player> ps = getTournamentParticipants();
				for (Player p: ps) {
					p.removeLastFromDisplay();
				}
				System.out.println("All players remove the last card played on their Display.");
				break;
			case "Outwit":
				System.out.println("played an outwit card");
				break;
			default:
				return false;
		}	
		calcHighScore();
		return true;
	}
}
