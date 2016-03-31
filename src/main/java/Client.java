package main.java;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import main.java.ClientView;
import main.java.ClientView.ConsoleView;
import main.resources.Config;
import main.resources.Language;
import main.resources.Language.Dialect;
import main.resources.Trace;

public class Client implements Runnable {

	public Thread receiveThread; // Client thread to receive from Server
	public ClientInput inputThread = null; // thread to input Client commands
	private boolean stop = false; // use to stop the Client
	private boolean shutDown = false; // shutdown() has been called
	private Socket socket = null; // socket to connect to Server
	private ObjectOutputStream clientOutputStream; // send objects to Server
	private ObjectInputStream clientInputStream; // receive objects from Server
	private BufferedReader input = null; // to get user input
	private Language language; // to translate chat
	private GameState gameState = null; // the local copy of the game state
	private Player player = null; // the local copy of this client's player
	private ClientView view = null;
	private static boolean gui = true; // start in gui mode or not (command line mode)
	
	public Prompt lastPrompt = null;
	
	// AI Client
	public boolean ai = false;
	public String aiPrompt = null;
	public ArrayList<Object> promptOptions = null;
	
	// testing methods
	public void setGui(boolean b) {
		gui = b;
	}
	
	public void setGameState(GameState g) {
		gameState = g;
		this.player = gameState.getPlayer(this.player.getName());
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public GameState getGameState() {
		return gameState;
	}

	public Client() {
		language = new Language(Language.Dialect.none, false);
	}

	public static void main(String args[]) {
		// GUI Mode - no command line arguments
		if (args.length == 0) {
			gui = true;
			
		// Command Line Mode - command line argument = "-c"
		} else if (args[0].equals("-c")) {
			gui = false;
			
		// invalid arguments
		} else {
			System.out.println("Error: Invalid command line arguments.");
			System.exit(0);
		}
				
		Client client = new Client(); // client object
		client.startUp();
	}

	// initialize the player and game states
	public void initialize(String name) {
		Player p = new Player(name, 0);
		GameState g = new GameState();
		initialize(p, g);
	}
	
	public void initialize(Player p, GameState g) {
		g.addPlayer(p);
		this.player = p;
		this.gameState = g;
	}

	// initial Client startup activities
	public void startUp() {
		if (gui) {
			view = new ClientView(this);
		} else {
			cliStartUp();
		}
	}
	
	/*
	 * gui start up
	 */
	public boolean guiStartUp(String address, int port) {
		// attempt to connect to Server
		if (connect(address, port)) {
			send(player); // send user's name to Server
			player = (Player) receive();
			// start new thread to get Client commands
			this.inputThread = new ClientInput(this, System.in);
			this.inputThread.start();

			// start new thread to receive from Server
			this.receiveThread = new Thread(this);
			this.receiveThread.start();				
			
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 *  command line start up
	 */
	public void cliStartUp() {
		// welcome message
		outputText(" _____                _                ");
		outputText("|_   _|              | |               ");
		outputText("  | |_   ____ _ _ __ | |__   ___   ___ ");
		outputText("  | \\ \\ / / _` | '_ \\| '_ \\ / _ \\ / _ \\");
		outputText(" _| |\\ V / (_| | | | | | | | (_) |  __/");
		outputText("|_____\\_/ \\__,_|_| |_|_| |_|\\___/ \\___|");
		outputText("\nClient: Welcome brave knight!");

		// get user's name
		String username = userInput("What is thy name?: ");

		// initialize states
		initialize(username);

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
						outputText("Please enter a port number...");
						continue;
					}
					break;
				}
			}

			// attempt to connect to Server
			if (connect(address, port)) {
				send(player); // send user
				player = (Player) receive();
				// start new thread to get Client commands
				this.inputThread = new ClientInput(this, System.in);
				this.inputThread.start();

				// start new thread to receive from Server
				this.receiveThread = new Thread(this);
				this.receiveThread.start();				
				outputText("\nType /help for a list of commands!");
				break;
			} else {
				outputText("There are no tournaments at that location! \n");

				while (true) {
					String ui = userInput("Do you want to search for a new tournament (y/n): ");
					if (ui.equalsIgnoreCase("y")) {
						break;
					} else if (ui.equalsIgnoreCase("n")) {
						outputText("Client: fare thee well!");
						search = false;
						shutdown();
						break;
					} else {
						outputText("That is not an option, my good knight!");
					}
				}
			}
		}

	}

	// shut down Client
	public boolean shutdown() {
		this.shutDown = true;
		this.stop = true;
		outputText("\nClient: Shutting down...");

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
		outputText("Client: fare thee well!");
		if(view != null)view.dispose();
		return true;
	}

	// continue receiving from Server
	public void run() {
		// while Client is running, keep connection with Server
		while (!this.stop) {
			Object o = receive(); // received object
			if (o != null) { // process object if it can be used
				process(o);
			}
			
			if (view != null && view.inGame) {
				view.updateComponents(gameState, player);
			}
		}
		if (!this.shutDown) {
			shutdown();
		}
	}

	// get user input from console
	public String userInput(String message) {
		outputText(message);
		if(view == null){
			this.input = new BufferedReader(new InputStreamReader(System.in));
			String strInput = "";
			try {
				strInput = this.input.readLine();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
				return null;
			}
			Trace.getInstance().write(this, message + strInput);
			return strInput;
		}else{
			view.getConsole().setMode(ConsoleView.USER_INPUT);
			String output = null;
			while(output == null){
				output = view.getConsole().getText();
				System.out.flush();
			}
			view.getConsole().clearText();
			view.getConsole().setMode(ConsoleView.COMMAND);
			return output;
		}
	}

	// connect to Server
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
			outputText("Unable to connect to a server.");
			Trace.getInstance().exception(this, se);
			this.stop = true;
		} catch (UnknownHostException uhe) {
			outputText("Unknown Host");
			Trace.getInstance().exception(this, uhe);
			this.stop = true;
		} catch (IOException ioe) {
			outputText("Unexpected exception");
			Trace.getInstance().exception(this, ioe);
			this.stop = true;
		}
		return false;
	}

	// send object to Server
	public Boolean send(Object o) {
		try {
			this.clientOutputStream.writeObject(o);
			return true;
		} catch (IOException e) {
			outputText("Unexpected exception: writing object to output stream");
			Trace.getInstance().exception(this, e);
		}
		return false;
	}

	// receive object from Server
	public Object receive() {
		// will return this null object if error reading from stream
		Object received = null;

		try {
			received = this.clientInputStream.readObject();
		} catch (SocketException se) {
			Trace.getInstance().exception(this, se);
			this.stop = true;
		} catch (ClassNotFoundException cnf) {
			outputText("Class Not Found Exception: reading object from input stream");
			Trace.getInstance().exception(this, cnf);
			this.stop = true;
		} catch (IOException ioe) {
			outputText("Unexpected Exception: reading object from input stream");
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
			// this signals that the game is done
			if (gameState.getNumPlayers() == 0) {
				this.gameState = null;
				this.player.reset();
				return true;
			}
			this.player = gameState.getPlayer(this.player.getName());
			Trace.getInstance().write(this, this.player.getName() + ": game state has been updated");
			
			if (gui) {
				if(!view.inGame)view.setupGameView();
			}
			return true;

			// ActionCard
		} else if (o instanceof ActionCard) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received");
			this.player.addToHand((ActionCard) o);
			outputText("Client: " + o.toString() + " added to hand");
			return true;

			// DisplayCard
		} else if (o instanceof DisplayCard) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received");
			this.player.addToHand((DisplayCard) o);
			outputText("Client: " + o.toString() + " added to hand");
			return true;

			/* ACTIONS: */
			//Info
		} else if (o instanceof Info) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received: "
					+ ((Info) o).getMessage());
			String message = ((Info) o).getMessage();
			message = this.language.translate(message);
			outputText(message, ClientView.INFO);
			return true;
			// Chat
		} else if (o instanceof Chat) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " received: "
					+ ((Chat) o).getMessage());
			String message = ((Chat) o).getMessage();
			message = this.language.translate(message);
			outputText(message, ClientView.CHAT);
			return true;
			// Prompt
		} else if (o instanceof Prompt) {
			Trace.getInstance().write(this, this.player.getName() + ": " + o.getClass().getSimpleName() + " was prompted: " + ((Prompt) o).getMessage());
			this.promptOptions = ((Prompt) o).getOptions();
			String s = null;
			if (ai) { // Client is run by AI	
				while (aiPrompt == null) { // wait for AI to answer prompt
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) { }
					s = aiPrompt;
					//if (s != null) { break; }
				}
				aiPrompt = null;
			} else {  // Client is human player
				if(view == null){
					this.input = new BufferedReader(new InputStreamReader(System.in));
					String strInput = "";
					try {
						strInput = this.input.readLine();
					} catch (IOException e) {
						Trace.getInstance().exception(this, e);
					}
					Trace.getInstance().write(this, ((Prompt) o).getMessage() + strInput);
					s = strInput;
				}else{
					Prompt prompt = (Prompt) o;
					ArrayList<String> optionNames = new ArrayList<String>();
					for (Object object : prompt.getOptions()){
						optionNames.add(object.toString());
					}
					s = view.showPromptOptions(optionNames, prompt.getMessage(), "Ser " + player.getName() + "...");
				}
			}
			this.promptOptions = null;
			send(new Prompt(s));
			return true;

			// unrecognized object
		} else if (o instanceof EndTurn) {
			if(view != null)view.toFront();
			return true;

			// unrecognized object
		} else {
			Exception e = new Exception(this.player.getName() + ": unrecognized object received");
			Trace.getInstance().exception(this, e);
			return false;
		}
	}
	
	/*
	 * process user's input (commands and chat)
	 */
	public boolean processInput(String input) {
		
		if (input.length() == 0) {
			return false;
		}
		// GUI mode
		if (view != null) { 
			// send everything as chat
			String translated = language.translate(input);
			send(new Chat(translated));
			Trace.getInstance().write(this, getPlayer().getName() + ": " + "chat sent: " + input);
			
		// CLI mode
		} else {
			if (validCmd(input)) { // process valid commands
				processCmd(input);
				Trace.getInstance().write(this, getPlayer().getName() + ": command processed: " + input);
			} else if (input.charAt(0) == '/') { // process invalid commands
				outputText("Client: invalid command, try typing '/help' for more info.");
			} else { // process chat
				String translated = language.translate(input);
				send(new Chat(translated));
				Trace.getInstance().write(this, getPlayer().getName() + ": " + "chat sent: " + input);
			}
		}
		return true;
	}

	/*
	 * returns if a valid command or not
	 */
	public boolean validCmd(String in) {
		// commands start with slash (/)
		if (in.charAt(0) != '/') {
			return false;
		}

		// check existing commands
		for (Config.ClientCommand cmd : Config.ClientCommand.values()) {
			if (in.startsWith(cmd.toString(), 1)) {
				return true;
			}
		}

		return false;
	}

	// deals with commands received from inputThread
	public boolean processCmd(String s) {
		Command cmd = new Command(s, this.player);
		ValidCommand args;
		ValidCommand turn;
		ValidCommand tournament;
		ValidCommand hasCard;
		ValidCommand stunned;
		
		// switch over command
		switch (cmd.getCmd()) {
		case "censor": // toggle the bad word censor
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdCensor();
			break;
		case "display": // look at display cards
			cmdDisplay(cmd.getArgs());
			break;
		case "end": // end turn
			args = new Arguments("!=", 0);
			turn = new IsTurn();
			args.setSuccessor(turn);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdEnd();
			break;
		case "gamestate": // show gamestate
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdGameState(gameState);
			break;
		case "hand": // look at cards in hand
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdHand();
			break;
		case "help":
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdHelp();
			break;
		case "list":
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdList();
			break;
		case "play":
			args = new Arguments("<", 1);
			tournament = new InTournament();
			hasCard = new CardInHand();
			stunned = new StunnedAndPlayedDC();
			hasCard.setSuccessor(stunned);
			tournament.setSuccessor(hasCard);
			args.setSuccessor(tournament);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdPlay(String.join(" ", cmd.getArgs()));
			break;
		case "ready":
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdReady();
			break;
		case "setname":
			args = new Arguments("==", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdSetname(cmd.getArgs());
			break;
		case "shutdown":
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			shutdown();
			break;
		case "tokens":
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdTokens();
			break;
		case "tournament":
			args = new OneOrTwoArguments();
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdTournament(cmd.getArgs());
			break;
		case "translate":
			args = new Arguments("!=", 1);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdTranslate(cmd.getArgs()[0]);
			break;
		case "withdraw":
			args = new Arguments("!=", 0);
			args.isValid(cmd);
			if (!cmd.isValid()) { 
				outputText("Client: " + cmd.getMessage());
				return false;
			} 
			cmdWithdraw();
			break;
		default:
			return false;
		}
		return true;
	}

	// toggle censoring of bad words
	public boolean cmdCensor() {
		Dialect dialect = this.language.getDialect();
		boolean censor = !this.language.isCensored(); // toggle censor
		this.language = new Language(dialect, censor);
		// censored
		if (censor) {
			outputText("Client: now censoring bad language.");
		// not censored
		} else {
			outputText("Client: no longer censoring bad language.");
		}
		return true;
	}

	// print out displays
	public boolean cmdDisplay(String[] arr) {
		String args = String.join(" ", arr); // join arguments into one string
		String status = "";

		// show own display
		if (arr.length == 0) {
			if (this.player.getShielded()) { // show if shielded
				status += " (SHIELD)"; 
			}
			if (this.player.getStunned()) { // show if stunned
				status += " (STUNNED)";
			}
			System.out.println(status);
			if (gameState.getTournament() != null) {
				if (!(this.player.getDisplay().print(gameState.getTournament().getColour()))) {
					outputText("Client: no cards in your display");
				}
			} else {
				outputText("Client: no tournament running, you have no display");
				return false;
			}
			// show all displays
		} else if (args.equalsIgnoreCase("-a")) {
			for (Player p : this.gameState.getPlayers()) {
				if (p.getShielded()) { // show if shielded
					status += " (SHIELD)"; 
				}
				if (p.getStunned()) { // show if stunned
					status += " (STUNNED)";
				}
				System.out.println(status);
				if (gameState.getTournament() != null) {
					if (!(p.getDisplay().print(gameState.getTournament().getColour()))) {
						outputText("Client: no cards in " + p.getName() + "'s display\n");
					}
				} else {
					outputText("Client: no tournament running, you have no display");
					return false;
				}
			}
			// show someone else's display
		} else {
			Player p = this.gameState.getPlayer(args);
			if (p == null) { // player doesn't exist
				outputText("Client: " + args + " doesn't exist.  Can't print their Display.");
				return false;
			} else {
				if (p.getShielded()) { // show if shielded
					status += " (SHIELD)"; 
				}
				if (p.getStunned()) { // show if stunned
					status += " (STUNNED)";
				}
				System.out.println(status);
				if (gameState.getTournament() != null) {
					if (!(p.getDisplay().print(gameState.getTournament().getColour()))) {
						outputText("Client: no cards in " + p.getName() + "'s display\n");
					}
				} else {
					outputText("Client: no tournament running, you have no display");
					return false;
				}
			}
		}
		return true;
	}

	// end your turn
	public boolean cmdEnd() {
		if (!this.player.isTurn()) { return false; } // not your turn
		if (gameState.getTournament() == null && this.player.hasValidDisplayCard(new Colour(Colour.c.NONE))) {
			outputText("Client: You MUST start a tournament if able.");
			return false;
		} else {
			this.player.setTurn(false);
			if (gameState.hasHighScore(player) == false) {
				player.setParticipation(false);
				player.getDisplay().clear();
			}
			if(gameState.getTournamentParticipants().size() == 1){
				gameState.endTournament();
			}
			if(view != null)view.updateComponents(gameState, player);
			send(new EndTurn());
			return true;
		}
	}

	// show cards in hand
	public boolean cmdHand() {
		if (this.player.getHand().isEmpty()) {
			outputText("Client: You have no cards in your hand.");
		} else {
			outputText("Client: You have the following cards in your hand: ");
			for (Card card : this.player.getHand()) {
				outputText("\t- " + card.toString());
			}
		}
		return true;
	}

	// list possible commands and their corresponding syntax
	public boolean cmdHelp() {
		outputText("Client: list of possible commands: ");
		for (Config.ClientCommand helpCmd : Config.ClientCommand.values()) {
			outputText("\t/" + helpCmd + helpCmd.getSyntax());
		}
		return true;
	}

	// list other players in game
	public boolean cmdList() {
		outputText("- State    : Player ");
		for (int i = 0; i < this.gameState.getPlayers().size(); i++) {
			Player p = this.gameState.getPlayers().get(i);
			String name = p.getName();
			if (name == this.player.getName()) { // found yourself
				name += " (you)";
			}
			outputText(String.format("%-10s : %s\n", p.getReadyState(), name));
		}
		return true;
	}

	// play a card
	public boolean cmdPlay(String card) {
		Card c = this.player.getCard(card); // get the card the user asked for

		// card doesn't exist in hand
		if (c == null) {
			outputText("Client: you don't have the card: " + card + "\n\t Type '/hand' to view the cards in your hand.");
			return false;
			// not the player's turn
		} else if (!this.player.isTurn()) {
			if (c.toString().equalsIgnoreCase("ivanhoe")) {
				// card to be player is the Ivanhoe action card:
				send(new Play(c));
				return true;
			} else {
				outputText("Client: you may not play that card when it is not your turn");
				return false;
			}
			// is player's turn
		} else {
			int errorCode = gameState.canPlay(c, player);
			if (c instanceof DisplayCard) {
				if(errorCode > 0){
					switch(errorCode){
					case GameState.NO_TOURNAMENT:
						outputText("Client: no tournament is running, start one with /tournament");
						break;
					case GameState.INVALID_COLOUR:
						outputText("Client: not a valid color for the current tournament");
						break;
					case GameState.MULTIPLE_MAIDEN:
						outputText("Client: you may not have more than one maiden in your Display.");
						break;
					}
					return false;
				}
				gameState.addDisplay(player, (DisplayCard) c);
				gameState.removeHand(player, c);
				// action card
			} else {
				if(errorCode > 0){
					switch(errorCode){
					case GameState.NO_TOURNAMENT:
						outputText("Client: no tournament is running, start one with /tournament");
						break;
					case GameState.NO_TARGETS:
						outputText("Client: no targets available.");
						break;
					}
					return false;
				}
				gameState.removeHand(player, c);
			}
		}
		
		send(new Play(c));
		if(view != null)view.updateComponents(gameState, player);
		return true;
	}

	// player is ready to start game
	public boolean cmdReady() {
		send(new Ready());
		return true;
	}

	// player changes their name
	public boolean cmdSetname(String[] arr) {
		String args = String.join(" ", arr); // join arguments into one string

		// check for invalid names
		if ((args.equals("")) || (args.startsWith("-") || (args.startsWith("/")))) {
			outputText("Client: can't change name to '" + args + "'. Invalid name.");
			return false;
			// valid name
		} else {
			send(new SetName(args));
			return true;
		}
	}

	// view everyone's tokens
	public boolean cmdTokens() {
		if (this.gameState.getNumPlayers() < 1) {
			outputText("Client: there are no players in the game.");
			return false;
		} else {
			outputText("Client: listing tokens: ");
		}
		for (Player p : this.gameState.getPlayers()) {
			outputText(p.getName() + ": ");
			outputText(p.listTokens());
		}
		return true;
	}
	
	// start a tournament
	public boolean cmdTournament(String[] args) {

		// Check that no tournaments are running
		if (this.gameState.getTournament() != null) {
			outputText("Client: a tournament is already in progress");
			return false;
		}
		// Check if its their turn
		if (!(this.player.isTurn())) {
			outputText("Client: you may not start a tournament when it is not your turn");
			return false;
		}

		String strCard;
		if (args.length == 2) {
			strCard = args[1];
		} else {
			strCard = args[0];
		}

		Card card = this.player.getCard(strCard); // get card to start
													// tournament with
		if (card == null) {
			outputText("Client: you don't have the card: " + strCard + "\n\t Type '/hand' to view the cards in your hand.");
			return false;
		}
		if (!(card instanceof DisplayCard)) {
			outputText("Client: " + card.toString() + " is not a display card.");
			return false;
		}
		DisplayCard displayCard = (DisplayCard) card;

		Colour colour;
		if (args.length == 2) {
			try {
				colour = new Colour(args[0]);
			} catch (IllegalArgumentException iae) {
				outputText("Client: " + args[0] + " is not a valid tournament colour. Type '/help'.");
				return false;
			}
		} else {
			colour = displayCard.getColour();
		}

		// tournament must be an actual colour, not NONE
		if (colour.isNone()) {
			outputText("Client: " + colour.toString() + " is not a valid tournament colour. Type '/help'.");
			return false;
		}

		// last tournament was purple (another colour must be chosen)
		if ((gameState.getLastColour().equals(Colour.c.PURPLE)) && (colour.equals(Colour.c.PURPLE))) {
			outputText("Client: the last tournament was Jousting (purple). "
					+ "\n A tournament of a different colour must be started.");
			return false;
		}

		// display card is not a squire or maiden
		if (!displayCard.getColour().isNone()) {
			// tournament colour selected doesn't equal display card colour
			if (!displayCard.getColour().equals(colour)) {
				outputText("Client: " + displayCard.toString()
						+ " does not match the colour of the tournament you are trying to start. "
						+ "\n\t Type '/hand' to view the cards in your hand.");
				return false;
			}
		}
		gameState.addDisplay(player, displayCard);
		gameState.removeHand(player, displayCard);
		gameState.startTournament(new Tournament(colour));
		send(new StartTournament(colour, displayCard));
		return true;
	}

	// change the language to translate chat messages with
	public boolean cmdTranslate(String d) {
		boolean censor = this.language.isCensored();
		// no translating
		if (Language.Dialect.none.toString().equals(d)) {
			this.language = new Language(Language.Dialect.none, censor);
			outputText("Client: no longer translating chat messages");
			return true;
		}
		// translating
		for (Language.Dialect dialect : Language.Dialect.values()) {
			if (dialect.toString().equals(d)) {
				this.language = new Language(dialect, censor);
				outputText("Client: translating chat to " + this.language.getDialect().toString());
				return true;
			}
		}
		return false;
	}

	// withdraw from the current tournament
	public boolean cmdWithdraw() {
		
		player.setParticipation(false);
		player.setTurn(false);
		player.getDisplay().clear();
		send(new Withdraw());
		if(view != null)view.updateComponents(gameState, player);
		return true;
	}

	// print a modicum of gamestate
	public boolean cmdGameState(GameState g) {
		if (g == null) {
			return false;
		}
		outputText("Gamestate::");
		if (g.getTournament() == null) {
			outputText("No tournament running.");
		}
		for (Player p : g.getPlayers()) {
			outputText(p.getName() + ":" + p.getId());
			outputText("  HAND:" + p.getHandSize() + "\n  TURN:" + p.isTurn() + "\n  TOUR:" + p.getParticipation() + "\n  ");
		}
		return true;
	}

	private boolean outputText(String message) {
		 return outputText(message, 0);
	}
	private boolean outputText(String message, int type) {
		System.out.println(message);
		if (this.view != null)
			view.writeConsole(message, type);
		Trace.getInstance().write(this, (this.player == null) ? "New Player" : this.player.getName() + ": " + message);
		return true;
	}

}
