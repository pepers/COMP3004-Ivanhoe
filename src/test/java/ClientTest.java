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
	
	@BeforeClass
	public static void before() {
		Server s = new Server(Config.DEFAULT_PORT);
		s.startup();
	}
	
	@Before
	public void setUp() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("ClientTest can't wait.");
		}
		
		c = new Client();
		c2 = new Client(); // for connect() test
		c3 = new Client(); // for shutdown() test
		
		String[] arr = {"TEST PLAYER"};
		Player p = new Player(arr[0], 0);
		GameState g = new GameState();
		c.initialize(p, g);
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(arr);
		
		// for shutdown() test
		String[] arr3 = {"SHUTDOWN TEST PLAYER"};
		Player p3 = new Player(arr3[0], 0);
		GameState g3 = new GameState();
		c3.initialize(p3, g3);
		c3.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c3.cmdSetname(arr);
	}
	
	@After
	public void tearDown () {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("ClientTest can't wait.");
		}
		
		c.shutdown();
	}
	
	@Test
	public void connect() {
		System.out.println("\n@Test(): connect()");
		Trace.getInstance().test(this, "@Test(): connect to Server");
		
		String[] arr = {"CONNECT TEST PLAYER"};
		Player p = new Player(arr[0], 0);
		GameState g = new GameState();
				
		c2.initialize(p, g);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c2.cmdSetname(arr);

		// attempt to connect (should fail if Server is not reachable)
		assertTrue(c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
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
		GameState g = new GameState();
		Player p = new Player("GAMESTATE TEST PLAYER");
		g.addPlayer(p);
		o = g;
		assertTrue(c.process(o));
		
		// ActionCard
		o = new ActionCard("TEST ACTION CARD");
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
		assertTrue(c.send(o));
	}
	
	@Test
	public void shutdown() {
		System.out.println("\n@Test(): shutdown()");
		Trace.getInstance().test(this, "@Test(): shutdown Client");
		String[] arr = {"TEST PLAYER"};
		Player p = new Player(arr[0], 0);
		GameState g = new GameState();
		c.initialize(p, g);
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(arr);
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		
		assertTrue(c2.shutdown());
	}
	
	@Test
	public void cmdDisplay() {
		System.out.println("\n@Test(): cmdDisplay()");
		Trace.getInstance().test(this, "@Test(): /display [player name ('-a' for all, or leave empty for own display)]");
		assertTrue(c.processCmd("/display"));
		assertTrue(c.processCmd("/display -a"));
		assertTrue(c.processCmd("/display"));
	}
	
	@Test
	public void cmdEnd() {
		System.out.println("\n@Test(): cmdEnd()");
		Trace.getInstance().test(this, "@Test(): /end"); // end turn
		assertFalse(c.processCmd("/end arguments not allowed"));
		assertTrue(c.processCmd("/end"));
	}
	 
	@Test
	public void cmdHand() {
		System.out.println("\n@Test(): cmdHand()");
		Trace.getInstance().test(this, "@Test(): /hand"); 
	}
	 
	@Test
	public void cmdHelp() {
		System.out.println("\n@Test(): cmdHelp()");
		Trace.getInstance().test(this, "@Test(): /help"); 
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
	}
	 
	@Test
	public void cmdShutdown() {
		System.out.println("\n@Test(): cmdShutdown()");
		Trace.getInstance().test(this, "@Test(): /shutdown"); 
	}
	 
	@Test
	public void cmdTranslate() {
		System.out.println("\n@Test(): cmdTranslate()");
		Trace.getInstance().test(this, "@Test(): /translate [dialect ('none', 'oldEnglish')] [-c (optional censor)]"); 
	}
	 
	@Test
	public void cmdTournament() {
		System.out.println("\n@Test(): cmdTournament()");
		Trace.getInstance().test(this, "@Test(): /tournament"); 
	}
	 
	@Test
	public void cmdWithdraw() {
		System.out.println("\n@Test(): cmdWithdraw()");
		Trace.getInstance().test(this, "@Test(): /withdraw"); 
	}
}
