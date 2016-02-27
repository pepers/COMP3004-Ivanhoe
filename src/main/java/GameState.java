package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class GameState implements Serializable{
	
	private static final long serialVersionUID = 1L;	
	//Game stuff
	Deck deck;
	Tournament tnmt = null;
	
	public GameState(){
		deck = new Deck();
	}
	
	public GameState(Server s){
		//set up player array
		Iterator<ServerThread> i = s.clients.keySet().iterator();
		deck = new Deck();
		deck.initialize();
	}
}
