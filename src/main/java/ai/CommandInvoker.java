package main.java.ai;

/*
 * Command Design Pattern - Invoker Class
 * to run AI commands
 */
public class CommandInvoker {
	public boolean execute (CommandInterface cmd) {
		return cmd.execute();
	}
}
