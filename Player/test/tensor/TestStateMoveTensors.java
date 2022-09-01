package tensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import utils.LudiiGameWrapper;
import utils.LudiiStateWrapper;

/**
 * Unit test to ensure that for every game we can create a tensor for
 * the initial game state, and tensors for all legal moves in the initial
 * game state, without crashing.
 *
 * @author Dennis Soemers
 */
public class TestStateMoveTensors
{
	
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();
		
		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);
			
			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (path.equals("../Common/res/lud/plex"))
						continue;
					
					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/wishlist"))
						continue;
					
					if (path.equals("../Common/res/lud/WishlistDLP"))
						continue;
					
					if (path.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles for now
					
					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					if (path.equals("../Common/res/lud/bad_playout"))
						continue;
					
					if (path.equals("../Common/res/lud/test"))
						continue;
					
					if (path.equals("../Common/res/lud/simulation"))
						continue;
					
					if (path.equals("../Common/res/lud/reconstruction"))
						continue;

					// We'll find files that we should be able to compile and run here
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}
		
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				// Load the string from file
				String desc = "";
				try
				{
					desc = FileHandling.loadTextContentsFromFile(fileName);	
				}
				catch (final FileNotFoundException ex)
				{
					System.out.println("Unable to open file '" + fileName + "'");
				}
				catch (final IOException ex)
				{
					System.out.println("Error reading file '" + fileName + "'");
				}

				// Parse and compile the game
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				
				testTensors(game);
			}
		}
	}
	
	/**
	 * Tries to instantiate tensors for given game, to test that that doesn't make us crash
	 * @param game
	 */
	public static void testTensors(final Game game)
	{
		if (game.hasSubgames())
			return;
				
		if (!game.isAlternatingMoveGame())
			return;

		if (game.isStochasticGame())
			return;

		if (game.hiddenInformation())
			return;

		if (game.isGraphGame())
			return;
		
		if (game.requiresBet())
			return;

		if (game.isDeductionPuzzle())
			return;

		// Create our wrappers
		final LudiiGameWrapper gameWrapper = new LudiiGameWrapper(game);
		final LudiiStateWrapper stateWrapper = new LudiiStateWrapper(gameWrapper);

		// Create tensors for initial game state and legal moves
		stateWrapper.toTensor();
		stateWrapper.legalMovesTensors();
	}

}
