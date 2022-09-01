package other;

import org.junit.Test;

import game.Game;
import main.Constants;

/**
 * Unit test to ensure that our lud path for the default game
 * in Constants is set correctly, to something that actually loads
 * a game.
 *
 * @author Dennis Soemers
 */
@SuppressWarnings("static-method")
public class TestDefaultGameLudPath
{
	
	/**
	 * Unit test to ensure that our lud path for the default game
	 * in Constants is set correctly, to something that actually loads
	 * a game.
	 */
	@Test
	public void testDefaultGameLudPath()
	{
		final Game game = GameLoader.loadGameFromName(Constants.DEFAULT_GAME_PATH);
		assert(game != null);
	}

}
