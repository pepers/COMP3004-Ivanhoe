package main.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import main.resources.Trace;

public class ServerThread extends Thread {
	
	private static int incID = 0;

	private int id;							//unique id for this thread and its client
	private Socket socket;					//socket to communicate with the client
	private boolean stop = false;			//flag to stop this thread
	private boolean disconnected = false;	//flag for if the client suddenly dropped
	private ObjectInputStream input;		//stream to receive objects from the client
	private ObjectOutputStream output;		//stream to send objects to the client

	public Queue<Object> actions;			// actions sent over the connection
	public Queue<Prompt> promptResponses;	// prompt actions sent over the connection

	public ServerThread(Server server, Socket socket) {
		this.id = ++incID;
		this.socket = socket;
		actions = new LinkedList<Object>();
		promptResponses = new LinkedList<Prompt>();
		try {
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			Trace.getInstance().write(this, "Opened socket stream at " + socket.getLocalSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Return the full address of the client
	public String getNetwork() {
		return socket.getInetAddress().toString() + ":" + socket.getPort();
	}

	// Return just the trimmed address of the client connection
	public String getAddress() {
		return socket.getInetAddress().toString().substring(1);
	}

	// Get the thread's unique identifier
	public int getID() {
		return id;
	}

	// Main execution thread
	public void run() {
		while (!stop) {
			Object o = receive();
			if (o != null) {
				if (o instanceof Prompt) {
					promptResponses.add((Prompt) o);
				} else {
					actions.add(o);
				}
			}
		}
	}

	// Send object o to the client
	public Boolean send(Object o) {
		try {
			output.writeObject(o);
		} catch (IOException e) {
			Trace.getInstance().write(this, "Unexpected exception: writing object to output stream");
			Trace.getInstance().exception(this, e);
			return false;
		}
		return true;
	}

	// Wait for an object from the client
	public Object receive() {
		try {
			return (input.readObject());
		} catch (ClassNotFoundException e) {
			Trace.getInstance().write(this, "Exception: Found foreign object.");
			Trace.getInstance().exception(this, e);
		} catch (SocketException e) {
			disconnected = true;
			Trace.getInstance().exception(this, e);
		} catch (IOException e) {
			stop = true;
			Trace.getInstance().exception(this, e);
		}
		return null;
	}

	// Return true if the client has disconnected suddenly
	public boolean getDead() {
		return disconnected;
	}

	// Send an updated game state to the client
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

	// Closes the socket and flags the thread to stop
	public void shutdown() {
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			Trace.getInstance().exception(this, e);
		}
		stop = true;
	}
}
