package test.java;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.java.Client;
import main.java.Server;
import main.resources.Config;

public class ServerTest {

	Server s;

	@Before
	public void before() {
		s = new Server(Config.DEFAULT_PORT);
		resetBans();
		s.startup();
	}

	@After
	public void after() {
		resetBans();
		s.shutdown();
	}

	private void resetBans() {
		// reset the banlist
		try {
			File f = new File("banList.txt");
			BufferedWriter b = new BufferedWriter(new FileWriter(f));
			b.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void TestAddClient() {
		System.out.println("\nTest: Adding a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
	}

	@Test
	public void TestKickClient() {
		System.out.println("\nTest: Kicking a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
		s.removeThread("Client");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, s.getConnected());
	}

	@Test
	public void TestRejectClient() {
		System.out.println("\nTest: Rejecting a Client");
		for (int i = 0; i < 6; i++) {
			Client c = new Client();
			c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
			c.cmdSetname(new String[] { "Client" + i });
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(5, s.getConnected());
	}

	@Test
	public void TestBanClient() {
		System.out.println("\nTest: Banning a Client");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
		// ban the player
		s.ban("127.0.0.1");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, s.getConnected());
	}

	@Test
	public void TestPardonClient() {
		System.out.println("\nTest: Pardoning a Client");
		
		s.ban("127.0.0.1");
		Client c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		// c.cmdSetname(new String[]{"Client"});
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, s.getConnected());

		s.unban("127.0.0.1");
		c = new Client();
		c.connect(Config.DEFAULT_HOST, Config.DEFAULT_PORT);
		c.cmdSetname(new String[] { "Client" });
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, s.getConnected());
	}
}
