package main.java;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable{
	private static final long serialVersionUID = 1L;	
	
	private Deck deck;
	private Tournament tnmt = null;
	private Colour lastColour = new Colour(); // initialized as NONE
	private ArrayList<Player> players;
	private int turnIndex = 0;
	private int numPlayers;
	private int highScore = 0;
	
	public Tournament getTournament(){return tnmt;}
	public int getHighScore(){return highScore;}
	public int getNumPlayers(){return numPlayers;}
	public int getTurnIndex(){return turnIndex;}
	public ArrayList<Player> getPlayers(){return players;}
	public Colour getLastColour() { return this.lastColour; }
	
	public void setHighScore(int i){highScore = i;}
	public void setTurnIndex(int i){turnIndex = i;}
	public void setNumPlayers(int i){numPlayers = i;}
	public void setLastColour(Colour colour) { this.lastColour = colour; }
	
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
	
	public ArrayList<Player> getTournamentParticipants(){
		ArrayList<Player> members = new ArrayList<Player>();
		for (Player p:players){
			if (p.getParticipation()) members.add(p);
		}
		return members;
	}
	
	public void endTournament(){
		lastColour = getTournament().getColour();
		tnmt = null;
		highScore = 0;
		for (Player p:players){
			p.setParticipation(false);
			p.clearDisplay();
		}
	}
	
	/* 
	 * determine if a player has the high score
	 * (their display score is greater than all other display scores)
	 */
	public boolean hasHighScore (Player p) {
		ArrayList<Player> ps = getTournamentParticipants();
		if (ps == null) { return false; }
		if (ps.remove(p)) { // remove self from temp list of players in tournament
			int high = 0;
			int score = 0;
			for (Player player : ps) {
				score = player.getScore(getTournament().getColour());
				if (score > high) { high = score; }
			}
			if (p.getScore(getTournament().getColour()) > high) { return true; } 
		}
		return false;
	}

	/* 
	 * deal with Action Cards' special abilities
	 */
	public boolean execute(ActionCard c) {
		ArrayList<Player> ps;
		switch(c.toString()){
			case "Disgrace":
				ps = getTournamentParticipants();
				Card s2 = new DisplayCard(2, new Colour(Colour.c.NONE));
				Card s3 = new DisplayCard(3, new Colour(Colour.c.NONE));
				Card m6 = new DisplayCard(6, new Colour(Colour.c.NONE));
				for (Player p: ps) {
					while (p.hasColourInDisplay("none")) { // player has supporters in display
						p.removeFromDisplay(s2); // squire:2
						p.removeFromDisplay(s3); // squire:3
						p.removeFromDisplay(m6); // maiden:6
					}
					
				}
				System.out.println("All players remove all their supporters from their Display.");
				break;
			case "Drop Weapon":
				String tcol = getTournament().getColour().toString();
				if ((tcol.equalsIgnoreCase("red")) ||
						(tcol.equalsIgnoreCase("blue")) ||
						(tcol.equalsIgnoreCase("yellow"))) {
					System.out.println("Tournament colour changed from " + tcol + " to green.");
					tnmt.setColour(new Colour(Colour.c.GREEN));
				} else {
					System.out.println("Drop Weapon card has no effect.");
				}
				break;
			case "Outmaneuver":
				ps = getTournamentParticipants();
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
		return true;
	}
}
