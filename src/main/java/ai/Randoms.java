package main.java.ai;

import java.util.ArrayList;
import java.util.Random;

public class Randoms {
	
	private Random r;

	public Randoms() {
		r = new Random();
	}

	/*
	 * return random element from arraylist
	 */
	public <T>T get(ArrayList<T> list) {
		if (list.isEmpty()) { return null; }
		int index = r.nextInt(list.size());
		return list.get(index);
	}

	/*
 	 * decide if a choice will be made, based on skill
 	 */
	public boolean makeChoice(double s) {
		double skill = Math.abs(s);     // absolute value of skill (percentage)
		double chance = r.nextDouble(); // get next chance of making choice
	
		if (chance <= skill) { // decide if choice will be made or not
			return true;
		} else {
			return false;
		}			
	}
}