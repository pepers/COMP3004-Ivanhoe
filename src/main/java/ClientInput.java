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
		language = new Language(Language.Dialect.none, false);
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
					if (client.processCmd(input)) {
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
	/*
	public boolean processCmd (String s){
		// get argument line
		String[] cmd = s.split("\\s+");                         // array of command + arguments
		String[] args = Arrays.copyOfRange(cmd, 1, cmd.length); // just arguments
		String sub = String.join(" ", args);                    // join arguments into one string
		
		// switch over command 
		switch (cmd[0]) {
			case "/display":
				if (args.length == 0) { // show own display
					if (!(client.player.printDisplay())) {
						System.out.println("Client: no cards in your display");
						Trace.getInstance().write(this, client.player.username + 
								": No cards in your display to show.");
					}
					return true;
				} else { // show someone else's display
					if (sub.equalsIgnoreCase("-a")) { // show all displays
						for (Player p: client.game.players) {
							if (!(p.printDisplay())) {
								System.out.println("Client: no cards in " +
										p.username + "'s display\n");
								Trace.getInstance().write(this, client.player.username +
										": No cards in " + p.username + "'s display.");
							}
						}
						return true;
					} else {
						Player p = client.game.getPlayer(sub);
						if (p == null) { // player doesn't exist
							return false;
						} else {
							if (!(p.printDisplay())) {
								System.out.println("Client: no cards in " +
										p.username + "'s display\n");
								Trace.getInstance().write(this, client.player.username +
										": No cards in " + p.username + "'s display.");
							}
							return true;
						}
					}
				}
			case "/end":  // end turn
				if (args.length != 0) { return false; } // no arguments allowed for this command
				client.player.isTurn = false;
				action = new EndTurn();
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
					System.out.println("\t- " + card.toString());
				}
				return true;
			case "/help":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				System.out.println("Client: list of possible commands: ");
				for (Config.ClientCommand helpCmd: Config.ClientCommand.values()) {
					System.out.println("\t/" + helpCmd + helpCmd.getSyntax());
				}
				return true;
			case "/list":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				System.out.println("- State    : Player ");
				for (int i=0; i<client.game.players.size(); i++) {
					Player p = client.game.players.get(i);
					String name = p.username;
					if (name == client.player.username) { // found yourself
						name += " (you)";
					}
					System.out.printf("%-10s : %s\n", p.getReadyState(), name);
				}
				return true;
			case "/play":
				if (!(client.player.inTournament)) { // not in tournament
					System.out.println("Client: can't perform that action while not in a tournament");
					Trace.getInstance().write(this, client.player.username + 
							": can't use " + cmd[0] + " while not in tournament.");
					return true;
				}
				if (args.length != 1) { return false; } // command must have exactly one argument
				Card c = client.player.getCard(sub);
				if(c == null){
					System.out.println("Client: you don't have the card: " + sub + 
							"\n\t Type '/hand' to view the cards in your hand.");
					return true;
				}
				if (!(client.player.isTurn)) { // not your turn
					// card to be player is not the Ivanhoe action card
					if (c.toString().equalsIgnoreCase("ivanhoe")) { 
						System.out.println("Client: you may not play that card when it is not your turn");
						return true; 
					}
				}
				action = new Play(c);
				client.send(action);
				return true;
			case "/tournament":
				if (client.game.tnmt != null){
					System.out.println("Client: a tournament is already in progress");
					Trace.getInstance().write(this, client.player.username + 
							": can't use " + cmd[0] + " while not in tournament.");
					return true;
				}
				Card card = client.player.getCard(args[0]);
				if(card instanceof ActionCard){
					System.out.println("Client: " + card.toString() +" is not a display card.");
					return true;
				}
				DisplayCard displayCard = (DisplayCard) card;
				if(displayCard == null){
					System.out.println("Client: you don't have the card: " + args[0] + 
							"\n\t Type '/hand' to view the cards in your hand.");
					return true;
				}
				if (!(client.player.isTurn)) { // not your turn
					// card to be player is not the Ivanhoe action card
					System.out.println("Client: you may not play that card when it is not your turn");
					return true; 
				}
				
				if (args.length == 1){
					action = new StartTournament(displayCard, displayCard.getColour());
				}else if(args.length == 2){
					action = new StartTournament(displayCard, args[1]);
				}
				client.send(action);
				return true;
			case "/ready":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				action = new Ready();
				client.send(action);
				return true;
			case "/setname":
				if ((sub == "") || (sub.startsWith("-"))) { return false; }
				action = new SetName(sub);
				client.send(action);
				return true;
			case "/shutdown":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				shutdown();
				return true;
			case "/translate":
				// only one or two arguments allowed
				if ((args.length != 1) && (args.length != 2)) { return false; }
				// if second argument, it must be "-c"
				if ((args.length == 2) && (!(args[1].equalsIgnoreCase("-c")))) { return false; }
				for (Language.Dialect dialect: Language.Dialect.values()) {
					if (dialect.toString().equals(args[0])) {
						if (args.length == 2) {
							client.language = new Language(dialect, true);
							language = new Language(dialect, true);
							Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString() + 
									", with censoring.");
							System.out.println("Client: Translating chat to " + language.getDialect().toString() + 
									", with censoring...");
						} else {
							client.language = new Language(dialect, false);
							language = new Language(dialect, false);
							Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString() + 
									", without censoring.");
							System.out.println("Client: Translating chat to " + language.getDialect().toString() + 
									", without censoring...");
						}
						return true;
					}
				}
				break;
			case "/withdraw":
				if (!(client.player.inTournament)) { // not in tournament
					System.out.println("Client: can't perform that action while not in a tournament");
					Trace.getInstance().write(this, client.player.username + 
							": can't use " + cmd[0] + " while not in tournament.");
					return true;
				}
				if (args.length != 0) { return false; } // no arguments allowed for this command
				action = new Withdraw();
				client.send(action);
				client.player.inTournament = false;
				return true;
			default:
				break;
		}
		return false;
	}
	*/
}
