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
	SearchThread searchThread;
	ServerSocket serverSocket;
	int port;
	int numClients;
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
			searchThread = new SearchThread(this);
			thread = new Thread(this);
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
				clients.put(serverThread, new Player("Knight "+serverThread.getId()));
				System.out.println("Player created: Knight "+serverThread.getId());
				this.numClients++;
				System.out.println("Client Accepted: " + socket.getPort());
				Trace.getInstance().write(this, "Client Accepted: " + socket.getPort());
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
	
	public void shutdown(){
		try {
			Trace.getInstance().write(this, "Shutting down server @ " + port + ", please wait  ...");
			stop = true;
			searchThread = null;
			
			//Added this so the .accept will stop blocking; avoid a socket close error
			new Socket("localhost", port);
			
			//close each of the clients individually
			for (ServerThread t : clients.keySet()){
				t.close();
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
			while(i.hasNext()){
				ServerThread t = i.next();
				Object o = t.actions.poll();
				Player p = clients.get(t);
				readyPlayers = readyPlayers + (p.ready ? 1 : 0);
				if(o != null){
					System.out.println("Got an action from " + p.username);
					actions.add(new Action(o, t));
				}
			}
			
			
			//System.out.println("(" + readyPlayers + "/" + numClients + ") players ready");
			
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
			return true;
		}
		
		System.out.println("Polled something else");
		return false;
	}
	
	public void kick(int id) {
		Trace.getInstance().write(this, "Kicking player @" + port + "...");		
	}
}
