package main.java;

import java.util.Scanner;

import main.resources.Config;
import main.resources.Trace;

public class ClientInput implements Runnable{

	Boolean stop = false;					  // use to stop the Client
	Scanner scanner = new Scanner(System.in); // scan console input
	String input;                             // user input
	Client client;                            // client class
	
	public ClientInput (Client client) {
		
	}
	
	public void run () {
		while(!stop) {
			input = scanner.nextLine();  // get next line of input
			
			if (validCmd(input)) {               // process valid commands
				
			} else if (input.charAt(0) == '/') { // process invalid commands
				System.out.println("Client: invalid command");
				Trace.getInstance().write(this, "invalid command: " + input);
			} else {                             // process chat
				
			}
		}
	}
	
	/*
	 * returns if a valid command or not
	 */
	public boolean validCmd (String in) {
		// commands start with slash (/)
		if (in.charAt(0) != '/') {
			return false;
		} 
		
		// check existing commands
		for (Config.Command cmd: Config.Command.values()) {
			if (in.startsWith(cmd.toString(), 1)) { return true; }
		}
		
		return false;
	}
	
}
