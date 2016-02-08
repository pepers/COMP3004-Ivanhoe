package main.java;

import java.net.Socket;

public class ServerThread extends Thread{
	private Server server;
	private Socket socket;
	private int ID;
	private String clientAddress;
	
	public ServerThread(Server server, Socket socket) {
		super();
		this.server = server;
		this.socket = socket;
		this.ID = socket.getPort();
		this.clientAddress = socket.getInetAddress().getHostAddress();
	}
	
	public int getID(){
		return ID;
	}
}
