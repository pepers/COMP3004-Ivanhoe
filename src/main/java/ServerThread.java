package main.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

import main.resources.Trace;

public class ServerThread extends Thread{
	private Server server;
	private Socket socket;
	private int ID;
	private String clientAddress;
	private boolean stop = false;
	private ObjectInputStream input;
	
	public ServerThread(Server server, Socket socket) {
		super();
		this.server = server;
		this.socket = socket;
		this.ID = socket.getPort();
		this.clientAddress = socket.getInetAddress().getHostAddress();
	}
	
	//opens a buffered input stream to accept client network objects
	public void open() throws IOException {
		input = new ObjectInputStream(socket.getInputStream());
		Trace.getInstance().write(this, "Opened socket stream at " + socket.getLocalSocketAddress());
		System.out.println("Opened socket stream at " + socket.getLocalSocketAddress());
	}
	
	public int getID(){
		return ID;
	}

	public void run(){
		while(!stop){
			Object o = receive();
		}
	}

	public Object receive(){
		
		try {
			Object o = input.readObject();
			if(o instanceof ClientAction){
				System.out.println("Recieved an action");
			}else{
				System.out.println("Recieved something else");
			}
			return o;
		} catch (ClassNotFoundException e) {
			System.out.println("Exception: Found foreign object.");
			e.printStackTrace();
		} catch (IOException e) {
			stop = true;
			System.out.println("Exception: IO");
			e.printStackTrace();
		}
		return null;
	}
	//shutdown command: closes the socket and flags the thread to stop
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop = true;
	}
}
