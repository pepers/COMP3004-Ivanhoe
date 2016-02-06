package main.java;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
	
	ServerSocket serverSocket;
	int port;
	
	public Server(int port) {
		this.port = port;
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
}
