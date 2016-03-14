package main.java;

import java.util.Arrays;

public class Command {
	private boolean isValid = true;  // originally valid, pass through ValidCommand chain
	private String cmd;              // the command
	private String[] args;           // the arguments
	private Player player;           // player that used command
	private String error;            // error message when command is not valid
	
	public Command (String input, Player p) {
		String[] command = input.split("\\s+"); // array of command + arguments
		this.args = Arrays.copyOfRange(command, 1, command.length); 
		this.cmd = command[0];
		this.player = p;
		
		if (this.cmd.startsWith("/")) {
			this.cmd = this.cmd.substring(1); // get rid of slash
		} else {
			this.isValid = false;  // doesn't start with slash (not command)
		}
	}
	
	public String   getCmd()             { return this.cmd; }
	public String[] getArgs()            { return this.args; }
	public Player   getPlayer()          { return this.player; }
	public String   getMessage()         { return this.error; }
	public void     setMessage(String s) { this.error = s; }
	public boolean  isValid()            { return this.isValid; }
	public boolean  inTournament()       { return this.player.getParticipation(); }
	
	public void notValid() {
		this.isValid = false;
	}
}
