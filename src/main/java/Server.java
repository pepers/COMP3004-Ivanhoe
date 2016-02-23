package main.java;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import main.resources.Config;
import main.resources.Trace;

public class Server implements Runnable {

	// Threads
	Thread thread; // main thread for the server
	ServerInput inputThread; // thread that handles console input (commands)
	SearchThread searchThread; // thread that searches for new players

	ServerSocket serverSocket; // primary network socket
	int port; // server port
	int numClients; // number of clients
	int numReady; // number of ready player
	ConcurrentHashMap<ServerThread, Player> clients; // holds the threads mapped
														// to player objects

	boolean stop = false; // stops the main thread
	public Queue<Action> actions; // server actions to operate upon

	// Constructor
	public Server(int port) {
		this.port = port;
		clients = new ConcurrentHashMap<ServerThread, Player>();
		actions = new LinkedList<Action>();
	}

	public static void main(String[] args) {
		System.out.println("Beginning server setup...");
		Server s = new Server(Config.DEFAULT_PORT);
		if (s.startup()) {
			System.out.println("Setup successful.");
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
		
		if (numClients < Config.MAX_PLAYERS) {
			//Create a new thread
			serverThread = new ServerThread(this, socket);
			serverThread.start();
			
			//Create a player object
			int n = clients.size();
			clients.put(serverThread, new Player("Knight " + n));
			Trace.getInstance().write(this, "Player " + (n - 1) + " created: Knight " + (n - 1));
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

	//Removing a connection via index
	public boolean removeThread(int index) {
		int k = 0;
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			if (k == index) {
				System.out.println("Removing player \"" + clients.get(t).username + "\"...");
				Trace.getInstance().write(this, "Removing player \"" + clients.get(t).username + "\"...");
				numClients--;
				t.shutdown();
				return true;
			}
			k++;
		}
		System.out.println("Couldnt find player (" + index + ")");
		return false;
	}
	public boolean removeThread(String name) {
		for (ServerThread t : clients.keySet()){
			if(clients.get(t).username.equals(name)){
				System.out.println("Removing player \"" + name + "\"...");
				Trace.getInstance().write(this, "Removing player \"" + name + "\"...");
				numClients--;
				t.shutdown();
				return true;
			}
		}
		System.out.println("Couldnt find player (" + name + ")");
		return false;
	}
	//Main thread
	public void run() {
		while (!stop) {

			int readyPlayers = 0;
			Iterator<ServerThread> i = clients.keySet().iterator();
			while (i.hasNext()) { 
				ServerThread t = i.next();
				Object o = t.actions.poll(); // get an action from the thread
				Player p = clients.get(t);
				readyPlayers = readyPlayers + (p.ready ? 1 : 0);
				if (o != null) {
					Trace.getInstance().write(this, "Got an action from " + p.username);
					actions.add(new Action(o, t)); // create a new local action
				}
			}
			numReady = readyPlayers;

			if (!actions.isEmpty()) {
				evaluate(actions.poll());
			}

		}
	}

	private boolean evaluate(Action action) {

		if (action.object instanceof SetName) {
			System.out.println(
					clients.get(action.origin).username + " changed name to " + ((SetName) action.object).getName());
			clients.get(action.origin).setName(((SetName) action.object).getName());
			return true;
		}
		if (action.object instanceof Chat) {
			System.out.println(clients.get(action.origin).username + ": " + ((Chat) action.object).getMessage());
			return true;
		}

		if (action.object instanceof Ready) {
			clients.get(action.origin).toggleReady();
			System.out.println(clients.get(action.origin).username + " is ready!");
			return true;
		}

		System.out.println("Polled something else");
		return false;
	}

	// send a message to all players(threads)
	public void broadcast(String input) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while (i.hasNext()) {
			ServerThread t = i.next();
			t.send(new Chat(input));
		}
	}

	//Start a game
	public boolean startGame() {

		if (numReady >= Config.MIN_PLAYERS) {
			System.out.println("(" + numReady + "/" + numClients + ") players ready.");
			System.out.println("Preparing to start a game...");
			// we start a game here
			return true;
		} else {
			System.out.println(Config.MIN_PLAYERS + " players are needed to start a game.");
			return false;
		}
	}

	//Shutdown the server
	public boolean shutdown() {
		Trace.getInstance().write(this, "Shutting down server, please wait  ...");
		System.out.println("Shutting down server, please wait  ...");
		
		//Shutdown the network infrastructure
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
		
		//Stop all the threads
		stop = true;
		thread = null;
		searchThread.stop = true;
		searchThread = null;
		inputThread.stop = true;
		inputThread = null;
		System.out.println("Shutdown complete. Goodbye!");
		return true;
	}
}