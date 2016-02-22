package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import main.resources.Config;

public class ServerInput extends Thread{

	Boolean stop = false;					  // use to stop the Server
	BufferedReader reader; 					  // reader from the user
	String input;                             // user input
	Server server;                                 // server class
	ClientAction action;                      // client's action to take
	
	public ServerInput (Server server, InputStream s) {
		this.server = server;
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
				System.out.println("Server: valid command recieved");

			} else if (input.charAt(0) == '/') { // process invalid commands
				System.out.println("Server: invalid command");

			} else {                             
				//chat to all players
				server.broadcast(input);
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
		for (Config.ServerCommand cmd: Config.ServerCommand.values()) {
			if (in.startsWith(cmd.toString(), 1)) { return true; }
		}
		
		return false;
	}
	
	public boolean processCmd(String s){

		if(s.equals("/start")){
			server.startGame();
			return true;
		}
		return false;
	}
}
