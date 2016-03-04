package test.java;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.Client;
import main.java.Server;
import main.resources.Config;

public class ServerTest {
	
	Server s;
	
	@Before
	public void before(){
		s = new Server(Config.DEFAULT_PORT);
		s.startup();
	}
	
	@After
	public void after(){
		s.shutdown();
	}
	
	@Test
	public void TestAddClient(){
		System.out.println("\nTest: Adding a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[]{"Client"});
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(1, s.getConnected());
	}
	
	@Test
	public void TestKickClient(){
		System.out.println("\nTest: Kicking a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[]{"Client"});
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(1, s.getConnected());
		s.removeThread("Client");
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(0, s.getConnected());
	}
	
	@Test
	public void TestRejectClient(){
		System.out.println("\nTest: Rejecting a Client");
		for(int i=0;i<6;i++){
			Client c = new Client();
			c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
			c.cmdSetname(new String[]{"Client" + i});
		}
		
		try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(5, s.getConnected());
	}
}
