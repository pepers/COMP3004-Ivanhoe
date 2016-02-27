package main.resources;

public class Config {
	public static int DEFAULT_PORT = 5050;
	public static String DEFAULT_HOST = "127.0.0.1";
	public static int MAX_PLAYERS = 5;
	public static int MIN_PLAYERS = 3;
	
	// possible commands:
	public enum ClientCommand { 
		display(""), 
		draw(""), 
		hand(""), 
		help(""), 
		list(""), 
		play(""), 
		ready(""), 
		setname(" [new name]"), 
		shutdown(""), 
		translate(" [dialect (none, oldEnglish)]"), 
		withdraw("");
								
		private String syntax;  // syntax for the command
		
		private ClientCommand (String syntax) {
			this.syntax = syntax;
		}
		
		public String getSyntax () {
			return syntax;
		}
	}
	
	public enum ServerCommand { 
		ban(""),
		display(""),
		end(""), 
		give(""),
		hand(""),
		help(""),
		kick(" [player name (or player number)]"),
		list(""),
		max(" [maximum # of players]"),
		min(" [minimum # of players]"),
		pardon(""),
		port(""),
		shutdown(""),
		start(""),  
		translate(" [dialect (none, oldEnglish)]");
		
		private String syntax;  // syntax for the command
		
		private ServerCommand (String syntax) {
			this.syntax = syntax;
		}
		
		public String getSyntax () {
			return syntax;
		}
	}
	
}
