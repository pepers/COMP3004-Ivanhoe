package main.java.ai;

import java.util.ArrayList;

import main.java.Card;
import main.java.Client;
import main.java.Colour;
import main.java.DisplayCard;
import main.java.GameState;
import main.java.Player;
import main.java.Token;

/*
 * Command Design Pattern - Concrete Command
 * decide what colour tournament to start
 */
public class StartTournament implements CommandInterface {
	Client c;
	GameState g;
	Player p;
	double skill;
	Randoms r;
	
	public StartTournament (Client c, double skill) {
		this.c = c;
		this.g = c.getGameState();
		this.p = c.getPlayer();
		this.skill = skill;
		this.r = new Randoms();
	}
	
	@Override
	public boolean execute() {
		// not player's turn, or tournament is running, or no DisplayCard in hand
		if (!p.isTurn) { 
			//System.out.println("Not my turn");
			return false; 
		}
		if (!p.hasDisplayCardInHand()) { 
			//System.out.println("No cards");
			return false; 
		}
		if (g.getTournament() != null){
			//System.out.println("Tournament is running");
			return false;
		}
		Colour colour = chooseColour();
		if (colour == null) { return false; }
		DisplayCard d = chooseCard(colour); 
		if (d == null) { return false; }
		this.c.processCmd("/tournament " + colour.toString() + " " + d.toString());
		
		return true;
	}
	
	/*
	 * choose a colour to start tournament
	 */
	private Colour chooseColour() {
		// valid colours to start tournament with
		ArrayList<Colour> clist = g.validTournamentColours();
		
		// choices to return a random element from at end of method
		ArrayList<Colour> cChoices = new ArrayList<Colour>();
		
		// basic possible choices (if supporter in hand, pChoices == clist)
		ArrayList<Colour> pChoices = new ArrayList<Colour>();
		for (Colour c : clist) {
			if (p.hasValidDisplayCard(c)) {
				pChoices.add(c);
			}
		}
		
		// colours of tokens already obtained
		ArrayList<Colour> cTokens = new ArrayList<Colour>();
		ArrayList<Token> tokens = p.getTokens();
		if (!tokens.isEmpty()) {
			for (Token t : tokens) {
				if (pChoices.contains(t.getColour())) {
					cTokens.add(t.getColour());
				}
			}
		}
		
		// colours of tokens not obtained
		ArrayList<Colour> cNeedTokens = new ArrayList<Colour>();
		if (!pChoices.isEmpty()) {
			for (Colour c : pChoices) {
				if (!cTokens.contains(c)) {
					cNeedTokens.add(c);
				}
			}
		}		
		
		// bad skill
		if (this.skill < 0) { 
			// chance to use colour of token you have
			if (!cTokens.isEmpty()) {
				for (Card c : p.getHand()) {
					if (c instanceof DisplayCard) {
						if (r.makeChoice(this.skill)) {
							Colour col = ((DisplayCard) c).getColour();
							if (col.equals(new Colour(Colour.c.NONE))) {
								cChoices.add(r.get(cTokens));
							} else {
								cChoices.add(col);
							}
						} else {
							cChoices.add(r.get(pChoices));
						}
					}
				}
				
			// chance to use colour of lowest number of cards in your hand
			} else {
				int blue = 0;
				int green = 0;
				int purple = 0;
				int red = 0;
				int yellow = 0;
				for (Card c : this.p.getHand()) {
					if (c instanceof DisplayCard) {
						Colour col = ((DisplayCard) c).getColour();
						switch (col.toString()) {
							case "Blue": blue += 1;
								break;
							case "Green": green += 1;
								break;
							case "Purple": purple += 1;
								break;
							case "red": red += 1;
								break;
							case "yellow": yellow += 1;
								break;
							default:
								break;
						}
					}
					
					int lowest = blue;
					if ((green < lowest) && (green > 0)) { lowest = green;
					} else if ((purple < lowest) && (purple > 0)) { lowest = purple;
					} else if ((red < lowest) && (red > 0)) { lowest = red;
					} else if ((yellow < lowest) && (yellow > 0)) { lowest = yellow; }
					
					ArrayList<Colour> cLowest = new ArrayList<Colour>();
					
					if (blue == lowest) { cLowest.add(new Colour(Colour.c.BLUE)); }
					if (green == lowest) { cLowest.add(new Colour(Colour.c.GREEN)); }
					if (purple == lowest) { cLowest.add(new Colour(Colour.c.PURPLE)); }
					if (red == lowest) { cLowest.add(new Colour(Colour.c.RED)); }
					if (yellow == lowest) { cLowest.add(new Colour(Colour.c.YELLOW)); }
					
					for (Card dc : p.getHand()) {
						if (dc instanceof DisplayCard) {
							if (r.makeChoice(this.skill)) {
								cChoices.add(r.get(cLowest));
							} else {
								cChoices.add(r.get(pChoices));
							}
						}
					}
				}
			}
			
		// good skill
		} else {
			// chance to use colour of token you don't have
			for (Card dc : p.getHand()) {
				if (dc instanceof DisplayCard) {
					if (!cNeedTokens.isEmpty()) {
						if (r.makeChoice(this.skill)) {
							cChoices.add(r.get(cNeedTokens));
						} else {
							cChoices.add(r.get(pChoices));
						}
					} else {
						cChoices.add(r.get(pChoices));
					}
				}
			}
		}
		
		if (cChoices.isEmpty()) {
			return r.get(pChoices); // return random possible colour
		} else {
			return r.get(cChoices); // return random colour based on skill
		}
	}
	
	/*
	 * choose a card to start certain colour tournament with
	 */
	private DisplayCard chooseCard(Colour c) {
		DisplayCard d = null;
		for (Card card : this.p.getHand()) {
			if (card instanceof DisplayCard) {
				if (((DisplayCard) card).getColour().equals(c) ||
						((DisplayCard) card).getColour().equals(new Colour(Colour.c.NONE))) {
					d = (DisplayCard) card;
					break;
				}
			}
		}
		return d;
	}

}
