package main.java.ai;

import main.java.Client;
import main.java.GameState;
import main.java.Player;

/*
 * Command Design Pattern - Concrete Command
 * decide if to end turn
 */
public class EndTurn implements CommandInterface {
	Client c;
	GameState g;
	Player p;
	double skill;
	Randoms r;
	
	/*
	 * absolute skill:
	 * 1 = will end turn if winning
	 * 0 = won't end turn 
	 */
	public EndTurn (Client c, double skill) {
		this.c = c;
		this.g = c.getGameState();
		this.p = c.getPlayer();
		this.skill = skill;
		this.r = new Randoms();
	}
	
	@Override
	public boolean execute() {
		// not your turn
		if (!this.p.isTurn) { return false;	}
		
		// not in tournament
		if (this.g.getTournament() == null) {
			// can't start tournament
			if (!this.p.hasDisplayCardInHand()) {
				this.c.processCmd("/end");
				return true;
				
			// can start tournament
			} else {
				return false;
			}
		}
		
		// no cards left to play TODO: add action cards 
		if (!this.p.hasValidDisplayCard(this.g.getTournament().getColour())) {
			this.c.processCmd("/end");
			return true;
		}
		
		// choose to end turn if in the lead
		if (this.r.makeChoice(this.skill)) {
			if (this.g.hasHighScore(this.p)) {
				this.c.processCmd("/end");
				return true;
			}
		}
		
		return false;
	}

}
