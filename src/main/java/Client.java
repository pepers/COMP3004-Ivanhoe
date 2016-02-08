package main.java;

import java.io.*;
import java.net.*;

import main.resources.Config;
import main.resources.Trace;

public class Client {
	
	private Socket socket = null;
	
	public Client() {
		Trace.exception(this, new IOException());  // just testing Trace, remove after!
	}
	
	public static void main (String args[]) {
		Client client = new Client();
		client.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
	}
	
	public Boolean connect(String IPAddress,int port) {
		try {  
			System.out.println("Attempting to connect to server...");
			this.socket = new Socket(IPAddress, port);
	    	System.out.println("Connected to server: " + socket.getInetAddress() + 
	    			" : " + socket.getLocalPort());
	    	return true;
		} catch(UnknownHostException uhe) {  
			System.out.println("Unknown Host");
			Trace.exception(this, uhe);
		} catch(IOException ioe) {  
			System.out.println("Unexpected exception");
			Trace.exception(this, ioe);
		}
		return false;
	}

}
