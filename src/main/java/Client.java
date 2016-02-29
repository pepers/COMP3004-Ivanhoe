package main.java;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class Client implements Runnable {

	Thread receiveThread; // Client thread to receive from Server
	ClientInput inputThread = null; // thread to input Client commands
	Boolean stop = false; // use to stop the Client
	Action action; // client's action
	private Socket socket = null; // socket to connect to Server
	ObjectOutputStream clientOutputStream; // send objects to Server
	ObjectInputStream clientInputStream; // receive objects from Server
	BufferedReader input = null; // to get user input
	Language language; // to translate chat

	GameState game; // the local copy of the game state
	Player player = new Player("Default Name"); // the local copy of this
												// client's player

	public static void main(String args[]) {
		Client client = new Client(); // client object
		client.startUp();
	}

	/*
	 * initial Client startup activities
	 */
	public void startUp() {

		// welcome message
		System.out.println(" _____                _                ");
		System.out.println("|_   _|              | |               ");
		System.out.println("  | |_   ____ _ _ __ | |__   ___   ___ ");
		System.out.println("  | \\ \\ / / _` | '_ \\| '_ \\ / _ \\ / _ \\");
		System.out.println(" _| |\\ V / (_| | | | | | | | (_) |  __/");
		System.out.println("|_____\\_/ \\__,_|_| |_|_| |_|\\___/ \\___|");
		System.out.println("\nClient: Welcome brave knight!");

		// set up language to translate chat
		language = new Language(Language.Dialect.none, false);

		// get user's name
		String username = userInput("What is thy name?: ");
		action = new SetName(username, true);
		player.username  = username;

		// connect to Server
		boolean search = true;
		while (search) {
			// get the Server's inet address
			String address = userInput(
					"Where are the tournaments to be held? (enter nothing for default InetAddress): ");
			if (address.isEmpty()) {
				address = Config.DEFAULT_HOST;
			}

			// get the Server's port
			int port;
			while (true) {
				String portStr = userInput("At which arena? (enter nothing for default Port): ");
				if (portStr.isEmpty()) {
					port = Config.DEFAULT_PORT;
					break;
				} else {
					try {
						port = Integer.parseInt(portStr);
					} catch (NumberFormatException nfe) {
						System.out.println("Please enter a port number...");
						continue;
					}
					break;
				}
			}

			// attempt to connect to Server
			if (connect(address, port)) {
				send(action); // send user's name to Server

				// start new thread to get Client commands
				inputThread = new ClientInput(this, System.in);
				inputThread.start();

				// start new thread to receive from Server
				receiveThread = new Thread(this);
				receiveThread.start();

				// init gamestate
				game = new GameState();
				break;
			} else {
				System.out.println("There are no tournaments at that location! \n");

				while (true) {
					String ui = userInput("Do you want to search for a new tournament (y/n): ");
					if (ui.equalsIgnoreCase("y")) {
						break;
					} else if (ui.equalsIgnoreCase("n")) {
						System.out.println("Client: fare thee well!");
						search = false;
						break;
					} else {
						System.out.println("That is not an option, my good knight!");
					}
				}
			}
		}

	}

	/*
	 * shut down Client
	 */
	public boolean shutdown() {
		Trace.getInstance().write(this, "Client shutting down...");
		System.out.println("\nClient: Shutting down...");

		// close socket
		try {
			socket.close();
		} catch (IOException e) {
			Trace.getInstance().exception(this, e);
			return false;
		}

		// close threads
		stop = true;
		receiveThread = null;
		inputThread.stop = true;
		inputThread = null;
	
		/*
		// close scanner
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
			}
		}
		*/
	
		/*
		// close socket streams
		if (clientInputStream != null) {
			try {
				clientInputStream.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
				return false;
			}
		}
		if (clientOutputStream != null) {
			try {
				clientOutputStream.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
				return false;
			}
		}
		*/
		

		Trace.getInstance().write(this, "Client shut down, successfully.");
		System.out.println("Client: fare thee well!");
		return true;
	}

	/*
	 * continue receiving from Server
	 */
	public void run() {
		// while Client is running, keep connection with Server
		while (!stop) {
			Object o = receive(); // received object
			if (o != null) { // process object if it can be used
				process(o);
			}
		}
	}

	/*
	 * get user input from console
	 */
	public String userInput(String message) {
		input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(message);
		String strInput = "";
		try {
			strInput = input.readLine();
		} catch (IOException e) {
			Trace.getInstance().exception(this, e);
			return null;
		}
		Trace.getInstance().write(this, message + strInput);
		return strInput;
	}

	/*
	 * connect to Server
	 */
	public Boolean connect(String IPAddress, int port) {
		try {
			Trace.getInstance().write(this, "attempting to connect to server...");
			this.socket = new Socket(IPAddress, port);
			Trace.getInstance().write(this,
					"connected to server: " + socket.getInetAddress() + " : " + socket.getLocalPort());

			clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
			clientInputStream = new ObjectInputStream(socket.getInputStream());
			return true;
		} catch (SocketException se) {
			System.out.println("Unable to connect to a server.");
			Trace.getInstance().exception(this, se);
		} catch (UnknownHostException uhe) {
			System.out.println("Unknown Host");
			Trace.getInstance().exception(this, uhe);
		} catch (IOException ioe) {
			System.out.println("Unexpected exception");
			Trace.getInstance().exception(this, ioe);
		}
		return false;
	}

	/*
	 * send object to Server
	 */
	public Boolean send(Object o) {
		try {
			clientOutputStream.writeObject(o);
			return true;
		} catch (IOException e) {
			System.out.println("Unexpected exception: writing object to output stream");
			Trace.getInstance().exception(this, e);
		}
		return false;
	}

	/*
	 * receive object from Server
	 */
	public Object receive() {
		// will return this null object if error reading from stream
		Object received = null;

		try {
			received = clientInputStream.readObject();
		} catch (SocketException se) {
			if (!(stop)) {
				System.out.println("Server was closed.");
				Trace.getInstance().exception(this, se);
				shutdown();
			}
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class Not Found Exception: reading object from input stream");
			Trace.getInstance().exception(this, cnf);
			shutdown();
		} catch (IOException ioe) {
			System.out.println("Unexpected Exception: reading object from input stream");
			Trace.getInstance().exception(this, ioe);
			shutdown();
		}

		return received;
	}

	/*
	 * determine type of object received and process accordingly return: true if
	 * recognized object, false if unrecognized object
	 */
	public boolean process(Object o) {
		/* STATES: */
		// Player
		if (o instanceof Player) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			player = (Player) o;
			Trace.getInstance().write(this, player.username + ": player has been updated");
			return true;

			// GameState
		} else if (o instanceof GameState) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			game = (GameState) o;	
			System.out.println("Got a gamestate where " + game.getPlayer(player.username).username + " turn=" + game.getPlayer(player.username).isTurn);
			player = game.getPlayer(player.username);			
			Trace.getInstance().write(this, player.username + ": game state has been updated");
			return true;

			// ActionCard
		} else if (o instanceof ActionCard) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			player.addHand((ActionCard) o);
			System.out.println("Client: " + o.toString() + " added to hand");
			Trace.getInstance().write(this, player.username + ": " + o.toString() + " added to hand");
			return true;

			// DisplayCard
		} else if (o instanceof DisplayCard) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			player.addHand((DisplayCard) o);
			System.out.println("Client: " + o.toString() + " added to hand");
			Trace.getInstance().write(this, player.username + ": " + o.toString() + " added to hand");
			return true;

			/* ACTIONS: */
			// Chat
		} else if (o instanceof Chat) {
			Trace.getInstance().write(this,
					player.username + ": " + o.getClass().getSimpleName() + " received: " + ((Chat) o).getMessage());
			String message = ((Chat) o).getMessage();
			System.out.println(language.translate(message));
			return true;

			// unrecognized object
		} else {
			Exception e = new Exception(player.username + ": unrecognized object received");
			Trace.getInstance().exception(this, e);
			return false;
		}
	}
	
	/*
	 * deals with commands received from inputThread
	 */
	public boolean processCmd (String s){
		// get argument line
		String[] cmd = s.split("\\s+");                         // array of command + arguments
		String[] args = Arrays.copyOfRange(cmd, 1, cmd.length); // just arguments
		String sub = String.join(" ", args);                    // join arguments into one string
		
		// switch over command 
		switch (cmd[0]) {
			case "/display":
				if (args.length == 0) { // show own display
					if (!(player.printDisplay())) {
						System.out.println("Client: no cards in your display");
						Trace.getInstance().write(this, player.username + 
								": No cards in your display to show.");
					}
					return true;
				} else { // show someone else's display
					if (sub.equalsIgnoreCase("-a")) { // show all displays
						for (Player p: game.players) {
							if (!(p.printDisplay())) {
								System.out.println("Client: no cards in " +
										p.username + "'s display\n");
								Trace.getInstance().write(this, player.username +
										": No cards in " + p.username + "'s display.");
							}
						}
						return true;
					} else {
						Player p = game.getPlayer(sub);
						if (p == null) { // player doesn't exist
							return false;
						} else {
							if (!(p.printDisplay())) {
								System.out.println("Client: no cards in " +
										p.username + "'s display\n");
								Trace.getInstance().write(this, player.username +
										": No cards in " + p.username + "'s display.");
							}
							return true;
						}
					}
				}
			case "/end":  // end turn
				if (args.length != 0) { return false; } // no arguments allowed for this command
				player.isTurn = false;
				action = new EndTurn();
				send(action);
				return true;
			case "/hand":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				if (player.getHand().isEmpty()) {
					System.out.println("Client: You have no cards in your hand.");
					return true;
				}
				System.out.println("Client: You have the following cards in your hand: ");
				for (Card card: player.getHand()) {
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
				for (int i=0; i<game.players.size(); i++) {
					Player p = game.players.get(i);
					String name = p.username;
					if (name == player.username) { // found yourself
						name += " (you)";
					}
					System.out.printf("%-10s : %s\n", p.getReadyState(), name);
				}
				return true;
			case "/play":
				if (!(player.inTournament)) { // not in tournament
					System.out.println("Client: can't perform that action while not in a tournament");
					Trace.getInstance().write(this, player.username + 
							": can't use " + cmd[0] + " while not in tournament.");
					return true;
				}
				if (args.length != 1) { return false; } // command must have exactly one argument
				Card c = player.getCard(sub);
				if(c == null){
					System.out.println("Client: you don't have the card: " + sub + 
							"\n\t Type '/hand' to view the cards in your hand.");
					return true;
				}
				if (!(player.isTurn)) { // not your turn
					// card to be player is not the Ivanhoe action card
					if (c.toString().equalsIgnoreCase("ivanhoe")) { 
						System.out.println("Client: you may not play that card when it is not your turn");
						return true; 
					}
				}
				action = new Play(c);
				send(action);
				return true;
			
			case "/ready":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				action = new Ready();
				send(action);
				return true;
			case "/setname":
				if ((sub == "") || (sub.startsWith("-"))) { return false; }
				action = new SetName(sub);
				send(action);
				return true;
			case "/shutdown":
				if (args.length != 0) { return false; } // no arguments allowed for this command
				shutdown();
				return true;
			case "/tournament":
				if (game.tnmt != null){
					System.out.println("Client: a tournament is already in progress");
					Trace.getInstance().write(this, player.username + 
							": can't use " + cmd[0] + " while not in tournament.");
					return true;
				}
				if (!(player.isTurn)) { // not your turn
					System.out.println("Client: you may not start a tournament when it is not your turn");
					return true; 
				}
				Card card = player.getCard(args[0]);
				if(card instanceof ActionCard){ // not a display card
					System.out.println("Client: " + card.toString() +" is not a display card.");
					return true;
				}
				DisplayCard displayCard = (DisplayCard) card;
				if(displayCard == null){  // don't have the card in hand
					System.out.println("Client: you don't have the card: " + args[0] + 
							"\n\t Type '/hand' to view the cards in your hand.");
					return true;
				}				
				if (args.length == 1){
					action = new StartTournament(displayCard, displayCard.getColour());
				}else if(args.length == 2){
					action = new StartTournament(displayCard, args[1]);
				}
				send(action);
				return true;
			case "/translate":
				// only one or two arguments allowed
				if ((args.length != 1) && (args.length != 2)) { return false; }
				// if second argument, it must be "-c"
				if ((args.length == 2) && (!(args[1].equalsIgnoreCase("-c")))) { return false; }
				for (Language.Dialect dialect: Language.Dialect.values()) {
					if (dialect.toString().equals(args[0])) {
						if (args.length == 2) {
							language = new Language(dialect, true);
							language = new Language(dialect, true);
							Trace.getInstance().write(this, "Translating chat to " + language.getDialect().toString() + 
									", with censoring.");
							System.out.println("Client: Translating chat to " + language.getDialect().toString() + 
									", with censoring...");
						} else {
							language = new Language(dialect, false);
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
				if (!(player.inTournament)) { // not in tournament
					System.out.println("Client: can't perform that action while not in a tournament");
					Trace.getInstance().write(this, player.username + 
							": can't use " + cmd[0] + " while not in tournament.");
					return true;
				}
				if (args.length != 0) { return false; } // no arguments allowed for this command
				action = new Withdraw();
				send(action);
				player.inTournament = false;
				return true;
			default:
				break;
		}
		return false;
	}
}
