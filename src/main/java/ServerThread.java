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
	private Socket socket;
	private boolean stop = false;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	public Queue<Object> actions;
	
	public ServerThread(Server server, Socket socket) {
		super();
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
		
			actions.add(receive());
			//System.out.println("Thread: Action Queue(" +actions.size()+ ")");
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
			System.out.println("Connection closed.");
		} catch (IOException e) {
			stop = true;
			e.printStackTrace();
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
}
