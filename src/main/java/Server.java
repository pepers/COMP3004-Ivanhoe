package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import main.resources.Config;
import main.resources.Trace;

public class Server implements Runnable{
	
	Thread thread;
	SearchThread searchThread;
	ServerSocket serverSocket;
	int port;
	int numClients;
	HashMap<ServerThread, Player> clients;
	
	
	boolean stop;
	public Queue<Object> actions;
	
	public Server(int port) {
		this.port = port;
		clients = new HashMap<ServerThread, Player>();
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
			
			actions = new LinkedList<Object>();
			stop = true;
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
			stop = false;
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
			
			for (ServerThread t : clients.keySet()){
				actions.add(t.actions.poll());
			}			
			//System.out.println(actions.size());
		}
	}

	public void kick(int id) {
		Trace.getInstance().write(this, "Kicking player @" + port + "...");		
	}
}
