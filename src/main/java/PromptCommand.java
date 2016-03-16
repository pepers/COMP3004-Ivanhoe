package main.java;

import java.util.ArrayList;

/*
 * Command Design Pattern - Concrete Command
 * to run prompt in GameState execute method
 */
public class PromptCommand implements CommandInterface {
	private Server server;        // the Server
	private String msg;           // prompt message
	private Player p;             // player to send prompt to
	private ArrayList<Object> options; // options for player to choose from
	
	public PromptCommand (Server server, String msg, Player p, ArrayList<Object> options) {
		this.server = server;
		this.msg = msg;
		this.p = p;
		this.options = options;
	}
	
	@Override
	public String execute() {
		return server.prompt(this.msg, this.p, this.options);
	}
}
