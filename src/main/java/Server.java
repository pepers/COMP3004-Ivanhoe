package main.java;

import java.io.IOException;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class Server implements Runnable, Serializable{

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
	Language language = new Language(Language.Dialect.none, false);  // to translate chat

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

		return true;
	}

	// Adding a new connection
	public boolean addThread(Socket socket) {
		Trace.getInstance().write(this, "Client Requesting connection: " + socket.getPort());
		ServerThread serverThread;

		if (numClients < maxPlayers) {
			// Create a new thread
			serverThread = new ServerThread(this, socket);
			SetName name = ((SetName) serverThread.receive());
			serverThread.start();
			// Create a player object

			clients.put(serverThread, new Player(name.getName()));
			numClients++;
		} else {
			Trace.getInstance().write(this, "Client Tried to connect:" + socket.getLocalSocketAddress());
			Trace.getInstance().write(this, "Client refused: maximum number of clients reached (" + numClients + ")");
			System.out.println("Client refused: maximum number of clients reached (" + numClients + ")");
			return false;
		}

		System.out.println(clients.get(serverThread).username + " joined.");
		Trace.getInstance().write(this, "Client Accepted: " + socket.getPort());
		return true;
	}

	// Removing a connection via id
	public boolean removeThread(int id) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (t.getID() == id) {
				System.out.println("Removing player \"" + clients.get(t).username + "\" (" + t.getID() + ")...");
				Trace.getInstance().write(this,
						"Removing player \"" + clients.get(t).username + "\" (" + t.getID() + ")...");
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
			if (clients.get(t).username.equals(name)) {
				System.out.println("Removing player \"" + name + "\" (" + t.getID() + ")...");
				Trace.getInstance().write(this, "Removing player \"" + name + "\" (" + t.getID() + ")...");
				numClients--;
				t.shutdown();
				clients.remove(t);
				return true;
			}
		}
		System.out.println("Couldnt find player (" + name + ")");
		return false;
	}

	public void listClients() {

		System.out.println("Connected Players:");
		System.out.printf(" %-3s %-20s %-8s %s\n", "#", "Name", "State", "Port");
		System.out.println(" ============================================");
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			Player p = clients.get(t);
			String state = p.getReadyState();
			System.out.printf(" %-3s %-20s %-8s %s\n", t.getID(), p.username, state, t.getNetwork());
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
				if (t.getDead()) {
					numClients--;
					String name = clients.get(t).username;
					t.shutdown();
					clients.remove(t);
					broadcast(name + " disconnected.");
					continue;
				}
				Object o = t.actions.poll(); // get an action from the thread
				Player p = clients.get(t);
				if (p == null) {
					continue;
				}
				readyPlayers = readyPlayers + (p.ready == 1 ? 1 : 0);
				if (o != null) {
					Trace.getInstance().write(this, "Got an action from " + p.username);
					actions.add(new ActionWrapper(o, p)); // create a new local
															// action
				}
			}
			numReady = readyPlayers;

			if (!actions.isEmpty()) {
				ActionWrapper a = actions.poll();
				evaluate(a);
				if(gameState != null){
					updateGameStates();
				}else{
					//a game command was received at a wrong time
				}		
			}
		}
	}

	private boolean evaluate(ActionWrapper action) {

		if (action.object instanceof SetName) {

			if (!((SetName) action.object).isInit()) {
				String s = (action.origin.username + " changed name to \"" + ((SetName) action.object).getName()
						+ "\"");
				broadcast(s);
			}
			action.origin.setName(((SetName) action.object).getName());
			return true;
		}
		if (action.object instanceof Chat) {
			String message = ((Chat) action.object).getMessage();
			String from = action.origin.username;
			String translated = language.translate(((Chat) action.object).getMessage());
			broadcast(from + ": " + translated);
			Trace.getInstance().write(this, "Server: " + action.object.getClass().getSimpleName() + 
									" received from " + from + ": " + message);
			return true;
		}

		if (action.object instanceof Ready) {
			String s = (action.origin.username + " is ready!");
			broadcast(s);
			action.origin.toggleReady();
			return true;
		}
		
		if(gameState == null){
			return false;
		}
		//Game state evaluation
		if (action.object instanceof DrawCard) {
			int n = action.origin.addHand(gameState.deck.draw());
			broadcast(action.origin.username + " draws a card. (" + n + ")");
			return true;
		}
		if (action.object instanceof Withdraw){
			action.origin.inTournament = false;
			broadcast(action.origin.username + " withdraws from " + gameState.tnmt.name);
			return true;
		}
		if (action.object instanceof Play) {
			broadcast(action.origin.username + " plays a " + ((Play) action.object).getCard().toString());
			return true;
		}
		return false;
	}
	
	// send a message to all players(threads)
	public void broadcast(String input) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			t.send(new Chat(input));
		}
		System.out.println(input);
	}
	
	// Start a game
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
			if(clients.get(t).ready == 1){
				clients.get(t).ready = 2;
				gameState.addPlayer(clients.get(t));
			}
		}
		return true;
	}

	// Update each client with a new gameState
	public void updateGameStates(){
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			t.update(gameState, clients.get(t));
		}
	}
	// Shutdown the server
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
		System.out.println("Shutdown complete. Goodbye!");
		return true;
	}

	public boolean printHand(String name) {
		Player p = fromString(name);
		if (p == null) {
			return false;
		}
		System.out.println("Hand:");
		for (Card c : p.hand) {
			System.out.println("  " + c.toString());
		}

		return true;
	}

	public Player fromString(String name) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (clients.get(t).username.equals(name)) {
				return clients.get(t);
			}
		}
		System.out.println("Couldnt find player (" + name + ")");
		return null;
	}
	
	public void setMinPlayers(int n){
		minPlayers = n;
		System.out.println("New MINIMUM players is " + n);
	}
	public void setMaxPlayers(int n){
		maxPlayers = n;
		System.out.println("New MAXIMUM players is " + n);
	}
}
