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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import main.java.ai.ClientAI;
import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;
import main.resources.Language.Dialect;

public class Server implements Runnable, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Thread thread; 									// main thread for the server
	private ServerInput inputThread; 						// thread that handles console input (commands)
	private SearchThread searchThread; 						// thread that searches for new players

	public ServerSocket serverSocket; 						// primary network socket
	public int port; 										// server port
	private String address = "unknown"; 					// server address
	
	private int minPlayers = Config.MIN_PLAYERS;			// min players needed
	private int maxPlayers = Config.MAX_PLAYERS;			// max players allowed
	private int numClients; 								// number of clients
	private int numReady; 									// number of ready player
	private ConcurrentHashMap<ServerThread, Player> clients;// holds the threads mapped to player objects

	private long time = System.currentTimeMillis();         // time at last Action Card
	private ActionWrapper blockedAction = null;             // last ActionWrapper over Action Card, waiting to be executed
	
	private boolean stop = false; 							// stops the main thread
	private Queue<ActionWrapper> actions; 					// server actions to operate upon
	private GameState gameState;							// data on the current game
	private Language language; 								// to translate chat

	private File banList;									// location of ban list
	private BufferedWriter banWriter;						// writer for ban list
	private BufferedReader banReader;						// reader for ban list

	// Read AI Client Profiles from file:
	private ArrayList<String[]> profiles;
	private File aiprofiles;
	private BufferedReader aiReader;
	
	public GameState getGameState(){return gameState;}
	public int getConnected() {return numClients;}
	
	// Constructor
	public Server(int port) {
		this.port = port;
		clients = new ConcurrentHashMap<ServerThread, Player>();
		actions = new LinkedList<ActionWrapper>();
		language = new Language(Language.Dialect.none, false);
		profiles = new ArrayList<String[]>();
	}

	public static void main(String[] args) {
		System.out.println("Beginning server setup...");
		Server s = new Server(Config.DEFAULT_PORT);
		if (s.startup()) {
			System.out.println("Setup successful.");
			System.out.println("Listening at " + s.address + ":" + s.port + "...\n");
		}
	}

	// Startup routine
	public boolean startup() {
		Trace.getInstance().write(this, "Binding to port " + port + ", please wait  ...");

		// Setup network
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			address = InetAddress.getLocalHost().getHostAddress();
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
			if(!banList.exists()){
				banList.createNewFile();
			}
			banReader = new BufferedReader(new FileReader(banList));
			banWriter = new BufferedWriter(new FileWriter(banList, true));
		} catch (FileNotFoundException e) {
			System.out.println("Error finding banlist.");
		} catch (IOException e) {
			System.out.println("Error read/writing banlist.");
		}
		
		// Setup AI Client profiles
		if (readAIProfiles("aiprofiles.txt")) {
			if (!profiles.isEmpty()) {
				System.out.println("AI Client Profiles read: ");
				for (String[] profile : profiles) {
					System.out.println("\t" + String.join(" ", profile));
				}
			} else {
				System.out.println("No AI Client Profiles to read.");
			}
		} else {
			System.out.println("Error reading AI Profiles list (aiprofiles.txt).");
		}
		
		return true;
	}
	
	/*
	 * read AI Client profiles from file
	 */
	public boolean readAIProfiles (String file) {
		aiprofiles = new File(file);
		if (!aiprofiles.exists()) { return false; } // file can't be opened
		try {
			aiReader = new BufferedReader(new FileReader(aiprofiles));
		} catch (FileNotFoundException e) { return false; } // file not found
		
		String line; // one line = one client ai profile
		try {
			while ((line = aiReader.readLine()) != null) {
				String[] split = line.split("\\s+");
				if (split.length > 4) { // need at least one word name + 4 skill numbers
					String[] skills = Arrays.copyOfRange(split, split.length-4, split.length);
					// check if skills are formatted correctly
					for (String skill : skills) {
						int num;
						try {
							num = Integer.parseInt(skill);
						} catch (NumberFormatException nfe) { return false; }
						if ((num < -1) || (num > 1)) { return false; }
					}
					this.profiles.add(split); // add ai profile
				}
			}
		} catch (IOException e) { return false; } // could not read
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
			Player requestedPlayer = ((Player) serverThread.receive());
			String name = requestedPlayer.getName();
			Player newPlayer = new Player(requestedPlayer.getName(), numClients++, requestedPlayer.getColor());
			if(!checkNewName(newPlayer, name)){
				newPlayer.setName(name + serverThread.getID());
			}
			clients.put(serverThread, newPlayer);
			serverThread.send(newPlayer);
			serverThread.start();
		} else {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.writeObject(new Info("Sorry, too many knights at that location."));
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

	/* 
	 * list out connected Clients
	 */
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

				// check if the server thread lost its client
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
					readyPlayers = readyPlayers + (p.getReadyValue() == Player.READY ? 1 : 0);
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
			
			// have action waiting?
			if (blockedAction != null) {
				// it has been longer than 2 sec
				if ((System.currentTimeMillis() - time) > 2000) { 
					if (gameState != null) {
						gameState.execute(blockedAction, this);
						blockedAction = null;
						updateGameStates();
					}
				}
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

	private boolean checkNewName(Player requestor, String requested){
		for (Player p : clients.values()){
			if(!p.equals(requestor)){
				if(p.getName().equals(requested)){
					message("There is already a player with that name.", requestor);
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean evaluate(ActionWrapper action) {

		if (action.object instanceof SetName) {
			String temp = action.origin.getName();
			String requested = ((SetName) action.object).getName();
			if(checkNewName(action.origin, requested)){
				action.origin.setName(requested);
				String s = (temp + " changed name to \"" + ((SetName) action.object).getName()+ "\"");
				broadcast(s);
				return true;
			}
			return false;
		}
		if (action.object instanceof Chat) {
			String message = ((Chat) action.object).getMessage();
			String from = action.origin.getName();
			String translated = language.translate(((Chat) action.object).getMessage());
			broadcastChat(from + ": " + translated);
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
			if(gameState.startTournament(t)){		
				DisplayCard c = (DisplayCard) ((StartTournament) action.object).getCard();
				gameState.addDisplay(gameState.getPlayer(action.origin.getName()), c);
				gameState.removeHand(gameState.getPlayer(action.origin.getName()), c);
				broadcast(t.getName() + " started by " + action.origin.getName() + " (" + t.getColour() + ")");
				return true;
			}
		}
		if (action.object instanceof EndTurn) {
			Player p = gameState.getPlayer(action.origin.getName());
			p.setAddedToDisplay(false); // if stunned, can add a card next turn 
			if (p != null) {
				if (gameState.getTournament() != null) {
					if (gameState.hasHighScore(p) == false) {
						p.setParticipation(false);
						message("YOU have been ELIMINATED from " + gameState.getTournament().getName() + "!", p);
						messageExcept(p.getName() + " has been ELIMINATED from " + gameState.getTournament().getName()+ "!", p);
						if (p.getDisplay().hasMaiden()) { // check for maiden
							if (p.getNumTokens() > 0) { // if they have tokens
								ArrayList<Object> tokens = new ArrayList<Object>();
								for (Object obj : p.getTokens()) {
									tokens.add(obj);
								}
								String strTok = prompt("You had a Maiden in your Display, please choose a token to return: ", p, tokens);
								Colour colour = new Colour(strTok.split(" ")[0]);
								Token token = new Token(colour, "token to remove");
								p.removeToken(token);
							}
						}
						p.getDisplay().clear();
					} else {
						gameState.setHighScore(p.getDisplay().score(gameState.getTournament().getColour()));
					}
				}
				endTurn();
			}
			return true;
		}
		if (action.object instanceof Withdraw) {
			Player p = gameState.getPlayer(action.origin.getId());
			p.setAddedToDisplay(false); // if stunned, can add a card next turn 
			if (p != null) {
				p.setParticipation(false);
				message("You withdraw from " + gameState.getTournament().getName() + "!", p);
				messageExcept(p.getName() + " has withdrew from " + gameState.getTournament().getName() + "!", p);
				if (p.getDisplay().hasMaiden()) { // check for maiden
					if (p.getNumTokens() > 1) { // if they have tokens
						ArrayList<Object> tokens = new ArrayList<Object>();
						for (Object obj : p.getTokens()) {
							tokens.add(obj);
						}
						String strTok = prompt("You had a Maiden in your Display, please choose a token to return: ", p, tokens);
						Colour colour = new Colour(strTok.split(" ")[0]);
						Token token = new Token(colour, "token to remove");
						p.removeToken(token);
					}else if (p.getNumTokens() == 1){
						p.removeToken(p.getTokens().get(0));
					}
				}
				p.getDisplay().clear();
				endTurn();
			}
			return true;
		}
		if (action.object instanceof Play) {
			Card c = ((Play) action.object).getCard();
			if (c instanceof DisplayCard) {
				// player is stunned, and has already played DisplayCard
				if (action.origin.getStunned() &&
						action.origin.getAddedToDisplay()) { 
					message("You are Stunned, and can't play another Display Card this turn.", gameState.getPlayer(action.origin.getId()));
					return false;
				} else {
					broadcast(action.origin.getName() + " plays a " + c.toString());
					gameState.addDisplay(gameState.getPlayer(action.origin.getName()), (DisplayCard) c);
					gameState.removeHand(gameState.getPlayer(action.origin.getName()), c);
					action.origin.setAddedToDisplay(true); // has added to display this turn
					return true;
				}
			}
			if (c instanceof ActionCard) {
				if (c.toString().equalsIgnoreCase("Ivanhoe")) {
					if (blockedAction != null) {
						broadcast(action.origin.getName() + " blocks a " + ((Play)blockedAction.object).getAction().toString() + " card with Ivanhoe!");
						System.out.println(action.origin.getName() + " blocks a " + ((Play)blockedAction.object).getAction().toString() + " card with Ivanhoe!");
					} else {
						broadcast(action.origin.getName() + " plays a useless Ivanhoe.");
						System.out.println(action.origin.getName() + " plays a useless Ivanhoe.");
					}
					gameState.removeHand(gameState.getPlayer(action.origin.getName()), c);
					GameState.getDeck().discard(c);
					blockedAction = null;
					return true;
				} else if (blockedAction == null) {
					broadcast(action.origin.getName() + " is playing a " + c.toString());
					gameState.removeHand(gameState.getPlayer(action.origin.getName()), c);
					GameState.getDeck().discard(c);
					this.time = System.currentTimeMillis();
					blockedAction = action;
					ArrayList<Player> originator = new ArrayList<Player>();
					originator.add(action.origin);
					for (ServerThread t : clients.keySet()){
						t.send(new Play(c, null, originator, null));
					}
					return true;
				} else {
					messageExcept("Your Action Card cooldown is " + (System.currentTimeMillis() - time)/1000 + " seconds.", gameState.getPlayer(action.origin.getName()));
					return false;
				}
			}
		}
		return false;
	}

	private boolean endTurn(){
		// check if tournament has a winner
		ArrayList<Player> a = gameState.getTournamentParticipants();
		Player next = null;
		if (a.size() <= 1) {
			Player winner = a.get(0);
			message("YOU have been VICTORIOUS in " + gameState.getTournament().getName() + "!", winner);
			messageExcept(winner.getName() + " has been VICTORIOUS in " + gameState.getTournament().getName() + "!", winner);
			
			Colour colour = gameState.getTournament().getColour();
			String strCol = colour.toString();
			if(strCol.equalsIgnoreCase("PURPLE")){
				ArrayList<Object> colours = new ArrayList<Object>();
				colours.add(new Colour(Colour.c.PURPLE));
				colours.add(new Colour(Colour.c.RED));
				colours.add(new Colour(Colour.c.BLUE));
				colours.add(new Colour(Colour.c.YELLOW));
				colours.add(new Colour(Colour.c.GREEN));
				for (Token t : winner.getTokens()){
					colours.remove(t.getColour());
				}
				strCol = prompt("Your deeds merit a token of your choice. What colour do you seek?", winner, colours);
				
				while(!Colour.getValid(strCol)){
					strCol = prompt("Please choose a valid colour of token...", winner, colours);
				}
				colour = new Colour(strCol);
			}
			
			if (winner.giveToken(new Token(colour, gameState.getTournament().getContext()))) {
				message("You get a " + colour + " token of favour!", winner);
			} else {
				message("You already have a " + colour
						+ " token, but you still get the satisfaction of winning.", winner);
			}
			gameState.endTournament();
			updateGameStates();
			//check if the game is done
			if (gameState.getNumPlayers() < 4 && winner.getNumTokens() == Config.MIN_PLAYER_TOKENS){
				broadcast(winner.getName() + " has won the game. Play again soon!");
				endGame(winner);
				return true;
			} else if (gameState.getNumPlayers() >= 4 && winner.getNumTokens() == Config.MAX_PLAYER_TOKENS){
				broadcast(winner.getName() + " has won the game. Play again soon!");
				endGame(winner);
				return true;
			}
			next = winner;
			gameState.setTurn(winner);
		}else{
			next = gameState.nextTurn();
		}
		Card drew = gameState.drawFromDeck();
		gameState.addHand(next, drew);
		for (ServerThread s : clients.keySet()){
			if (clients.get(s).equals(next)){
				s.send(new EndTurn());
			}
		}
		message("Your turn has begun.  You drew a " + drew.toString() + " card!", next);
		messageExcept(next.getName() + " has begun their turn!", next);
		updateGameStates();
		return false;
	}
	
	/*
	 * send a message to all players(threads)
	 */
	public void broadcast(String input) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			t.send(new Info(input));
		}
		System.out.println(input);
	}

	/*
	 * send a chat to all players(threads)
	 */
	public void broadcastChat(String input) {
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
				t.send(new Info(input));
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
				t.send(new Info(input));
				return true;
			}
		}
		return false;
	}

	/*
	 * prompt player for some input
	 */
	public String prompt(String input, Player p, ArrayList<Object> options) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if ((clients.get(t).equals(p))) {
				t.send(new Prompt(input, options));
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
			if (clients.get(t).getReadyValue() == Player.READY) {
				clients.get(t).setReady(Player.IN_GAME);
				
				for (int j = 0; j < 8; j++) {
					clients.get(t).addToHand(gameState.drawFromDeck());
				}
				
				gameState.addPlayer(clients.get(t));
			}
		}
		updateGameStates();
		Player startPlayer = gameState.nextTurn();
		Card drew = gameState.drawFromDeck();
		gameState.addHand(startPlayer, drew); 
		messageExcept(startPlayer.getName() + " starts their turn.", startPlayer);
		message("You are the starting player, and drew a " + drew.toString() + " card.", startPlayer);
		message("Start a tournament if able.", startPlayer);
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
			if (t.update(gameState))
				c++;
		}
		return c;
	}

	public boolean endGame(Player winner) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			t.send(new EndGame(winner));
			if (clients.get(t).getReadyValue() == Player.IN_GAME) {
				clients.get(t).reset();
			}
		}
		gameState.setNumPlayers(0);
		gameState = null;
		updateGameStates();
		broadcast("Game is finished");
		
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
		searchThread.shutdown();
		searchThread = null;
		inputThread.shutdown();
		inputThread = null;

		// Close the banlist
		try {
			banWriter.close();
			banReader.close();
			aiReader.close();
		} catch (IOException e) {

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
		if (gameState.getTournament() == null) {
			System.out.println("No tournament running.");
		}
		for (Player p : gameState.getPlayers()) {
			System.out.println(p.getName() + ":" + p.getId());
			System.out.println("  HAND:" + p.getHandSize() + "\n  TURN:" + p.isTurn() + "\n  TOUR:" + p.getParticipation() + "\n  ");
		}
		return true;
	}

	/*
	 * print the displays of all players in game
	 */
	public boolean printLargeDisplays() {
		System.out.println("DISPLAYS: ");
		if (gameState.getTournament() == null) {
			System.out.println(" ========================================================== ");
		} else {
			System.out.println(" ===== " + gameState.getTournament().getName() + " (" 
					+ gameState.getTournament().getColour().toString() + ") =====");
		}
		GameState temp = gameState;
		Colour colour;
		if (gameState.getTournament() == null) {
			colour = new Colour();
		} else {
			colour = gameState.getTournament().getColour();
		}
		for (Player p : gameState.getPlayers()) {
			String tokens = "";
			for (Token t : p.getTokens()){
				tokens += t.getColour().toString().substring(0, 1) + " ";
			}
			System.out.printf("%-20s", tokens);
		}
		System.out.println();
		for (Player p : gameState.getPlayers()) {
			String display = p.getName() + " (" + p.getDisplay().score(colour)	+ ")";
			if (p.getShielded()) { // show if shielded
				display += " (SHIELD)"; 
			}
			if (p.getStunned()) { // show if stunned
				display += " (STUNNED)";
			}
			System.out.printf("%-20s", display);
		}
		System.out.println();
		for (int i = 0; i < 10; i++) {
			for (Player p : temp.getPlayers()) {
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
		if (gameState.getTournament() == null) {
			System.out.println("Error: no tournament started, so no display");
			return false;
		}
		String status = "";
		if (p.getShielded()) { // show if shielded
			status += " (SHIELD)"; 
		}
		if (p.getStunned()) { // show if stunned
			status += " (STUNNED)";
		}
		System.out.println(status);
		if (!(p.getDisplay().print(gameState.getTournament().getColour()))) {
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
			if (p.getReadyValue()!= Player.IN_GAME) {
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
				message("The King has shown you favour, and given you a " + card.toString() + " card!", p);
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
					for (Colour.c colour : Colour.c.values()) {
						if (colour.toString().equalsIgnoreCase(strColour)) {
							card = new DisplayCard(value, new Colour(colour));
							p.addToHand(card); // give Display Card
							Trace.getInstance().write(this,
									card.toString() + " Display Card given to " + p.getName() + ".");
							System.out.println(card.toString() + " Display Card given to " + p.getName() + ".");
							message("The King has shown you favour, and given you a " + card.toString() + " card!", p);
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
		for (Player p : this.gameState.getPlayers()) {
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
	
	/*
	 * start an AI Client
	 */
	public boolean startAI(String[] strArgs) {
		double tournamentSkill;
		double displaySkill;
		double actionSkill;
		double withdrawSkill;
		
		// check if strArgs matches a profile name
		for (String[] profile : this.profiles) {
			if ((String.join(" ", (Arrays.copyOfRange(profile, 0, profile.length-4)))).equals(String.join(" ", strArgs))) {
				try {
					tournamentSkill = Double.parseDouble(profile[profile.length-4]);
					displaySkill = Double.parseDouble(profile[profile.length-3]);
					actionSkill = Double.parseDouble(profile[profile.length-2]);
					withdrawSkill = Double.parseDouble(profile[profile.length-1]);
				} catch (NumberFormatException nfe) {
					Trace.getInstance().exception(this, nfe);
					System.out.println("Error: skills for this profile did not load correctly. AI Client could not start.");
					return false;
				}
				Thread ai = new ClientAI(this.address, this.port, 
						String.join(" ", strArgs),
						tournamentSkill, 
						displaySkill, 
						actionSkill, 
						withdrawSkill);
				ai.start();
				return true;
			}
		}
		
		// not enough skills in args
		if (strArgs.length != 4) { 
			System.out.println("Error: arguments did not match a profile, and were not enough skills for new AI. AI Client could not start.");
			return false; 
		}
		
		// get skills from arguments
		try {
			tournamentSkill = Double.parseDouble(strArgs[0]);
			displaySkill = Double.parseDouble(strArgs[1]);
			actionSkill = Double.parseDouble(strArgs[2]);
			withdrawSkill = Double.parseDouble(strArgs[3]);
		} catch (NumberFormatException nfe) {
			Trace.getInstance().exception(this, nfe);
			System.out.println("Error: arguments were not numbers, and did not match a profile. AI Client could not start.");
			return false;
		}
		
		Thread ai = new ClientAI(this.address, this.port, 
				tournamentSkill, 
				displaySkill, 
				actionSkill, 
				withdrawSkill);
		ai.start();
		
		return true;
	}
	
	/*
	 * end current tournament or game
	 */
	public boolean cmdEnd(String arg) {
		// end tournament
		if (arg.equals("-t")) {
			if ((gameState != null) && (gameState.getTournament() != null)) { 
				broadcast("Server ending current tournament.");
				gameState.endTournament();
				return true;
			} else {
				System.out.println("Error: no tournament running.");
				return false;
			}
			
		// end game
		} else if (arg.equals("-g")) {
			if (gameState != null) {
				broadcast("Server ending game.");
				endGame(null);
				return true;
			} else {
				System.out.println("Error: no game running.");
				return false;
			}
			
		// error
		} else {
			System.out.println("Error: incorrect argument for /end command. Type /help.");
			return false;
		}
	}
	
	/* 
	 * list AI Client Profiles that were read in
	 */
	public boolean listAIProfiles() {
		// no profiles were read on startup
		if (profiles.isEmpty()) { 
			System.out.println("No AI Client Profiles were read in on startup.");
			return false;
		}

		// list out the profiles
		System.out.println("AI Client Profiles:");
		System.out.printf(" %-20s %3s \n", "Name:", "Skills:");
		System.out.println(" ======================ST===D===A===W===");
		for (String[] profile : profiles) {
			System.out.printf(" %-20s %3s %3s %3s %3s\n", 
					String.join(" ", Arrays.copyOfRange(profile, 0, profile.length-4)), 
					profile[profile.length-4], 
					profile[profile.length-3], 
					profile[profile.length-2], 
					profile[profile.length-1]);
		}
		return true;
	}
}
