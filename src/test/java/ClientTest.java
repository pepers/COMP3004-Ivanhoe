package test.java;

import static org.junit.Assert.*;
import org.junit.*;

import main.java.Client;
import main.java.Server;
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
		Trace.getInstance().test(this, "@Test(): connect");
		
		
		// attempt to connect (should fail if Server is not reachable)
		assertTrue(c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
	}
	
	@Test
	public void shutdown() {
		Trace.getInstance().test(this, "@Test(): shutdown");
		assertTrue(c.shutdown());
	}
}
