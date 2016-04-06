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
		s.noDrawing = true;
		GameState g = new GameState();

		c1 = new Client();
		c1.setGui(false);
		c1.testMode = true;
		c1.initialize(p1, g);
		c1.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c1.send(p1);
		
		c2 = new Client();
		c2.setGui(false);
		c1.testMode = true;
		c2.initialize(p2, g);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.send(p2);
		
		c3 = new Client();
		c3.setGui(false);
		c1.testMode = true;
		c3.initialize(p3, g);
		c3.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c3.send(p3);
		
		c4 = new Client();
		c4.setGui(false);
		c1.testMode = true;
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
	public void TestTournament1() {
		System.out.println("\nTest: Tournament 1 Card.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		
		String[] cards = {"green:1", "blue:3", "yellow:3", "red:5"};
		for(String card : cards){
			s.getGameState().setTurn(c1.getPlayer());
			s.cmdGive(0, card);
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			c1.cmdTournament(card.split(" "));
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			
			assertEquals(new Colour(card.split(":")[0]), s.getGameState().getTournament().getColour());
			assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
			if(cards.equals("green:1")){
				assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(card.split(":")[0])));
			}else{
				assertEquals(new DisplayCard(card).getValue(), s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(card.split(":")[0])));
			}
			
			
			s.getGameState().setTurn(c2.getPlayer());
			s.cmdGive(1, card);
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			c2.cmdPlay(card);
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
			c2.cmdWithdraw();
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			s.getGameState().setTurn(c3.getPlayer());
			c3.cmdWithdraw();
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			s.getGameState().setTurn(c4.getPlayer());
			c4.cmdWithdraw();
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			if(c1.getPlayer().getTokens().size() > 0)c1.getPlayer().getTokens().remove(0);
		}
	}
	
	@Test
	public void TestTournamentM1() {
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
	public void TestTournamentSquire() {
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
	public void TestTournamentMaiden() {
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
		assertEquals(0, s.getGameState().getPlayer(c3.getPlayer()).getNumTokens());
	}
	
	@Test
	public void TestTournamentSupporters() {
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
	public void TestTournamentMultiRound() {
		System.out.println("\nTest: Multiple Rounds.\n******************************\n");
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
		
		s.getGameState().setTurn(c2.getPlayer());
		c2.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c4.getPlayer());
		c4.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().size());
		assertEquals(2, s.getGameState().getPlayer(c3.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		assertEquals(2, s.getGameState().getTournamentParticipants().size());
		
		s.getGameState().setTurn(c1.getPlayer());
		c1.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}		
		
		s.getGameState().setTurn(c4.getPlayer());
		s.cmdGive(3, "red:5");
		s.cmdGive(3, "red:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdTournament("red:5".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdPlay("red:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "red:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("red:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c1.getPlayer());
		s.cmdGive(0, "red:4");
		s.cmdGive(0, "none:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdPlay("red:4");
		c1.cmdPlay("maiden:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c3.getPlayer());
		c3.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c4.getPlayer());
		s.cmdGive(3, "red:5");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdPlay("red:5");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c1.getPlayer());
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "blue:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdTournament("blue:3".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c3.getPlayer());
		s.cmdGive(2, "blue:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdPlay("blue:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c3.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c4.getPlayer());
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c4.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c1.getPlayer());
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "blue:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdPlay("blue:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		
		assertEquals(new Colour(Colour.c.BLUE), s.getGameState().getPlayer(c3.getPlayer()).getTokens().get(1).getColour());
		assertEquals(new Colour(Colour.c.RED), s.getGameState().getPlayer(c4.getPlayer()).getTokens().get(0).getColour());
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getPlayer(c3.getPlayer()).getTokens().get(0).getColour());
	}
	
	@Test
	public void TestTournamentA1() {
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
	public void TestTournamentX() {
		System.out.println("\nTest: Tournament 1 Card.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		
		String[] cards = {"green:1", "blue:3", "yellow:3", "red:5"};
		for(String card : cards){
			s.getGameState().setTurn(c1.getPlayer());
			s.cmdGive(0, card);
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			c1.cmdTournament(card.split(" "));
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			
			assertEquals(new Colour(card.split(":")[0]), s.getGameState().getTournament().getColour());
			assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
			if(cards.equals("green:1")){
				assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(card.split(":")[0])));
			}else{
				assertEquals(new DisplayCard(card).getValue(), s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(card.split(":")[0])));
			}
			
			s.getGameState().setTurn(c2.getPlayer());
			s.cmdGive(1, card);
			s.cmdGive(1, card);
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			c2.cmdPlay(card);
			c2.cmdPlay(card);
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			assertEquals(2, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
			c2.cmdWithdraw();
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			s.getGameState().setTurn(c3.getPlayer());
			c3.cmdWithdraw();
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
			s.getGameState().setTurn(c4.getPlayer());
			c4.cmdWithdraw();
			try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}	
			if(c1.getPlayer().getTokens().size() > 0)c1.getPlayer().getTokens().remove(0);
		}
	}
	
	@Test
	public void TestTournamentMX() {
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
	public void TestTournamentAX() {
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
	public void TestTournamentWithdraw() {
		System.out.println("\nTest: Tournament withdraws.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		
		
		//Green
		s.cmdGive(0, "green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.GREEN), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.GREEN)));
		c1.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c2.getPlayer());
		c2.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c3.getPlayer());
		c3.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c4.getPlayer());
		c4.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}		
		assertEquals(null, s.getGameState().getTournament());
		
		//Blue
		s.cmdGive(0, "blue:2");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdTournament("blue:2".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.BLUE), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(2, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.BLUE)));
		c1.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c2.getPlayer());
		c2.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c3.getPlayer());
		c3.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c4.getPlayer());
		c4.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}		
		assertEquals(null, s.getGameState().getTournament());
		
		//Red
		s.cmdGive(0, "red:6");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdTournament("red:6".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.RED), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(6, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().score(new Colour(Colour.c.RED)));
		c1.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c2.getPlayer());
		c2.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c3.getPlayer());
		c3.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c4.getPlayer());
		c4.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}		
		assertEquals(null, s.getGameState().getTournament());
		
		//Yellow
		s.getGameState().setTurn(c2.getPlayer());
		s.cmdGive(1, "yellow:3");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c2.cmdTournament("yellow:3".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(new Colour(Colour.c.YELLOW), s.getGameState().getTournament().getColour());
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
		assertEquals(3, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().score(new Colour(Colour.c.YELLOW)));
		c2.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c1.getPlayer());
		c1.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c3.getPlayer());
		c3.cmdWithdraw();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c4.getPlayer());
		c4.cmdWithdraw();
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(null, s.getGameState().getTournament());
		
		
	}
	@Test
	public void TestStunned() {
		System.out.println("\nTest: Stunned.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());

		s.getGameState().setTurn(c1.getPlayer());
		c1.getPlayer().setStunned(true);
		
		s.cmdGive(0, "red:4");
		s.cmdGive(0, "red:4");
		s.cmdGive(0, "red:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdPlay("red:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(1, c1.getPlayer().getDisplay().size());
		c1.cmdPlay("red:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(1, c1.getPlayer().getDisplay().size());
	}
	
	@Test
	public void TestPurpleTournaments() {
		System.out.println("\nTest: Tournament 1 Card.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		
		s.getGameState().startTournament(new Tournament(new Colour(Colour.c.PURPLE)));
		assertEquals(new Colour(Colour.c.PURPLE), s.getGameState().getTournament().getColour());
		s.getGameState().endTournament();
		s.getGameState().startTournament(new Tournament(new Colour(Colour.c.PURPLE)));
		assertEquals(null, s.getGameState().getTournament());
	}
	
	@Test
	public void TestInvalidDisplayColour() {
		System.out.println("\nTest: Invalid play command.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());

		s.getGameState().setTurn(c1.getPlayer());
		s.cmdGive(0, "red:4");
		s.cmdGive(0, "blue:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdTournament("red:4".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(1, c1.getPlayer().getDisplay().size());
		c1.cmdPlay("blue:4");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(1, c1.getPlayer().getDisplay().size());
	}

	@Test
	public void TestNoDisplayCards() {
		System.out.println("\nTest: No Display cards.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		
		s.getGameState().setTurn(c1.getPlayer());
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(0, s.getGameState().getPlayer(c1.getPlayer()).getHandSize());
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdEnd();
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
	}
	
	@Test
	public void TestChargeGreen() {
		System.out.println("\nTest: Green Tournament gets Charged.\n******************************\n");
		c1.setGameState(s.getGameState());
		c2.setGameState(s.getGameState());
		c3.setGameState(s.getGameState());
		c4.setGameState(s.getGameState());
		
		s.getGameState().setTurn(c1.getPlayer());
		s.cmdGive(0, "green:1");
		s.cmdGive(0, "green:1");
		s.cmdGive(0, "green:1");
		s.cmdGive(1, "green:1");
		s.cmdGive(1, "green:1");
		s.cmdGive(1, "charge");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdTournament("green:1".split(" "));
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		c1.cmdPlay("green:1");
		c1.cmdPlay("green:1");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}	
		s.getGameState().setTurn(c2.getPlayer());
		c2.cmdPlay("green:1");
		c2.cmdPlay("green:1");
		c2.cmdPlay("charge");
		try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}	
		assertEquals(1, s.getGameState().getPlayer(c1.getPlayer()).getDisplay().size());
		assertEquals(1, s.getGameState().getPlayer(c2.getPlayer()).getDisplay().size());
	}
	
}
