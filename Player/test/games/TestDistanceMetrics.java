package games;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import metrics.Levenshtein;
import metrics.ZhangShasha;

//-----------------------------------------------------------------------------

/**
 * Test distance metrics.
 *
 * @author cambolbro
 */
public class TestDistanceMetrics
{
	@Test
	public void testGameFileNames() throws IOException
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		// Find the .lud files (and not .def)
		final List<File> entries = new ArrayList<>();

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

					if (path.equals("../Common/res/lud/wishlist"))
						continue;

					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/bad"))
						continue;

					if (path.equals("../Common/res/lud/bad_playout"))
						continue;

					gameDirs.add(fileEntry);
				}
				else if (fileEntry.getName().contains(".lud"))
				{
					entries.add(fileEntry);
				}
			}
		}

		// Measure distance between each pair of games
		final int NUM_TO_TEST = 10;  //entries.size();
		for (int a = 0; a < NUM_TO_TEST; a++)
		{
			final File fileA = entries.get(a);
			final String filepathA = fileA.getPath();
			
			System.out.println("\n---------------------------\nGame A: " + filepathA);
			
			final String descA = FileHandling.loadTextContentsFromFile(filepathA);
			
//			final GameDescription gameDescriptionA = new GameDescription(descA);
//			Parser.getParser().parse(gameDescriptionA, new UserSelections(new ArrayList<String>()), false); 
//			final Game gameA = new Game(fileA.getName(), gameDescriptionA);			

			final Game gameA = (Game)Compiler.compileTest(new Description(descA), false);
			if (gameA == null)
			{
				System.out.println("** Failed to compile " + filepathA + ".");
				continue;
			}
			
			for (int b = 0; b < NUM_TO_TEST; b++)
			{
				final File fileB = entries.get(b);
				final String filepathB = fileB.getPath();
				
				System.out.println("\nA: " + fileA.getName());
				System.out.println("B: " + fileB.getName());
				
				final String descB = FileHandling.loadTextContentsFromFile(filepathB);
				
//				final GameDescription gameDescriptionB = new GameDescription(descB);
//				Parser.getParser().parse(gameDescriptionB, new UserSelections(new ArrayList<String>()), false); 
//				final Game gameB = new Game(fileB.getName(), gameDescriptionB);

				final Game gameB = (Game)Compiler.compileTest(new Description(descB), false);
				if (gameB == null)
				{
					System.out.println("** Failed to compile " + filepathB + ".");
					continue;
				}
				
				System.out.println("-- Levenshtein = " + new Levenshtein().distance(gameA, gameB).score());
				
				// Zhang Shasha tree edit distance is slow for larger files, so cap description length
				if 
				(
					gameA.description().tokenForest().tokenTree().count() 
					+
					gameB.description().tokenForest().tokenTree().count() 
					< 
					1500
				)
					System.out.println("-- ZhangShasha = " + new ZhangShasha().distance(gameA, gameB).score());
			}
		}
	}

}
