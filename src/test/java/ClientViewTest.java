package test.java;

import java.io.File;

import abbot.tester.ComponentTester;
import junit.extensions.abbot.ComponentTestFixture;
import junit.extensions.abbot.TestHelper;
import main.java.*;
import main.java.ClientView.CardView;
import main.java.ClientView.DisplayView;
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
    	try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
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
    	try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    	s = new Server(Config.DEFAULT_PORT);
    	s.noDrawing = true;
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
    
    public void testCanPlayCardAndContextChange() throws InterruptedException {
    	try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
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
        tester.assertImage(view.cardContext, new File("./res/displaycards/red4"), true);
        s.shutdown();
    }
    
    public void testCannotPlayCardAndContextChange() throws InterruptedException {
    	try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
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
        tester.assertImage(view.cardContext, new File("./res/actioncards/charge"), true);
        s.shutdown();
    }
    
    public void testSelectionMenuWhenTargetting() throws InterruptedException {
    	try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
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
        
        
        s.cmdGive(0, "none:3");
        try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
        view.updateComponents(s.getGameState(), s.getGameState().getPlayer(c.getPlayer()));
        tester.mouseMove(view.hand.getComponent(0));
        assertEquals(true, (((CardView)view.hand.getComponent(0)).highlighted));
        
        tester.click(view.hand.getComponent(0));
        assertTrue(view.menu != null);
        s.shutdown();
    }
    
    public void testDisplayGrows() throws InterruptedException {
    	try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
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
        
        s.cmdGive(0, "blue:4");
        s.updateGameStates();
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        c.setGameState(s.getGameState());
        view.updateComponents(s.getGameState(), s.getGameState().getPlayer(c.getPlayer()));
        tester.mouseMove(view.hand.getComponent(0));
        assertEquals(true, (((CardView)view.hand.getComponent(0)).highlighted));
        tester.click(view.hand.getComponent(0));

        int size = ((DisplayView)view.arena.getComponent(0)).height;
        
        s.cmdGive(0, "blue:3");
        s.updateGameStates();
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        c.setGameState(s.getGameState());
        view.updateComponents(s.getGameState(), s.getGameState().getPlayer(c.getPlayer()));
        tester.mouseMove(view.hand.getComponent(0));
        tester.click(view.hand.getComponent(0));
        
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        assertTrue(size <((DisplayView)view.arena.getComponent(0)).height);
        s.shutdown();
    }
    
    
    public ClientViewTest(String name) { super(name); }
    public static void main(String[] args) {
        TestHelper.runTests(args, ClientViewTest.class);
    }
}
