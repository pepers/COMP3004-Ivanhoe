package main.java.ai;

import java.util.ArrayList;

import main.java.Card;
import main.java.Client;
import main.java.Colour;
import main.java.DisplayCard;
import main.java.GameState;
import main.java.Player;

/*
 * Command Design Pattern - Concrete Command
 * decide when to withdraw from the tournament
 */
public class Withdraw implements CommandInterface {
	Client c;
	GameState g;
	Player p;
	double skill;
	Randoms r;
	
	public Withdraw (Client c, double skill) {
		this.c = c;
		this.g = c.getGameState();
		this.p = c.getPlayer();
		this.skill = skill;
		this.r = new Randoms();
	}
	
	@Override
	public boolean execute() {
		// not in tournament, or not your turn
		if ((this.g.getTournament() == null) || (this.p.isTurn)) { return false; }
		
		// determine if going to lose hand, withdraw if so
		Colour tcol = this.g.getTournament().getColour(); // tournament colour
		ArrayList<DisplayCard> dcards = new ArrayList<DisplayCard>(); // playable display cards this turn
		for (Card card : this.p.getHand()) {
			if (card instanceof DisplayCard) {
				DisplayCard dc = (DisplayCard) card;
				if (dc.getColour().equals(tcol)) {
					dcards.add(dc);
				}
			}
		}
		int possibleScore = this.p.getDisplay().score(tcol); // highest possible score you can get
		int highScore = this.g.getHighScore(); // score to beat
		for (DisplayCard d : dcards) {
			possibleScore += d.getValue();
		}
		if (possibleScore <= highScore) {
			this.c.processCmd("/withdraw");
			return true;
		}
		
		// take action, or don't withdraw
		if (this.r.makeChoice(Math.abs(skill))) {
			// bad action (always withdraw)
			if (skill < 0) {
				this.c.processCmd("/withdraw");
				return true;
			
			// good action (think ahead)
			} else {
				if (thinkAhead(tcol, possibleScore, highScore)) {
					this.c.processCmd("/withdraw");
					return true;
				} else {
					return false;
				}				
			}
		} else {
			return false;
		}
	}
	
	/*
	 * think ahead and determine likelyhood of card draws
	 * determine if should withdraw
	 */
	private boolean thinkAhead(Colour tcol, int possibleScore, int highScore) {
		// average card values, per colour:
		final double bavg = 3.2857;
		final double pavg = 4.4286;
		final double ravg = 3.7143;
		final double yavg = 2.8571;
		final double savg = 3.2;
		
		double cardAvg = 1; // average card value
		
		// switch over current tournament colour
		switch (tcol.toString()) {
			case "Blue": cardAvg = (bavg + savg)/2; 
				break;
			case "Green": cardAvg = 1;
				break;
			case "Purple": cardAvg = (pavg + savg)/2;
				break;
			case "Red": cardAvg = (ravg + savg)/2;
				break;
			case "Yellow": cardAvg = (yavg + savg)/2;
				break;
			default:
				return false;
		}
		
		// determine if opponent has better possible score than you
		ArrayList<Player> opponents = this.g.getOpponents(this.p); // opponents of this AI
		final double dcInDeck = 0.8181818;
		for (Player opponent : opponents) {
			if (((opponent.getDisplay().score(tcol)) + (opponent.getHandSize() * cardAvg * dcInDeck)) >
					(possibleScore + cardAvg)) {
				//System.out.println("Opponent: " + ((opponent.getDisplay().score(tcol)) + (opponent.getHandSize() * cardAvg * dcInDeck))
				//		+ "   AI: " + (possibleScore + cardAvg));
				return true;
			}
		}		
		
		return false;
	}

}
