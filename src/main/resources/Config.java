package main.resources;

public class Config {
	public static int DEFAULT_PORT = 5050;
	public static String DEFAULT_HOST = "127.0.0.1";
	public static int MAX_PLAYERS = 5;
	public static int MIN_PLAYERS = 3;
	
	// possible commands:
	public enum ClientCommand { display, 
								draw, 
								hand, 
								help, 
								list, 
								play, 
								ready, 
								setname, 
								shutdown, 
								translate, 
								withdraw }
	
	public enum ServerCommand { ban,
								display,
								end, 
								give,
								hand,
								help,
								kick,
								list,
								max,
								min,
								pardon,
								port,
								shutdown,
								start,  
								translate }
	
}
