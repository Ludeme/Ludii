package games;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import other.context.Context;
import other.trial.Trial;

/**
 * Unit Test to test efficiency for all the games on the lud folder
 * 
 * @author Eric.Piette
 */
public class SanityChecks
{

	private void recurseFrom
	(
		final List<String> results, 
		final List<String> resultsCompFail,
		final List<String> resultsPlayFail,
		final File folder
	)
	{
		for (final File fileEntry : folder.listFiles())
		{
			if (fileEntry.isDirectory())
			{
				if (fileEntry.getName().equals("plex"))
					continue;
				else if (fileEntry.getName().equals("wip"))
					continue;
				else if (fileEntry.getName().equals("test"))
					continue;
				else if (fileEntry.getName().equals("bad"))
					recurseFrom(resultsCompFail, null, null, fileEntry);
				else if (fileEntry.getName().equals("bad_playout"))
					recurseFrom(resultsPlayFail, null, null, fileEntry);
				else
					recurseFrom(results, resultsCompFail, resultsPlayFail, fileEntry);
			}
			else if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				results.add(fileName);
			}	
		}
	}
	
	@Test
	public void test()
	{
		final List<String> failList = new ArrayList<>();
		final List<String> results = new ArrayList<>();
		final List<String> resultsCompFail = new ArrayList<>();
		final List<String> resultsPlayFail = new ArrayList<>();
		
		final URL url = getClass().getResource("/lud/board/space/connection/Hex.lud");
		final File folder = new File(url.getPath()).getParentFile().getParentFile().getParentFile().getParentFile();

		recurseFrom(results, resultsCompFail, resultsPlayFail, folder);
		
		System.out.println("Found " + results.size() + " normal .lud files");
		System.out.println("Found " + resultsCompFail.size() + " .lud files expected to fail compilation");
		System.out.println("Found " + resultsPlayFail.size() + " .lud files expected to fail in playout");
		
		final int MOVE_LIMIT = 200;
		
		int count = 0;
		int fails = 0;
		int timeouts = 0;
		
		for (final String fileName : results)
		{
			System.out.print("#"+(++count)+". "+fileName+": ");
			try
			{
				final String desc = FileHandling.loadTextContentsFromFile(fileName);
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);
				game.start(context);
				game.playout(context, null, 0.01, null, 0, MOVE_LIMIT, ThreadLocalRandom.current());

				if (context.trial().over())
				{
					System.out.println(" success!");
				}
				else 
				{
					System.out.println(" move limit (" + MOVE_LIMIT + ") exceeded!");
					timeouts++;
				}
			} 
			catch (final Throwable e) 
			{
				failList.add(fileName);
				e.printStackTrace();
				System.err.println(" ERROR - "+e.getMessage());
				fails++;
			}
		}
		
		for (final String fileName : resultsCompFail)
		{
			System.out.print("#"+(++count)+". "+fileName+": ");
			try
			{
				final String desc = FileHandling.loadTextContentsFromFile(fileName);
				//final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				Compiler.compileTest(new Description(desc), false);
				failList.add(fileName);
				System.err.println(" ERROR - we expected compilation to fail for " + fileName);
				fails++;
			} 
			catch (final Throwable e) 
			{
				System.out.println("Compilation failed as expected for " + fileName);
			}
		}
		
		for (final String fileName : resultsPlayFail)
		{
			System.out.print("#"+(++count)+". "+fileName+": ");
			try
			{
				final String desc = FileHandling.loadTextContentsFromFile(fileName);
				final Game game   = (Game)Compiler.compileTest(new Description(desc), false);
				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);
				game.start(context);
				game.playout(context, null, 0.01, null, 0, MOVE_LIMIT, ThreadLocalRandom.current());

				if (!context.trial().over())
				{
					System.out.println(" move limit (" + MOVE_LIMIT + ") exceeded!");
					timeouts++;
				}
				
				failList.add(fileName);
				fails++;
				System.err.println(" ERROR - we expected playout to fail for " + fileName);
			} 
			catch (final Throwable e) 
			{
				System.out.println("Playout failed as expected for " + fileName);
			}
		}
		
		System.out.println
		(
			"Test complete. "
			+ (results.size() + resultsCompFail.size() + resultsPlayFail.size()) 
			+ " games, " + fails + " errors, " + timeouts + " not terminated");
		
		System.out.println(failList);
		
		assertEquals(0, fails);
	}

}
