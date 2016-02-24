package main.java;

public class ActionWrapper {
	public Object object;
	public ServerThread origin;
	public ActionWrapper(Object o, ServerThread t){
		object = o;
		origin = t;
	}
}
