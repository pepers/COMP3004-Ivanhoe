package main.java;

/*
 * Command Design Pattern - Concrete Command
 * to run prompt in GameState execute method
 */
public class PromptCommand implements CommandInterface {
	private Server server; // the Server
	private String msg;    // prompt message
	private Player p;      // player to send prompt to
	
	public PromptCommand (Server server, String msg, Player p) {
		this.server = server;
		this.msg = msg;
		this.p = p;
	}
	
	@Override
	public String execute() {
		return server.prompt(this.msg, this.p);
	}
}
