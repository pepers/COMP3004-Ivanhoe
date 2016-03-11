package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class ClientInput extends Thread {

	private Boolean stop = false; // use to stop the Client
	private BufferedReader reader; // reader from the user
	private String input; // user input
	private Client client; // client class
	private Action action; // client's action to take
	private Language language; // to translate chat

	public ClientInput(Client client, InputStream s) {
		this.client = client;
		reader = new BufferedReader(new InputStreamReader(s));

		// set up language to translate chat
		language = new Language(Language.Dialect.none, false);
	}

	public void run() {
		while (!this.stop) {
			try {
				while (this.reader.ready()) {
					this.input = this.reader.readLine(); // get next line of
					processInput(this.input);										// input
				}
			} catch (IOException e) {
				break;
			}
		}
	}

	public boolean processInput(String input) {
		if (input.length() > 0) {
			if (validCmd(input)) { // process valid commands
				if (client.processCmd(input)) {
					Trace.getInstance().write(this,
							client.getPlayer().getName() + ": command processed: " + input);
				} else {
					Trace.getInstance().write(this,
							client.getPlayer().getName() + ": invalid command: " + input);
					System.out.println("Client: invalid command, try typing '/help' for more info.");
				}
			} else if (input.charAt(0) == '/') { // process invalid
														// commands
				System.out.println("Client: invalid command, try typing '/help' for more info.");
				Trace.getInstance().write(this, client.getPlayer().getName() + ": invalid command: " + input);
			} else { // process chat
				String translated = language.translate(input);
				this.action = new Chat(translated);
				client.send(this.action);
				Trace.getInstance().write(this, client.getPlayer().getName() + ": " + "chat sent: " + input);
			}
		}
		return true;
	}

	/*
	 * returns if a valid command or not
	 */
	public boolean validCmd(String in) {
		// commands start with slash (/)
		if (in.charAt(0) != '/') {
			return false;
		}

		// check existing commands
		for (Config.ClientCommand cmd : Config.ClientCommand.values()) {
			if (in.startsWith(cmd.toString(), 1)) {
				return true;
			}
		}

		return false;
	}

	public void shutdown() {
		stop = true;
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
