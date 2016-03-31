package main.java.ai;

import java.util.ArrayList;

import main.java.Client;

public class ClientAI extends Thread {
	CommandInterface cmd; 
	CommandInvoker invoker;
	Randoms r;
	
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
		this.r = new Randoms();
		
		// start Client connection
		client.setGui(false);
		client.ai = true;
		client.initialize(this.name);
		if (client.connect(this.address, this.port)) {
			client.send(client.getPlayer());
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
			
			if (this.client.getGameState() != null) {
				cmd = new StartTournament(this.client, this.tournamentSkill);
				invoker.execute(cmd);
			} else {
				this.client.processInput("Good game! Fair thee well!");
				this.client.shutdown();
			}

			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			if (this.client.getGameState() != null) {
				cmd = new PlayCard(this.client, this.displaySkill, this.actionSkill);
				invoker.execute(cmd);
			} else {
				this.client.processInput("Good game! Fair thee well!");
				this.client.shutdown();
			}
			
			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			// deal with prompt
			if (this.client.promptOptions != null) {
				this.client.aiPrompt = processPrompt(this.client.promptOptions);
				this.client.promptOptions = null;
			}
			
			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			if (this.client.getGameState() != null) {
				cmd = new EndTurn(this.client, this.withdrawSkill);
				invoker.execute(cmd);
			} else {
				this.client.processInput("Good game! Fair thee well!");
				this.client.shutdown();
			}

			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
			
			if (this.client.getGameState() != null) {
				cmd = new Withdraw(this.client, this.withdrawSkill);
				invoker.execute(cmd);
			} else {
				this.client.processInput("Good game! Fair thee well!");
				this.client.shutdown();
			}
			
			try {
				this.sleep(100);
			} catch (InterruptedException e) {}
		}
	}
	
	/*
	 * deal with prompts
	 */
	private String processPrompt(ArrayList<Object> options) {
		String response = "";
		response =  r.get(options).toString();
		return response;
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
