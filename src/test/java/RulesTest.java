package test.java;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.Client;
import main.java.Colour;
import main.java.DisplayCard;
import main.java.GameState;
import main.java.Player;
import main.java.Server;
import main.java.Token;
import main.java.Tournament;
import main.resources.Config;

public class RulesTest {
	Server s;
	Client c1, c2;

	@Before
	public void before() {
		s = new Server(Config.DEFAULT_PORT);
		s.startup();
		GameState g = new GameState();

		c1 = new Client();
		String[] arr = { "TEST PLAYER 1" };
		Player p1 = new Player(String.join(" ", arr));
		c1.initialize(p1, g);
		c1.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c1.cmdSetname(arr);

		c2 = new Client();
		String[] arr2 = { "TEST PLAYER 2" };
		Player p2 = new Player(String.join(" ", arr2));
		c2.initialize(p2, g);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.cmdSetname(arr2);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.cmdReady();
		c2.cmdReady();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s.getGameState().setTurnIndex(0);
	}

	@Test
	public void TestEliminated() {
		System.out.println("\nTest: Elimination from tournament.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(1, new Colour("blue")));
		s.getGameState().startTournament(new Tournament(new Colour("blue")));

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.cmdPlay("blue:1");
		c1.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(false, s.getGameState().getPlayer(c2.getPlayer()).getParticipation());
	}

	@Test
	public void TestRewards() {
		System.out.println("\nTest: Token Rewards.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(1, new Colour("blue")));
		s.getGameState().startTournament(new Tournament(new Colour("blue")));

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.cmdPlay("blue:1");
		c1.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).hasToken(new Token(new Colour("blue"), "unknown")));
	}

	@Test
	public void TestRewardsRestrict() {
		System.out.println("\nTest: Rewards with restriction.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(1, new Colour("red")));
		s.getGameState().startTournament(new Tournament(new Colour("red")));

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.getPlayer().giveToken(new Token(new Colour("red"), "unknown"));
		c1.cmdPlay("red:1");
		c1.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).hasToken(new Token(new Colour("red"), "unknown")));
	}

	@Test
	public void TestConsTournaments() {
		System.out.println("\nTest: Multiple tournaments.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(1, new Colour("red")));
		s.getGameState().startTournament(new Tournament(new Colour("red")));

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.cmdPlay("red:1");
		c1.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(true, s.getGameState().startTournament(new Tournament(new Colour("green"))));
	}

	@Test
	public void TestTournamentGreen() {
		System.out.println("\nTest: Test green tournament.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(2, new Colour("green")));
		s.getGameState().startTournament(new Tournament(new Colour("green")));
		assertEquals("green", s.getGameState().getTournament().getColour());

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.cmdPlay("green:2");
		c1.cmdEnd();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));

		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestTournamentPurple() {
		System.out.println("\nTest: Test purple tournament.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(2, new Colour("purple")));
		s.getGameState().startTournament(new Tournament(new Colour("purple")));
		assertEquals("purple", s.getGameState().getTournament().getColour());

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.cmdPlay("purple:2");
		c1.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals ("purple", s.getGameState().getLastColour());
	}
	
	@Test
	public void TestTournamentYellow() {
		System.out.println("\nTest: Test yellow tournament.");
		s.getGameState().addHand(c1.getPlayer(), new DisplayCard(3, new Colour("yellow")));
		s.getGameState().startTournament(new Tournament(new Colour("yellow")));
		assertEquals("yellow", s.getGameState().getTournament().getColour());

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.setGameState(s.getGameState());
		c1.cmdPlay("yellow:3");
		c1.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		c2.setGameState(s.getGameState());
		c2.cmdEnd();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals ("yellow", s.getGameState().getLastColour());
	}
	
	@After
	public void after() {
		s.shutdown();
	}
}
