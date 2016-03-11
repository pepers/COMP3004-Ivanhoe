package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ClientInput extends Thread {

	private Boolean stop = false; // use to stop the Client
	private BufferedReader reader; // reader from the user
	private String input; // user input
	private Client client; // client class

	public ClientInput(Client client, InputStream s) {
		this.client = client;
		reader = new BufferedReader(new InputStreamReader(s));
	}

	public void run() {
		while (!this.stop) {
			try {
				while (this.reader.ready()) {
					this.input = this.reader.readLine(); // get next line of
					client.processInput(this.input);										// input
				}
			} catch (IOException e) {
				break;
			}
		}
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
