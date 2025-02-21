package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.junit.Test;

import game.Game;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

/**
 * A Unit Test to generate, and store, random trials for every game.
 * Games for which trials are already stored will be skipped.
 * 
 * @author Dennis Soemers
 */
public class GenerateRandomTestTrials
{
	
	/** Number of random trials to generate per game */
	private static final int NUM_TRIALS_PER_GAME = 2;
	
	/**
	 * Generates trials for Travis tests.
	 */
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
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (fileEntryPath.equals("../Common/res/lud/plex"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/wip"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/test"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/reconstruction"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/puzzle/deduction"))
						continue;	// skip puzzles for now
					
					if (fileEntryPath.equals("../Common/res/lud/bad"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
						continue;
					
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
				final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
				
				final String trialDirPath = 
						ludPath
						.replaceFirst(Pattern.quote("/Common/res/"), Matcher.quoteReplacement("/Player/res/"))
						.replaceFirst(Pattern.quote("/lud/"), Matcher.quoteReplacement("/random_trials/"))
						.replace(".lud", "");

				for (int i = 0; i < NUM_TRIALS_PER_GAME; ++i)
				{
					final String trialFilepath = trialDirPath + File.separator + "RandomTrial_" + i + ".txt";
					final File trialFile = new File(trialFilepath);
					
					if (trialFile.exists())
					{
						System.out.println("Skipping " + ludPath + "; trial already exists at: " + trialFilepath);
					}
					else
					{
						trialFile.getParentFile().mkdirs();
						
						final Game game = GameLoader.loadGameFromFile(fileEntry);
		
						System.out.println("Starting playout for: " + ludPath + "...");
						
						// disable custom playouts that cannot properly store history of legal moves per state
						game.disableMemorylessPlayouts();
						
						final Trial trial = new Trial(game);
						final Context context = new Context(game, trial);
						
						final RandomProviderDefaultState gameStartRngState = (RandomProviderDefaultState) context.rng().saveState();
		
						trial.storeLegalMovesHistorySizes();
						if (context.isAMatch())
							context.currentInstanceContext().trial().storeLegalMovesHistorySizes();
						
						game.start(context);
						game.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
						
						try
						{
							trial.saveTrialToTextFile(trialFile, ludPath, new ArrayList<String>(), gameStartRngState);
							System.out.println("Saved trial for " + ludPath + " to file: " + trialFilepath);
						}
						catch (final IOException e)
						{
							e.printStackTrace();
							fail("Crashed when trying to save trial to file.");
						}
					}
				}
			}
		}
	}

}
