package main.java;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

import main.resources.Config;
import main.resources.Trace;

public class Client implements Runnable {

	Thread receiveThread; // Client thread to receive from Server
	ClientInput inputThread;
	Boolean stop = false; // use to stop the Client
	Action action; // client's action
	private Socket socket = null; // socket to connect to Server
	ObjectOutputStream clientOutputStream; // send objects to Server
	ObjectInputStream clientInputStream; // receive objects from Server

	public static void main(String args[]) {
		Client client = new Client(); // client object
		client.startUp();
	}

	/*
	 * initial Client startup activities
	 */
	public void startUp() {

		// welcome message
		System.out.println(" _____                _                ");
		System.out.println("|_   _|              | |               ");
		System.out.println("  | |_   ____ _ _ __ | |__   ___   ___ ");
		System.out.println("  | \\ \\ / / _` | '_ \\| '_ \\ / _ \\ / _ \\");
		System.out.println(" _| |\\ V / (_| | | | | | | | (_) |  __/");
		System.out.println("|_____\\_/ \\__,_|_| |_|_| |_|\\___/ \\___|");
		System.out.println("\nClient: Welcome brave knight!");
		
		// get user's name
		String username = userInput("What is thy name?: ");
		action = new SetName(username, true);
		
		// connect to Server
		while(true){
			// get the Server's inet address
			String address = userInput("Where are the tournaments to be held? (enter nothing for default InetAddress): ");
			if (address == "") {
				address = Config.DEFAULT_HOST;
			}
			
			// get the Server's port
			int port;
			while (true) {
				String portStr = userInput("At which arena? (enter nothing for default Port): ");
				if (portStr == "") {
					port = Config.DEFAULT_PORT;
				} else {
					try {
						port = Integer.parseInt(portStr);
					} catch (NumberFormatException nfe) {
						System.out.println("Please enter a port number...");
						continue;
					}
					break;
				}
			}
			
			// attempt to connect to Server
			if (connect(address, port)) {
				send(action); // send user's name to Server
				
				// start new thread to get Client commands
				inputThread = new ClientInput(this, System.in);
				inputThread.start();
				
				// start new thread to receive from Server
				receiveThread = new Thread(this);
				receiveThread.start();
				break;
			} else {
				System.out.println("There are no tournaments at that location! \n");
				
				while (true) {
					String ui = userInput("Do you want to search for a new tournament (y/n): ");
					if (ui.equalsIgnoreCase("y")) {
						break;
					} else if (ui.equalsIgnoreCase("n")) {
						System.exit(0); // TODO: make shutdown method
					} else {
						System.out.println("That is not an option, my good knight!");
					}
				}
			}
		}		
		
	}

	public void run() {

		while (!stop) {

			Object o = receive();
			if(o != null){
				evaluate(o);
			}

		}
	}

	/*
	 * get user input from console
	 */
	public String userInput(String message) {
		Scanner user_input = new Scanner(System.in);
		System.out.println(message);
		String input = user_input.nextLine();
		Trace.getInstance().write(this, message + input);
		return input;
	}

	/*
	 * connect to Server
	 */
	public Boolean connect(String IPAddress, int port) {
		try {
			Trace.getInstance().write(this, "attempting to connect to server...");
			this.socket = new Socket(IPAddress, port);
			Trace.getInstance().write(this,
					"connected to server: " + socket.getInetAddress() + " : " + socket.getLocalPort());
			
			clientOutputStream = new ObjectOutputStream(socket.getOutputStream());	
			clientInputStream = new ObjectInputStream(socket.getInputStream());
			return true;
		} catch (SocketException se) {
			System.out.println("Unable to connect to a server.");
			Trace.getInstance().exception(this, se);
		} catch (UnknownHostException uhe) {
			System.out.println("Unknown Host");
			Trace.getInstance().exception(this, uhe);
		} catch (IOException ioe) {
			System.out.println("Unexpected exception");
			Trace.getInstance().exception(this, ioe);
		}
		return false;
	}

	/*
	 * send object to Server
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
	 * receive object from Server
	 */
	public Object receive() {
		// will return this null object if received object was not of known
		// type,
		// or error reading from stream
		Object received = null;

		try {
			received = clientInputStream.readObject();
		} catch (SocketException se) {
				System.out.println("Server was closed.");
				stop = true;
				receiveThread = null;
				inputThread.stop = true;
				inputThread = null;		
				Trace.getInstance().exception(this, se);
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class Not Found Exception: reading object from input stream");
			Trace.getInstance().exception(this, cnf);
		} catch (IOException ioe) {
			System.out.println("Unexpected Exception: reading object from input stream");
			stop = true;
			Trace.getInstance().exception(this, ioe);
		}

		// TODO: determine type of object received
		if (received instanceof ActionCard) { // Action Card received
			received = (ActionCard) received;
			Trace.getInstance().test(this, "ActionCard object received");
		} else if (received instanceof DisplayCard) { // Display Card received
			received = (DisplayCard) received;
			Trace.getInstance().test(this, "DisplayCard object received");
		} else if (received instanceof Chat){
			received = (Chat) received;
			Trace.getInstance().test(this, "Chat object received");
		} else {
			received = null; // unrecognized object received
			Trace.getInstance().test(this, "unrecognized object received");
		}

		return received;
	}

	private boolean evaluate(Object action) {

		if (action instanceof Chat) {
			System.out.println(((Chat) action).getMessage());
			return true;
		}
		
		System.out.println("Polled something else");
		return false;
	}
}
