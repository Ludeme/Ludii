package games.options;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import game.Game;
import other.GameLoader;

/**
 * A small unit test to make sure that we can correctly compile
 * Hex with options. This unit test tries a couple of different
 * board sizes and makes sure we end up with a board with the
 * correct number of cells.
 *
 * @author Dennis Soemers
 */
public class TestHexOptions
{
	
	@Test
	public static void testHexBoardSizes()
	{
		final List<String> options = new ArrayList<String>();
		
		// First load default Hex (should be 11x11 board --> 121 cells)
		final Game defaultHex = GameLoader.loadGameFromName("/Hex.lud");
		assert(defaultHex.board().numSites() == 121);
		
		// Now try 3x3 Hex (9 cells)
		options.add("Board Size/3x3");
		final Game hexThreeByThree = GameLoader.loadGameFromName("/Hex.lud", options);
		assert(hexThreeByThree.board().numSites() == 9);
		
		// Finally try 15x15 Hex (225 cells)
		options.clear();
		options.add("Board Size/15x15");
		final Game hexFifteenByFifteen = GameLoader.loadGameFromName("/Hex.lud", options);
		assert(hexFifteenByFifteen.board().numSites() == 225);
	}

}
