package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class GameState implements Serializable{
	private static final long serialVersionUID = 1L;	
	
	private static Deck deck;
	private Tournament tnmt = null;
	private Colour lastColour = new Colour(); // initialized as NONE
	private ArrayList<Player> players;
	private int turnIndex = 0;
	private int numPlayers;
	private int highScore = 0;
	
	public static final int NO_TOURNAMENT = 1;
	public static final int  INVALID_COLOUR = 2;
	public static final int  NO_TARGETS = 3;
	public static final int  MULTIPLE_MAIDEN = 4;
	
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
	
	/*
	 * get all opponents of player, if they are not shielded
	 */
	public ArrayList<Player> getOpponents (Player player) {
		ArrayList<Player> opponents = new ArrayList<Player>();
		for (Player p : players) {
			if (p.getParticipation() && !p.equals(player) && !p.getShielded()) opponents.add(p);
		}
		return opponents;
	}
	
	/* 
	 * cleanup after tournament ends
	 */
	public void endTournament(){
		lastColour = getTournament().getColour();
		tnmt = null;
		highScore = 0;
		for (Player p:players){
			p.setParticipation(false);
			p.getDisplay().clear();
			p.setStunned(false);
			p.setShielded(false);
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
	 * return arraylist of valid colours to start tournament with
	 */
	public ArrayList<Colour> validTournamentColours() {
		ArrayList<Colour> alist = new ArrayList<Colour>();
		if (!this.lastColour.equals(new Colour(Colour.c.PURPLE))) {
			alist.add(new Colour(Colour.c.PURPLE));
		}
		alist.add(new Colour(Colour.c.BLUE));
		alist.add(new Colour(Colour.c.GREEN));
		alist.add(new Colour(Colour.c.RED));
		alist.add(new Colour(Colour.c.YELLOW));
		return alist;
	}
	
	/*
	 * remove shielded from Player list
	 * (to be used when executing Action cards)
	 */
	public ArrayList<Player> removeShielded (ArrayList<Player> in, ActionCard c) {
		if (in.isEmpty()) { return in; }
		ArrayList<Player> out = new ArrayList<Player>();
		for (Player p : in) {
			if (!p.getShielded()) { 
				out.add(p); 
			} else {
				System.out.println(p.getName() + " is Shielded from the " + c.toString() + " card.");
			}
		}
		return out;
	}

	/* 
	 * deal with Action Cards' special abilities
	 */
	public <T> boolean execute(ActionWrapper action, Server s) {
		ArrayList<Player> ps;
		Play play = (Play) action.object;
		ActionCard c = (ActionCard) play.getCard(); // the Action Card to be played
		Server server = s;
		CommandInterface prompt; // part of Command Pattern
		CommandInvoker invoker = new CommandInvoker(); // part of Command Pattern
		String clientInput = null; // client's input after prompted
		Player target = null; // target opponent
		DisplayCard dc = null; // display card retrieved
		ActionCard ac = null; // action card retrieved
		Card card = null; // card retrieved
		ArrayList<Player> targets = new ArrayList<Player>(); // targets
		
		switch(c.toString()){
			case "Adapt":
				// shielded players aren't affected
				targets = removeShielded(getTournamentParticipants(), c);
				if (targets.isEmpty()) { break; }
				for (Player p : targets) {
					ArrayList<Card>keep = new ArrayList<Card>();
					if (play.getCards() == null) {
						// get all different values of cards in Display
						Set<Integer> values = p.getDisplay().getValues();
						if (values.isEmpty()) { break; }
						for (Integer v : values) {
							// get all cards of value v
							ArrayList<DisplayCard> cards =  p.getDisplay().getAll(v);
							if (!cards.isEmpty()) {
								// if only one option, keep and don't prompt
								if (cards.size() > 1) {
									ArrayList<Object> options = new ArrayList<Object>();
									options.addAll(cards);
									prompt = new PromptCommand(server, "Choose which Display Card of value " + v + " to keep in your Display: ", p, options);
									while (true) {
										clientInput = invoker.execute(prompt);
										dc = p.getDisplay().get(clientInput);
										if (dc != null) { break; }
									}
									keep.add(dc); // add card of value v that player wants to keep
								} else {
									keep.add(cards.get(0)); // if only one option just keep
								}
							}
						}
					} else {
						keep = play.getCards();
					}
					if (!keep.isEmpty()) {
						// delete all cards from Display that weren't chosen to be kept
						p.getDisplay().clear();
						for (Card k : keep) {
							p.getDisplay().add((DisplayCard) k);
						}
					}
				}
				System.out.println("Each player keeps one Display Card of each value.");
				break;
			case "Break Lance":
				if(play.getOpponents() == null){
					prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						target = getPlayer(clientInput);
						if (target != null) { break; }
					}
				}else{
					target = getPlayer(play.getOpponents().get(0).getName());
				}
				target.getDisplay().removeAll(new Colour(Colour.c.PURPLE));
				System.out.println("Removed all Purple cards from " + clientInput + "'s Display.");
				break;
			case "Change Weapon":
				if(getTargets(c, action.origin).size() == 0){
					System.out.println("Tournament is not Red, Blue, or Yellow. Can't change weapon.");
					return false;
				}
				this.lastColour = getTournament().getColour();
				if(play.getColours() == null){
					prompt = new PromptCommand(server, "Change tournament to which colour? (red, blue, yellow)", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						if ((clientInput.equalsIgnoreCase("red")) ||
							(clientInput.equalsIgnoreCase("blue")) ||
							(clientInput.equalsIgnoreCase("yellow"))) {
						break;
						}
					}
					getTournament().setColour(new Colour(clientInput));
				}else{
					getTournament().setColour(play.getColours().get(0));
				}
				
				System.out.println("Tournament colour changed to " + getTournament().getColour().toString());
				break;
			case "Charge":
				int lowest = 99;
				// shielded players aren't affected
				ps = removeShielded(getTournamentParticipants(), c);
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
				// shielded players aren't affected
				ps = removeShielded(getTournamentParticipants(), c);
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
				// shielded players aren't affected
				ps = removeShielded(getTournamentParticipants(), c);
				for (Player p: ps) {
					p.getDisplay().removeAll(new Colour(Colour.c.NONE));
				}
				System.out.println("All players remove all their supporters from their Display.");
				break;
			case "Dodge":
				if(play.getOpponents() == null){
					prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						target = getPlayer(clientInput);
						if (target != null) { break; }
					}
					ArrayList<Object> options = new ArrayList<Object>();
					options.addAll(target.getDisplay().elements());
					prompt = new PromptCommand(server, "Which card would you like to discard from " + clientInput + "'s Display?", action.origin, options);
					while (true) {
						clientInput = invoker.execute(prompt);
						dc = target.getDisplay().get(clientInput);
						if (dc != null) { break; }
					}
				}else{
					target = getPlayer(play.getOpponents().get(0).getName());
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
			case "Knock Down":
				if(play.getOpponents() == null){
					prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						target = getPlayer(clientInput);
						if (target != null) { break; }
					}
				}else{
					target = getPlayer(play.getOpponents().get(0).getName());
				}
				int random = ThreadLocalRandom.current().nextInt(0, target.getHand().size());
				card = target.getHand().get(random);
				target.getHand().remove(card);
				action.origin.addToHand(card);
				System.out.println(card.toString() + " was taken from " + target.getName() + "'s Hand, and added to " + action.origin.getName() + "'s Hand.");
				break;
			case "Outmaneuver":
				// shielded players aren't affected
				ps = removeShielded(getTournamentParticipants(), c);
				for (Player p: ps) {
					p.getDisplay().removeLast();
				}
				System.out.println("All players remove the last card played on their Display.");
				break;
			case "Outwit":
				DisplayCard dc2 = null; // second display card retrieved
				ActionCard ac2 = null;  // second action card retrieved
				Card given = null;      // card given
				Card taken = null;      // card taken
				
				if(play.getOpponents() == null){
					prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						target = getPlayer(clientInput);
						if (target != null) { break; }
					}
					
					// populate options with player's cards
					ArrayList<Object> options = new ArrayList<Object>();
					options.addAll(action.origin.getDisplay().elements()); // add Display Cards to options
					if (action.origin.getStunned()) { // add Stunned to options
						Card stunned = new ActionCard("Stunned");
						options.add(stunned);
					}
					if (action.origin.getShielded()) { // add Shield to options
						Card shield = new ActionCard("Shield");
						options.add(shield);
					}
					
					prompt = new PromptCommand(server, "Which card would you like to give to " + target.getName() + "?", action.origin, options);
					while (true) {
						clientInput = invoker.execute(prompt);
						if (clientInput.equalsIgnoreCase("Stunned")) {
							ac = new ActionCard("Stunned");
							break;
						} else if (clientInput.equalsIgnoreCase("Shield")) {
							ac = new ActionCard("Shield");
							break;
						}
						dc = action.origin.getDisplay().get(clientInput);
						if (dc != null) { break; }
					}

					// populate options with target's cards
					options.clear();
					options.addAll(target.getDisplay().elements()); // add Display Cards to options
					if (target.getStunned()) { // add Stunned to options
						Card stunned = new ActionCard("Stunned");
						options.add(stunned);
					}
					if (target.getShielded()) { // add Shield to options
						Card shield = new ActionCard("Shield");
						options.add(shield);
					}
					
					prompt = new PromptCommand(server, "Which card would you like to take from " + target.getName() + "?", action.origin, options);
					while (true) {
						clientInput = invoker.execute(prompt);
						if (clientInput.equalsIgnoreCase("Stunned")) {
							ac2 = new ActionCard("Stunned");
							break;
						} else if (clientInput.equalsIgnoreCase("Shield")) {
							ac2 = new ActionCard("Shield");
							break;
						}
						dc2 = target.getDisplay().get(clientInput);
						if (dc2 != null) { break; }
					}
				}else{
					target = getPlayer(play.getOpponents().get(0).getName());
					given = play.getCards().get(0);
					taken = play.getCards().get(1);
				}
				
				
				// give card to target and remove from player
				if (ac != null) {
					if (ac.toString().equalsIgnoreCase("Stunned")) {
						target.setStunned(true);
						action.origin.setStunned(false);
					} else if (ac.toString().equalsIgnoreCase("Shield")) {
						target.setShielded(true);
						action.origin.setShielded(false);
					}
					given = ac;
				} else if (dc != null) {
					target.getDisplay().add(dc);
					action.origin.getDisplay().remove(dc);
					given = dc;
				}
				
				// take card from target and give to player
				if (ac2 != null) {
					if (ac2.toString().equalsIgnoreCase("Stunned")) {
						target.setStunned(false);
						action.origin.setStunned(true);
					} else if (ac2.toString().equalsIgnoreCase("Shield")) {
						target.setShielded(false);
						action.origin.setShielded(true);
					}
					taken = ac2;
				} else if (dc2 != null) {
					action.origin.getDisplay().add(dc2);
					target.getDisplay().remove(dc2);
					taken = dc2;
				}
					
				System.out.println(action.origin.getName() + " took a " + taken.toString() + " card from "
						+ target.getName() + ", and gave them a " + given.toString() + " card.");
				break;
			case "Retreat":
				if(play.getCards() == null){
					prompt = new PromptCommand(server, "Which Display Card would you like to put back in your hand?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						dc = action.origin.getDisplay().get(clientInput);
						if (dc != null) { break; }
					}
				}else{
					dc = (DisplayCard) play.getCards().get(0);
				}
				action.origin.getDisplay().remove(dc);
				action.origin.addToHand(dc);
				System.out.println(dc.toString() + " was removed from " + action.origin.getName() + "'s Display, and added to their hand.");
				break;
			case "Riposte":
				if(play.getOpponents() == null) {
					prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						target = getPlayer(clientInput);
						if (target != null) { break; }
					}
				} else {
					target = getPlayer(play.getOpponents().get(0).getName());
				}
				dc = target.getDisplay().getLast();
				if (dc != null) {
					if (target.getDisplay().removeLast()) { 
						action.origin.addToDisplay(dc);
						System.out.println(dc.toString() + " was removed from " + target.getName() + 
								"'s Display, and added to " + action.origin.getName() + "'s Display.");
					} else {
					System.out.println("Last card of " + target.getName() + "'s Display could not be removed and added to " 
							+ action.origin.getName() + "'s Display.");
					}
				} else {
					System.out.println("Last card of " + target.getName() + "'s Display could not be found.");
				}
				break;
			case "Shield":
				action.origin.setShielded(true);
				System.out.println(action.origin.getName() + " is now Shielded.");
				break;
			case "Stunned":
				if(play.getOpponents() == null) {
					prompt = new PromptCommand(server, "Which opponent would you like to target?", action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						target = getPlayer(clientInput);
						if (target != null) { break; }
					}
				} else {
					target = getPlayer(play.getOpponents().get(0).getName());
				}
				if (target.getStunned()) { // check if already stunned
					System.out.println("Can't stun " + target.getName() + ". They were already stunned.");
				} else {
					target.setStunned(true); // stun target opponent
					System.out.println(target.getName() + " has been stunned.");
				}
				break;
			case "Unhorse":
				if (!getTournament().getColour().equals(Colour.c.PURPLE)) {
					System.out.println("Tournament is not Purple, can't unhorse.");
					return false;
				}
				this.lastColour = getTournament().getColour();
				if (play.getColours() == null) {
					prompt = new PromptCommand(server, "Change tournament to which colour? (red, blue, yellow)",
						action.origin, getTargets(c, action.origin));
					while (true) {
						clientInput = invoker.execute(prompt);
						if ((clientInput.equalsIgnoreCase("red")) || (clientInput.equalsIgnoreCase("blue"))
								|| (clientInput.equalsIgnoreCase("yellow"))) {
							break;
						}
					}
					getTournament().setColour(new Colour(clientInput));
				} else {
					getTournament().setColour(play.getColours().get(0));
				}
				System.out.println("Tournament colour changed to " + getTournament().getColour().toString());
				break;
			default:
				return false;
		}	
		return true;
	}
	
	public ArrayList<Object> getTargets(ActionCard ac, Player controller){
		ArrayList<Object> targets = new ArrayList<Object>();
		ArrayList<Player> unshielded = new ArrayList<Player>();
		switch(ac.toString()){
			case "Break Lance":
				// shielded players aren't affected
				unshielded = removeShielded(getOpponents(controller), ac);
				targets.addAll(unshielded);
				break;
			case "Change Weapon":
				if(getTournament().getColour().equals("purple") || getTournament().getColour().equals("green")){
					break;
				}
				targets.add(new Colour(Colour.c.RED));
				targets.add(new Colour(Colour.c.BLUE));
				targets.add(new Colour(Colour.c.YELLOW));
				targets.remove(getTournament().getColour());
				break;
			case "Dodge":
				// shielded players aren't affected
				unshielded = removeShielded(getOpponents(controller), ac);
				targets.addAll(unshielded);
				targets.addAll(getOpponents(controller));
				break;
			case "Knock Down":
				targets.addAll(getOpponents(controller));
				break;
			case "Outwit":
				// shielded players aren't affected
				unshielded = removeShielded(getOpponents(controller), ac);
				targets.addAll(unshielded);
				break;
			case "Retreat":
				targets.addAll(controller.getDisplay().elements());
				break;
			case "Riposte":
				// shielded players aren't affected
				unshielded = removeShielded(getOpponents(controller), ac);
				targets.addAll(unshielded);
				break;
			case "Stunned":
				targets.addAll(getOpponents(controller));
				break;
			case "Unhorse":
				targets.add(new Colour(Colour.c.RED));
				targets.add(new Colour(Colour.c.BLUE));
				targets.add(new Colour(Colour.c.YELLOW));
				break;
			default:
				return null;
		}	
		return targets;		
	}
	
	public int canPlay(Card card, Player player){
		
		if(card instanceof DisplayCard){
			if (getTournament() == null) {
				return 0;
			}
			if (!(((DisplayCard) card).getColour().equals("none")
					|| getTournament().getColour().equals(((DisplayCard) card).getColour()))) {
				return INVALID_COLOUR;
			} else if (card.toString().equalsIgnoreCase("Maiden:6") && player.getDisplay().hasCard((DisplayCard) card)) {
				return MULTIPLE_MAIDEN;
			}
			return 0;
		}else{
			if (getTournament() == null) {
				return NO_TOURNAMENT;
			}
			if (((ActionCard) card).hasTargets()){
				ArrayList<Object> targets = getTargets((ActionCard) card, player);
				if(targets == null || targets.isEmpty()){
					return NO_TARGETS;
				}else{
					return 0;
				}
			}else{
				return 0;
			}
		}
	}
}
