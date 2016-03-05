package test.java;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import main.java.GameState;
import main.java.Player;
import main.java.Tournament;

public class GameStateTest {
	
	GameState g;
	Player[] players = {new Player("P1"),new Player("P2"),new Player("P3"),new Player("P4")};
	
	@Before
	public void before(){
		g = new GameState();
		for (Player p : players){
			g.addPlayer(p);
		}
	}
	
	@Test
	public void TestTournamentParticipants(){
		System.out.println("\nTest: Counting the tournament participants.");
		g.startTournament(new Tournament("blue"));
		assertEquals(4, g.getTournamentParticipants().size());
	}
	
	@Test
	public void TestNextTurn(){
		System.out.println("\nTest: Getting the next turn.");
		Player currentPlayer = players[0];
		for (int i = 0; i<10; i++){
			System.out.println(currentPlayer.getName() + "'s turn...");
			assertEquals(players[i%players.length].getName(), currentPlayer.getName());
			currentPlayer = g.nextTurn();
		}	
	}
}
