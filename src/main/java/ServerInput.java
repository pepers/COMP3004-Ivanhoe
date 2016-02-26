package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class ServerInput extends Thread{

	Boolean stop = false;					  // use to stop the Server
	BufferedReader reader; 					  // reader from the user
	String input;                             // user input
	Server server;                                 // server class
	Action action;                      // client's action to take
	Language language = new Language(Language.Dialect.none);
	
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
			if(input.length() > 0){
				if (validCmd(input)) {               // process valid commands
					processCmd(input);
				} else if (input.charAt(0) == '/') { // process invalid commands
					System.out.println("Server: invalid command");
				} else {                             
					//chat to all players
					server.broadcast("Server: " + language.translate(input));
				}
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
		// get argument line
		String[] cmd = s.split("\\s+");                         // array of command + arguments
		String[] args = Arrays.copyOfRange(cmd, 1, cmd.length); // just arguments
		String sub = String.join(" ", args);                    // join arguments into one string
		
		// switch over command 
		switch (cmd[0]) {
			case "/start":
				server.startGame();
				return true;
			case "/kick":
				if(sub.charAt(0) >= '0' && sub.charAt(0) <= '9'){
					int toRemove = Integer.parseInt(sub);
					server.removeThread(toRemove);
					return true;
				} else {
					server.removeThread(sub);
					return true;
				}
			case "/shutdown":
				server.shutdown();
				return true;
			case "/list":
				server.listClients();
				return true;
			case "/hand":
				server.printHand(sub);
				return true;
			case "/translate":
				if (args.length != 1) { return false; }
				for (Language.Dialect dialect: Language.Dialect.values()) {
					if (dialect.toString().equals(args[0])) {
						server.language = new Language(dialect);
						language = new Language(dialect);
						Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString());
					}
				}
				break;
			default:
				break;
		}
		return false;
	}
}
