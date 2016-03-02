package test.java;

import static org.junit.Assert.*;
import org.junit.*;

import main.java.*;
import main.resources.Config;
import main.resources.Trace;

public class ClientTest {

	Server s = new Server(Config.DEFAULT_PORT);
	Client c = null;
	Client c2 = new Client(); // for connect and shutdown tests
	
	@Before
	public void setUp() {
		s.startup();
		c = new Client();
		Player p = new Player("TEST PLAYER", 0);
		GameState g = new GameState();
		c.initialize(p, g);
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
	}
	
	@After
	public void tearDown () {
		c.shutdown();
		s.shutdown();
	}
	
	@Test
	public void connect() {
		Trace.getInstance().test(this, "@Test(): connect to Server");
				
		// attempt to connect (should fail if Server is not reachable)
		assertTrue(c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
	}
	
	@Test
	public void process() {
		Trace.getInstance().test(this, "@Test(): process received objects");
		Object o;
		
		// Player
		o = new Player("TEST PLAYER");
		assertTrue(c.process(o));
		
		// GameState
		GameState g = new GameState();
		Player p = new Player("TEST PLAYER");
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
		Trace.getInstance().test(this, "@Test(): send to Server");
		Object o = new Player("TEST PLAYER");
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		assertTrue(c.send(o));
	}
	
	@Test
	public void shutdown() {
		Trace.getInstance().test(this, "@Test(): shutdown Client");
		c2.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		assertTrue(c2.shutdown());
	}
	
	@Test
	public void cmdDisplay() {
		Trace.getInstance().test(this, "@Test(): /display [player name ('-a' for all, or leave empty for own display)]");
		assertTrue(c.processCmd("/display"));
		assertTrue(c.processCmd("/display -a"));
		assertTrue(c.processCmd("/display"));
	}
	
	@Test
	public void cmdEnd() {
		Trace.getInstance().test(this, "@Test(): /end"); // end turn
		assertFalse(c.processCmd("/end arguments not allowed"));
		assertTrue(c.processCmd("/end"));
	}
	 
	@Test
	public void cmdHand() {
		Trace.getInstance().test(this, "@Test(): /hand"); 
	}
	 
	@Test
	public void cmdHelp() {
		Trace.getInstance().test(this, "@Test(): /help"); 
	}
	 
	@Test
	public void cmdList() {
		Trace.getInstance().test(this, "@Test(): /list"); 
	}
	 
	@Test
	public void cmdPlay() {
		Trace.getInstance().test(this, "@Test(): /play [card name]"); 
	}
	 
	@Test
	public void cmdReady() {
		Trace.getInstance().test(this, "@Test(): /ready"); 
	}
	 
	@Test
	public void cmdSetname() {
		Trace.getInstance().test(this, "@Test(): /setname [new name (can't already exist, be empty, or start with '-' or '/')]"); 
	}
	 
	@Test
	public void cmdShutdown() {
		Trace.getInstance().test(this, "@Test(): /shutdown"); 
	}
	 
	@Test
	public void cmdTranslate() {
		Trace.getInstance().test(this, "@Test(): /translate [dialect ('none', 'oldEnglish')] [-c (optional censor)]"); 
	}
	 
	@Test
	public void cmdTournament() {
		Trace.getInstance().test(this, "@Test(): /tournament"); 
	}
	 
	@Test
	public void cmdWithdraw() {
		Trace.getInstance().test(this, "@Test(): /withdraw"); 
	}
}
