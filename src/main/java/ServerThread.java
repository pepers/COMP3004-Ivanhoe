package main.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import main.resources.Trace;

public class ServerThread extends Thread{
	private Socket socket;
	private int ID;
	private boolean stop = false;
	private ObjectInputStream input;
	private Player player;
	
	private Queue<Object> actions;
	
	public ServerThread(Server server, Socket socket) {
		super();
		this.socket = socket;
		this.player = new Player(String.valueOf(Thread.currentThread().getId()));
		this.ID = socket.getPort();
		actions = new LinkedList<Object>();
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
			actions.add(receive());
		}
	}

	public Object receive(){
		
		try {
			return(input.readObject());
			/*
			if(o instanceof ClientAction){
				if(o instanceof SetName){
					System.out.println("SetName recieved");
					player.setName(((SetName) o).getName());
				}
			}else{
				System.out.println("Recieved something else");
			}
			*/
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
