package test.java;

import org.junit.*;

import main.java.Client;
import main.resources.Config;

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
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
	}

}
