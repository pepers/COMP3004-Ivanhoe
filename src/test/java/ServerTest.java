package test.java;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.Server;
import main.resources.Config;

public class ServerTest {
	
	Server s = new Server(Config.DEFAULT_PORT);
	
	@Before
	public void before(){
		s.startup();
	}
	
	
	
	@After
	public void after(){
		s.shutdown();
	}
}
