package test.java;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import main.java.ActionCard;
import main.java.DisplayCard;
import main.java.DisplayCard.Colour;
import main.java.GameState;
import main.java.Player;
import main.java.Tournament;

public class GameStateTest {

	GameState g;
	Player[] players = { new Player("P1"), new Player("P2"), new Player("P3"), new Player("P4") };

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
		g.startTournament(new Tournament("blue"));
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
		g.startTournament(new Tournament("red"));
		assert (g.getTournament() != null);
	}

	@Test
	public void TestStartTournamentPurple() {
		System.out.println("\nTest: Starting a tournament, restrictions apply.");
		g.startTournament(new Tournament("purple"));
		assertEquals("purple", g.getTournament().getColour());
		g.endTournament();
		g.startTournament(new Tournament("purple"));
		assert (g.getTournament() == null);
	}

	@Test
	public void TestDisgrace() {
		System.out.println("\nTest: Disgrace Action");
		g.addDisplay(players[0], new DisplayCard(2, DisplayCard.Colour.none));
		g.addDisplay(players[0], new DisplayCard(3, DisplayCard.Colour.none));
		assertEquals(2, g.getPlayer(players[0].getName()).getDisplay().size());
		g.startTournament(new Tournament("red"));

		g.execute(new ActionCard("Disgrace"));
		assertEquals(0, g.getPlayer(players[0].getName()).getDisplay().size());
	}

	@Test
	public void TestOutmaneuver() {
		System.out.println("\nTest: Outmaneuver Action");
		g.addDisplay(players[0], new DisplayCard(2, DisplayCard.Colour.red));
		g.addDisplay(players[0], new DisplayCard(1, DisplayCard.Colour.blue));
		g.addDisplay(players[1], new DisplayCard(1, DisplayCard.Colour.green));
		g.addDisplay(players[2], new DisplayCard(1, DisplayCard.Colour.red));
		g.addDisplay(players[2], new DisplayCard(4, DisplayCard.Colour.red));

		g.startTournament(new Tournament("red"));

		g.execute(new ActionCard("Outmaneuver"));
		assertEquals(1, g.getPlayer(players[0].getName()).getDisplay().size());
		assertEquals(0, g.getPlayer(players[1].getName()).getDisplay().size());
		assertEquals(1, g.getPlayer(players[2].getName()).getDisplay().size());

		assertEquals(new DisplayCard(2, DisplayCard.Colour.red), g.getPlayer(players[0].getName()).getDisplay().get(0));
		assertEquals(new DisplayCard(1, DisplayCard.Colour.red), g.getPlayer(players[2].getName()).getDisplay().get(0));
	}

	@Test
	public void TestDropWeapon() {
		System.out.println("\nTest: Drop Weapon Action");
		g.startTournament(new Tournament("red"));
		g.execute(new ActionCard("Drop Weapon"));
		assertEquals("green", g.getTournamentColour());
		g.endTournament();
		
		g.startTournament(new Tournament("blue"));
		g.execute(new ActionCard("Drop Weapon"));
		assertEquals("green", g.getTournamentColour());
		g.endTournament();
		
		g.startTournament(new Tournament("yellow"));
		g.execute(new ActionCard("Drop Weapon"));
		assertEquals("green", g.getTournamentColour());
		g.endTournament();
	}
	
	@Test
	public void TestDropWeapon() {
		
	}
}
