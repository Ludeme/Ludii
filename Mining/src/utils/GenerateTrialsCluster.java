package utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import main.Constants;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.AlphaBetaSearch.AllowedSearchDepths;

/**
 * To generate, and store, trials for every game.
 * Games for which trials are already stored will be skipped.
 * 
 * @author Eric Piette
 */
public class GenerateTrialsCluster
{
	/** Number of random trials to generate per game */
	private static final int NUM_TRIALS_PER_GAME = 100;
	
	/** The move limit to use to generate the trials. */
	private static int moveLimit;
	
	//----------------------------------------------------------------
	
	/**
	 * Generates trials.
	 * 
	 * Arg 1 = Move Limit.
	 * Arg 2 = Thinking time for the agents.
	 * Arg 3 = Name of the agent. // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", or "Random"
	 */
	public static void main(final String[] args)
	{
		moveLimit = args.length == 0 ? Constants.DEFAULT_MOVES_LIMIT : Integer.parseInt(args[0]);
		final double thinkingTime = args.length < 2 ? 1 : Double.parseDouble(args[1]);
		final String agentName = args.length < 3 ? "Random" : args[2];
		
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
						.replaceFirst(Pattern.quote("/Common/res/"), Matcher.quoteReplacement("./"))
						.replaceFirst(Pattern.quote("/lud/"), Matcher.quoteReplacement("/" + agentName + "_trials/"))
						.replace(".lud", "");
				
				for (int i = 0; i < NUM_TRIALS_PER_GAME; ++i)
				{
					final String trialFilepath = trialDirPath + File.separator + agentName + "Trial_" + i + ".txt";
					final File trialFile = new File(trialFilepath);
					
					if (trialFile.exists())
					{
						System.out.println("Skipping " + ludPath + "; trial already exists at: " + trialFilepath);
					}
					else
					{
						trialFile.getParentFile().mkdirs();
						
						final Game game = GameLoader.loadGameFromFile(fileEntry);
						game.setMaxMoveLimit(moveLimit);
						
						System.out.println("Starting playout for: " + ludPath + "...");
						
						// Set the agents.
						final List<AI> ais = chooseAI(game, agentName, i);
						for(final AI ai : ais)
							if(ai != null)
								ai.setMaxSecondsPerMove(thinkingTime);
						
						final Trial trial = new Trial(game);
						final Context context = new Context(game, trial);

						game.start(context);
						
						// Init the ais.
						for (int p = 1; p <= game.players().count(); ++p)
							ais.get(p).initAI(game, p);
						final Model model = context.model();
						
						// Run the trial.
						while (!trial.over())
							model.startNewStep(context, ais, thinkingTime);
						
						try
						{
							trial.saveTrialToTextFile(trialFile, ludPath, new ArrayList<String>(), (RandomProviderDefaultState) context.rng().saveState());
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
	
	/**
	 * @param game The game.
	 * @param agentName The name of the agent.
	 * @param indexPlayout The index of the playout.
	 * @return The list of AIs to play that playout.
	 */
	private static List<AI> chooseAI(final Game game, final String agentName, final int indexPlayout)
	{
		final List<AI> ais = new ArrayList<AI>();
		ais.add(null);
		
		for (int p = 1; p <= game.players().count(); ++p)
		{
			if(agentName.equals("UCT"))
			{
				final AI ai = AIFactory.createAI("UCT");
				if(ai.supportsGame(game))
				{
					ais.add(ai);
				}
				else
				{
					ais.add(new utils.RandomAI());
				}
			}
			else if(agentName.equals("Alpha-Beta"))
			{
				AI ai = AIFactory.createAI("Alpha-Beta");
				if(ai.supportsGame(game))
				{
					ais.add(ai);
				}
				else if (AIFactory.createAI("UCT").supportsGame(game))
				{
					ai = AIFactory.createAI("UCT");
					ais.add(ai);
				}
				else 
				{
					ais.add(new utils.RandomAI());
				}
			}
			else if(agentName.equals("Alpha-Beta-UCT")) // AB/UCT/AB/UCT/...
			{
				if(indexPlayout % 2 == 0)
				{
					if(p % 2 == 1)
					{
						AI ai = AIFactory.createAI("Alpha-Beta");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						final AI ai = AIFactory.createAI("UCT");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
				else
				{
					if(p % 2 == 1)
					{
						final AI ai = AIFactory.createAI("UCT");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						AI ai = AIFactory.createAI("Alpha-Beta");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
			}
			else if(agentName.equals("AB-Odd-Even")) // Alternating between AB Odd and AB Even
			{
				if(indexPlayout % 2 == 0)
				{
					if(p % 2 == 1)
					{
						AI ai = new AlphaBetaSearch();
						((AlphaBetaSearch)ai).setAllowedSearchDepths(AllowedSearchDepths.Odd);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						final AlphaBetaSearch ai = new AlphaBetaSearch();
						ai.setAllowedSearchDepths(AllowedSearchDepths.Even);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
				else
				{
					if(p % 2 == 1)
					{
						final AlphaBetaSearch ai = new AlphaBetaSearch();
						ai.setAllowedSearchDepths(AllowedSearchDepths.Even);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						AI ai = new AlphaBetaSearch();
						((AlphaBetaSearch)ai).setAllowedSearchDepths(AllowedSearchDepths.Odd);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
			}
			else
			{
				ais.add(new utils.RandomAI());
			}
		}
		return ais;
	}

}

