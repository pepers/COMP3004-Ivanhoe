package test.java;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.Client;
import main.java.Server;
import main.resources.Config;

public class ServerTestKick {
	
	Server s = new Server(Config.DEFAULT_PORT);
	
	@Before
	public void before(){
		s.startup();
	}
	
	@Test
	public void TestKick(){
		Client c = new Client();
		c.connect("localhost", Config.DEFAULT_PORT);
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(s.getConnected(), 1);
		
		
		int id = c.getID();
		s.kick(id);
		assertEquals(s.getConnected(), 0);
		
	}
	
	@After
	public void after(){
		s.shutdown();
	}
}
