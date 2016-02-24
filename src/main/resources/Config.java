package main.resources;

public class Config {
	public static int DEFAULT_PORT = 5050;
	public static String DEFAULT_HOST = "127.0.0.1";
	public static int MAX_PLAYERS = 5;
	public static int MIN_PLAYERS = 3;
	
	// possible commands:
	public enum ClientCommand { setname, ready, draw, help, shutdown, play, withdraw, list, hand, display }
	public enum ServerCommand { kick, start, shutdown, list, help, end, max, min, port, 
								give, ban, pardon, hand, display }
	
}
