package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Deck implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Card> deck;		//the primary structure for the deck
	private ArrayList<Card> discard;	//used cards are added here so we can recreate the deck later
	
	public Deck(){
		deck = new ArrayList<Card>();
		discard = new ArrayList<Card>();
	}
	
	//Add all the cards to the deck
	public int initialize(){
		// colours for display cards:
		Colour none = new Colour(Colour.c.NONE);
		Colour blue = new Colour(Colour.c.BLUE);
		Colour green = new Colour(Colour.c.GREEN);
		Colour purple = new Colour(Colour.c.PURPLE);
		Colour red = new Colour(Colour.c.RED);
		Colour yellow = new Colour(Colour.c.YELLOW);
		
		//Joust
		add(new DisplayCard(3, purple), 4);
		add(new DisplayCard(4, purple), 4);
		add(new DisplayCard(5, purple), 4);
		add(new DisplayCard(7, purple), 2);
		
		//Sword
		add(new DisplayCard(3, red), 6);
		add(new DisplayCard(4, red), 6);
		add(new DisplayCard(5, red), 2);
		
		//DISPLAY CARDS:
		//Axe
		add(new DisplayCard(2, blue), 4);
		add(new DisplayCard(3, blue), 4);
		add(new DisplayCard(4, blue), 4);
		add(new DisplayCard(5, blue), 2);
		
		//Morningstar
		add(new DisplayCard(2, yellow), 4);
		add(new DisplayCard(3, yellow), 8);
		add(new DisplayCard(4, yellow), 2);
		
		//No Weapon
		add(new DisplayCard(1, green), 14);
		
		//Squires
		add(new DisplayCard(2, none), 8);
		add(new DisplayCard(3, none), 8);
		
		//Maidens
		add(new DisplayCard(6, none), 4);
		
		//ACTION CARDS:
		//Change Colours
		add(new ActionCard("Unhorse"));
		add(new ActionCard("Change Weapon"));
		add(new ActionCard("Drop Weapon"));
		
		//Special
		add(new ActionCard("Shield"));
		add(new ActionCard("Stunned"));
		add(new ActionCard("Ivanhoe"));
		
		//Affect Displays
		add(new ActionCard("Break Lance"));
		add(new ActionCard("Riposte"), 3);
		add(new ActionCard("Dodge"));
		add(new ActionCard("Retreat"));
		add(new ActionCard("Knock Down"), 2);
		add(new ActionCard("Outmaneuver"));
		add(new ActionCard("Charge"));
		add(new ActionCard("Countercharge"));
		add(new ActionCard("Disgrace"));
		add(new ActionCard("Adapt"));
		add(new ActionCard("Outwit"));
		
		shuffle();
		return deck.size();
	}
	
	//Add a card c to the deck
	public void add(Card c){
		deck.add(c);
	}
	
	//Add N cards c to the deck
	public void add(Card c, int n){
		for (int i = 0; i<n; i++){
			add(c);
		}
	}
	
	//Remove and return the top card of the deck
	//If the deck is empty, move the discard to the deck
	public Card draw(){
		if(deck.size() == 0){
			deck = discard;
			shuffle();
		}
		return deck.remove(0);
	}
	
	//Add a card to the discard pile
	public void discard(Card c) {
		discard.add(c);
	}
	
	//Shuffle the deck
	public void shuffle(){
		ArrayList<Card> a = new ArrayList<Card>();
		Random r = new Random();
		while(deck.size() > 0){
			a.add(deck.remove(r.nextInt(deck.size())));
		}
		deck = a;
	}
	
	//Return the size of the deck
	public int size() {
		return deck.size();
	}

	//Look at the top card of the deck
	public Card peek(){
		return deck.get(0);
	}
}
