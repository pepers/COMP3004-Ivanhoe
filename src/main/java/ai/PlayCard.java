package main.java.ai;

import java.util.ArrayList;

import main.java.ActionCard;
import main.java.Card;
import main.java.Client;
import main.java.Colour;
import main.java.DisplayCard;
import main.java.GameState;
import main.java.Player;

/*
 * Command Design Pattern - Concrete Command
 * decide what card to play
 */
public class PlayCard implements CommandInterface {
	Client c;
	GameState g;
	Player p;
	double dSkill; // Display Card skill
	double aSkill; // Action Card skill
	Randoms r;
	
	public PlayCard(Client c, double dSkill, double aSkill) {
		this.c = c;
		this.g = c.getGameState();
		this.p = c.getPlayer();
		this.dSkill = dSkill;
		this.aSkill = aSkill;
		this.r = new Randoms();
	}

	@Override
	public boolean execute() {
		// no tournament running
		if (this.g.getTournament() == null) { return false; }
		
		// not your turn (TODO: can only play Ivanhoe)
		if (!this.p.isTurn) {
			return false;
		}
		
		// decide if playing Display Card or Action Card
		Card card = null;
		//if (r.makeChoice(Math.abs(Math.abs(this.dSkill) - Math.abs(this.aSkill)))) {
			card = playDisplay();
		//} else {
		//	card = playAction();
		//}
		
		// don't play card
		if (card == null) { return false; }
		
		// play card
		this.c.processCmd("/play " + card.toString());
		
		return true;
	}
	
	/*
	 * play a Display Card
	 */
	private Card playDisplay() {
		// get arraylist of possible display cards to play
		ArrayList<DisplayCard> pCards = new ArrayList<DisplayCard>();
		for (Card c : this.p.getHand()) {
			if (c instanceof DisplayCard) {
				DisplayCard dc = (DisplayCard) c;
				if (dc.getColour().equals(new Colour(Colour.c.NONE)) ||
						this.g.getTournament().getColour().equals(dc.getColour())) {
					pCards.add(dc);
				}
			}
		}
		
		return r.get(pCards);
	}
	
	/*
	 * play an Action Card
	 */
	private Card playAction() {
		Card card = null; // card to return
		ArrayList<ActionCard> pCards = new ArrayList<ActionCard>();
		return card;
	}

}
