package main.java;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import main.resources.Config;
import main.resources.Trace;

public class Client {
	
	private Socket socket = null;
	ObjectOutputStream clientOutputStream;  // send objects to Server
	ObjectInputStream clientInputStream;    // receive objects from Server
	
	public static void main (String args[]) {
		Client client = new Client();  // client object
		ClientAction action;           // client's action
		
		// welcome message
		System.out.println(" _____                _                ");
		System.out.println("|_   _|              | |               ");
		System.out.println("  | |_   ____ _ _ __ | |__   ___   ___ ");
		System.out.println("  | \\ \\ / / _` | '_ \\| '_ \\ / _ \\ / _ \\");
		System.out.println(" _| |\\ V / (_| | | | | | | | (_) |  __/");
		System.out.println("|_____\\_/ \\__,_|_| |_|_| |_|\\___/ \\___|");
		System.out.println("\nClient: Welcome brave knight!");
				                                         
				                                         
		// get user's name
		String username = client.userInput("What is thy name?: ");
		action = new SetName(username);
		
		// connect to Server
		if (client.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT)) {
			client.send(action);  // send user's name to Server
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
	    	clientInputStream = new ObjectInputStream(socket.getInputStream());
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
		if (received instanceof Card) { // TODO: example for Card object
			received = (Card)received;
		} else {
			received = null;            // unrecognized object received
		}
		
		return received;
	}

}
