package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
	
	Thread searchThread;
	ServerSocket serverSocket;
	int port;
	int numClients;
	
	boolean search;
	
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
			
			search = true;
			searchThread = new Thread(this);
			searchThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown(){
		try {
			System.out.println("Shutting down server @ " + port + ", please wait  ...");
			searchThread = null;
			search = false;
			serverSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (search){
			try {
				Socket s = serverSocket.accept();
				System.out.println("Connection recieved");
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
}
