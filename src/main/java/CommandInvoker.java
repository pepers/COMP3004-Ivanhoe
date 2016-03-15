package main.java;

/*
 * Command Design Pattern - Invoker Class
 * to run prompt in GameState execute method
 */
public class CommandInvoker {
	public String execute (CommandInterface cmd) {
		return cmd.execute();
	}
}
