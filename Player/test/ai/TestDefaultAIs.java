package ai;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import compiler.Compiler;
import game.Game;
import main.FileHandling;
import main.grammar.Description;
import other.context.Context;
import other.trial.Trial;
import utils.LudiiAI;

/**
 * Unit test to ensure that for every game we can load a default AI
 * that can make at least one move without crashing in that game.
 *
 * @author Dennis Soemers
 */
public class TestDefaultAIs
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
					
					if (path.equals("../Common/res/lud/subgame"))
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
			
				testDefaultAI(game);
			}
		}
	}
	
	/**
	 * Tests default AI on given game
	 * @param game
	 */
	public static void testDefaultAI(final Game game)
	{
		if (game.isDeductionPuzzle())
			return;
		
		if (game.hasSubgames())
			return;
		
		try
		{
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
	
			game.start(context);
	
			// Create our AI and init it
			final LudiiAI ai = new LudiiAI();
			ai.initAI(game, 1);
	
			// Have it select one move
			ai.selectAction(game, context, 0.1, 10, 1);
		}
		catch (final Exception e)
		{
			System.err.println("Game = " + game.name());
			e.printStackTrace();
			fail();
		}
	}

}
