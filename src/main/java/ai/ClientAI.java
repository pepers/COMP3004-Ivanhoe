package main.java.ai;

import main.java.Client;

public class ClientAI implements Runnable {
	CommandInterface cmd; 
	CommandInvoker invoker;
	Client client;
	
	// skills:
	double tournamentSkill;
	double displaySkill;
	double actionSkill;
	double withdrawSkill;
	
	/*
	 * Skill values (-1 to 1):
	 * low value = bad decisions
	 * high value = good decisions
	 */
	public ClientAI(double tournamentSkill, 
					double displaySkill,
					double actionSkill,
					double withdrawSkill) {
		this.tournamentSkill = inRange(tournamentSkill);
		this.displaySkill = inRange(displaySkill);
		this.actionSkill = inRange(actionSkill);
		this.withdrawSkill = inRange(withdrawSkill);
		this.invoker = new CommandInvoker();
		this.client = new Client();
	}
	
	public void run() {
		/* example:
			cmd = new StartTournament(ARGUMENTS);
			invoker.execute(cmd);
	 	*/
		// maybe start ClientAI only from Server as a Server command instead
	}
	
	/*
	 * set skills to be within [-1,1]
	 */
	private double inRange (double skill) {
		double value = skill;
		if (skill > 1) {
			value = 1;
		} else if (skill < -1) {
			value = -1;
		} 
		return value;
	}
}
