package main.java;

public class Action {
	public Object object;
	public ServerThread origin;
	public Action(Object o, ServerThread t){
		object = o;
		origin = t;
	}
}
