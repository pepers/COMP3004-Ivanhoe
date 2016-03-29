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
		if ((!p.isTurn) || 
			(g.getTournament() != null) ||
			(!p.hasDisplayCardInHand())) { 
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
		ArrayList<Colour> clist = new ArrayList<Colour>();
		clist.add(new Colour(Colour.c.BLUE));
		clist.add(new Colour(Colour.c.GREEN));
		clist.add(new Colour(Colour.c.RED));
		clist.add(new Colour(Colour.c.YELLOW));
		if (!this.g.getLastColour().equals(new Colour(Colour.c.PURPLE))) {
			clist.add(new Colour(Colour.c.PURPLE));
		}
		
		// basic possible choices
		ArrayList<Colour> cChoices = new ArrayList<Colour>();
		ArrayList<Colour> pChoices = new ArrayList<Colour>();
		for (Colour c : clist) {
			if (p.hasValidDisplayCard(c)) {
				cChoices.add(c);
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
		// chance to start tournament of colour you already have
		if (this.skill < 0) { 
			if (!cTokens.isEmpty()) {
				for (int i=0; i<(Math.abs(this.skill)*10); i++) {
					for (Colour c : cTokens) {
						cChoices.add(c);
					}
				}
			}
			
		// good skill
		// chance to start tournament of colour you don't have
		} else {
			if (!cNeedTokens.isEmpty()) {
				for (int i=0; i<(Math.abs(this.skill)*10); i++) {
					for (Colour c : cNeedTokens) {
						cChoices.add(c);
					}
				}
			}
			
			
		}
		
		if (cChoices.isEmpty()) {
			return null;
		} else {
			return r.get(cChoices); // return random colour
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
