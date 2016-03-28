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

	private Boolean stop = false;					  // use to stop the Server
	private BufferedReader reader; 					  // reader from the user
	private String input;                             // user input
	private Server server;                                 // server class
	private Language language;
	
	public ServerInput (Server server, InputStream s) {
		this.server = server;
		reader = new BufferedReader(new InputStreamReader(s));
		language = new Language(Language.Dialect.none, false);
	}
	
	//Main thread execution
	public void run(){
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
	
	//returns if a valid command or not
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
	
	//process the command s
	public boolean processCmd(String s){
		Command cmd = new Command(s);
		ValidCommand args;
		
		String argJoin = String.join(" ", cmd.getArgs());
		
		// switch over command 
		switch (cmd.getCmd()) {
			case "ban":
				args = new Arguments("==", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.ban(argJoin);
				return true;
			case "censor": // toggle the bad word censor
				args = new Arguments("!=", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.cmdCensor();
				break;
			case "display":
				if (cmd.getArgs().length == 0) { // show every player's display
					if (server.getGameState() != null) {
						for (Player p: server.getGameState().getPlayers()) {
							server.printSingleDisplay(p);
						}
					} else {
						System.out.println("Error: no game started");
					}
				} else {
					Player p = server.getGameState().getPlayer(argJoin);
					if (p == null) { // player doesn't exist
						return false;
					} else {
						server.printSingleDisplay(p);
					}
				}
				break;
			case "end":
				args = new Arguments("!=", 1);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.cmdEnd(argJoin);
				break;
			case "gamestate":
				if(!server.printGameState()){ System.out.println("Failed to find gamestate."); }
				break;
			case "give":
				args = new Arguments("<", 2);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				int playerNum;
				try {
					playerNum = Integer.parseInt(cmd.getArgs()[0]);
				} catch (NumberFormatException e) {	return false; }
				if (server.cmdGive(playerNum, String.join(" ", Arrays.copyOfRange(cmd.getArgs(), 1, cmd.getArgs().length)))) {
					server.updateGameStates();
				}
				break;
			case "hand":
				args = new Arguments("==", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.printHand(argJoin);
				break;
			case "help":
				args = new Arguments("!=", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				System.out.println("Server: list of possible commands: ");
				for (Config.ServerCommand helpCmd: Config.ServerCommand.values()) {
					System.out.println("\t/" + helpCmd + helpCmd.getSyntax());
				}
                break;
			case "kick":
				args = new Arguments("==", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				if(argJoin.charAt(0) >= '0' && argJoin.charAt(0) <= '9'){
					int toRemove = Integer.parseInt(argJoin);
					server.removeThread(toRemove);
				} else {
					server.removeThread(argJoin);
				}
				break;
			case "list":
				args = new Arguments("!=", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.listClients();
				break;
			case "max":
				args = new Arguments("!=", 1);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.setMaxPlayers(Integer.parseInt(argJoin));
				break;
			case "min":
				args = new Arguments("!=", 1);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.setMinPlayers(Integer.parseInt(argJoin));
				break;
			case "pardon":
				args = new Arguments("==", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.unban(argJoin);
				return true;
			case "port":  // change Server's port on which Clients connect
				args = new Arguments("!=", 1);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				int port;
				try {
					port = Integer.parseInt(argJoin);
				} catch (NumberFormatException nfe) { return false; }
				server.port = port;
				break;
			case "shutdown":
				args = new Arguments("!=", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.shutdown();
				break;
			case "start":
				args = new Arguments("!=", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.startGame();
				break;
			case "tokens":
				args = new Arguments("!=", 0);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.cmdTokens();
				break;	
			case "translate":
				args = new Arguments("!=", 1);
				args.isValid(cmd);
				if (!cmd.isValid()) { 
					System.out.println("Error: " + cmd.getMessage());
					return false;
				} 
				server.cmdTranslate(argJoin);
				break;
			default:
				return false;
		}
		return true;
	}

	//Stop this thread
	public void shutdown() {
		stop = true;
	}
}
