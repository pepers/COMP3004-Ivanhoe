package main.java;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
	
	ServerSocket serverSocket;
	int port;
	int numClients;
	
	
	public Server(int port) {
		this.port = port;
	}
	
	public int getConnected(){
		return numClients;
	}
	
	public void startup(){
		try {
			System.out.println("Binding to port " + port + ", please wait  ...");
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown(){
		try {
			System.out.println("Shutting down server @ " + port + ", please wait  ...");
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
