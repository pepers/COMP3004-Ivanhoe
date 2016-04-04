package test.java;

import static org.junit.Assert.*;
import org.junit.*;

import main.java.*;
import main.resources.Config;
import main.resources.Trace;

public class ClientTest {

	Server s = null;
	Client c = null;
	Client c2 = null; // for connect() test
	Client c3 = null; // for shutdown() test
	Action action;
	static int pnum;
	Player p;
	GameState g;
	Colour none = new Colour(Colour.c.NONE);
	Colour blue = new Colour(Colour.c.BLUE);
	Colour green = new Colour(Colour.c.GREEN);
	Colour purple = new Colour(Colour.c.PURPLE);
	Colour red = new Colour(Colour.c.RED);
	Colour yellow = new Colour(Colour.c.YELLOW);
	
	@BeforeClass
	public static void before() {
		Server s = new Server(Config.DEFAULT_PORT);
		s.startup();
		pnum = 0;
	}
	
	@Before
	public void TestSetup() {
		c = new Client();
		c.setGui(false); // start in CLI mode
		pnum += 1;
		String[] arr = {"TEST PLAYER " + pnum};
		p = new Player(String.join(" ", arr));
		g = new GameState();
		c.initialize(p, g);
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.send(p);
	}
	
	@After
	public void TestTearDown () {
		System.out.println(c.getPlayer().getName() + " is leaving...");
		c.shutdown();
	}
	
	@Test
	public void TestConnect() {
		System.out.println("\n@Test(): connect()");
		Trace.getInstance().test(this, "@Test(): connect to Server");
		
		c2 = new Client(); // for connect() test
		c2.setGui(false); // start in CLI mode

		String[] arr2 = {"CONNECT TEST PLAYER"};
		Player p2 = new Player(String.join(" ", arr2));
		GameState g2 = new GameState();
				
		c2.initialize(p2, g2);

		// attempt to connect (should fail if Server is not reachable)
		assertTrue(c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
		
		c2.send(p2);

		System.out.println(c2.getPlayer().getName() + " is leaving...");
		c2.shutdown();
	}
	
	@Test
	public void TestProcess() {
		System.out.println("\n@Test(): process()");
		Trace.getInstance().test(this, "@Test(): process received objects");
		Object o;
		
		// Player
		o = new Player("NEW PLAYER TEST PLAYER");
		assertTrue(c.process(o));
		
		// GameState
		g.addPlayer((Player) o);  // add player from previous player assert
		o = g;
		assertTrue(c.process(o));
		
		// ActionCard
		o = new ActionCard("Ivanhoe");
		assertTrue(c.process(o));
		
		// DisplayCard
		o = new DisplayCard(3, purple);
		assertTrue(c.process(o));
		
		// unrecognized object
		o = null;
		assertFalse(c.process(o));

	}
	
	@Test
	public void TestSend() {
		System.out.println("\n@Test(): send()");
		Trace.getInstance().test(this, "@Test(): send to Server");
		Object o = new String("TEST PLAYER");
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		assertTrue(c.send(o));
	}
	
	@Test
	public void TestShutdown() {
		System.out.println("\n@Test(): shutdown()");
		Trace.getInstance().test(this, "@Test(): shutdown Client");

		// for shutdown() test
		c3 = new Client(); 
		c3.setGui(false); // start in CLI mode
		
		String[] arr3 = {"SHUTDOWN TEST PLAYER"};
		Player p3 = new Player(String.join(" ", arr3));
		GameState g3 = new GameState();
		c3.initialize(p3, g3);
		c3.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c3.send(p3);
		assertTrue(c3.shutdown());
	}
	
	@Test
	public void TestProcessCmd() {
		System.out.println("\n@Test(): processCmd()");
		Trace.getInstance().test(this, "@Test(): processCmd() - # arguments"); 
		assertFalse(c.processCmd("/censor too many arguments")); // too many arguments
		assertFalse(c.processCmd("/end arguments not allowed")); // too many arguments
		assertFalse(c.processCmd("/hand too many arguments")); // too many arguments
		assertFalse(c.processCmd("/help too many arguments")); // too many arguments
		assertFalse(c.processCmd("/list too many arguments")); // too many arguments
		assertFalse(c.processCmd("/play")); // too few arguments
		assertFalse(c.processCmd("/ready too many arguments")); // too many arguments
		assertFalse(c.processCmd("/setname")); // too few arguments
		assertFalse(c.processCmd("/shutdown too many arguments")); // too many arguments
		assertFalse(c.processCmd("/tokens too many arguments")); // too many arguments
		assertFalse(c.processCmd("/tournament too many arguments")); // too many arguments
		assertFalse(c.processCmd("/tournament")); // too few arguments
		assertFalse(c.processCmd("/translate too many arguments")); // too many arguments
		assertFalse(c.processCmd("/translate")); // too few arguments
		assertFalse(c.processCmd("/withdraw too many arguments")); // too many arguments
	}
	
	@Test
	public void TestCensor() {
		System.out.println("\n@Test(): cmdCensor()");
		Trace.getInstance().test(this, "@Test(): /censor"); 
		
		assertTrue(c.cmdCensor()); // censor
		assertTrue(c.cmdCensor()); // stop censoring
	}
	
	@Test
	public void TestDisplay() {
		System.out.println("\n@Test(): cmdDisplay()");
		Trace.getInstance().test(this, "@Test(): /display [player name ('-a' for all, or leave empty for own display)]");
		
		String pname = p.getName();
		Tournament t = new Tournament(new Colour(Colour.c.RED));
		c.getGameState().startTournament(t);
		if (!p.getParticipation()) {
			p.toggleTnmt(); // adds them to tournament
		}
		String[] arr = pname.split("\\s+"); 
		assertTrue(c.cmdDisplay(arr)); // pname's display
		
		String all = "-a";
		arr = all.split("\\s+");
		Player otherPlayer = new Player("Other Player");
		DisplayCard card1 = new DisplayCard(3, red);
		DisplayCard card2 = new DisplayCard(6, none);
		otherPlayer.getDisplay().add(card1);
		otherPlayer.getDisplay().add(card2);
		g.addPlayer(otherPlayer);
		if (!otherPlayer.getParticipation()) {
			otherPlayer.toggleTnmt(); // adds them to tournament
		}
		assertTrue(c.cmdDisplay(arr)); // all players' displays (p = empty display, otherPlayer = 2 cards)
		
		arr = new String[0]; 
		assertTrue(c.cmdDisplay(arr)); // this player's display display
		
		String name = "Non-Existent Player";
		arr = name.split("\\s+"); 
		assertFalse(c.cmdDisplay(arr)); // player doesn't exist
	}
	
	@Test
	public void TestEnd() {
		System.out.println("\n@Test(): cmdEnd()");
		Trace.getInstance().test(this, "@Test(): /end"); // end turn
		
		assertFalse(c.cmdEnd()); // not your turn
		
		c.getPlayer().setTurn(true);
		assertTrue(c.cmdEnd()); // is your turn
		
		Card card = new DisplayCard(3, purple);
		p.addToHand(card);
		c.getPlayer().setTurn(true);
		assertFalse(c.cmdEnd()); // not in tournament, and have card to start tournament with
	}
	 
	@Test
	public void TestHand() {
		System.out.println("\n@Test(): cmdHand()");
		Trace.getInstance().test(this, "@Test(): /hand"); 
		
		assertTrue(c.cmdHand());  // no cards in hand, let user know
		
		Card card1 = new DisplayCard(3, purple);
		Card card2 = new ActionCard("ivanhoe");
		Card card3 = new DisplayCard(2, none);
		p.addToHand(card1);
		p.addToHand(card2);
		p.addToHand(card3);
		assertTrue(c.cmdHand());  // cards in hand
	}
	 
	@Test
	public void TestHelp() {
		System.out.println("\n@Test(): cmdHelp()");
		Trace.getInstance().test(this, "@Test(): /help"); 
		assertTrue(c.cmdHelp());
	}
	 
	@Test
	public void TestList() {
		System.out.println("\n@Test(): cmdList()");
		Trace.getInstance().test(this, "@Test(): /list"); 
		assertTrue(c.cmdList());
	}
	 
	@Test
	public void TestPlay() {
		System.out.println("\n@Test(): cmdPlay()");
		Trace.getInstance().test(this, "@Test(): /play [card name]"); 
		
		assertFalse(c.cmdPlay("not real card")); // not a real card
		
		assertFalse(c.cmdPlay("purple:3")); // real display card, but not in hand
		assertFalse(c.cmdPlay("Dodge"));  // real action card, but not in hand
		
		// add some cards to hand
		DisplayCard card1 = new DisplayCard(3, purple);
		ActionCard card2 = new ActionCard("Drop Weapon");
		ActionCard card3 = new ActionCard("ivanhoe");
		DisplayCard card4 = new DisplayCard(2, none);
		DisplayCard card5 = new DisplayCard(6, none);
		p.addToHand(card1);
		p.addToHand(card2);
		p.addToHand(card3);
		p.addToHand(card4);
		p.addToHand(card5);
		
		assertFalse(c.cmdPlay("purple:3")); // display card in hand, but not my turn
		assertFalse(c.cmdPlay("Drop Weapon"));  // action card in hand, but not my turn
		
		assertTrue(c.cmdPlay("ivanhoe")); // can play Ivanhoe when not your turn
		
		c.getPlayer().setTurn(true);
		assertFalse(c.cmdPlay("Drop Weapon"));  // is turn, but no tournament running
		
		Tournament t = new Tournament(purple);
		g.startTournament(t);
		if (!p.getParticipation()) {
			p.toggleTnmt(); // adds them to tournament
		}
		assertTrue(c.cmdPlay("purple:3")); // is turn, and in tournament, play the card
		assertTrue(c.cmdPlay("squire:2")); // is turn, and in tournament, play the card
		assertTrue(c.cmdPlay("Drop Weapon"));  // is turn, and in tournament, play the card
		
		p.getDisplay().add(card5);
		assertFalse(c.cmdPlay("maiden:6")); // already have maiden in display
	}
	 
	@Test
	public void TestReady() {
		System.out.println("\n@Test(): cmdReady()");
		Trace.getInstance().test(this, "@Test(): /ready"); 
		assertTrue(c.cmdReady());
	}
	 
	@Test
	public void TestSetName() {
		System.out.println("\n@Test(): cmdSetname()");
		Trace.getInstance().test(this, "@Test(): /setname [new name (can't already exist, be empty, or start with '-' or '/')]"); 
		String name = p.getName();
		String[] args = name.split("\\s+"); 
		//TODO: assertFalse(c.cmdSetname(args)); // can't already exist

		args = new String[1];
		args[0] = "";
		assertFalse(c.cmdSetname(args)); // can't be empty
		
		name = "-can't start with -";
		args = name.split("\\s+"); 
		assertFalse(c.cmdSetname(args)); // can't start with '-'
		
		name = "/can't start with /";
		args = name.split("\\s+"); 
		assertFalse(c.cmdSetname(args)); // can't start with '/'
	}
	 
	@Test
	public void TestTokens() {
		System.out.println("\n@Test(): cmdTokens()");
		Trace.getInstance().test(this, "@Test(): /tokens");
		
		assertTrue(c.cmdTokens()); // no tokens
		
		p.giveToken(new Token(blue, "the test realm."));
		p.giveToken(new Token(red, "the test realm."));
		assertTrue(c.cmdTokens()); // two tokens
	}
	
	@Test
	public void TestStartTournament() {
		System.out.println("\n@Test(): cmdTournament()");
		Trace.getInstance().test(this, "@Test(): /tournament [tournament colour (purple, red, blue, yellow, or green)] [card name]"); 
		
		String[] args1 = new String[1]; // start with one argument (coloured display card)
		String[] args2 = new String[2]; // start with two arguments (colour, display card)
		
		Tournament t = new Tournament(yellow);
		g.startTournament(t);
		args1[0] = "purple:3";
		args2[0] = "red";
		args2[1] = "red:5";
		assertFalse(c.cmdTournament(args1)); // can't start, tournament already started
		assertFalse(c.cmdTournament(args2));
		
		g.endTournament();
		assertFalse(c.cmdTournament(args1)); // can't start, not your turn 
		assertFalse(c.cmdTournament(args2));
		
		c.getPlayer().setTurn(true);
		assertFalse(c.cmdTournament(args1)); // can't start, don't have card in hand
		assertFalse(c.cmdTournament(args2));
		
		Card card1 = new DisplayCard(3, purple);
		Card card2 = new DisplayCard(5, red);
		p.addToHand(card1);
		p.addToHand(card2);
		assertTrue(c.cmdTournament(args1)); // can start tournament
		
		t = new Tournament(purple);
		g.startTournament(t);
		g.endTournament();
		assertFalse(c.cmdTournament(args1)); // can't start purple tournament when last tournament was purple
		
		args1[0] = "maiden:6";
		args2[0] = "blue";
		args2[1] = "squire:2";
		Card card3 = new DisplayCard(6, none);
		Card card4 = new DisplayCard(2, none);
		p.addToHand(card3);
		p.addToHand(card4);
		assertFalse(c.cmdTournament(args1)); // can't start, colour not specified with colourless card
		assertTrue(c.cmdTournament(args2)); // can start tournament, colour specified with colourless card
	}
	 
	@Test
	public void TestTranslate() {
		System.out.println("\n@Test(): cmdTranslate()");
		Trace.getInstance().test(this, "@Test(): /translate [dialect ('none', 'oldEnglish')]"); 
		
		assertFalse(c.cmdTranslate("non-existant language")); // can't translate 
		
		assertTrue(c.cmdTranslate("none")); // stop translating
		
		assertTrue(c.cmdTranslate("oldEnglish")); // translate to old english
	}
	
	@Test
	public void TestWithdraw() {
		System.out.println("\n@Test(): cmdWithdraw()");
		Trace.getInstance().test(this, "@Test(): /withdraw"); 
		c.getPlayer().setTurn(true);
		Tournament t = new Tournament(purple);
		g.startTournament(t);
		assertTrue(c.cmdWithdraw());
	}
}
