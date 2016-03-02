package main.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import main.resources.Trace;

public class ServerThread extends Thread{
	private static int incID = 0;
	
	private int id;
	private Socket socket;
	private boolean stop = false;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	public Queue<Object> actions;
	
	public ServerThread(Server server, Socket socket) {
		super();
		this.id = ++incID;
		this.socket = socket;
		actions = new LinkedList<Object>();
		//Open socket streams
		setup();
	}
	
	//opens a buffered input stream to accept client network objects
	public boolean setup(){
		try {
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		Trace.getInstance().write(this, "Opened socket stream at " + socket.getLocalSocketAddress());
		return true;
	}
	
	public Boolean send(Object o) {
		try {
			output.writeObject(o);
			return true;
		} catch (IOException e) {
			System.out.println("Unexpected exception: writing object to output stream");
			Trace.getInstance().exception(this, e);
		}
		return false;
	}
	
	public void run(){
		while(!stop){
		
			Object o = receive();
			if(o != null){
				actions.add(o);
			}
		}
	}

	public Object receive(){
		try {
			return(input.readObject());
			
		} catch (ClassNotFoundException e) {
			System.out.println("Exception: Found foreign object.");
			e.printStackTrace();
		} catch (SocketException e){
			stop = true;
		} catch (IOException e) {
			stop = true;
		}
		return null;
	}
	//shutdown command: closes the socket and flags the thread to stop
	public void shutdown() {
		stop = true;
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop = true;
	}

	public String getNetwork() {
		return socket.getInetAddress().toString() + ":" + socket.getPort();
	}

	public int getID() {
		return id;
	}
	
	public boolean getDead(){
		return stop;
	}

	public boolean update(GameState gameState) {
		try {
			output.reset();
			output.writeObject(gameState);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
