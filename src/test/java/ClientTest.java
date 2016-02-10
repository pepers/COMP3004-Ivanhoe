package test.java;

import static org.junit.Assert.*;
import org.junit.*;

import main.java.Client;
import main.resources.Config;
import main.resources.Trace;

public class ClientTest {

	Client c = null;
	
	@Before
	public void setUp() {
		c = new Client();
	}
	
	@After
	public void tearDown () {
		c = null;
	}

	@Test
	public void connect() {
		Trace.getInstance().test(this, "@Test(): connect");

		// attempt to connect (should fail if Server is not reachable
		assertTrue(c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT));
	}

}
