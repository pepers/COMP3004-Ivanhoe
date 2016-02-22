package main.java;

import java.io.IOException;

public class SearchThread extends Thread{
	Server server;
	boolean stop = false;
	public SearchThread(Server s){
		super();
		this.server = s;
	}
	
	public void run() {
		while (!stop){
			try {
				server.addThread(server.serverSocket.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
}
