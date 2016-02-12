package test.java;

import org.junit.Before;
import org.junit.Test;

import main.java.Card;
import main.java.Deck;

public class DeckTest {
	
	Deck d;
	
	@Before
	public void before(){
		d = new Deck();
		d.addDummyCards(30);
	}
	
	@Test
	public void TestDrawing(){
		int i = 30;
		int counter = 0;
		boolean b = true;
		
		while (b){
			Card c = d.draw();
			d.discard(c);
			if(i == 0){
				if (counter > 2){
					b = false;
				}
				i = 30;		
				counter++;
			}else{
				i--;
			}
			assert(d.size() == i);
		}
	}
}
