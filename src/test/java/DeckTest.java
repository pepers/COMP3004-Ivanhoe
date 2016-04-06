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
		d.initialize();
	}
	
	@Test
	public void TestDrawing(){
		System.out.println("/nTest: Drawing the whole deck.");
		int i = 70;
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
	@Test
	public void TestShuffle(){
		System.out.println("/nTest: Shuffling the deck.");
		Card top = d.peek();
		d.shuffle();
		Card top2 = d.peek();
		assert(!top.equals(top2));
	}
	
	@Test
	public void TestTotal(){
		System.out.println("/nTest: Counting the cards.");
		assert(d.size() == 110);
	}
}
