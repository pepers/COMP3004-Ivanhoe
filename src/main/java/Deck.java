package main.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Deck implements Serializable{

	private static final long serialVersionUID = 1L;
	private ArrayList<Card> deck;
	private ArrayList<Card> discard;
	
	public Deck(){
		deck = new ArrayList<Card>();
		discard = new ArrayList<Card>();
	}
	
	public int initialize(){
		
		//Joust
		add(new DisplayCard(3, DisplayCard.Colour.purple), 4);
		add(new DisplayCard(4, DisplayCard.Colour.purple), 4);
		add(new DisplayCard(5, DisplayCard.Colour.purple), 4);
		add(new DisplayCard(7, DisplayCard.Colour.purple), 2);
		
		//Sword
		add(new DisplayCard(3, DisplayCard.Colour.red), 6);
		add(new DisplayCard(4, DisplayCard.Colour.red), 6);
		add(new DisplayCard(5, DisplayCard.Colour.red), 2);
		
		//Axe
		add(new DisplayCard(2, DisplayCard.Colour.blue), 4);
		add(new DisplayCard(3, DisplayCard.Colour.blue), 4);
		add(new DisplayCard(4, DisplayCard.Colour.blue), 4);
		add(new DisplayCard(5, DisplayCard.Colour.blue), 2);
		
		//Morningstar
		add(new DisplayCard(2, DisplayCard.Colour.yellow), 4);
		add(new DisplayCard(3, DisplayCard.Colour.yellow), 8);
		add(new DisplayCard(4, DisplayCard.Colour.yellow), 2);
		
		//No Weapon
		add(new DisplayCard(1, DisplayCard.Colour.green), 14);
		
		//Squires
		add(new DisplayCard(2, DisplayCard.Colour.none), 8);
		add(new DisplayCard(3, DisplayCard.Colour.none), 8);
		
		//Maidens
		add(new DisplayCard(6, DisplayCard.Colour.none), 4);
		
		shuffle();
		return deck.size();
	}
	
	public void add(Card c){
		deck.add(c);
	}
	
	public void add(Card c, int n){
		for (int i = 0; i<n; i++){
			add(c);
		}
	}
	public Card draw(){
		if(deck.size() == 0){
			deck = discard;
			shuffle();
		}
		return deck.remove(0);
	}
	
	public void discard(Card c) {
		discard.add(c);
	}
	
	private void shuffle(){
		ArrayList<Card> a = new ArrayList<Card>();
		Random r = new Random();
		while(deck.size() > 0){
			a.add(deck.remove(r.nextInt(deck.size())));
		}
		deck = a;
	}

	public int size() {
		return deck.size();
	}

	public void addDummyCards(int n) {
		for (int i = 0; i < n; i++){
			deck.add(new DisplayCard(3, DisplayCard.Colour.purple));
		}
		
	}

}
