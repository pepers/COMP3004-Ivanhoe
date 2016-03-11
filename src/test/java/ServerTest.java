package test.java;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.ActionCard;
import main.java.Card;
import main.java.Client;
import main.java.Colour;
import main.java.DisplayCard;
import main.java.GameState;
import main.java.Player;
import main.java.Server;
import main.java.Tournament;
import main.resources.Config;

public class ServerTest {

	Server s;

	@Before
	public void before() {
		s = new Server(Config.DEFAULT_PORT);
		resetBans();
		s.startup();
	}

	@After
	public void after() {
		s.shutdown();
	}

	private void resetBans() {
		try {
			File f = new File("banList.txt");
			BufferedWriter b = new BufferedWriter(new FileWriter(f));
			b.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestDraw() {
		System.out.println("\nTest: Drawing a Card");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		GameState g = new GameState();
		Player p = new Player("Player1");
		g.addPlayer(p);
		Tournament t = new Tournament(new Colour(Colour.c.BLUE));
		g.startTournament(t);
		System.out.println("Deck Size: " + GameState.getDeck().size());
		System.out.println("Discard Size: " + GameState.getDeck().discardSize());
		System.out.println("Display Size: " + p.getDisplay().size());
		for (int i=0; i<200; i++) {
			Card card = g.drawFromDeck();
			if (card instanceof ActionCard) {
				g.execute((ActionCard) card);
			} else if (card instanceof DisplayCard) {
				g.addDisplay(p, (DisplayCard) card); 
			}
			System.out.println("Deck Size: " + GameState.getDeck().size());
			System.out.println("Discard Size: " + GameState.getDeck().discardSize());
			System.out.println("Display Size: " + p.getDisplay().size());
		}
		
	}
	
	@Test
	public void TestClientJoin() {
		System.out.println("\nTest: Joined Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
	}

	@Test
	public void TestKickClient() {
		System.out.println("\nTest: Kicking a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
		s.removeThread("Client");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, s.getConnected());
	}

	@Test
	public void TestRejectClient() {
		System.out.println("\nTest: Rejecting a Client");
		for (int i = 0; i < 6; i++) {
			Client c = new Client();
			c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
			c.cmdSetname(new String[] { "Client" + i });
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(5, s.getConnected());
	}

	@Test
	public void TestBanClient() {
		System.out.println("\nTest: Banning a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
		// ban the player
		s.ban("127.0.0.1");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, s.getConnected());
	}

	@Test
	public void TestPardonClient() {
		resetBans();
		System.out.println("\nTest: Pardoning a Client");
		
		s.ban("127.0.0.1");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		// c.cmdSetname(new String[]{"Client"});
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, s.getConnected());

		s.unban("127.0.0.1");
		c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
		resetBans();
	}
	
	@Test
	public void TestAutoStart(){
		System.out.println("\nTest: Game Auto-start");
		
		assert(s.getGameState() == null);
		
		Client c1 = new Client();
		c1.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c1.cmdSetname(new String[] { "Client1" });
		
		Client c2 = new Client();
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.cmdSetname(new String[] { "Client2" });
		
		assert(s.getGameState() != null);
	}
}
