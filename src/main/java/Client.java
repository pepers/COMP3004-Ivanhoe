package main.java;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import main.resources.Config;
import main.resources.Trace;

public class Client implements Runnable {
	
	Thread receiveThread;                   // Client thread to receive from Server
	Boolean stop = false;					// use to stop the Client
	ClientAction action;                    // client's action
	private Socket socket = null;           // socket to connect to Server
	ObjectOutputStream clientOutputStream;  // send objects to Server
	ObjectInputStream clientInputStream;    // receive objects from Server
	
	public static void main (String args[]) {
		Client client = new Client();  // client object
		client.startUp();
	}
	
	/*
	 * initial Client startup activities
	 */
	public void startUp() {
		
		System.out.println(" ▄█   ▄█    █▄     ▄████████ ███▄▄▄▄      ▄█    █▄     ▄██████▄     ▄████████  ");
		System.out.println(" ███  ███    ███   ███    ███ ███▀▀▀██▄   ███    ███   ███    ███   ███    ███ ");
		System.out.println(" ███▌ ███    ███   ███    ███ ███   ███   ███    ███   ███    ███   ███    █▀  ");
		System.out.println(" ███▌ ███    ███   ███    ███ ███   ███  ▄███▄▄▄▄███▄▄ ███    ███  ▄███▄▄▄     ");
		System.out.println(" ███▌ ███    ███ ▀███████████ ███   ███ ▀▀███▀▀▀▀███▀  ███    ███ ▀▀███▀▀▀     ");
		System.out.println(" ███  ███    ███   ███    ███ ███   ███   ███    ███   ███    ███   ███    █▄  ");
		System.out.println(" ███  ███    ███   ███    ███ ███   ███   ███    ███   ███    ███   ███    ███ ");
		System.out.println(" █▀    ▀██████▀    ███    █▀   ▀█   █▀    ███    █▀     ▀██████▀    ██████████ ");
		                                                                               
		// welcome message
		System.out.println("\nClient: Welcome brave knight!");
				                                         
				                                         
		// get user's name
		String username = userInput("What is thy name?: ");
		action = new SetName(username);
		
		// connect to Server
		if (connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT)) {
			send(action);  // send user's name to Server
		}		

		// start new thread to receive from Server
		receiveThread = new Thread(this);
		receiveThread.start();
	}
	
	public void run () {
		while(!stop) { // while Client is running
			action = new Ready();
			send(action);
			action = new DrawCard();
			send(action);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/* 
	 * get user input from console
	 */
	public String userInput (String message) {
		Scanner user_input = new Scanner(System.in);
		System.out.println(message);
		String input = user_input.nextLine();
		Trace.getInstance().write(this, message + input);
		user_input.close();
		return input;
	}
	
	/*
	 *  connect to Server
	 */
	public Boolean connect(String IPAddress,int port) {
		try {  
			Trace.getInstance().write(this, "attempting to connect to server...");
			this.socket = new Socket(IPAddress, port);
	    	Trace.getInstance().write(this, "connected to server: " + socket.getInetAddress() + 
	    			" : " + socket.getLocalPort());
	    	clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
	    	//clientOutputStream.flush();
	    	//clientInputStream = new ObjectInputStream(socket.getInputStream());
	    	return true;
		} catch(UnknownHostException uhe) {  
			System.out.println("Unknown Host");
			Trace.getInstance().exception(this, uhe);
		} catch(IOException ioe) {  
			System.out.println("Unexpected exception");
			Trace.getInstance().exception(this, ioe);
		}
		return false;
	}
	
	/*
	 *  send object to Server
	 */
	public Boolean send(Object o) {
		try {
			clientOutputStream.writeObject(o);
			return true;
		} catch (IOException e) {
			System.out.println("Unexpected exception: writing object to output stream");
			Trace.getInstance().exception(this, e);
		}
		return false;
	}
	
	/*
	 *  receive object from Server
	 */
	public Object receive() {
		// will return this null object if received object was not of known type,
		// or error reading from stream
		Object received = null; 
		
		try {
			received = clientInputStream.readObject();
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class Not Found Exception: reading object from input stream");
			Trace.getInstance().exception(this, cnf);
		} catch (IOException ioe) {
			System.out.println("Unexpected Exception: reading object from input stream");
			Trace.getInstance().exception(this, ioe);
		}
		
		// TODO: determine type of object received
		if (received instanceof ActionCard) {          // Action Card received
			received = (ActionCard)received;
			Trace.getInstance().test(this, "ActionCard object received");
		} else if (received instanceof DisplayCard) {  // Display Card received
			received = (DisplayCard)received;
			Trace.getInstance().test(this, "DisplayCard object received");
		} else {
			received = null;                           // unrecognized object received
			Trace.getInstance().test(this, "unrecognized object received");
		}
		
		return received;
	}

}
