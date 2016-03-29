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
		gamestate(""),
		hand(""), 
		help(""), 
		list(""), 
		play(" [card name]"), 
		ready(""), 
		setname(" [new name (can't already exist, be empty, or start with '-' or '/')]"), 
		shutdown(""), 
		translate(" [dialect ('none', 'oldEnglish')]"), 
		tokens(""),
		tournament(" [tournament colour (purple, red, blue, yellow, or green)] [card name]"),
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
		ai(" [Tournament Skill] [Display Skill] [Action Skill] [Withdraw Skill] (-1 to 1 for each option)"),
		ban(" [ip address]"),
		censor(""),
		display(" [player name (leave empty for all)]"),
		end(" [-t (for tournament) or -g (for game)]"), 
		gamestate(""),
		give(" [player number] [card name (eg: ivanhoe, purple:3)]"),
		hand(" [player name]"),
		help(""),
		kick(" [player name (or player number)]"),
		list(""),
		max(" [maximum # of players]"),
		min(" [minimum # of players]"),
		pardon(" [ip address]"),
		port(" [port #]"),
		shutdown(""),
		start(""),  
		tokens(""),
		translate(" [dialect ('none', 'oldEnglish')]");
		
		private String syntax;  // syntax for the command
		
		private ServerCommand (String syntax) {
			this.syntax = syntax;
		}
		
		public String getSyntax () {
			return syntax;
		}
	}
	
}
