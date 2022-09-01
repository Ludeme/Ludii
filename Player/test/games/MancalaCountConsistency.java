package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.StringRoutines;
import main.collections.FastArrayList;
import main.grammar.Description;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Unit test to make sure that the sum of counts of all sites in all
 * Mancala games remains constant throughout random playouts.
 *
 * @author Dennis Soemers
 */
public class MancalaCountConsistency
{
	
	/** If any of these directories exist, we'll save trials in it where the total count change */
	private static final String[] SAVE_TRIALS_DIRS = new String[]{
			"D:/Apps/Ludii_Local_Experiments/MancalaCountTrials/",		// Dennis desktop
			"C:/Apps/Ludii_Local_Experiments/MancalaCountTrials/", // Dennis
																	// laptop
			"C:/Users/eric.piette/MancalaTrials/" // Eric
	};
	
	/**
	 * The test to run.
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
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (path.equals("../Common/res/lud/plex"))
						continue;
					
					if (path.equals("../Common/res/lud/wip"))
						continue;
					
					if (path.equals("../Common/res/lud/wishlist"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/puzzle/deduction"))
						continue;	// skip puzzles for now
					
					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					if (path.equals("../Common/res/lud/bad_playout"))
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
			if 
			(
				fileEntry.getName().contains(".lud") && 
				fileEntry.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/").contains("/mancala/")
			)
			{
				final String fileName = fileEntry.getPath();

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
				if (game == null)
				{
					System.out.println("** FAILED TO COMPILE GAME.");
					fail("COMPILATION FAILED for the file : " + fileName);
				}

				// run our trial
				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);
				
				final SplitMix64 playoutRNG = new SplitMix64();
				final RandomProviderDefaultState playoutStartRngState = (RandomProviderDefaultState) playoutRNG.saveState();
				final RandomProviderDefaultState gameStartRngState = (RandomProviderDefaultState) context.rng().saveState();
				
//				playoutRNG.restoreState(new RandomProviderDefaultState(new byte[] {-32, -120, 27, -51, 86, 12, 88, -112}));
//				context.rng().restoreState(new RandomProviderDefaultState(new byte[] {-103, -52, -76, -70, 8, -37, -65, 29}));
				
				System.out.println("Game = " + fileEntry.getName());
				System.out.println("Playout start RNG = " + Arrays.toString(playoutStartRngState.getState()));
				System.out.println("Game start RNG = " + Arrays.toString(gameStartRngState.getState()));

				game.start(context);
				final int startCount = totalCount(context);
				
				boolean saved = false;
				
				while (!trial.over() && !saved)
				{
					final FastArrayList<Move> moves = game.moves(context).moves();
					final Move randomMove = moves.get(playoutRNG.nextInt(moves.size()));
					game.apply(context, randomMove);
					
					// Make sure our total count didn't change
					if (startCount != totalCount(context))
					{
						// Unit test is about to fail. Save trials if any of our directories exist						
						for (final String filepath : SAVE_TRIALS_DIRS)
						{
							final File dirFile = new File(filepath);
							
							if (dirFile.exists() && dirFile.isDirectory())
							{
								try
								{
									final File saveFile = new File(filepath + StringRoutines.cleanGameName(fileEntry.getName()) + ".trl");
									
									trial.saveTrialToTextFile
									(
										saveFile, 
										fileEntry.getPath(), 
										new ArrayList<String>(), 
										gameStartRngState
									);
									
									saved = true;
									System.err.println("Saved trial file: " + saveFile.getCanonicalPath());
								}
								catch (final IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
					
					assert (startCount == totalCount(context));
				}
			}
		}
	}
	
	/**
	 * @param context
	 * @return Sum of counts of all sites in given context
	 */
	private static final int totalCount(final Context context)
	{
		final State state = context.state();
		final ContainerState cs = state.containerStates()[0];
		
		int sum = 0;
		
		for (int i = 0; i < context.game().equipment().totalDefaultSites(); ++i)
		{
			sum += cs.countCell(i);
		}
		
		return sum;
	}

}
