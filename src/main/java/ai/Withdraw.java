package main.java.ai;

import main.java.Client;
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
		// TODO Auto-generated method stub
		return false;
	}

}
