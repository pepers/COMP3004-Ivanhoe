package main.java;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import main.resources.Config;
import main.resources.Language;
import main.resources.Language.Dialect;
import main.resources.Trace;

public class Client implements Runnable {

	private Thread receiveThread;						// Client thread to receive from Server
	private ClientInput inputThread = null; 			// thread to input Client commands
	private boolean stop = false; 						// use to stop the Client
	private boolean shutDown = false; 					// shutdown() has been called
	private Socket socket = null;						// socket to connect to Server
	private ObjectOutputStream clientOutputStream;		// send objects to Server
	private ObjectInputStream clientInputStream; 		// receive objects from Server
	private BufferedReader input = null; 				// to get user input
	private Language language; 							// to translate chat
	private GameState gameState = null; 				// the local copy of the game state
	private Player player = null; 						// the local copy of this client's player

	public Player getPlayer(){return player;}
	
	public Client(){
		language = new Language(Language.Dialect.none, false);
	}
	
	public static void main(String args[]) {
		Client client = new Client(); // client object
		client.startUp();
	}

	//initialize the player and game states
	public void initialize(Player p, GameState g) {
		g.addPlayer(p);
		this.player = p;
		this.gameState = g;
	}

	//initial Client startup activities
	public void startUp() {

		// welcome message
		System.out.println(" _____                _                ");
		System.out.println("|_   _|              | |               ");
		System.out.println("  | |_   ____ _ _ __ | |__   ___   ___ ");
		System.out.println("  | \\ \\ / / _` | '_ \\| '_ \\ / _ \\ / _ \\");
		System.out.println(" _| |\\ V / (_| | | | | | | | (_) |  __/");
		System.out.println("|_____\\_/ \\__,_|_| |_|_| |_|\\___/ \\___|");
		System.out.println("\nClient: Welcome brave knight!");

		// get user's name
		String username = userInput("What is thy name?: ");
		//this.action = new SetName(username);

		// initialize states
		Player p = new Player(username, 0);
		GameState g = new GameState();
		initialize(p, g);

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
				send(new SetName(this.player.getName())); // send user's name to Server
				player = (Player) receive();
				// start new thread to get Client commands
				this.inputThread = new ClientInput(this, System.in);
				this.inputThread.start();

				// start new thread to receive from Server
				this.receiveThread = new Thread(this);
				this.receiveThread.start();

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

	//shut down Client
	public boolean shutdown() {
		this.shutDown = true;
		this.stop = true;
		Trace.getInstance().write(this, "Client shutting down...");
		System.out.println("\nClient: Shutting down...");

		if (inputThread != null) {
			inputThread.shutdown();
		}

		// close socket
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
				return false;
			}
		}

		// close threads
		if (inputThread != null) {
			inputThread.shutdown();
		}
		receiveThread = null;

		Trace.getInstance().write(this, "Client shut down, successfully.");
		System.out.println("Client: fare thee well!");
		return true;
	}

	//continue receiving from Server
	public void run() {
		// while Client is running, keep connection with Server
		while (!this.stop) {
			Object o = receive(); // received object
			if (o != null) { // process object if it can be used
				process(o);
			}
		}
		if (!this.shutDown) {
			shutdown();
		}
	}

	//get user input from console
	public String userInput(String message) {
		this.input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(message);
		String strInput = "";
		try {
			strInput = this.input.readLine();
		} catch (IOException e) {
			Trace.getInstance().exception(this, e);
			return null;
		}
		Trace.getInstance().write(this, message + strInput);
		return strInput;
	}

	//connect to Server
	public Boolean connect(String IPAddress, int port) {
		try {
			Trace.getInstance().write(this, "attempting to connect to server...");
			this.socket = new Socket(IPAddress, port);
			Trace.getInstance().write(this,
					"connected to server: " + this.socket.getInetAddress() + " : " + this.socket.getLocalPort());
			this.clientOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
			this.clientInputStream = new ObjectInputStream(this.socket.getInputStream());
			return true;
		} catch (SocketException se) {
			System.out.println("Unable to connect to a server.");
			Trace.getInstance().exception(this, se);
			this.stop = true;
		} catch (UnknownHostException uhe) {
			System.out.println("Unknown Host");
			Trace.getInstance().exception(this, uhe);
			this.stop = true;
		} catch (IOException ioe) {
			System.out.println("Unexpected exception");
			Trace.getInstance().exception(this, ioe);
			this.stop = true;
		}
		return false;
	}

	//send object to Server
	public Boolean send(Object o) {
		try {
			this.clientOutputStream.writeObject(o);
			return true;
		} catch (IOException e) {
			System.out.println("Unexpected exception: writing object to output stream");
			Trace.getInstance().exception(this, e);
		}
		return false;
	}

	//receive object from Server
	public Object receive() {
		// will return this null object if error reading from stream
		Object received = null;

		try {
			received = this.clientInputStream.readObject();
		} catch (SocketException se) {
			Trace.getInstance().exception(this, se);
			this.stop = true;
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class Not Found Exception: reading object from input stream");
			Trace.getInstance().exception(this, cnf);
			this.stop = true;
		} catch (IOException ioe) {
			System.out.println("Unexpected Exception: reading object from input stream");
			Trace.getInstance().exception(this, ioe);
			this.stop = true;
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
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received");
			this.player = (Player) o;
			Trace.getInstance().write(this, this.player.getName() + ": player has been updated");
			return true;

			// GameState
		} else if (o instanceof GameState) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received");
			gameState = (GameState) o;
			//this signals that the game is done
			if (gameState.getNumPlayers() == 0){
				this.gameState = null;
				this.player.reset();
				return true;
			}
			player = gameState.getPlayer(this.player.getName());
			this.player = gameState.getPlayer(this.player.getName());
			Trace.getInstance().write(this, this.player.getName() + ": game state has been updated");
			return true;

			// ActionCard
		} else if (o instanceof ActionCard) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received");
			this.player.addToHand((ActionCard) o);
			System.out.println("Client: " + o.toString() + " added to hand");
			Trace.getInstance().write(this, this.player.getName() + ": " + o.toString() + " added to hand");
			return true;

			// DisplayCard
		} else if (o instanceof DisplayCard) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received");
			this.player.addToHand((DisplayCard) o);
			System.out.println("Client: " + o.toString() + " added to hand");
			Trace.getInstance().write(this, this.player.getName() + ": " + o.toString() + " added to hand");
			return true;

			/* ACTIONS: */
			// Chat
		} else if (o instanceof Chat) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received: "
					+ ((Chat) o).getMessage());
			String message = ((Chat) o).getMessage();
			System.out.println(this.language.translate(message));
			return true;
			// Prompt
		} else if (o instanceof Prompt) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName()
					+ " was prompted: " + ((Prompt) o).getMessage());
			String s = userInput(((Prompt) o).getMessage());
			send(new Prompt(s));
			return true;

			// unrecognized object
		} else {
			Exception e = new Exception(this.player.getName() + ": unrecognized object received");
			Trace.getInstance().exception(this, e);
			return false;
		}
	}

	//deals with commands received from inputThread
	public boolean processCmd(String s) {
		// get argument line
		String[] cmd = s.split("\\s+"); // array of command + arguments
		String[] args = Arrays.copyOfRange(cmd, 1, cmd.length); // just arguments
		// switch over command
		switch (cmd[0]) {
		case "/censor": // toggle the bad word censor
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdCensor();
			break;
		case "/display": // look at display cards
			cmdDisplay(args);
			break;
		case "/end": // end turn
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdEnd();
			break;
		case "/gamestate": // show gamestate
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdGameState(gameState);
			break;
		case "/hand": // look at cards in hand
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdHand();
			break;
		case "/help":
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdHelp();
			break;
		case "/list":
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdList();
			break;
		case "/play":
			if (args.length != 1) {
				return false;
			} // check number of arguments
			if (!tournamentAction(cmd[0])) {
				return false;
			} // checks if in tournament
			cmdPlay(args[0]);
			break;
		case "/ready":
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdReady();
			break;
		case "/setname":
			if (args.length == 0) {
				return false;
			} // check number of arguments
			cmdSetname(args);
			break;
		case "/shutdown":
			if (args.length != 0) {
				return false;
			} // check number of arguments
			shutdown();
			break;
		case "/tokens":
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdTokens();
			break;
		case "/tournament":
			if (!((args.length == 2) || (args.length == 1))) {
				return false;
			} // check number of arguments
			cmdTournament(args);
			break;
		case "/translate":
			if (args.length != 1) {
				return false;
			} // check number of arguments
			cmdTranslate(args[0]);
			break;
		case "/withdraw":
			if (args.length != 0) {
				return false;
			} // check number of arguments
			cmdWithdraw();
			break;
		default:
			return false;
		}
		return true;
	}

	/*
	 * checks if player is in tournament and warns that an action can't be taken
	 * if they are not
	 */
	public boolean tournamentAction(String cmd) {
		if (!(this.player.getParticipation())) {
			System.out.println("Client: can't perform that action while not in a tournament");
			Trace.getInstance().write(this, this.player.getName() + ": can't use " + cmd + " while not in tournament.");
			return false;
		}
		return true;
	}

	//toggle censoring of bad words
	public boolean cmdCensor() {
		Dialect dialect = this.language.getDialect();
		boolean censor = !this.language.isCensored(); // toggle censor
		this.language = new Language(dialect, censor);
		// censored
		if (censor) {
			System.out.println("Client: now censoring bad language.");
			Trace.getInstance().write(this, "Client: now censoring bad language.");
			// not censored
		} else {
			System.out.println("Client: no longer censoring bad language.");
			Trace.getInstance().write(this, "Client: no longer censoring bad language.");
		}
		return true;
	}

	//print out displays
	public boolean cmdDisplay(String[] arr) {
		String args = String.join(" ", arr); // join arguments into one string

		// show own display
		if (arr.length == 0) {
			if (!(this.player.printDisplay(gameState.getTournamentColour()))) {
				System.out.println("Client: no cards in your display");
				Trace.getInstance().write(this, this.player.getName() + ": No cards in your display to show.");
			}
			// show all displays
		} else if (args.equalsIgnoreCase("-a")) {
			for (Player p : this.gameState.getPlayers()) {
				if (!(p.printDisplay(gameState.getTournamentColour()))) {
					System.out.println("Client: no cards in " + p.getName() + "'s display\n");
					Trace.getInstance().write(this,
							this.player.getName() + ": No cards in " + p.getName() + "'s display.");
				}
			}
			// show someone else's display
		} else {
			Player p = this.gameState.getPlayer(args);
			if (p == null) { // player doesn't exist
				Trace.getInstance().write(this, args + " doesn't exist.  Can't print their Display.");
				System.out.println("Client: " + args + " doesn't exist.  Can't print their Display.");
				return false;
			} else {
				if (!(p.printDisplay(gameState.getTournamentColour()))) {
					System.out.println("Client: no cards in " + p.getName() + "'s display\n");
					Trace.getInstance().write(this,
							this.player.getName() + ": No cards in " + p.getName() + "'s display.");
				}
			}
		}
		return true;
	}

	//end your turn
	public boolean cmdEnd() {
		if (!this.player.isTurn) {
			System.out.println("Client: Its not your turn.");
			return false;
		} else if (gameState.getTournament() == null && this.player.hasValidDisplayCard("none")) {
			System.out.println("Client: You MUST start a tournament if able.");
			return false;
		} else {
			this.player.isTurn = false;
			send(new EndTurn());
			return true;
		}
	}
	
	//show cards in hand
	public boolean cmdHand() {
		if (this.player.getHand().isEmpty()) {
			System.out.println("Client: You have no cards in your hand.");
		} else {
			System.out.println("Client: You have the following cards in your hand: ");
			for (Card card : this.player.getHand()) {
				System.out.println("\t- " + card.toString());
			}
		}
		return true;
	}

	//list possible commands and their corresponding syntax
	public boolean cmdHelp() {
		System.out.println("Client: list of possible commands: ");
		for (Config.ClientCommand helpCmd : Config.ClientCommand.values()) {
			System.out.println("\t/" + helpCmd + helpCmd.getSyntax());
		}
		return true;
	}

	//list other players in game
	public boolean cmdList() {
		System.out.println("- State    : Player ");
		for (int i = 0; i < this.gameState.getPlayers().size(); i++) {
			Player p = this.gameState.getPlayers().get(i);
			String name = p.getName();
			if (name == this.player.getName()) { // found yourself
				name += " (you)";
			}
			System.out.printf("%-10s : %s\n", p.getReadyState(), name);
		}
		return true;
	}

 	//play a card
	public boolean cmdPlay(String card) {
		Card c = this.player.getCard(card); // get the card the user asked for

		// card doesn't exist in hand
		if (c == null) {
			System.out.println(
					"Client: you don't have the card: " + card + "\n\t Type '/hand' to view the cards in your hand.");
			return false;
			// not the player's turn
		} else if (!this.player.isTurn) {
			if (c.toString().equalsIgnoreCase("ivanhoe")) {
				// card to be player is the Ivanhoe action card:
				send(new Play(c));
				return true;
			} else {
				System.out.println("Client: you may not play that card when it is not your turn");
				return false;
			}
			// is player's turn
		} else {
			if (c instanceof DisplayCard) {
				if (gameState.getTournament() == null) {
					System.out.println("Client: no tournament is running, start one with /tournament");
					return false;
				} else if (!(((DisplayCard) c).getColour().equals("none")
						|| gameState.getTournament().getColour().equals(((DisplayCard) c).getColour()))) {
					System.out.println("Client: not a valid color for the current tournament");
					return false;
				}
				// action card
			} else {
				if (gameState.getTournament() == null) {
					System.out.println("Client: no tournament is running, start one with /tournament");
					return false;
				}
			}
		}
		send(new Play(c));
		return true;
	}

	//player is ready to start game
	public boolean cmdReady() {
		send(new Ready());
		return true;
	}

	//player changes their name
	public boolean cmdSetname(String[] arr) {
		String args = String.join(" ", arr); // join arguments into one string
		
		// check for invalid names
		if ((args.equals("")) || (args.startsWith("-") || (args.startsWith("/")))) {
			Trace.getInstance().write(this, "can't change name to '" + args + "'. Invalid name.");
			System.out.println("Client: can't change name to '" + args + "'. Invalid name.");
			return false;
			// valid name
		} else {
			send(new SetName(args));
			return true;
		}
	}

	//view everyone's tokens
	public boolean cmdTokens() {
		if (this.gameState.getNumPlayers() < 1) {
			System.out.println("Client: there are no players in the game.");
			return false;
		} else {
			System.out.println("Client: listing tokens: ");
		}
		for (Player p : this.gameState.getPlayers()) {
			Trace.getInstance().write(this, "Tokens: " + p.getName() + " : " + p.listTokens());
			System.out.printf("%-20s: %s\n", p.getName(), p.listTokens());
		}
		return true;
	}

	//start a tournament
	public boolean cmdTournament(String[] args) {

		// Check that no tournaments are running
		if (this.gameState.getTournament()!= null) {
			System.out.println("Client: a tournament is already in progress");
			Trace.getInstance().write(this,
					this.player.getName() + ": can't use /tournament, a tournament is already in progress.");
			return false;
		}
		// Check if its their turn
		if (!(this.player.isTurn)) {
			System.out.println("Client: you may not start a tournament when it is not your turn");
			return false;
		}

		String strCard, colour;
		if (args.length == 2) {
			strCard = args[1];
		} else {
			strCard = args[0];
		}
		
		Card card = this.player.getCard(strCard); // get card to start tournament with
		if (card == null){
			System.out.println("Client: you don't have the card: " + strCard
			+ "\n\t Type '/hand' to view the cards in your hand.");
			return false;
		}
		if (!(card instanceof DisplayCard)) {
			System.out.println("Client: " + card.toString() + " is not a display card.");
			return false;
		}
		DisplayCard displayCard = (DisplayCard) card;
		
		if (args.length == 2) {
			colour = args[0];
		}else{
			colour = displayCard.getColour();
		}
		if ((!colour.equals("purple")) && (!colour.equals("red")) && (!colour.equals("blue"))
				&& (!colour.equals("yellow")) && (!colour.equals("green"))) {
			System.out.println("Client: " + colour + " is not a valid tournament colour. Type '/help'.");
			return false;
		}
			
		// last tournament was purple (another colour must be chosen)
		if ((gameState.getLastColour().equals("purple")) && (colour.equals("purple"))) {
			System.out.println("Client: the last tournament was Jousting (purple). "
					+ "\n A tournament of a different colour must be started.");
			return false;
		}

		// display card is not a squire or maiden
		if (!displayCard.getColour().equals("none")) {
			// tournament colour selected doesn't equal display card colour
			if (!displayCard.getColour().equals(colour)) {
				System.out.println("Client: " + displayCard.toString()
						+ " does not match the colour of the tournament you are trying to start. "
						+ "\n\t Type '/hand' to view the cards in your hand.");
				return false;
			}
		}
		send(new StartTournament(colour, displayCard));
		return true;
	}

	//change the language to translate chat messages with
	public boolean cmdTranslate(String d) {
		boolean censor = this.language.isCensored();
		// no translating
		if (Language.Dialect.none.toString().equals(d)) {
			this.language = new Language(Language.Dialect.none, censor);
			Trace.getInstance().write(this, "Client: no longer translating chat messages");
			System.out.println("Client: no longer translating chat messages");
			return true;
		}
		// translating
		for (Language.Dialect dialect : Language.Dialect.values()) {
			if (dialect.toString().equals(d)) {
				this.language = new Language(dialect, censor);
				Trace.getInstance().write(this, "Client: translating chat to " + this.language.getDialect().toString());
				System.out.println("Client: translating chat to " + this.language.getDialect().toString());
				return true;
			}
		}
		return false;
	}

	//withdraw from the current tournament
	public boolean cmdWithdraw() {
		send(new Withdraw());
		this.player.setParticipation(false);
		this.player.isTurn = false;
		return true;
	}

	//print a modicum of gamestate
	public boolean cmdGameState(GameState g) {
		if (g == null) {
			return false;
		}
		System.out.println("Gamestate::");
		if (g.getTournament() == null) {
			System.out.println("No tournament running.");
		}
		for (Player p : g.getPlayers()) {
			System.out.println(p.getName() + ":" + p.getId());
			System.out.println("  HAND:" + p.getHandSize() + "\n  TURN:" + p.isTurn + "\n  TOUR:" + p.getParticipation() + "\n  ");
		}
		return true;
	}
}
