package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import main.resources.Config;
import main.resources.Trace;

public class ClientInput extends Thread{

	Boolean stop = false;					  // use to stop the Client
	BufferedReader reader; 					  // reader from the user
	String input;                             // user input
	Client c;                                 // client class
	ClientAction action;                      // client's action to take
	
	public ClientInput (Client client, InputStream s) {
		this.c = client;
		reader = new BufferedReader(new InputStreamReader(s));
	}
	
	public void run () {
		while(!stop) {
			try {
				input = reader.readLine();       // get next line of input
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
			if (validCmd(input)) {               // process valid commands
				System.out.println("Client: valid command recieved");
				sendCmd(input);
			} else if (input.charAt(0) == '/') { // process invalid commands
				System.out.println("Client: invalid command");
				Trace.getInstance().write(this, "invalid command: " + input);
			} else {                             // process chat
				action = new Chat(input);
				c.send(action);
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
		for (Config.ClientCommand cmd: Config.ClientCommand.values()) {
			if (in.startsWith(cmd.toString(), 1)) { return true; }
		}
		
		return false;
	}
	
	public boolean sendCmd (String s){
		//sends the server an appropriate command based on the input
		
		if(s.equals("/ready")){
			action = new Ready();
			c.send(action);
			return true;
		}
		if(s.equals("/setname")){
			String name = s.substring(9);
			action = new SetName(name);
			c.send(action);
			return true;
		}
		System.out.println("Command sending error - " + s);
		return false;
	}
	
}
