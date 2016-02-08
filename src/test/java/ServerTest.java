package test.java;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.Client;
import main.java.Server;
import main.resources.Config;

public class ServerTest {
	
	Server s = new Server(Config.DEFAULT_PORT);
	
	@Before
	public void before(){
		s.startup();
	}
	
	//@Test
	public void TestInit(){
		
	}
	
	@Test
	public void TestClientAdded(){
		Client c = new Client();
		c.connect("localhost", Config.DEFAULT_PORT);
		assertEquals(s.getConnected(), 0);
	}
	
	@After
	public void after(){
		s.shutdown();
	}
}
