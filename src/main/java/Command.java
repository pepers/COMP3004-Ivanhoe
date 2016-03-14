package main.java;

import java.util.Arrays;

import main.resources.Config;

public class Command {
	private boolean isValid = true;  // originally valid, pass through ValidCommand chain
	private String cmd;              // the command
	private String[] args;           // the arguments
	private Player player;           // player that used command
	private ClientView view;         // gui
	
	public Command (String input, Player p, ClientView view) {
		String[] command = input.split("\\s+"); // array of command + arguments
		this.args = Arrays.copyOfRange(command, 1, command.length); 
		this.cmd = command[0];
		this.player = p;
		this.view = view;
		
		
		if (this.cmd.startsWith("/")) {
			this.cmd = this.cmd.substring(1); // get rid of slash
		} else {
			this.isValid = false;  // doesn't start with slash (not command)
		}
	}
	
	public String   getCmd()       { return this.cmd; }
	public String[] getArgs()      { return this.args; }
	public Player   getPlayer()    { return this.player; }
	public boolean  isValid()      { return this.isValid; }
	public boolean  inTournament() { return this.player.getParticipation(); }
	
	public void output(String s) {
		System.out.println(s);
		if (this.view != null)
			view.writeConsole(s, 0);
	}
	
	public void notValid() {
		this.isValid = false;
	}

}
