package main.java;

public class ActionWrapper {
	/*
	 * Helper class to tag actions with an owner in a clean way
	 */
	
	public Object object;		//The Action class
	public Player origin;		//The player that spawned it
	
	public ActionWrapper(Object o, Player p){
		this.object = o;
		this.origin = p;
	}
}
