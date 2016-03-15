package main.java;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable{
	private static final long serialVersionUID = 1L;	
	
	private static Deck deck;
	private Tournament tnmt = null;
	private Colour lastColour = new Colour(); // initialized as NONE
	private ArrayList<Player> players;
	private int turnIndex = 0;
	private int numPlayers;
	private int highScore = 0;
	
	public static Deck getDeck() {return deck;}
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

	public boolean addDisplay(Player player, DisplayCard card) {
		return getPlayer(player.getName()).getDisplay().add(card);
	}

	public boolean removeDisplay(Player player, DisplayCard card) {
		return getPlayer(player.getName()).getDisplay().remove(card);
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
			p.getDisplay().clear();
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
				score = player.getDisplay().score(getTournament().getColour());
				if (score > high) { high = score; }
			}
			if (p.getDisplay().score(getTournament().getColour()) > high) { return true; } 
		}
		return false;
	}

	/* 
	 * deal with Action Cards' special abilities
	 */
	public boolean execute(ActionWrapper action, Server s) {
		ArrayList<Player> ps;
		ActionCard c = (ActionCard) ((Play) action.object).getCard(); // the Action Card to be played
		Server server = s;
		CommandInterface prompt; // part of Command Pattern
		CommandInvoker invoker = new CommandInvoker(); // part of Command Pattern
		String clientInput = null; // client's input after prompted
		Player target = null; // target opponent
		DisplayCard dc = null; // display card retrieved
		
		switch(c.toString()){
			case "Break Lance":
				prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					target = getPlayer(clientInput);
					if (target != null) { break; }
				}
				target.getDisplay().removeAll(new Colour(Colour.c.PURPLE));
				System.out.println("Removed all Purple cards from " + clientInput + "'s Display.");
				break;
			case "Change Weapon":
				if (!(getTournament().getColour().equals(Colour.c.RED) ||
						getTournament().getColour().equals(Colour.c.BLUE) ||
						getTournament().getColour().equals(Colour.c.YELLOW))) {
					System.out.println("Tournament is not Red, Blue, or Yellow. Can't change weapon.");
					return false;
				}
				prompt = new PromptCommand(server, "Change tournament to which colour? (red, blue, yellow)", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					if ((clientInput.equalsIgnoreCase("red")) ||
						(clientInput.equalsIgnoreCase("blue")) ||
						(clientInput.equalsIgnoreCase("yellow"))) {
					break;
					}
				}
				this.lastColour = getTournament().getColour();
				getTournament().setColour(new Colour(clientInput));
				System.out.println("Tournament colour changed to " + getTournament().getColour().toString());
				break;
			case "Charge":
				int lowest = 99;
				ps = getTournamentParticipants();
				for (Player p : ps) {
					if (p.getDisplay().lowestValue() < lowest) {
						lowest = p.getDisplay().lowestValue();
					}
				}
				if (lowest == 99) {
					System.out.println("No cards in Displays, the lowest valued cards were not removed.");
				} else {
					for (Player p : ps) {
						p.getDisplay().removeValue(lowest);
					}
					System.out.println("All Display Cards of the value " + lowest + " were removed.");
				}
				break;
			case "Countercharge":
				int highest = 0;
				ps = getTournamentParticipants();
				for (Player p : ps) {
					if (p.getDisplay().highestValue() > highest) {
						highest = p.getDisplay().highestValue();
					}
				}
				if (highest == 0) {
					System.out.println("No cards in Displays, the highest valued cards were not removed.");
				} else {
					for (Player p : ps) {
						p.getDisplay().removeValue(highest);
					}
					System.out.println("All Display Cards of the value " + highest + " were removed.");
				}
				break;
			case "Disgrace":
				ps = getTournamentParticipants();
				for (Player p: ps) {
					p.getDisplay().removeAll(new Colour(Colour.c.NONE));
				}
				System.out.println("All players remove all their supporters from their Display.");
				break;
			case "Dodge":
				prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					target = getPlayer(clientInput);
					if (target != null) { break; }
				}
				prompt = new PromptCommand(server, "Which card would you like to discard from " + clientInput + "'s Display?", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					dc = target.getDisplay().get(clientInput);
					if (dc != null) { break; }
				}
				target.getDisplay().remove(dc);
				System.out.println(dc.toString() + " was removed from " + target.getName() + "'s Display.");
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
					p.getDisplay().removeLast();
				}
				System.out.println("All players remove the last card played on their Display.");
				break;
			case "Outwit":
				System.out.println("played an outwit card");
				break;
			case "Retreat":
				prompt = new PromptCommand(server, "Which Display Card would you like to put back in your hand?", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					dc = action.origin.getDisplay().get(clientInput);
					if (dc != null) { break; }
				}
				action.origin.getDisplay().remove(dc);
				action.origin.addToHand(dc);
				System.out.println(dc.toString() + " was removed from " + action.origin.getName() + "'s Display, and added to their hand.");
				break;
			case "Riposte":
				prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					target = getPlayer(clientInput);
					if (target != null) { break; }
				}
				Card card = target.getDisplay().get(target.getDisplay().size()-1);
				target.getDisplay().removeLast();
				action.origin.addToDisplay(card);
				System.out.println(card.toString() + " was removed from " + clientInput + 
						"'s Display, and added to " + action.origin.getName() + "'s Display.");
				break;
			case "Unhorse":
				if (!getTournament().getColour().equals(Colour.c.PURPLE)) {
					System.out.println("Tournament is not Purple, can't unhorse.");
					return false;
				}
				prompt = new PromptCommand(server, "Change tournament to which colour? (red, blue, yellow)", action.origin);
				while (true) {
					clientInput = invoker.execute(prompt);
					if ((clientInput.equalsIgnoreCase("red")) ||
						(clientInput.equalsIgnoreCase("blue")) ||
						(clientInput.equalsIgnoreCase("yellow"))) {
					break;
					}
				}
				this.lastColour = getTournament().getColour();
				getTournament().setColour(new Colour(clientInput));
				System.out.println("Tournament colour changed to " + getTournament().getColour().toString());
				break;
			default:
				return false;
		}	
		return true;
	}
}
