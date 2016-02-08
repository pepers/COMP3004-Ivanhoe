package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import main.resources.Config;

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
	
	public int getConnected(){
		return numClients;
	}
	
	public void startup(){
		try {
			System.out.println("Binding to port " + port + ", please wait  ...");
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			
			search = true;
			searchThread = new Thread(this);
			searchThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void addThread(Socket socket) {
		System.out.println("Client Requesting connection: " + socket.getPort());
		if (numClients < Config.MAX_PLAYERS) {
			ServerThread serverThread = new ServerThread(this, socket);
			serverThread.start();
			clients.put(serverThread.getID(), serverThread);
			this.numClients++;
			System.out.println("Client Accepted: " + socket.getPort());
		} else {
			System.out.println("Client Tried to connect:" + socket.getLocalSocketAddress());
			System.out.println("Client refused: maximum number of clients reached ("+ numClients +")");
		}
	}
	
	public void shutdown(){
		try {
			System.out.println("Shutting down server @ " + port + ", please wait  ...");
			search = false;
			searchThread = null;
			
			//Added this so the .accept will stop blocking; avoid a socket close error
			new Socket("localhost", port);
			
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
}
