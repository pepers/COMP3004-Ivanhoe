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
	Language language = new Language(Language.Dialect.none, false);
	
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
					if (processCmd(input)) {
						Trace.getInstance().write(this, "Server: command processed: " + input);
					} else {
						Trace.getInstance().write(this, "Server: invalid command: " + input);
						System.out.println("Server: invalid command, try typing '/help' for more info.");
					}
				} else if (input.charAt(0) == '/') { // process invalid commands
					System.out.println("Server: invalid command, try typing '/help' for more info.");
					Trace.getInstance().write(this, "Server: invalid command: " + input);
				} else {                             
					//chat to all players
					server.broadcast("Server: " + language.translate(input));
					Trace.getInstance().write(this, "Server: chat sent: " + input);
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
			case "/ban":
				// TODO: return true
				return false;
			case "/display":
				if (args.length == 0) { // show every player's display
					for (Player p: server.gameState.players) {
						if (!(p.printDisplay())) {
								System.out.println("No cards in " +
										p.getName() + "'s display\n");
								Trace.getInstance().write(this, "Server: no cards in " + 
										p.getName() + "'s display.");
							}
						}
						return true;
				} else {
					Player p = server.gameState.getPlayer(sub);
					if (p == null) { // player doesn't exist
						return false;
					} else {
						if (!(p.printDisplay())) {
							System.out.println("No cards in " +
									p.getName() + "'s display\n");
							Trace.getInstance().write(this, "Server: no cards in " + 
									p.getName() + "'s display.");
						}
						return true;
					}
				}
			case "/end":
				if (args.length != 1) { return false; } // no arguments allowed for this command
				// TODO: return true
				return false;
			case "/gamestate":
				if(!server.printGameState()){
					System.out.println("Failed to find gamestate.");
				}
				return true;
			case "/give":
				// TODO: return true
				return false;
			case "/hand":
				server.printHand(sub);
				return true;
			case "/help":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				System.out.println("Server: list of possible commands: ");
				for (Config.ServerCommand helpCmd: Config.ServerCommand.values()) {
					System.out.println("\t/" + helpCmd + helpCmd.getSyntax());
				}
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
			case "/list":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				server.listClients();
				return true;
			case "/max":
				if (args.length != 1) { return false; } // only one argument allowed
				server.setMaxPlayers(Integer.parseInt(sub));
				return true;
			case "/min":
				if (args.length != 1) { return false; } // only one argument allowed
				server.setMinPlayers(Integer.parseInt(sub));
				return true;
			case "/pardon":
				// TODO: return true
				return false;
			case "/port":  // change Server's port on which Clients connect
				if (args.length != 1) { return false; } // only one argument allowed
				int port;
				try {
					port = Integer.parseInt(args[0]);
				} catch (NumberFormatException nfe) { return false; }
				server.port = port;
				return true;
			case "/shutdown":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				server.shutdown();
				return true;
			case "/start":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				server.startGame();
				return true;
			case "/translate":
				// only one or two arguments allowed
				if ((args.length != 1) && (args.length != 2)) { return false; }
				// if second argument, it must be "-c"
				if ((args.length == 2) && (!(args[1].equalsIgnoreCase("-c")))) { return false; }
				for (Language.Dialect dialect: Language.Dialect.values()) {
					if (dialect.toString().equals(args[0])) {
						if (args.length == 2) {
							server.language = new Language(dialect, true);
							language = new Language(dialect, true);
							Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString() + 
									", with censoring.");
							System.out.println("Translating chat to " + language.getDialect().toString() + 
									", with censoring...");
						} else {
							server.language = new Language(dialect, false);
							language = new Language(dialect, false);
							Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString() + 
									", without censoring.");
							System.out.println("Translating chat to " + language.getDialect().toString() + 
									", without censoring...");
						}
						return true;
					}
				}
				break;
			default:
				break;
		}
		return false;
	}
}
