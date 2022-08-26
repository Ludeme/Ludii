package travis.quickTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import game.Game;
import game.types.board.SiteType;
import main.Constants;
import other.GameLoader;
import other.action.move.move.ActionMove;
import other.action.state.ActionSetNextPlayer;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Unit Test to test efficiency for all the games on the lud folder
 * 
 * @author mrraow
 */
@SuppressWarnings("static-method")
public class HashTests
{
	/**
	 * Tests that score hashes are working correctly, including modulus
	 */
	@Test
	public void testScoreHashes()
	{
		final Game game = GameLoader.loadGameFromName("Chaturaji.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().fullHash();
		
		context.setScore(1, 100);
		final long hash100 = context.state().fullHash();
		Assert.assertNotEquals("Score has changed but hash has not.", startHash, hash100);

		context.setScore(1, 100 + Constants.MAX_SCORE);
		final long hash1100 = context.state().fullHash();
		Assert.assertNotEquals("Score has changed but hash has not.", startHash, hash1100);
		Assert.assertNotEquals("Hash(core) should not be Hash(score+MaxScore).", hash100, hash1100);

		context.setScore(1, 100);
		final long hash100b = context.state().fullHash();
		Assert.assertEquals("Same score should give same hash", hash100, hash100b);
	}

	/**
	 * Tests that temp hashes are working correctly
	 */
	@Test
	public void testTempHashes()
	{
		final Game game = GameLoader.loadGameFromName("Chaturaji.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().stateHash();
		final int temp = context.state().temp();
		
		context.state().setTemp(temp+1);
		final long newHash = context.state().stateHash();
		Assert.assertNotEquals("Temp has changed but hash has not.", startHash, newHash);

		context.state().setTemp(temp);
		final long restoredHash = context.state().stateHash();
		Assert.assertEquals("Same temp value should give same hash", startHash, restoredHash);
	}

	/**
	 * Tests that [players swapped] hash is working correctly
	 */
	@Test
	public void testPlayersSwappedHashes()
	{
		final Game game = GameLoader.loadGameFromName("Chaturaji.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().stateHash();
		
		context.state().swapPlayerOrder(1, 2);
		final long newHash = context.state().stateHash();
		Assert.assertNotEquals("Players swapped state has changed but hash has not.", startHash, newHash);

		context.state().swapPlayerOrder(1, 2);
		final long restoredHash = context.state().stateHash();
		Assert.assertEquals("Same swap value should give same hash", startHash, restoredHash);
		assert(true);
	}

	/**
	 * Tests that [consecutive turns] hash and [player turn switch] hash are working correctly
	 */
	@Ignore
	@Test
	public void testTurnHashes()
	{
		final Game game = GameLoader.loadGameFromName("Chaturaji.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().fullHash();
		
		context.state().incrementNumTurnSamePlayer();
		final long newHash = context.state().fullHash();
		Assert.assertNotEquals("Consecutive turns has changed but hash has not.", startHash, newHash);

		context.state().reinitNumTurnSamePlayer();
		final long restoredHash = context.state().fullHash();
		Assert.assertNotEquals("Same consecutive turns should not give same hash (turn has switched)", startHash, restoredHash);
	}

	/**
	 * Tests that [player team] hash is working correctly
	 */
	@Ignore
	@Test
	public void teamHashes()
	{
		final Game game = GameLoader.loadGameFromName("Pachisi.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().stateHash();
		final int playerTeam = context.state().getTeam(1);
		
		context.state().setPlayerToTeam(1, playerTeam+1);
		final long newHash = context.state().stateHash();
		Assert.assertNotEquals("Player team changed but hash has not.", startHash, newHash);

		context.state().setPlayerToTeam(1, playerTeam);
		final long restoredHash = context.state().stateHash();
		Assert.assertEquals("Same team value should give same hash", startHash, restoredHash);
	}
	
	/**
	 * Tests that [player phase] hash is working correctly
	 */
	@Ignore
	@Test 
	public void testPhaseHashes()
	{
		final Game game = GameLoader.loadGameFromName("Lasca.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().stateHash();
		final int startPhase = context.state().currentPhase(1);
		
		context.state().setPhase(1, startPhase+1);
		final long newHash = context.state().stateHash();
		Assert.assertNotEquals("Player phase has changed but hash has not.", startHash, newHash);

		context.state().setPhase(1, startPhase);
		final long restoredHash = context.state().stateHash();
		Assert.assertEquals("Same phase value should give same hash", startHash, restoredHash);
	}
	
	/**
	 * Tests that [player amount] hash is working correctly
	 */
	@Ignore
	@Test
	public void testAmountHashes()
	{
		final Game game = GameLoader.loadGameFromName("testBet.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().fullHash();
		final int startAmount = context.state().amount(1);
		
		context.state().setAmount(1, startAmount + 1);
		final long newHash = context.state().fullHash();
		Assert.assertNotEquals("Player amount has changed but hash has not.", startHash, newHash);

		context.state().setAmount(1, startAmount);
		final long restoredHash = context.state().fullHash();
		Assert.assertEquals("Same amount should give same hash", startHash, restoredHash);
	}
	
	/**
	 * Tests that [visited] hash is working correctly
	 */
	@Ignore
	@Test
	public void visitedHashes()
	{
		final Game game = GameLoader.loadGameFromName("Castello.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().stateHash();
		
		context.state().visit(1);
		final long newHash = context.state().stateHash();
		Assert.assertNotEquals("Visits changed but hash has not.", startHash, newHash);

		context.state().reInitVisited();
		final long restoredHash = context.state().stateHash();
		Assert.assertEquals("Same visits should give same hash", startHash, restoredHash);
	}
	
	/**
	 * Tests that [to remove] hash is working correctly
	 */
	@Ignore
	@Test 
	public void toRemoveHashes()
	{
		final Game game = GameLoader.loadGameFromName("Fanorona.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		game.start(context);

		final long startHash = context.state().stateHash();
		
//		context.state().addPieceToRemove(1);
//		final long newHash = context.state().stateHash();
//		Assert.assertNotEquals("Remove sites changed but hash has not.", startHash, newHash);

		context.state().reInitCapturedPiece();
		final long restoredHash = context.state().stateHash();
		Assert.assertEquals("Same removed sites value should give same hash", startHash, restoredHash);
	}
	
	/**
	 * Plays same 4 chess moves in different order, verifies position is the same
	 */
	@Test
	public void testSameChess()
	{
		Game game = GameLoader.loadGameFromName("/Chess.lud");
		Trial trial = new Trial(game);
		Context context = new Context(game, trial);
		game.start(context);

		step(context, game, 2, 12, 28);
		step(context, game, 1, 52, 36);
		step(context, game, 2, 11, 27);
		step(context, game, 1, 51, 35);

		final long stateHash = context.state().stateHash();
		
		game = GameLoader.loadGameFromName("/Chess.lud");
		trial = new Trial(game);
		context = new Context(game, trial);
		game.start(context);

		step(context, game, 2, 11, 27);
		step(context, game, 1, 51, 35);
		step(context, game, 2, 12, 28);
		step(context, game, 1, 52, 36);

		final long stateHash2 = context.state().stateHash();
		
		assertEquals(stateHash, stateHash2);
	}

	/**
	 * Plays different 2 chess moves, verifies position is different
	 * Note that these are the same two moves used in testSame, so we break the symmetry then restore it
	 */
	@Test
	public void testDifferentChess()
	{
		Game game = GameLoader.loadGameFromName("/Chess.lud");
		Trial trial = new Trial(game);
		Context context = new Context(game, trial);
		game.start(context);

		step(context, game, 2, 12, 28);
		step(context, game, 1, 52, 36);

		final long stateHash = context.state().stateHash();
		
		game = GameLoader.loadGameFromName("/Chess.lud");
		trial = new Trial(game);
		context = new Context(game, trial);
		game.start(context);

		step(context, game, 2, 11, 27);
		step(context, game, 1, 51, 35);

		final long stateHash2 = context.state().stateHash();
		
		assertFalse(stateHash==stateHash2);
	}

	private static void step(final Context context, final Game game, final int nextPlayer, final int from, final int to) 
	{
		game.apply(context,
				new Move(ActionMove.construct(SiteType.Cell, from, Constants.UNDEFINED, SiteType.Cell, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false).withDecision(true),
						new ActionSetNextPlayer(nextPlayer)).withFrom(from).withTo(to));
	}
}
