package main.java;

public class GameEngine extends Thread{
	boolean stop = false;
	Server server = null;
	
	public GameEngine(Server s){
		server = s;
	}
	
	public void run(){
		while (!stop){
			System.out.println("Game is running");
		}
	}
}
