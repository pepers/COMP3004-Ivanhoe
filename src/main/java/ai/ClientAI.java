package main.java.ai;

import main.java.Client;

public class ClientAI extends Thread {
	CommandInterface cmd; 
	CommandInvoker invoker;
	
	// Client info:
	Client client;
	String name;
	static int nameNum = 0;
	
	// Server info:
	String address;
	int port;
	
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
	public ClientAI(String address,
					int port,
					String name,
					double tournamentSkill, 
					double displaySkill,
					double actionSkill,
					double withdrawSkill) {
		this.address = address;
		this.port = port;
		this.name = name;
		this.tournamentSkill = inRange(tournamentSkill);
		this.displaySkill = inRange(displaySkill);
		this.actionSkill = inRange(actionSkill);
		this.withdrawSkill = inRange(withdrawSkill);
		this.invoker = new CommandInvoker();
		this.client = new Client();
		
		// start Client connection
		client.setGui(false);
		client.initialize(this.name);
		if (client.connect(this.address, this.port)) {
			client.processCmd("/setname " + client.getPlayer().getName());
		} else {
			System.out.println("Error: " + this.name + " could not connect to Server");
			client.shutdown();
		}
	}
	
	// without a name, generic name is given
	public ClientAI(String address,
					int port,
					double tournamentSkill, 
					double displaySkill,
					double actionSkill,
					double withdrawSkill) {
		this(address, port, "AIplayer" + (++nameNum), tournamentSkill, displaySkill, actionSkill, withdrawSkill);
	}
	
	public void run() {
		// start new thread to receive from Server
		client.receiveThread = new Thread(client);
		client.receiveThread.start();
	
		// get ready for game
		client.processInput("Prepare for battle!");
		client.processCmd("/ready");
		
		while (true) {
			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			cmd = new StartTournament(this.client, this.tournamentSkill);
			if (invoker.execute(cmd)) {
				System.out.println(this.name + ": " + this.client.getPlayer().getHand().toString());
			}

			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			cmd = new PlayCard(this.client, this.displaySkill, this.actionSkill);
			if (invoker.execute(cmd)) {
				System.out.println(this.name + ": " + this.client.getPlayer().getHand().toString());
			}
			
			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			cmd = new EndTurn(this.client, this.displaySkill);
			invoker.execute(cmd);

			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			cmd = new Withdraw(this.client, this.withdrawSkill);
			invoker.execute(cmd);
		}
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
