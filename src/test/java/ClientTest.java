package test.java;

import static org.junit.Assert.*;
import org.junit.Test;

import main.java.Client;
import main.resources.Config;

public class ClientTest {

	@Test
	public void connect() {
		Client c = new Client();
		c.connect("localhost", Config.DEFAULT_PORT);
	}

}
