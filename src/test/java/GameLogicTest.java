package test.java;

import static org.junit.Assert.assertEquals;

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

public class GameLogicTest {
	Server s;
	Client c1, c2, c3, c4;
	Player p1 = new Player("PLAYER1", 0);
	Player p2 = new Player("PLAYER2", 1);
	Player p3 = new Player("PLAYER3", 2);
	Player p4 = new Player("PLAYER4", 3);
	Client[] clients = {c1, c2, c3, c4};
	
	@Before
	public void before() {
		s = new Server(Config.DEFAULT_PORT);
		s.startup();
		GameState g = new GameState();

		c1 = new Client();
		c1.setGui(false);
		c1.initialize(p1, g);
		c1.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c1.send(p1);
		
		c2 = new Client();
		c2.setGui(false);
		c2.initialize(p2, g);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.send(p2);
		
		c3 = new Client();
		c3.setGui(false);
		c3.initialize(p3, g);
		c3.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c3.send(p3);
		
		c4 = new Client();
		c4.setGui(false);
		c4.initialize(p4, g);
		c4.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c4.send(p4);
		
		c1.cmdReady();
		c2.cmdReady();
		c3.cmdReady();
		c4.cmdReady();

		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(p1);
	}
	
	@After
	public void after() {
		s.shutdown();
	}
	
	@Test
	public void TestTournament1Green() {
		System.out.println("\nTest: Green tournament 1 Card.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentM1Green() {
		System.out.println("\nTest: Green tournament multiple 1 Card.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(1, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentSquireGreen() {
		System.out.println("\nTest: Green tournament 1 Squire.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "none:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		c3.cmdPlay("squire:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentMaidenGreen() {
		System.out.println("\nTest: Green tournament 1 Maiden.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		c1.getPlayer().giveToken(new Token(new Colour("blue"), "origin"));
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "none:6");
		s.cmdGive(2, "none:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		c3.cmdPlay("maiden:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("maiden:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		c3.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
	}
	
	@Test
	public void TestTournamentSupportersGreen() {
		System.out.println("\nTest: Green tournament 1 Maiden.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "none:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		c3.cmdPlay("maiden:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentA1Green() {
		System.out.println("\nTest: Green tournament all 1 Card.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c4.getPlayer());
		s.cmdGive(3, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdPlay("green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(1, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(1, s.getGameState().getPlayer(c4.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c4.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentXGreen() {
		System.out.println("\nTest: Green tournament X Card.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "green:2");
		s.cmdGive(1, "green:2");
		s.cmdGive(1, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("green:2");
		c2.cmdPlay("green:2");
		c2.cmdPlay("green:2");
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentMXGreen() {
		System.out.println("\nTest: Green tournament multiple X Card.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "green:2");
		s.cmdGive(1, "green:2");
		s.cmdGive(1, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("green:2");
		c2.cmdPlay("green:2");
		c2.cmdPlay("green:2");
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		c3.cmdPlay("green:2");
		c3.cmdPlay("green:2");
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(3, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentAXGreen() {
		System.out.println("\nTest: Green tournament multiple X Card.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "green:2");
		s.cmdGive(1, "green:2");
		s.cmdGive(1, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("green:2");
		c2.cmdPlay("green:2");
		c2.cmdPlay("green:2");
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "green:2");
		s.cmdGive(2, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("green:2");
		c3.cmdPlay("green:2");
		c3.cmdPlay("green:2");
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c4.getPlayer());
		s.cmdGive(3, "green:2");
		s.cmdGive(3, "green:2");
		s.cmdGive(3, "green:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdPlay("green:2");
		c4.cmdPlay("green:2");
		c4.cmdPlay("green:2");
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(3, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(3, s.getGameState().getPlayer(c4.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c4.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		
		assertEquals(4, s.getGameState().getTournamentParticipants().size());
	}
	
	@Test
	public void TestTournamentWithdrawGreen() {
		System.out.println("\nTest: Green tournament withdraws.\n******************************\n");
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		c1.cmdEnd();
		
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		while(s.getGameState().getTournamentParticipants().size()>0){
			c1.cmdWithdraw();
			c2.cmdWithdraw();
			c3.cmdWithdraw();
			c4.cmdWithdraw();
		}
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}		
		
		assertEquals(null, s.getGameState().getTournament());
		assertEquals(1, c1.getPlayer().getTokens().size());
	}
}
