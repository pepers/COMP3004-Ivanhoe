package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class ClientInput extends Thread{

	Boolean stop = false;  // use to stop the Client
	BufferedReader reader; // reader from the user
	String input;          // user input
	Client client;         // client class
	Action action;         // client's action to take
	Language language;     // to translate chat
	
	public ClientInput (Client client, InputStream s) {
		this.client = client;
		reader = new BufferedReader(new InputStreamReader(s));
		
		// set up language to translate chat
		language = new Language(Language.Dialect.none);
	}
	
	public void run () {
		while(!stop) {
			try {
				input = reader.readLine(); // get next line of input
			} catch (IOException e) {
				e.printStackTrace();
			} 
			if(input.length() > 0){
				if (validCmd(input)) {               // process valid commands
					if (processCmd(input)) {
						Trace.getInstance().write(this, client.player.username +
									": command processed: " + input);
					} else {
						Trace.getInstance().write(this, client.player.username +
									": invalid command: " + input);
						System.out.println("Client: invalid command, try typing '/help' for more info.");
					}
				} else if (input.charAt(0) == '/') { // process invalid commands
					System.out.println("Client: invalid command, try typing '/help' for more info.");
					Trace.getInstance().write(this, client.player.username +
									": invalid command: " + input);
				} else {                             // process chat
					String translated = language.translate(input);
					action = new Chat(translated); 
					client.send(action);
					Trace.getInstance().write(this, client.player.username + ": " + 
									"chat sent: " + input);
				}
			}
		}
	}
	
	/*
	 * shut down Client
	 */
	private boolean shutdown() {
		stop = true;
		client.shutdown();
		return true;
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
	
	public boolean processCmd (String s){
		// get argument line
		String[] cmd = s.split("\\s+");                         // array of command + arguments
		String[] args = Arrays.copyOfRange(cmd, 1, cmd.length); // just arguments
		String sub = String.join(" ", args);                    // join arguments into one string
		
		// switch over command 
		switch (cmd[0]) {
			case "/draw":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				action = new DrawCard();
				client.send(action);
				return true;
			case "/hand":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				if (client.player.getHand().isEmpty()) {
					System.out.println("Client: You have no cards in your hand.");
					return true;
				}
				System.out.println("Client: You have the following cards in your hand: ");
				for (Card card: client.player.getHand()) {
					System.out.println("/t- " + card.toString());
				}
				return true;
			case "/help":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				System.out.println("Client: list of possible commands: ");
				for (Config.ClientCommand helpCmd: Config.ClientCommand.values()) {
					System.out.println("\t/" + helpCmd + helpCmd.getSyntax());
				}
				return true;
			case "/play":
				Card c = client.player.getCard(sub);
				if(c == null){
					System.out.println("Client: you don't have the card: " + sub + 
							"\n\t Type '/hand' to view the cards in your hand.");
					return false;
				}
				action = new Play(c);
				client.send(action);
				return true;
			case "/ready":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				action = new Ready();
				client.send(action);
				return true;
			case "/setname":
				action = new SetName(sub);
				client.send(action);
				return true;
			case "/shutdown":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				shutdown();
				return true;
			case "/translate":
				if (args.length != 1) { return false; } // only one argument allowed
				for (Language.Dialect dialect: Language.Dialect.values()) {
					if (dialect.toString().equals(args[0])) {
						client.language = new Language(dialect);
						language = new Language(dialect);
						Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString());
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
