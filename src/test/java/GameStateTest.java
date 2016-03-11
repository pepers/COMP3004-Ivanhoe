package test.java;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import main.java.ActionCard;
import main.java.Colour;
import main.java.DisplayCard;
import main.java.GameState;
import main.java.Player;
import main.java.Tournament;

public class GameStateTest {

	GameState g;
	Player[] players = { new Player("P1"), new Player("P2"), new Player("P3"), new Player("P4") };
	Colour none = new Colour(Colour.c.NONE);
	Colour blue = new Colour(Colour.c.BLUE);
	Colour green = new Colour(Colour.c.GREEN);
	Colour purple = new Colour(Colour.c.PURPLE);
	Colour red = new Colour(Colour.c.RED);
	Colour yellow = new Colour(Colour.c.YELLOW);

	@Before
	public void before() {
		g = new GameState();
		for (Player p : players) {
			g.addPlayer(p);
		}
	}

	@Test
	public void TestTournamentParticipants() {
		System.out.println("\nTest: Counting the tournament participants.");
		g.startTournament(new Tournament(blue));
		assertEquals(4, g.getTournamentParticipants().size());
	}

	@Test
	public void TestNextTurn() {
		System.out.println("\nTest: Getting the next turn.");
		Player currentPlayer = players[0];
		for (int i = 0; i < 10; i++) {
			assertEquals(players[i % players.length].getName(), currentPlayer.getName());
			currentPlayer = g.nextTurn();
		}
	}

	@Test
	public void TestStartTournament() {
		System.out.println("\nTest: Starting a tournament success.");
		g.startTournament(new Tournament(red));
		assert (g.getTournament() != null);
	}

	@Test
	public void TestStartTournamentPurple() {
		System.out.println("\nTest: Starting a tournament, restrictions apply.");
		g.startTournament(new Tournament(purple));
		assertEquals(purple, g.getTournament().getColour());
		g.endTournament();
		g.startTournament(new Tournament(purple));
		assert (g.getTournament() == null);
	}
	
	@Test
	public void TestCharge() {
		System.out.println("\nTest: Charge Action");
		g.addDisplay(players[0], new DisplayCard(2, none));
		g.addDisplay(players[0], new DisplayCard(3, none));
		assertEquals(2, g.getPlayer(players[0].getName()).getDisplay().size());

		g.addDisplay(players[1], new DisplayCard(3, blue));
		g.addDisplay(players[1], new DisplayCard(4, blue));
		g.addDisplay(players[1], new DisplayCard(5, blue));
		assertEquals(3, g.getPlayer(players[1].getName()).getDisplay().size());

		g.addDisplay(players[2], new DisplayCard(5, blue));
		g.addDisplay(players[2], new DisplayCard(2, none));
		g.addDisplay(players[2], new DisplayCard(2, blue));
		assertEquals(3, g.getPlayer(players[2].getName()).getDisplay().size());
		
		g.addDisplay(players[3], new DisplayCard(2, blue));
		assertEquals(1, g.getPlayer(players[3].getName()).getDisplay().size());
		
		g.startTournament(new Tournament(blue));
		g.execute(new ActionCard("Charge"));
		
		assertEquals(1, g.getPlayer(players[0].getName()).getDisplay().size());
		assertEquals(3, g.getPlayer(players[1].getName()).getDisplay().size());
		assertEquals(1, g.getPlayer(players[2].getName()).getDisplay().size());
		assertEquals(0, g.getPlayer(players[3].getName()).getDisplay().size());
	}
	
	@Test
	public void TestCountercharge() {
		System.out.println("\nTest: Countercharge Action");
		g.addDisplay(players[0], new DisplayCard(2, none));
		g.addDisplay(players[0], new DisplayCard(3, none));
		assertEquals(2, g.getPlayer(players[0].getName()).getDisplay().size());

		g.addDisplay(players[1], new DisplayCard(3, blue));
		g.addDisplay(players[1], new DisplayCard(4, blue));
		g.addDisplay(players[1], new DisplayCard(5, blue));
		assertEquals(3, g.getPlayer(players[1].getName()).getDisplay().size());

		g.addDisplay(players[2], new DisplayCard(5, blue));
		g.addDisplay(players[2], new DisplayCard(2, none));
		g.addDisplay(players[2], new DisplayCard(2, blue));
		assertEquals(3, g.getPlayer(players[2].getName()).getDisplay().size());
		
		g.addDisplay(players[3], new DisplayCard(2, blue));
		assertEquals(1, g.getPlayer(players[3].getName()).getDisplay().size());
		
		g.startTournament(new Tournament(blue));
		g.execute(new ActionCard("Countercharge"));
		
		assertEquals(2, g.getPlayer(players[0].getName()).getDisplay().size());
		assertEquals(2, g.getPlayer(players[1].getName()).getDisplay().size());
		assertEquals(2, g.getPlayer(players[2].getName()).getDisplay().size());
		assertEquals(1, g.getPlayer(players[3].getName()).getDisplay().size());
	}
	
	@Test
	public void TestDisgrace() {
		System.out.println("\nTest: Disgrace Action");
		g.addDisplay(players[0], new DisplayCard(2, none));
		g.addDisplay(players[0], new DisplayCard(3, none));
		assertEquals(2, g.getPlayer(players[0].getName()).getDisplay().size());
		g.startTournament(new Tournament(red));

		g.execute(new ActionCard("Disgrace"));
		assertEquals(0, g.getPlayer(players[0].getName()).getDisplay().size());
	}

	@Test
	public void TestOutmaneuver() {
		System.out.println("\nTest: Outmaneuver Action");
		g.addDisplay(players[0], new DisplayCard(2, red));
		g.addDisplay(players[0], new DisplayCard(1, blue));
		g.addDisplay(players[1], new DisplayCard(1, green));
		g.addDisplay(players[2], new DisplayCard(1, red));
		g.addDisplay(players[2], new DisplayCard(4, red));

		g.startTournament(new Tournament(red));

		g.execute(new ActionCard("Outmaneuver"));
		assertEquals(1, g.getPlayer(players[0].getName()).getDisplay().size());
		assertEquals(0, g.getPlayer(players[1].getName()).getDisplay().size());
		assertEquals(1, g.getPlayer(players[2].getName()).getDisplay().size());

		assertEquals(new DisplayCard(2, red), g.getPlayer(players[0].getName()).getDisplay().get(0));
		assertEquals(new DisplayCard(1, red), g.getPlayer(players[2].getName()).getDisplay().get(0));
	}

	@Test
	public void TestDropWeapon() {
		System.out.println("\nTest: Drop Weapon Action");
		g.startTournament(new Tournament(red));
		g.execute(new ActionCard("Drop Weapon"));
		assertEquals(green, g.getTournament().getColour());
		g.endTournament();
		
		g.startTournament(new Tournament(blue));
		g.execute(new ActionCard("Drop Weapon"));
		assertEquals(green, g.getTournament().getColour());
		g.endTournament();
		
		g.startTournament(new Tournament(yellow));
		g.execute(new ActionCard("Drop Weapon"));
		assertEquals(green, g.getTournament().getColour());
		g.endTournament();
	}
}
