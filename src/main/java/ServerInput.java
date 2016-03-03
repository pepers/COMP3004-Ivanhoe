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
			case "/censor": // toggle the bad word censor
				if (args.length != 0) {	return false; } // check number of arguments 
				server.cmdCensor();
				break;
			case "/display":
				if (args.length == 0) { // show every player's display
					for (Player p: server.gameState.players) {
						server.printSingleDisplay(p);
					}
				} else {
					Player p = server.gameState.getPlayer(sub);
					if (p == null) { // player doesn't exist
						return false;
					} else {
						server.printSingleDisplay(p);
					}
				}
				break;
			case "/end":
				if (args.length != 1) { return false; } // no arguments allowed for this command
				// TODO: return true
				return false;
			case "/gamestate":
				if(!server.printGameState()){ System.out.println("Failed to find gamestate."); }
				break;
			case "/give":
				if (args.length < 2) {	return false; } // check number of arguments
				int playerNum;
				try {
					playerNum = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {	return false; }
				if (server.cmdGive(playerNum, String.join(" ", Arrays.copyOfRange(args, 1, args.length)))) {
					server.updateGameStates();
				}
				break;
			case "/hand":
				server.printHand(sub);
				break;
			case "/help":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				System.out.println("Server: list of possible commands: ");
				for (Config.ServerCommand helpCmd: Config.ServerCommand.values()) {
					System.out.println("\t/" + helpCmd + helpCmd.getSyntax());
				}
                break;
			case "/kick":
				if(sub.charAt(0) >= '0' && sub.charAt(0) <= '9'){
					int toRemove = Integer.parseInt(sub);
					server.removeThread(toRemove);
				} else {
					server.removeThread(sub);
				}
				break;
			case "/list":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				server.listClients();
				break;
			case "/max":
				if (args.length != 1) { return false; } // only one argument allowed
				server.setMaxPlayers(Integer.parseInt(sub));
				break;
			case "/min":
				if (args.length != 1) { return false; } // only one argument allowed
				server.setMinPlayers(Integer.parseInt(sub));
				break;
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
				break;
			case "/shutdown":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				server.shutdown();
				break;
			case "/start":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				server.startGame();
				break;
			case "/translate":
				if (args.length != 1) { return false; } // check number of arguments 
				server.cmdTranslate(args[0]);
				break;
			default:
				return false;
		}
		return true;
	}
}
