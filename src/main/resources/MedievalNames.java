package main.resources;

import java.util.Random;

public class MedievalNames {
	static Random r = new Random();
	
	public static String genTournament(){
		switch (r.nextInt(4)){
			case 0:
				return castle[r.nextInt(castle.length)] + " Joust";
			case 1:
				return "Tournament of " + castle[r.nextInt(castle.length)];
			case 2:
				return "Grand Tourney of  " + festival[r.nextInt(festival.length)];
			case 3:
				return festival[r.nextInt(festival.length)]+ " Celebratory Joust";
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
