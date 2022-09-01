package games.symmetry;

import org.junit.Test;

import game.Game;
import other.GameLoader;
import other.context.Context;
import other.state.symmetry.AcceptAll;
import other.trial.Trial;

public class TestSmallGameHashes
{
	@Test
	public static void testBicycleGameDoesntCrash()
	{
		final Game game1 = GameLoader.loadGameFromName("board/war/other/Ja-Jeon-Geo-Gonu.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		context1.state().canonicalHash(new AcceptAll(), true);
	}
	
	@Test
	public static void testBoseogGonuGameDoesntCrash()
	{
		final Game game1 = GameLoader.loadGameFromName("board/war/other/Boseog Gonu.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		context1.state().canonicalHash(new AcceptAll(), true);
	}
}
