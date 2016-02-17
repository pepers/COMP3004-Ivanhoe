package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.sun.security.ntlm.Client;

import main.resources.Config;
import main.resources.Trace;

public class Server implements Runnable{
	
	Thread searchThread;
	ServerSocket serverSocket;
	int port;
	int numClients;
	private HashMap<Integer, ServerThread> clients;
	
	boolean search;
	
	public Server(int port) {
		this.port = port;
		clients = new HashMap<Integer, ServerThread>();
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
			
			search = true;
			searchThread = new Thread(this);
			searchThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Trace.getInstance().write(this, "Setup successful.");
		System.out.println("Setup successful.");
	}
	
	private void addThread(Socket socket) {
		System.out.println("Client Requesting connection: " + socket.getPort());
		Trace.getInstance().write(this, "Client Requesting connection: " + socket.getPort());
		if (numClients < Config.MAX_PLAYERS) {		
			try {
				ServerThread serverThread = new ServerThread(this, socket);
				serverThread.open();
				serverThread.start();
				clients.put(serverThread.getID(), serverThread);
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
			search = false;
			searchThread = null;
			
			//Added this so the .accept will stop blocking; avoid a socket close error
			new Socket("localhost", port);
			
			//close each of the clients individually
			for (int t : clients.keySet()){
				clients.get(t).close();
			}
			serverSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (search){
			try {
				addThread(serverSocket.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}

	public void kick(int id) {
		Trace.getInstance().write(this, "Kicking player @" + port + "...");
		clients.get(id).close();
		
	}
}
