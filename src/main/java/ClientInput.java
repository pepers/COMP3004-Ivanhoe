package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.resources.Config;
import main.resources.Language;
import main.resources.Trace;

public class ClientInput extends Thread{

	Boolean stop = false;  // use to stop the Client
	BufferedReader reader; // reader from the user
	String input;          // user input
	Client client;         // client class
	Action action;         // client's action to take
	Language language;     // to translate chat
	
	public ClientInput (Client client, InputStream s) {
		this.client = client;
		reader = new BufferedReader(new InputStreamReader(s));
		
		// set up language to translate chat
		language = new Language(Language.Dialect.none, false);
	}
	
	public void run () {
		while(!this.stop) {
			try {
				input = reader.readLine(); // get next line of input
			} catch (IOException e) {
				e.printStackTrace();
			} 
			if(input.length() > 0){
				if (validCmd(input)) {               // process valid commands
					if (client.processCmd(input)) {
						Trace.getInstance().write(this, client.player.getName() +
									": command processed: " + input);
					} else {
						Trace.getInstance().write(this, client.player.getName() +
									": invalid command: " + input);
						System.out.println("Client: invalid command, try typing '/help' for more info.");
					}
				} else if (input.charAt(0) == '/') { // process invalid commands
					System.out.println("Client: invalid command, try typing '/help' for more info.");
					Trace.getInstance().write(this, client.player.getName() +
									": invalid command: " + input);
				} else {                             // process chat
					String translated = language.translate(input);
					action = new Chat(translated); 
					client.send(action);
					Trace.getInstance().write(this, client.player.getName() + ": " + 
									"chat sent: " + input);
				}
			}
		}
	}
	
	/*
	 * returns if a valid command or not
	 */
	public boolean validCmd (String in) {
		// commands start with slash (/)
		if (in.charAt(0) != '/') {
			return false;
		} 
		
		// check existing commands
		for (Config.ClientCommand cmd: Config.ClientCommand.values()) {
			if (in.startsWith(cmd.toString(), 1)) { return true; }
		}
		
		return false;
	}
}
