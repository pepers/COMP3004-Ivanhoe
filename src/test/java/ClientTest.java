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
	
	@BeforeClass
	public static void before() {
		Server s = new Server(Config.DEFAULT_PORT);
		s.startup();
		pnum = 0;
	}
	
	@Before
	public void setUp() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			System.out.println("ClientTest can't wait.");
		}
		
		c = new Client();
		pnum += 1;
		String[] arr = {"TEST PLAYER " + pnum};
		p = new Player(String.join(" ", arr));
		g = new GameState();
		c.initialize(p, g);
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(arr);
		
	}
	
	@After
	public void tearDown () {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			System.out.println("ClientTest can't wait.");
		}
		
		c.shutdown();
	}
	
	@Test
	public void connect() {
		System.out.println("\n@Test(): connect()");
		Trace.getInstance().test(this, "@Test(): connect to Server");
		
		c2 = new Client(); // for connect() test
		
		String[] arr2 = {"CONNECT TEST PLAYER"};
		Player p2 = new Player(String.join(" ", arr2));
		GameState g2 = new GameState();
				
		c2.initialize(p2, g2);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.cmdSetname(arr2);

		// attempt to connect (should fail if Server is not reachable)
		assertTrue(c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
		
		c2.shutdown();
	}
	
	@Test
	public void process() {
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
		o = new DisplayCard(3, DisplayCard.Colour.purple);
		assertTrue(c.process(o));
		
		// unrecognized object
		o = null;
		assertFalse(c.process(o));

	}
	
	@Test
	public void send() {
		System.out.println("\n@Test(): send()");
		Trace.getInstance().test(this, "@Test(): send to Server");
		Object o = new Player("TEST PLAYER");
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		assertFalse(c.send(o)); // not an action
	}
	
	@Test
	public void shutdown() {
		System.out.println("\n@Test(): shutdown()");
		Trace.getInstance().test(this, "@Test(): shutdown Client");

		// for shutdown() test
		c3 = new Client(); 
		
		String[] arr3 = {"SHUTDOWN TEST PLAYER"};
		Player p3 = new Player(String.join(" ", arr3));
		GameState g3 = new GameState();
		c3.initialize(p3, g3);
		c3.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c3.cmdSetname(arr3);
		assertTrue(c3.shutdown());
	}
	
	@Test
	public void processCmd() {
		System.out.println("\n@Test(): processCmd()");
		Trace.getInstance().test(this, "@Test(): processCmd() - # arguments"); 
		assertFalse(c.processCmd("/censor too many arguments")); // too many arguments
		assertFalse(c.processCmd("/end arguments not allowed")); // too many arguments
		assertFalse(c.processCmd("/hand too many arguments")); // too many arguments
		assertFalse(c.processCmd("/help too many arguments")); // too many arguments
		assertFalse(c.processCmd("/list too many arguments")); // too many arguments
		assertFalse(c.processCmd("/play too many arguments")); // too many arguments
		assertFalse(c.processCmd("/ready too many arguments")); // too many arguments
		assertFalse(c.processCmd("/setname")); // too few arguments
		assertFalse(c.processCmd("/shutdown too many arguments")); // too many arguments
		assertFalse(c.processCmd("/tokens too many arguments")); // too many arguments
		assertFalse(c.processCmd("/tournament too many arguments")); // too many arguments
		assertFalse(c.processCmd("/tournament few")); // too few arguments
		assertFalse(c.processCmd("/translate too many arguments")); // too many arguments
		assertFalse(c.processCmd("/translate")); // too few arguments
		assertFalse(c.processCmd("/withdraw too many arguments")); // too many arguments
	}
	
	@Test
	public void cmdCensor() {
		System.out.println("\n@Test(): cmdCensor()");
		Trace.getInstance().test(this, "@Test(): /censor"); 
		
		assertTrue(c.cmdCensor()); // censor
		assertTrue(c.cmdCensor()); // stop censoring
	}
	
	@Test
	public void cmdDisplay() {
		System.out.println("\n@Test(): cmdDisplay()");
		Trace.getInstance().test(this, "@Test(): /display [player name ('-a' for all, or leave empty for own display)]");
		
		String pname = p.getName();
		if (!p.inTournament()) {
			p.toggleTnmt(); // adds them to tournament
		}
		String[] arr = pname.split("\\s+"); 
		assertTrue(c.cmdDisplay(arr)); // pname's display
		
		String all = "-a";
		arr = all.split("\\s+");
		Player otherPlayer = new Player("Other Player");
		Card card1 = new DisplayCard(3, DisplayCard.Colour.red);
		Card card2 = new DisplayCard(6, DisplayCard.Colour.none);
		otherPlayer.addToDisplay(card1);
		otherPlayer.addToDisplay(card2);
		g.addPlayer(otherPlayer);
		if (!otherPlayer.inTournament()) {
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
	public void cmdEnd() {
		System.out.println("\n@Test(): cmdEnd()");
		Trace.getInstance().test(this, "@Test(): /end"); // end turn
		
		assertFalse(c.cmdEnd()); // not your turn
		
		p.setTurn();
		assertTrue(c.cmdEnd()); // is your turn
		
		g.endTournament();
		Card card = new DisplayCard(3, DisplayCard.Colour.purple);
		p.addToHand(card);
		p.setTurn();
		assertFalse(c.cmdEnd()); // not in tournament, and have card to start tournament with
	}
	 
	@Test
	public void cmdHand() {
		System.out.println("\n@Test(): cmdHand()");
		Trace.getInstance().test(this, "@Test(): /hand"); 
		
		assertTrue(c.cmdHand());  // no cards in hand, let user know
		
		Card card1 = new DisplayCard(3, DisplayCard.Colour.purple);
		Card card2 = new ActionCard("ivanhoe");
		Card card3 = new DisplayCard(2, DisplayCard.Colour.none);
		p.addToHand(card1);
		p.addToHand(card2);
		p.addToHand(card3);
		assertTrue(c.cmdHand());  // cards in hand
	}
	 
	@Test
	public void cmdHelp() {
		System.out.println("\n@Test(): cmdHelp()");
		Trace.getInstance().test(this, "@Test(): /help"); 
		assertTrue(c.cmdHelp());
	}
	 
	@Test
	public void cmdList() {
		System.out.println("\n@Test(): cmdList()");
		Trace.getInstance().test(this, "@Test(): /list"); 
	}
	 
	@Test
	public void cmdPlay() {
		System.out.println("\n@Test(): cmdPlay()");
		Trace.getInstance().test(this, "@Test(): /play [card name]"); 
	}
	 
	@Test
	public void cmdReady() {
		System.out.println("\n@Test(): cmdReady()");
		Trace.getInstance().test(this, "@Test(): /ready"); 
	}
	 
	@Test
	public void cmdSetname() {
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
	public void cmdShutdown() {
		System.out.println("\n@Test(): cmdShutdown()");
		Trace.getInstance().test(this, "@Test(): /shutdown"); 
	}
	
	@Test
	public void cmdTokens() {
		System.out.println("\n@Test(): cmdTokens()");
		Trace.getInstance().test(this, "@Test(): /tokens"); 
	}
	
	@Test
	public void cmdTournament() {
		System.out.println("\n@Test(): cmdTournament()");
		Trace.getInstance().test(this, "@Test(): /tournament [tournament colour (purple, red, blue, yellow, or green)] [card name]"); 
	}
	 
	@Test
	public void cmdTranslate() {
		System.out.println("\n@Test(): cmdTranslate()");
		Trace.getInstance().test(this, "@Test(): /translate [dialect ('none', 'oldEnglish')]"); 
	}
	
	@Test
	public void cmdWithdraw() {
		System.out.println("\n@Test(): cmdWithdraw()");
		Trace.getInstance().test(this, "@Test(): /withdraw"); 
	}
}
