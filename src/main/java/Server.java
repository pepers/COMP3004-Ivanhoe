package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import main.resources.Config;
import main.resources.Trace;

public class Server implements Runnable{
	
	Thread thread;
	ServerInput inputThread;
	SearchThread searchThread;
	ServerSocket serverSocket;
	int port;
	int numClients;
	int numReady;
	ConcurrentHashMap<ServerThread, Player> clients;
	
	
	boolean stop = false;
	public Queue<Action> actions;
	
	public Server(int port) {
		this.port = port;
		clients = new ConcurrentHashMap<ServerThread, Player>();
	}
	
	public static void main(String[] args){
		Server s = new Server(Config.DEFAULT_PORT);
		s.startup();
	}
	
	public int getConnected(){
		return numClients;
	}
	
	public void startup(){
		try {
			Trace.getInstance().write(this, "Binding to port " + port + ", please wait  ...");
			System.out.println("Binding to port " + port + ", please wait  ...");
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
		
			actions = new LinkedList<Action>();
			stop = false;
			
			inputThread = new ServerInput(this, System.in);
			searchThread = new SearchThread(this);
			thread = new Thread(this);
			
			inputThread.start();
			searchThread.start();
			thread.start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		Trace.getInstance().write(this, "Setup successful.");
		System.out.println("Setup successful.");
	}
	
	void addThread(Socket socket) {
		System.out.println("Client Requesting connection: " + socket.getPort());
		Trace.getInstance().write(this, "Client Requesting connection: " + socket.getPort());
		
		if (numClients < Config.MAX_PLAYERS) {		
			try {
				ServerThread serverThread = new ServerThread(this, socket);
				serverThread.open();
				serverThread.start();
				
				int n = clients.size();
				clients.put(serverThread, new Player("Knight "+n));
				Trace.getInstance().write(this, "Player " + (n-1) +" created: Knight "+(n-1));
				numClients++;
				System.out.println("Client Accepted: " + socket.getPort());
				Trace.getInstance().write(this, "Client Accepted: " + socket.getPort());
				
				System.out.println("Client Accepted: " + socket.getPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Trace.getInstance().write(this, "Client Tried to connect:" + socket.getLocalSocketAddress());
			Trace.getInstance().write(this, "Client refused: maximum number of clients reached ("+ numClients +")");
			System.out.println("Client Tried to connect:" + socket.getLocalSocketAddress());
			System.out.println("Client refused: maximum number of clients reached ("+ numClients +")");
		}
	}
	
	public boolean removeThread(int ID) {
		
		int k = 0;
		Iterator<ServerThread> i = clients.keySet().iterator();
		while(i.hasNext()){		
			ServerThread t = i.next();				
			if(k == ID){
				System.out.println("Removing player \"" + clients.get(t).username + "\"...");
				Trace.getInstance().write(this, "Removing player \"" + clients.get(t).username + "\"...");
				numClients--;
				t.shutdown();
				return true;
			}
			k++;
		}
		System.out.println("Couldnt find player (" + ID + ")");
		return false;
	}
	
	public void shutdown(){
		try {
			Trace.getInstance().write(this, "Shutting down server @ " + port + ", please wait  ...");
			stop = true;
			searchThread = null;
			
			//Added this so the .accept will stop blocking; avoid a socket close error
			new Socket("localhost", port);
			
			//close each of the clients individually
			for (ServerThread t : clients.keySet()){
				t.shutdown();
			}
			serverSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (!stop){
		
			Iterator<ServerThread> i = clients.keySet().iterator();
			
			int readyPlayers = 0;
			while(i.hasNext()){							//iterate over the players
				ServerThread t = i.next();				
				Object o = t.actions.poll();			//get an action from the thread
				Player p = clients.get(t);				
				readyPlayers = readyPlayers + (p.ready ? 1 : 0);
				if(o != null){
					Trace.getInstance().write(this, "Got an action from " + p.username);
					actions.add(new Action(o, t));		//create a new local action to process
				}
			}
			numReady = readyPlayers;
			
			if(!actions.isEmpty()){
				evaluate(actions.poll());
			}
			
		}
	}

	private boolean evaluate(Action action){
	
		if(action.object instanceof SetName){
			System.out.println(clients.get(action.origin).username + " changed name to " + ((SetName)action.object).getName());
			clients.get(action.origin).setName(((SetName)action.object).getName());
			return true;
		}
		if(action.object instanceof Chat){
			System.out.println(clients.get(action.origin).username + ": " + ((Chat)action.object).getMessage());
			return true;
		}
		
		if(action.object instanceof Ready){
			clients.get(action.origin).toggleReady();
			System.out.println(clients.get(action.origin).username + " is ready!");
			return true;
		}
		
		System.out.println("Polled something else");
		return false;
	}

	//send a message to all players(threads)
	public void broadcast(String input) {
		Iterator<ServerThread> i = clients.keySet().iterator();
		while(i.hasNext()){							
			ServerThread t = i.next();				
			t.send(new Chat(input));
		}
		
	}

	public boolean startGame() {
		
		if(numReady >= Config.MIN_PLAYERS){
			System.out.println("(" + numReady + "/" + numClients + ") players ready.");
			System.out.println("Preparing to start a game...");
			//we start a game here
			return true;
		}else{
			System.out.println(Config.MIN_PLAYERS + " players are needed to start a game.");
			return false;
		}
	}
}
