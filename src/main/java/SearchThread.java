package main.java;

import java.io.IOException;
import java.net.SocketException;
import main.resources.Trace;

public class SearchThread extends Thread{
	
	private Server server;			//The server that spawned this thread
	private boolean stop = false;	//flag to stop this thread
	
	public SearchThread(Server s){
		this.server = s;
	}
	
	//Main execution thread. This thread waits for new connections, then sends them to the server.
	public void run() {
		while (!stop){
			try {
				server.addThread(server.serverSocket.accept());
			} catch (SocketException e){
				Trace.getInstance().exception(this, e);
			} catch (IOException e) {
				Trace.getInstance().exception(this, e);
			}	
		}
	}
	
	//Stop this thread
	public void shutdown() {
		stop = true;
	}
}
