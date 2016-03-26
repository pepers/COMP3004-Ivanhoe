package main.java.ai;

public class ClientAI {
	CommandInterface cmd; 
	CommandInvoker invoker = new CommandInvoker();
	
	public ClientAI() {
	/* example:
		cmd = new StartTournament(ARGUMENTS);
		invoker.execute(cmd);
	*/
	}
	
	public static void main(String args[]) {
		// ClientAI ai = new ClientAI();
		// maybe start ClientAI only from Server as a Server command instead
	}
}
