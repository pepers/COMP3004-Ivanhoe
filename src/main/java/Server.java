package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;
import main.resources.Language.Dialect;

public class Server implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	// Threads
	Thread thread; // main thread for the server
	ServerInput inputThread; // thread that handles console input (commands)
	SearchThread searchThread; // thread that searches for new players
	GameState gameState;

	ServerSocket serverSocket; // primary network socket
	int minPlayers = Config.MIN_PLAYERS;
	int maxPlayers = Config.MAX_PLAYERS;
	int port; // server port
	String address = "unknown"; // server address
	int numClients; // number of clients
	int numReady; // number of ready player
	ConcurrentHashMap<ServerThread, Player> clients; // holds the threads mapped
														// to player objects

	boolean stop = false; // stops the main thread
	public Queue<ActionWrapper> actions; // server actions to operate upon
	Language language = new Language(Language.Dialect.none, false); // to
																	// translate
																	// chat

	// Banlist
	File banList;
	BufferedWriter banWriter;
	BufferedReader banReader;

	// Constructor
	public Server(int port) {
		this.port = port;
		clients = new ConcurrentHashMap<ServerThread, Player>();
		actions = new LinkedList<ActionWrapper>();
	}

	public static void main(String[] args) {
		System.out.println("Beginning server setup...");
		Server s = new Server(Config.DEFAULT_PORT);
		if (s.startup()) {
			System.out.println("Setup successful.");
			System.out.println("Listening at " + s.address + ":" + s.port + "...\n");
		}
	}

	// Returns the number of players on this server
	public int getConnected() {
		return numClients;
	}

	// Startup routine
	public boolean startup() {
		Trace.getInstance().write(this, "Binding to port " + port + ", please wait  ...");

		// Setup network
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			address = InetAddress.getLocalHost().toString();
		} catch (BindException e) {
			System.out.println("Error: There is already a running server on this port.");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		Trace.getInstance().write(this, "Network setup finished.");

		// Setup threads
		thread = new Thread(this);
		inputThread = new ServerInput(this, System.in);
		searchThread = new SearchThread(this);
		inputThread.start();
		searchThread.start();
		thread.start();
		Trace.getInstance().write(this, "Thread setup finished.");

		// Setup banlist
		banList = new File("banList.txt");
		try {
			banReader = new BufferedReader(new FileReader(banList));
			banWriter = new BufferedWriter(new FileWriter(banList, true));
		} catch (FileNotFoundException e) {
			System.out.println("Error finding to banlist.");
		} catch (IOException e) {
			System.out.println("Error finding to banlist.");
		}
		return true;
	}

	// Adding a new connection
	public boolean addThread(Socket socket) {
		Trace.getInstance().write(this, "Client Requesting connection: " + socket.getLocalAddress());
		ServerThread serverThread;

		// Check the banList
		try {
			for (String line : Files.readAllLines(Paths.get("banList.txt"))) {
				if (line.equals(socket.getInetAddress().toString().substring(1))) {
					System.out.print("Banned ip " + socket.getInetAddress() + " attempted to join.");
					return false;
				}
			}
			
		}catch (NoSuchFileException e){
			Trace.getInstance().write(this, "Couldn't find banList.txt.");
		}catch (IOException e1) {
			e1.printStackTrace();
		} 

		// Check if there is space
		if (numClients < maxPlayers) {
			serverThread = new ServerThread(this, socket);
			SetName name = ((SetName) serverThread.receive());
			serverThread.start();
			clients.put(serverThread, new Player(name.getName(), serverThread.getID()));
			numClients++;
		} else {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.writeObject(new Chat("Sorry, too many knights at that location."));
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Trace.getInstance().write(this, "Client refused: maximum number of clients reached (" + numClients + ")");
			System.out.println("Client refused: maximum number of clients reached (" + numClients + ")");
			return false;
		}

		System.out.println(clients.get(serverThread).getName() + " joined.");
		Trace.getInstance().write(this, "Client Accepted: " + socket.getPort());
		return true;
	}

	// Removing a connection via id
	public boolean removeThread(int id) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (t.getID() == id) {
				System.out.println("Removing player \"" + clients.get(t).getName() + "\" (" + t.getID() + ")...");
				Trace.getInstance().write(this,
						"Removing player \"" + clients.get(t).getName() + "\" (" + t.getID() + ")...");
				numClients--;
				t.shutdown();
				clients.remove(t);
				return true;
			}
		}
		System.out.println("Couldnt find player (" + id + ")");
		return false;
	}

	// Remove via name
	public boolean removeThread(String name) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (clients.get(t).getName().equals(name)) {
				System.out.println("Removing player \"" + name + "\" (" + t.getID() + ")...");
				Trace.getInstance().write(this, "Removing player \"" + name + "\" (" + t.getID() + ")...");
				numClients--;
				clients.remove(t);
				t.shutdown();
				return true;
			}
		}
		System.out.println("Couldnt find player (" + name + ")");
		return false;
	}

	public void listClients() {

		System.out.println("Connected Players:");
		System.out.printf(" %-3s %-20s %-8s %s\n", "#", "Name", "State", "Inet Address");
		System.out.println(" ==================================================");
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			Player p = clients.get(t);
			String state = p.getReadyState();
			System.out.printf(" %-3s %-20s %-8s %s\n", t.getID(), p.getName(), state, t.getNetwork());
		}
	}

	// Main thread
	public void run() {
		while (!stop) {

			int readyPlayers = 0;
			Iterator<ServerThread> i = clients.keySet().iterator();
			while (i.hasNext()) {
				ServerThread t = i.next();

				// check if the serverthread lost its client
				if (t == null) {
					continue;
				}
				if (t.getDead()) {
					Player c = clients.get(t);
					String name = c.getName();
					t.shutdown();
					clients.remove(t);
					broadcast(name + " disconnected.");
					numClients = clients.size();
					continue;
				}

				Player p = clients.get(t);
				if (p != null) {
					readyPlayers = readyPlayers + (p.ready == 1 ? 1 : 0);
				}
				Object o = t.actions.poll(); // get an action from the thread
				if (o != null) {
					Trace.getInstance().write(this, "Got an action from " + p.getName());
					actions.add(new ActionWrapper(o, p)); // create a new local
															// action
				}
			}
			numReady = readyPlayers;

			// start game automatically if:
			// all players are ready and
			// the minimum number of players are ready
			if ((numReady == numClients) && (numReady >= minPlayers)) {
				startGame();
			}

			if (!actions.isEmpty()) {
				ActionWrapper a = actions.poll();
				evaluate(a);
				if (gameState != null) {
					updateGameStates();
				}
			}
		}
	}

	private boolean evaluate(ActionWrapper action) {

		if (action.object instanceof SetName) {
			if (!((SetName) action.object).isInit()) {
				String s = (action.origin.getName() + " changed name to \"" + ((SetName) action.object).getName()
						+ "\"");
				broadcast(s);
			}
			action.origin.setName(((SetName) action.object).getName());
			return true;
		}
		if (action.object instanceof Chat) {
			String message = ((Chat) action.object).getMessage();
			String from = action.origin.getName();
			String translated = language.translate(((Chat) action.object).getMessage());
			broadcast(from + ": " + translated);
			Trace.getInstance().write(this,
					"Server: " + action.object.getClass().getSimpleName() + " received from " + from + ": " + message);
			return true;
		}

		if (action.object instanceof Ready) {
			String s = "";
			if (action.origin.toggleReady()) {
				s = (action.origin.getName() + " is ready!");
			} else {
				s = (action.origin.getName() + " is no longer ready.");
			}
			broadcast(s);
			return true;
		}

		// Game state evaluation
		if (gameState == null) {
			return false;
		}

		if (action.object instanceof StartTournament) {
			Tournament t = new Tournament(((StartTournament) action.object).getColour());
			gameState.startTournament(t);
			
			Card c = ((StartTournament) action.object).getCard();
			gameState.addDisplay(gameState.getPlayer(action.origin.getName()), c);
			gameState.removeHand(gameState.getPlayer(action.origin.getName()), c);
			broadcast(t.name + " started by " + action.origin.getName() + " (" + t.getColour() + ")");
			return true;
		}
		if (action.object instanceof EndTurn) {
			Player p = gameState.getPlayer(action.origin.getName());
			if (p != null) {
				if (p.getScore(gameState.tnmt.getColour()) <= gameState.highScore) {
					p.inTournament = false;
					p.getDisplay().clear();
					p.displayScore = 0;
					message("YOU have been ELIMINATED from " + gameState.tnmt.name + "!", p);
					messageExcept(p.getName() + " has been ELIMINATED from " + gameState.tnmt.name + "!", p);
				} else {
					gameState.highScore = p.getScore(gameState.tnmt.getColour());
				}
				endTurn();
			}
			return true;
		}
		if (action.object instanceof Withdraw) {
			Player p = gameState.getPlayer(action.origin.getName());
			if (p != null) {
				p.inTournament = false;
				p.getDisplay().clear();
				p.displayScore = 0;
				message("You withdraw from " + gameState.tnmt.name + "!", p);
				messageExcept(p.getName() + " has withdrew from " + gameState.tnmt.name + "!", p);
				endTurn();
			}
			return true;
		}
		if (action.object instanceof Play) {
			Card c = ((Play) action.object).getCard();
			broadcast(action.origin.getName() + " plays a " + c.toString());
			if (c instanceof DisplayCard) {
				gameState.addDisplay(gameState.getPlayer(action.origin.getName()), c);
				gameState.removeHand(gameState.getPlayer(action.origin.getName()), c);
			}
			return true;
		}
		return false;
	}

	private boolean endTurn(){
		// check if tournament has a winner
		ArrayList<Player> a = gameState.getTournamentParticipants();
		if (a.size() == 1) {
			Player winner = a.get(0);
			message("YOU have been VICTORIOUS in " + gameState.tnmt.name + "!", winner);
			messageExcept(winner.getName() + " has been VICTORIOUS in " + gameState.tnmt.name + "!", winner);
			
			String colour = gameState.tnmt.getColour();
			if(colour.equals("purple")){
				colour = prompt("Your deeds merit a token of your choice. What colour do you seek?", winner);
			}
			
			if (winner.giveToken(new Token(colour, gameState.tnmt.getContext()))) {
				message("You get a " +colour + " token of favour!", winner);
			} else {
				message("You already have a " + colour
						+ " token, but you still get the satisfaction of winning.", winner);
			}
			gameState.endTournament();
			//check if the game is done TODO fix
			if (gameState.numPlayers < 4 && winner.getNumTokens() == 5){
				broadcast(winner.getName() + " has won the game. Play again soon!");
				endGame();
				return true;
			} else if (gameState.numPlayers >= 4 && winner.getNumTokens() == 4){
				broadcast(winner.getName() + " has won the game. Play again soon!");
				endGame();
				return true;
			}
		}
		
		Player next = gameState.nextTurn();
		Card drew = gameState.deck.draw();
		gameState.addHand(next, drew);
		message("Your turn has begun.  You drew a " + drew.toString() + " card!", next);
		messageExcept(next.getName() + " has begun their turn!", next);
		return false;
	}
	
	/*
	 * send a message to all players(threads)
	 */
	public void broadcast(String input) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			t.send(new Chat(input));
		}
		System.out.println(input);
	}

	/*
	 * send a message to everyone in game, except for one player
	 */
	public void messageExcept(String input, Player p) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (!(clients.get(t).equals(p))) {
				t.send(new Chat(input));
			}

		}
		System.out.println(input);
	}

	/*
	 * send a message to one player
	 */
	public boolean message(String input, Player p) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if ((clients.get(t).equals(p))) {
				t.send(new Chat(input));
				return true;
			}
		}
		return false;
	}

	public String prompt(String input, Player p) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if ((clients.get(t).equals(p))) {
				t.send(new Prompt(input));
				while(t.promptResponses.isEmpty()){
					System.out.flush();
				}
				Prompt o = t.promptResponses.poll();
				return o.getMessage();
			}
		}
		return "player doesn't exist...";
	}
	
	/*
	 * Start a game
	 */
	public boolean startGame() {

		if (numClients < minPlayers) {
			System.out.println("(" + numClients + "/" + minPlayers + ") players are needed to start a game.");
			return false;
		}
		if (numReady < minPlayers) {
			System.out.println("(" + numReady + "/" + numClients + ") players ready.");
			return false;
		}
		broadcast("(" + numReady + "/" + numClients + ") players ready.");
		broadcast("Preparing to start a game...");

		gameState = new GameState();

		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (clients.get(t).ready == 1) {
				clients.get(t).ready = 2;

				for (int j = 0; j < 8; j++) {
					clients.get(t).addToHand(gameState.deck.draw());
				}
				gameState.addPlayer(clients.get(t));
			}
		}

		int startIndex = (new Random()).nextInt(gameState.numPlayers);
		Player startPlayer = gameState.players.get(startIndex);
		startPlayer.isTurn = true;
		gameState.turnIndex = startIndex;
		messageExcept(startPlayer.getName() + " starts their turn.", startPlayer);
		message("You are the starting player. Start a tournament if able.", startPlayer);
		updateGameStates();
		return true;
	}
	
	/*
	 * Update each client with a new gameState
	 */
	public int updateGameStates() {
		if (gameState != null){printLargeDisplays();}
		Iterator<ServerThread> i = clients.keySet().iterator();
		int c = 0;
		while (i.hasNext()) {
			ServerThread t = i.next();
			Player p = clients.get(t);
			if (p.ready == 2) {
				if (t.update(gameState))
					c++;
			}
		}
		return c;
	}

	public boolean endGame() {

		gameState.numPlayers = 0;
		updateGameStates();
		
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (clients.get(t).ready == 2) {
				clients.get(t).reset();
			}
		}

		broadcast("Game is finished");
		gameState = null;
		return true;
	}
	
	/*
	 * shuts down the server
	 */
	public boolean shutdown() {
		Trace.getInstance().write(this, "Shutting down server, please wait  ...");
		System.out.println("Shutting down server, please wait  ...");

		// Shutdown the network infrastructure
		try {
			// close each of the clients individually
			for (ServerThread t : clients.keySet()) {
				t.shutdown();
			}
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// Stop all the threads
		stop = true;
		thread = null;
		searchThread.stop = true;
		searchThread = null;
		inputThread.stop = true;
		inputThread = null;

		// Close the banlist
		try {
			banWriter.close();
			banReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Shutdown complete. Goodbye!");
		return true;
	}

	/*
	 * prints a player's hand of cards
	 */
	public boolean printHand(String name) {
		Player p = fromString(name);
		if (p == null) {
			return false;
		}
		System.out.println("Hand:");
		for (Card c : p.getHand()) {
			System.out.println("  " + c.toString());
		}

		return true;
	}

	/*
	 * finds player in clients (map thread and player) by their name
	 */
	public Player fromString(String name) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (clients.get(t).getName().equals(name)) {
				return clients.get(t);
			}
		}
		System.out.println("Couldnt find player (" + name + ")");
		return null;
	}

	/*
	 * set the minimum number of allowed players connected
	 */
	public void setMinPlayers(int n) {
		minPlayers = n;
		System.out.println("New MINIMUM players is " + n);
	}

	/*
	 * set the maximum number of allowed players connected
	 */
	public void setMaxPlayers(int n) {
		maxPlayers = n;
		System.out.println("New MAXIMUM players is " + n);
	}

	/*
	 * print information about the current game state
	 */
	public boolean printGameState() {
		if (gameState == null) {
			return false;
		}
		System.out.println("Gamestate::");
		if (gameState.tnmt == null) {
			System.out.println("No tournament running.");
		}
		for (Player p : gameState.players) {
			System.out.println(p.getName() + ":" + p.getId());
			System.out.println("  HAND:" + p.handSize + "\n  TURN:" + p.isTurn + "\n  TOUR:" + p.inTournament + "\n  ");
		}
		return true;
	}

	/*
	 * print the displays of all players in game
	 */
	public boolean printLargeDisplays() {
		System.out.println("DISPLAYS:");
		System.out.println(" ============================================");
		GameState temp = gameState;
		for (Player p : gameState.players) {
			String display = p.getName() + " (" + p.getScore(gameState.getTournamentColour()) + ")";
			System.out.printf("%-20s", display);
		}
		System.out.println();
		for (int i = 0; i < 10; i++) {
			for (Player p : temp.players) {
				if (p.getDisplay().size() < i + 1) {
					System.out.printf("%-20s", "");
				} else {
					System.out.printf("%-20s", p.getDisplay().get(i));
				}
			}
			System.out.println();
		}
		return true;
	}

	/*
	 * print one player's display
	 */
	public boolean printSingleDisplay(Player p) {
		if (!(p.printDisplay(""))) {
			System.out.println("No cards in " + p.getName() + "'s display\n");
			Trace.getInstance().write(this, "Server: no cards in " + p.getName() + "'s display.");
		}
		return true;
	}

	/*
	 * toggle censoring of bad words
	 */
	public boolean cmdCensor() {
		Dialect dialect = this.language.getDialect();
		boolean censor = !this.language.isCensored(); // toggle censor
		this.language = new Language(dialect, censor);
		// censored
		if (censor) {
			System.out.println("now censoring bad language");
			Trace.getInstance().write(this, "Server: now censoring bad language.");
			// not censored
		} else {
			System.out.println("no longer censoring bad language");
			Trace.getInstance().write(this, "Server: no longer censoring bad language.");
		}
		return true;
	}

	/*
	 * give a player a card
	 * 
	 * valid strCard examples: - ivanhoe - purple:3
	 */
	public boolean cmdGive(int pnum, String strCard) {
		Card card;
		Player p = null;
		for (Entry<ServerThread, Player> entry : clients.entrySet()) {
			if (entry.getValue().getId() == pnum) {
				p = entry.getValue();
			}
		}
		if (p != null) { // found player
			// check if player in game
			if (p.ready != 2) {
				Trace.getInstance().write(this,
						"Could not give card. " + p.getName() + " is not yet in the game. Try /list");
				System.out.println("Could not give card. " + p.getName() + " is not yet in the game. Try /list");
				return false;
			}
			card = new ActionCard(strCard);
			if (card.toString() != null) {
				p.addToHand(card); // give Action Card
				Trace.getInstance().write(this, card.toString() + " Action Card given to " + p.getName() + ".");
				System.out.println(card.toString() + " Action Card given to " + p.getName() + ".");
				return true;
			} else {
				String[] strDC = strCard.split(":");
				if (strDC.length != 2) {
					Trace.getInstance().write(this,
							"Invalid Card Format: could not give card to " + p.getName() + ". Try /help");
					System.out.println("Invalid Card Format: could not give card to " + p.getName() + ". Try /help");
					return false; // can't give card
				}
				int value;
				try {
					value = Integer.parseInt(strDC[1]);
				} catch (NumberFormatException e) {
					Trace.getInstance().write(this,
							"Invalid Card Format: could not give card to " + p.getName() + ". Try /help");
					System.out.println("Invalid Card Format: could not give card to " + p.getName() + ". Try /help");
					return false;
				}
				if ((value >= 1) && (value <= 7)) {
					String strColour = strDC[0];
					for (DisplayCard.Colour colour : DisplayCard.Colour.values()) {
						if (colour.toString().equalsIgnoreCase(strColour)) {
							card = new DisplayCard(value, colour);
							p.addToHand(card); // give Display Card
							Trace.getInstance().write(this,
									card.toString() + " Display Card given to " + p.getName() + ".");
							System.out.println(card.toString() + " Display Card given to " + p.getName() + ".");
							return true;
						}
					}
				} else {
					Trace.getInstance().write(this,
							"Invalid Card Format: could not give card to " + p.getName() + ". Try /help");
					System.out.println("Invalid Card Format: could not give card to " + p.getName() + ". Try /help");
					return false; // can't give card
				}
			}
		} else { // could not find player in game state
			Trace.getInstance().write(this,
					"Could not give card. No player associated with player number " + pnum + ". Try /list");
			System.out.println("Could not give card. No player associated with player number " + pnum + ". Try /list");
			return false;
		}
		Trace.getInstance().write(this, "Invalid Card Format: could not give card. Try /help");
		System.out.println("Invalid Card Format: could not give card. Try /help");
		return false;
	}

	/*
	 * view everyone's tokens
	 */
	public boolean cmdTokens() {
		if (this.gameState == null) {
			System.out.println("There are no players in the game.");
			return false;
		} else {
			System.out.println("Listing tokens: ");
		}
		for (Player p : this.gameState.players) {
			Trace.getInstance().write(this, "Tokens: " + p.getName() + " : " + p.listTokens());
			System.out.printf("%-20s: %s\n", p.getName(), p.listTokens());
		}
		return true;
	}

	/*
	 * change the language to translate chat messages with
	 */
	public boolean cmdTranslate(String d) {
		boolean censor = this.language.isCensored();
		// no translating
		if (Language.Dialect.none.toString().equals(d)) {
			this.language = new Language(Language.Dialect.none, censor);
			Trace.getInstance().write(this, "No longer translating chat messages.");
			System.out.println("No longer translating chat messages.");
			return true;
		}
		// translating
		for (Language.Dialect dialect : Language.Dialect.values()) {
			if (dialect.toString().equals(d)) {
				this.language = new Language(dialect, censor);
				Trace.getInstance().write(this, "Translating chat to " + this.language.getDialect().toString() + ".");
				System.out.println("Translating chat to " + this.language.getDialect().toString() + ".");
				return true;
			}
		}
		return false;
	}

	/*
	 * add client ip address to ban list
	 */
	public boolean ban(String address) {

		// Check the banList
		try {
			boolean found = false;
			for (String line : Files.readAllLines(Paths.get("banList.txt"))) {
				if (line.equals(address)) {
					System.out.println(address + " is already banned.");
					found = true;
				}
			}
			if(!found){
				banWriter.write(address + System.getProperty("line.separator"));
				banWriter.flush();
				System.out.println("Banning: " + address + "...");
			}
		} catch (IOException e) {
			System.out.println("Error writing to banlist.");
		}

		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (t.getAddress().equals(address)) {
				removeThread(clients.get(t).getName());
			}
		}
		printBannedIPs();
		return true;
	}

	/*
	 * remove client ip address from ban list
	 */
	public boolean unban(String address) {
		ArrayList<String> bannedPlayers = new ArrayList<String>();
		try {
			for (String line : Files.readAllLines(Paths.get("banList.txt"))) {
				bannedPlayers.add(line);
			}
			
			BufferedWriter tempBanWriter = new BufferedWriter(new FileWriter(banList));
			for (String line : bannedPlayers) {
				if(!line.equals(address)){
					tempBanWriter.write(line + System.getProperty("line.separator"));
				}
			}
			tempBanWriter.close();
		} catch (IOException e) {
			System.out.println("Error unbanning a player (IOException)");
		}
		System.out.println("Unbanning: " + address + "...");
		printBannedIPs();
		return true;
	}
	
	public void printBannedIPs(){
		System.out.println("Banned IPs:");
		try {
			for (String line : Files.readAllLines(Paths.get("banList.txt"))) {
				System.out.println("  " + line);
			}
		} catch (IOException e) {
			System.out.println("Error reading bannedPlayers (IOException)");
		}
	}
}
