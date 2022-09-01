package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

/**
 * Unit Test to run one playout by game
 * 
 * @author Eric.Piette, Dennis Soemers
 */
public class OnePlayoutByGameTest
{
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();
		final List<File> badPlayoutEntries = new ArrayList<File>();
		
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

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles for now
					
					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					if (path.equals("../Common/res/lud/bad_playout"))
					{
						// We'll only find lud files here which should compile, but fail to run
						for (final File fileEntryInter : fileEntry.listFiles())
						{
							badPlayoutEntries.add(fileEntryInter);
						}
					}
					else
					{
						// We'll find files that we should be able to compile and run here
						gameDirs.add(fileEntry);
					}
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}
		
		// the following entries should correctly compile and run
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				final Game game = GameLoader.loadGameFromFile(fileEntry);

				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);

				game.start(context);
				game.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
				System.out.println("PLAYOUT COMPLETE FOR " + game.name());
			}
		}
		
		// the following entries should compile, but then fail to run
		for (final File fileEntry : badPlayoutEntries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				final Game game = GameLoader.loadGameFromFile(fileEntry);

				try
				{
					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
	
					game.start(context);
					game.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
					System.err.println("PLAYOUT COMPLETE FOR " + game.name());
					fail("COMPLETED PLAYOUT for file which was supposed to file: " + fileName);
				}
				catch (final Exception exception)
				{
					System.out.println("Running game failed as expected.");
				}
			}
		}
	}

}
