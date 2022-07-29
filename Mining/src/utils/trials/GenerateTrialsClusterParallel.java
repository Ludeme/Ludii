package utils.trials;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import main.Constants;
import main.FileHandling;
import main.options.Ruleset;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.AlphaBetaSearch.AllowedSearchDepths;
import utils.AIFactory;

/**
 * To generate, and store, trials for every game in parallel.
 * Games for which trials are already stored will be skipped.
 * 
 * @author Eric Piette
 */
public class GenerateTrialsClusterParallel
{
	/** Number of random trials to generate per game */
	private static int NUM_TRIALS_PER_GAME;
	
	/** The move limit to use to generate the trials. */
	private static int moveLimit;
	
	/** The move limit to use to generate the trials. */
	private static String rootPath = "." + File.separator; //""; (for local use this).
	
	/** Number of parallel playouts we run */
	private static final int NUM_PARALLEL = 3;
	
	//----------------------------------------------------------------
	
	/**
	 * Generates trials.
	 * 
	 * Arg 1 = Move Limit.
	 * Arg 2 = Thinking time for the agents.
	 * Arg 3 = Num trials to generate.
	 * Arg 4 = Name of the agent. // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", or "Random"
	 * Arg 5 = Name of the game.
	 * Arg 6 = Name of the ruleset.
	 */
	public static void main(final String[] args)
	{
		moveLimit = args.length == 0 ? Constants.DEFAULT_MOVES_LIMIT : Integer.parseInt(args[0]);
		final double thinkingTime = args.length < 2 ? 1 : Double.parseDouble(args[1]);
		NUM_TRIALS_PER_GAME = args.length < 3 ? 100 : Integer.parseInt(args[2]);
		final String agentName = args.length < 4 ? "Random" : args[3];
		final String gameNameExpected = args.length < 5 ? "" : args[4];
		final String rulesetExpected = args.length < 6 ? "" : args[5];
		
		final File startFolder = new File("Ludii/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final String[] gamePaths = FileHandling.listGames();
		int index = 0;
		String gamePath = "";
		
		System.out.println("Game looking for is " + gameNameExpected);
		System.out.println("Ruleset looking for is " + rulesetExpected);
		
		/** Check if the game path exits. */
		for (; index < gamePaths.length; index++)
		{
			if (!gamePaths[index].contains(gameNameExpected))
				continue;
			
			gamePath = gamePaths[index];
			break;
		}
		
		// Game not found !
		if(index >= gamePaths.length)
			System.err.println("ERROR GAME NOT FOUND");
		else
			System.out.println("GAME FOUND");
			
		gamePath = gamePath.replaceAll(Pattern.quote("\\"), "/");
		
		final Game game = GameLoader.loadGameFromName(gamePath);
		game.setMaxMoveLimit(moveLimit);
		game.start(new Context(game, new Trial(game)));
			
		System.out.println("Loading game: " + game.name());
		
		final String testPath = rootPath + "Trials" + agentName;
		final File testfile = new File(testPath);
		System.out.println(testPath);
		if(!testfile.exists())
			System.out.println("not existing :(");
		
		final String gameFolderPath = rootPath + "Trials" + agentName + File.separator + game.name() ;
		final File gameFolderFile = new File(gameFolderPath);
		
		System.out.println(gameFolderFile);
			
		if(!gameFolderFile.exists())
			gameFolderFile.mkdirs();
			
		// Check if the game has a ruleset.
		final List<Ruleset> rulesetsInGame = game.description().rulesets();
		
		// Has many rulesets.
		if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) 
		{
			for (int rs = 0; rs < rulesetsInGame.size(); rs++)
			{
				final Ruleset ruleset = rulesetsInGame.get(rs);
				
				// We check if we want a specific ruleset.
				if(!rulesetExpected.isEmpty() && !rulesetExpected.equals(ruleset.heading()))
					continue;
				
				if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
				{
					final Game rulesetGame = GameLoader.loadGameFromName(gamePath, ruleset.optionSettings());
					rulesetGame.setMaxMoveLimit(moveLimit);

					final String rulesetFolderPath = gameFolderPath + File.separator + rulesetGame.getRuleset().heading().replace("/", "_");
					final File rulesetFolderFile = new File(rulesetFolderPath);
						
					if(!rulesetFolderFile.exists())
						rulesetFolderFile.mkdirs();

					System.out.println("Loading ruleset: " + rulesetGame.getRuleset().heading());
						
					final ExecutorService executorService = Executors.newFixedThreadPool(NUM_PARALLEL);
					final CountDownLatch latch = new CountDownLatch(NUM_PARALLEL);
					
					for (int i = 0; i < NUM_TRIALS_PER_GAME; ++i)
					{
						System.out.println("Starting playout " + i + ": ...");
						final String trialFilepath = rulesetFolderPath + File.separator + agentName + "Trial_" + i + ".txt";
						final File trialFile = new File(trialFilepath);
							
						if(trialFile.exists())
							continue;

						try {
							final int numTrial = i ;
							final String path = gamePath;
							executorService.submit
							(
							() -> 
								{
									// Set the agents.
									final List<AI> ais = chooseAI(rulesetGame, agentName, numTrial);
									for(final AI ai : ais)
										if(ai != null)
											ai.setMaxSecondsPerMove(thinkingTime);
		
									final Trial trial = new Trial(rulesetGame);
									final Context context = new Context(rulesetGame, trial);
				
									final byte[] startRNGState = ((RandomProviderDefaultState) context.rng().saveState()).getState();
									rulesetGame.start(context);
										
									// Init the ais.
									for (int p = 1; p <= rulesetGame.players().count(); ++p)
										ais.get(p).initAI(rulesetGame, p);
									final Model model = context.model();
								
									// Run the trial.
									while (!trial.over())
										model.startNewStep(context, ais, thinkingTime);
										
									try
									{
										trial.saveTrialToTextFile(trialFile, path, rulesetGame.getOptions(), new RandomProviderDefaultState(startRNGState));
										System.out.println("Saved trial for " + rulesetGame.name() + "|" + rulesetGame.getRuleset().heading().replace("/", "_") +" to file: " + trialFilepath);
									}
									catch (final IOException e)
									{
										e.printStackTrace();
										fail("Crashed when trying to save trial to file.");
									}
								}
							);
							}				
							catch (final Exception e)
							{
								e.printStackTrace();
							}
							finally
							{
								latch.countDown();
							}
					}
					try
					{
						latch.await();
					}
					catch (final InterruptedException e)
					{
						e.printStackTrace();
					}

					executorService.shutdown();
				}
			}
		}
		else // Code for the default ruleset.
		{
			final ExecutorService executorService = Executors.newFixedThreadPool(NUM_PARALLEL);
			final CountDownLatch latch = new CountDownLatch(NUM_PARALLEL);
			
			for (int i = 0; i < NUM_TRIALS_PER_GAME; ++i)
			{
				System.out.println("Starting playout " + i + ": ...");
				final String trialFilepath = gameFolderPath + File.separator + agentName + "Trial_" + i + ".txt";
				final File trialFile = new File(trialFilepath);
				
				if(trialFile.exists())
					continue;
				
				try {
					final int numTrial = i ;
					final String path = gamePath;
					executorService.submit
					(
					() -> 
						{
							// Set the agents.
							final List<AI> ais = chooseAI(game, agentName, numTrial);
							for(final AI ai : ais)
								if(ai != null)
									ai.setMaxSecondsPerMove(thinkingTime);
								
							final Trial trial = new Trial(game);
							final Context context = new Context(game, trial);
				
							final byte[] startRNGState = ((RandomProviderDefaultState) context.rng().saveState()).getState();
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
								trial.saveTrialToTextFile(trialFile, path, new ArrayList<String>(), new RandomProviderDefaultState(startRNGState));
								System.out.println("Saved trial for " + game.name() +" to file: " + trialFilepath);
							}
							catch (final IOException e)
							{
								e.printStackTrace();
								fail("Crashed when trying to save trial to file.");
							}
					}
				);
				}				
				catch (final Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					latch.countDown();
				}
			}
			try
			{
				latch.await();
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}

			executorService.shutdown();
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

