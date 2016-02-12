package main.java;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
	private ArrayList<Card> deck;
	private ArrayList<Card> discard;
	
	public Deck(){
		deck = new ArrayList<Card>();
		discard = new ArrayList<Card>();
	}
	
	public Card draw(){
		if(deck.size() == 0){
			deck = discard;
			shuffle();
		}
		return deck.get(0);
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
			deck.add(new Card());
		}
		
	}

}
