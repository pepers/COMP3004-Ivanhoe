package test.java;

import static org.junit.Assert.*;
import org.junit.*;

import main.java.*;
import main.resources.Config;
import main.resources.Trace;

public class ClientTest {

	Server s = new Server(Config.DEFAULT_PORT);
	Client c = null;
	
	@Before
	public void setUp() {
		s.startup();
		c = new Client();
	}
	
	@After
	public void tearDown () {
		s.shutdown();
		c = null;
	}
	
	@Test
	public void connect() {
		Trace.getInstance().test(this, "@Test(): connect to Server");
		
		// attempt to connect (should fail if Server is not reachable)
		assertTrue(c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
	}
	
	@Test
	public void process() {
		Trace.getInstance().test(this, "@Test(): process received objects");
		Object o;
		
		// Player
		o = new Player("TEST PLAYER");
		assertTrue(c.process(o));
		
		// GameState
		o = new GameState();
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
		assertTrue(c.shutdown());
	}
}
