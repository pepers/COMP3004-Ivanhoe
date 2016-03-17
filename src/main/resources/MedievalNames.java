package main.resources;

import java.util.Random;

public class MedievalNames {
	static Random r = new Random();
	
	public static String genTrinket(String context){
		
		switch (r.nextInt(4)){
			case 0:
				return context + " Competition";
			case 1:
				return "Tournament of " + context;
			case 2:
				return "Grand Tourney of " + context;
			case 3:
				return context+ " Celebratory Competition";
			default:
				return "An Unknown Tournament";
		}
	}
	
	public static String genContext(){
		switch (r.nextInt(2)){
			case 0:
				return castle[r.nextInt(castle.length)];
			case 1:
				return festival[r.nextInt(festival.length)];
			default:
				return "An Unknown Tournament";
		}
	}
	
	static final String[] castle = {
			"Scatterby Castle",
			"Artanges Palace",
			"Blaise Castle",
			"Windsor Keep",
			"Dandlestone Castle",
			"Werthingham Castle",
			"Miserth Stronghold",
			"Leyebourne Stronghold",
			"Aysel Citadel",
			"Nightwell Fortress",
			"Ardleby Fort",
			"Maetrine Stronghold",
			"Berlington Keep",
			"Haersley Palace",
			"Narlington Fort",
			"Pernstow Fort",
			"Rachdale Palace",
			"Yanborough Keep",
			"Highcalere Keep",
			"Rye Castle",
			"Middleborough Keep",
			"Calbridge Keep",
			"Haword Castle",
			"Easkerton Palace",
			"Rye Stronghold",
			"Kalepeck Fort",
			"Karthmere Fort",
			"Swanton Keep",
			"Khurleigh Fortress",
			"New Wandour Palace"
	};
	static final String[] festival = {
			"New Year’s Day",
			"The Feast of Epiphany",
			"The Feast of the Magi",
			"Candlemas",
			"Mandatum",
			"Resurrection Day",
			"Paschal",
			"St. George’s Day",
			"Rogationtide",
			"Willowsbank",
			"Trinity Sunday",
			"Whitsun",
			"The Feast of Corpus Christi",
			"Midsummer Day",
			"St. Swithun’s Day",
			"Lammastide",
			"The Harvest",
			"All Hallows Eve",
			"Twelfthnight"
	};
}
