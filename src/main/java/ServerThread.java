package main.java;

import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread{
	private Server server;
	private Socket socket;
	private int ID;
	private String clientAddress;
	private boolean stop = false;
	
	public ServerThread(Server server, Socket socket) {
		super();
		this.server = server;
		this.socket = socket;
		this.ID = socket.getPort();
		this.clientAddress = socket.getInetAddress().getHostAddress();
		start();
	}
	
	public int getID(){
		return ID;
	}

	public void run(){
		while(!stop){
			
		}
	}
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop = true;
	}
}
