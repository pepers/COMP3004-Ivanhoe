package main.resources;

public class Config {
	public static int DEFAULT_PORT = 5050;
	public static String DEFAULT_HOST = "127.0.0.1";
	public static int MAX_PLAYERS = 5;
	public static int MIN_PLAYERS = 2;
	
	// possible commands:
	public enum ClientCommand { 
		censor(""),
		display(" [player name ('-a' for all, or leave empty for own display)]"), 
		end(""),
		hand(""), 
		help(""), 
		list(""), 
		play(" [card name]"), 
		ready(""), 
		setname(" [new name (can't already exist, be empty, or start with '-' or '/')]"), 
		shutdown(""), 
		translate(" [dialect ('none', 'oldEnglish')]"), 
		tournament(""),
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
		display(" [player name (leave empty for all)]"),
		end(""), 
		gamestate(""),
		give(""),
		hand(""),
		help(""),
		kick(" [player name (or player number)]"),
		list(""),
		max(" [maximum # of players]"),
		min(" [minimum # of players]"),
		pardon(""),
		port(" [port #]"),
		shutdown(""),
		start(""),  
		translate(" [dialect ('none', 'oldEnglish')] [-c (optional censor)]");
		
		private String syntax;  // syntax for the command
		
		private ServerCommand (String syntax) {
			this.syntax = syntax;
		}
		
		public String getSyntax () {
			return syntax;
		}
	}
	
}
