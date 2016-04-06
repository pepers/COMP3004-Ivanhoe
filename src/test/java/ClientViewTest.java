package test.java;

import abbot.tester.ComponentTester;
import junit.extensions.abbot.ComponentTestFixture;
import junit.extensions.abbot.TestHelper;
import main.java.*;
import main.java.ClientView.CardView;
import main.resources.Config;

public class ClientViewTest extends ComponentTestFixture{
    private ComponentTester tester;
    protected void setUp() {
        tester = ComponentTester.getTester(ClientView.class);
    }
    
    Server s;
    Client c;
    ClientView view;
    
    public void testEnterGameView() throws InterruptedException {
    	s = new Server(Config.DEFAULT_PORT);
    	s.setMinPlayers(1);
		s.startup();
    	c = new Client();
    	c.startUp();
        ClientView view = c.getView(); 
        tester.actionClick(view.lobbyView.login.connect);
        tester.actionClick(view.getConsole().ready);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        
        assertEquals(true, view.inGame);
        s.shutdown();
    }
    
    public void testCensorButton() throws InterruptedException{
    	s = new Server(Config.DEFAULT_PORT);
    	s.setMinPlayers(1);
		s.startup();
    	c = new Client();
    	c.startUp();
        ClientView view = c.getView(); 
        tester.actionClick(view.lobbyView.login.connect);
        tester.actionClick(view.getConsole().ready);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        
        tester.actionClick(view.censor);
        assertEquals("Client: now censoring bad language.", view.getConsole().lastMessage);
        tester.actionClick(view.censor);
        assertEquals("Client: no longer censoring bad language.", view.getConsole().lastMessage);
        s.shutdown();
    }
    
    public void testCanPlayCard() throws InterruptedException {
    	s = new Server(Config.DEFAULT_PORT);
    	s.setMinPlayers(1);
    	s.noDrawing = true;
		s.startup();
    	c = new Client();
    	c.startUp();
        ClientView view = c.getView(); 
        tester.actionClick(view.lobbyView.login.connect);
        tester.actionClick(view.getConsole().ready);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        
        
        s.cmdGive(0, "red:4");
        try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
        view.updateComponents(s.getGameState(), s.getGameState().getPlayer(c.getPlayer()));
        tester.mouseMove(view.hand.getComponent(0));
        assertEquals(true, (((CardView)view.hand.getComponent(0)).highlighted));
        s.shutdown();
    }
    
    public void testCannotPlayCard() throws InterruptedException {
    	s = new Server(Config.DEFAULT_PORT);
    	s.setMinPlayers(1);
    	s.noDrawing = true;
		s.startup();
    	c = new Client();
    	c.startUp();
        ClientView view = c.getView(); 
        tester.actionClick(view.lobbyView.login.connect);
        tester.actionClick(view.getConsole().ready);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        
        
        s.cmdGive(0, "charge");
        try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
        view.updateComponents(s.getGameState(), s.getGameState().getPlayer(c.getPlayer()));
        tester.mouseMove(view.hand.getComponent(0));
        assertEquals(false, (((CardView)view.hand.getComponent(0)).highlighted));
        s.shutdown();
    }
    
    
    public ClientViewTest(String name) { super(name); }
    public static void main(String[] args) {
        TestHelper.runTests(args, ClientViewTest.class);
    }
}
