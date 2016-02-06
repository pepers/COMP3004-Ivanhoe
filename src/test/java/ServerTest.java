package test.java;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {
	
	Server s = new Server();
	
	@Before
	public void before(){
		s.startup();
	}
	
	
	
	@After
	public void after(){
		s.shutdown();
	}
}
