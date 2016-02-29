package main.java;

import java.io.*;
import java.net.*;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class Client implements Runnable {

	Thread receiveThread; // Client thread to receive from Server
	ClientInput inputThread = null; // thread to input Client commands
	Boolean stop = false; // use to stop the Client
	Action action; // client's action
	private Socket socket = null; // socket to connect to Server
	ObjectOutputStream clientOutputStream; // send objects to Server
	ObjectInputStream clientInputStream; // receive objects from Server
	BufferedReader input = null; // to get user input
	Language language; // to translate chat

	GameState game; // the local copy of the game state
	Player player = new Player("Default Name"); // the local copy of this
												// client's player

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

		// set up language to translate chat
		language = new Language(Language.Dialect.none, false);

		// get user's name
		String username = userInput("What is thy name?: ");
		action = new SetName(username, true);

		// connect to Server
		while (true) {
			// get the Server's inet address
			String address = userInput(
					"Where are the tournaments to be held? (enter nothing for default InetAddress): ");
			if (address.isEmpty()) {
				address = Config.DEFAULT_HOST;
			}

			// get the Server's port
			int port;
			while (true) {
				String portStr = userInput("At which arena? (enter nothing for default Port): ");
				if (portStr.isEmpty()) {
					port = Config.DEFAULT_PORT;
					break;
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

				// init gamestate
				game = new GameState();
				break;
			} else {
				System.out.println("There are no tournaments at that location! \n");

				while (true) {
					String ui = userInput("Do you want to search for a new tournament (y/n): ");
					if (ui.equalsIgnoreCase("y")) {
						break;
					} else if (ui.equalsIgnoreCase("n")) {
						shutdown(); // shut down client
					} else {
						System.out.println("That is not an option, my good knight!");
					}
				}
			}
		}

	}

	/*
	 * shut down Client
	 */
	public boolean shutdown() {
		Trace.getInstance().write(this, "Client shutting down...");
		System.out.println("\nClient: Shutting down...");

		// close socket
		try {
			socket.close();
		} catch (IOException e) {
			Trace.getInstance().exception(this, e);
			return false;
		}

		// close threads
		stop = true;
		receiveThread = null;
		inputThread.stop = true;
		inputThread = null;
	
		/*
		// close scanner
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
			}
		}
		*/
	
		/*
		// close socket streams
		if (clientInputStream != null) {
			try {
				clientInputStream.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
				return false;
			}
		}
		if (clientOutputStream != null) {
			try {
				clientOutputStream.close();
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
				return false;
			}
		}
		*/
		

		Trace.getInstance().write(this, "Client shut down, successfully.");
		return true;
	}

	/*
	 * continue receiving from Server
	 */
	public void run() {
		// while Client is running, keep connection with Server
		while (!stop) {
			Object o = receive(); // received object
			if (o != null) { // process object if it can be used
				process(o);
			}
		}
	}

	/*
	 * get user input from console
	 */
	public String userInput(String message) {
		input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(message);
		String strInput = "";
		try {
			strInput = input.readLine();
		} catch (IOException e) {
			Trace.getInstance().exception(this, e);
			return null;
		}
		Trace.getInstance().write(this, message + strInput);
		return strInput;
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
		// will return this null object if error reading from stream
		Object received = null;

		try {
			received = clientInputStream.readObject();
		} catch (SocketException se) {
			if (!(stop)) {
				System.out.println("Server was closed.");
				Trace.getInstance().exception(this, se);
				shutdown();
			}
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class Not Found Exception: reading object from input stream");
			Trace.getInstance().exception(this, cnf);
			shutdown();
		} catch (IOException ioe) {
			System.out.println("Unexpected Exception: reading object from input stream");
			Trace.getInstance().exception(this, ioe);
			shutdown();
		}

		return received;
	}

	/*
	 * determine type of object received and process accordingly return: true if
	 * recognized object, false if unrecognized object
	 */
	public boolean process(Object o) {
		/* STATES: */
		// Player
		if (o instanceof Player) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			player = (Player) o;
			Trace.getInstance().write(this, player.username + ": player has been updated");
			return true;

			// GameState
		} else if (o instanceof GameState) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			game = (GameState) o;
			Trace.getInstance().write(this, player.username + ": game state has been updated");
			return true;

			// ActionCard
		} else if (o instanceof ActionCard) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			player.addHand((ActionCard) o);
			System.out.println("Client: " + o.toString() + " added to hand");
			Trace.getInstance().write(this, player.username + ": " + o.toString() + " added to hand");
			return true;

			// DisplayCard
		} else if (o instanceof DisplayCard) {
			Trace.getInstance().write(this, player.username + ": " + o.getClass().getSimpleName() + " received");
			player.addHand((DisplayCard) o);
			System.out.println("Client: " + o.toString() + " added to hand");
			Trace.getInstance().write(this, player.username + ": " + o.toString() + " added to hand");
			return true;

			/* ACTIONS: */
			// Chat
		} else if (o instanceof Chat) {
			Trace.getInstance().write(this,
					player.username + ": " + o.getClass().getSimpleName() + " received: " + ((Chat) o).getMessage());
			String message = ((Chat) o).getMessage();
			System.out.println(language.translate(message));
			return true;

			// unrecognized object
		} else {
			Exception e = new Exception(player.username + ": unrecognized object received");
			Trace.getInstance().exception(this, e);
			return false;
		}
	}

}
