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
		c1.send(p1);

		c2 = new Client();
		String[] arr2 = { "TEST PLAYER 2" };
		Player p2 = new Player(String.join(" ", arr2));
		c2.initialize(p2, g);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.send(p2);

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
	
	@After
	public void after() {
		s.shutdown();
	}
}
