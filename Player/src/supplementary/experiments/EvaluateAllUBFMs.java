package supplementary.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.trial.Trial;
import search.mcts.MCTS;
//import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.BiasedUBFM;
import search.minimax.HybridUBFM;
import search.minimax.LazyUBFM;
import search.minimax.NaiveActionBasedSelection;
import search.minimax.UBFM;
import utils.RandomAI;

/**
 * Class to run experiments and compare an Unbounded Best-First Minimax agent
 * to an Alpha-Beta agent and an UCT agent.
 * 
 * @author cyprien
 */
public class EvaluateAllUBFMs
{
	
	/** Activation of some displays to help debugging if true: */
	public static boolean debugDisplays = false;
	
	/** Number of trials that will be played to compare the agents: (must be even)*/
	final private static int numTrialsPerComparison = 100;
	
	/** Time for the AI to think in the simulations (seconds): */
	final private static double thinkingTime = 1;
	
	private boolean compareHeuristics = true;
	
	final String repository = "/home/cyprien/Documents/M1/Internship/data/";
	
	//-------------------------------------------------------------------------
	
	private static EvaluateAllUBFMs evaluator = null;
	
	/** Game played (first arg of main): */
	public String gameName;
	
	/** The different configurations to test: */
	private List<String[]> configurations;
	
	//-------------------------------------------------------------------------

	// Thread executor (maximum number of threads possible)
	final static int numThreads = 10;
	final ExecutorService executor = Executors.newFixedThreadPool(numThreads); // note: was static on the previous file
	
	//-------------------------------------------------------------------------
	
	/** Main function, running the experiment */
	public void runExperiment()
	{
		System.out.println("Launching all the matches in the game "+gameName+"...");
		
		final Game game = GameLoader.loadGameFromName(gameName+".lud");

		configurations = new ArrayList<String[]>(80);
		
		if (compareHeuristics)
		{
			configurations.add(new String[] {"TrainedUBFM", Float.toString(0.2f), Integer.toString(1)});
			for (int i=1; i<=15; i++)
				configurations.add(new String[] {"TrainedUBFM", Float.toString(0.2f), Integer.toString(i*5)});
		}
		else
		{
			if ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null))
			{
				configurations.add(new String[] {"Naive Action Based Selection"});
				System.out.println("features found");
			}
			for (String epsilon : new String[] {"0", "0.1", "0.2", "0.3", "0.5"})
			{
				
				configurations.add(new String[] {"UBFM",epsilon});
				
				configurations.add(new String[] {"DescentUBFM",epsilon});
				
				if ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null))
				{
					for (String weight : new String[] {"0.1","0.3","0.5","1","2"})
						configurations.add(new String[] {"LazyUBFM",epsilon,weight});
					
					for (String n : new String[] {"2","4","6","10"})
						configurations.add(new String[] {"BiasedUBFM",epsilon,n});
				}
				
				for (String weight : new String[] {"0.2","0.5","0.9"})
					configurations.add(new String[] {"HybridUBFM",epsilon,weight});
			}
			
			configurations.add(new String[] {"Bob"});
		}
		
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		try
		{
			// Run trials concurrently
			final CountDownLatch latch = new CountDownLatch(configurations.size()*2); //*2 because there are two opponents

			final List<Future<double[]>> futures = new ArrayList<Future<double[]>>(numTrialsPerComparison);
			
			for (String[] config: configurations)
			{	
				for (String opp: new String[] {"AlphaBeta","UCT"})
				{
					final String[] configuration = config;
					final String opponent = opp;
					futures.add(
						executor.submit
						(
							() -> 
							{

								final double[] agentScores = new double[]{0.,0.,0.,0.};
								try
								{
									LocalDateTime beginTime = LocalDateTime.now();  
									
									UBFM UBFM_AI = null;
									AI not_UBFM_AI = null;
									switch (configuration[0])
									{
									case "UBFM":
										UBFM_AI = new UBFM();
										break;
									case "TrainedUBFM":
										String fileName = repository+"learning/learned_heuristics/"+gameName+configuration[2]+".sav";
										File f = new File(fileName);
										if (f.exists())
											UBFM_AI = new UBFM();
										break;
									case "DescentUBFM":
										UBFM_AI = new UBFM();
										UBFM_AI.setIfFullPlayouts(true);
										break;
									case "LazyUBFM":
										final LazyUBFM LazyAI = new LazyUBFM();
										UBFM_AI = LazyAI;
										LazyUBFM.setActionEvaluationWeight(Float.parseFloat(configuration[2]));
										break;
									case "HybridUBFM":
										final HybridUBFM HybridAI = new HybridUBFM();
										UBFM_AI = HybridAI;
										HybridAI.setHeuristicScoreWeight(Float.parseFloat(configuration[2]));
										break;
									case "BiasedUBFM":
										final BiasedUBFM BiasedAI = new BiasedUBFM();
										UBFM_AI = BiasedAI;
										BiasedAI.setNbStateEvaluationsPerNode(Integer.parseInt(configuration[2]));
										break;
									case "Naive Action Based Selection":
										not_UBFM_AI = new NaiveActionBasedSelection();
										break;
									default:
										System.out.println(configuration[0]);
										throw new RuntimeException("Configuration not understood");
									}

									AI Tested_AI = null;
									if (UBFM_AI != null)
									{
										UBFM_AI.setSelectionEpsilon(Float.parseFloat(configuration[1]));
										UBFM_AI.setSelectionPolicy(UBFM.SelectionPolicy.SAFEST);
										Tested_AI = UBFM_AI;
										UBFM_AI.savingSearchTreeDescription = false;
										
										//if (gameName == "Chess")
										UBFM_AI.setTTReset(true); // FIXME: could be changed
										
										UBFM_AI.debugDisplay = false;
										UBFM_AI.savingSearchTreeDescription = false;
									}
									else if (not_UBFM_AI != null)
										Tested_AI = not_UBFM_AI;
									
									if (Tested_AI != null)
									{
										final AI opponentAI;
										switch (opponent)
										{
										case "AlphaBeta":
											opponentAI = new AlphaBetaSearch();
											break;
										case "UCT":
											opponentAI = MCTS.createUCT();
											break;
										case "RandomAI":
											opponentAI = new RandomAI();
											break;
										default:
											throw new RuntimeException("Unkown opponent");
										}
										
										compareAgents(game, Tested_AI, opponentAI, agentScores, numTrialsPerComparison, configuration);
		
										try
										{
											File directory = new File(String.valueOf(repository+gameName+"/"+opponent+"/"));
											directory.mkdirs();
											FileWriter myWriter = new FileWriter(repository+gameName+"/"+opponent+"/"+configurationToString(configuration)+".sav");
											myWriter.write("Results of the duel between "+configurationToString(configuration)+" against "+opponent+":\n");
											myWriter.write("Game: "+gameName+"\n");
											myWriter.write("(thinking time: "+Double.toString(thinkingTime)+", numberOfPlayouts: "+Integer.toString(numTrialsPerComparison)+")\n");
											myWriter.write("(begin time "+dtf.format(beginTime)+", end time "+dtf.format(LocalDateTime.now())+")\n\n");
											myWriter.write("UBFM WR as 1st player:"+Double.toString(agentScores[0])+"\n");
											myWriter.write("Opponent WR as 1st player:"+Double.toString(agentScores[1])+"\n");
											myWriter.write("UBFM WR as 2nd player:"+Double.toString(agentScores[2])+"\n");
											myWriter.write("Opponent WR as 2nd player:"+Double.toString(agentScores[3])+"\n");
											myWriter.write("\n");
											myWriter.write("UBFM WR average:"+Double.toString((agentScores[2]+agentScores[0])/2.)+"\n");
											myWriter.write("Opponent WR average:"+Double.toString((agentScores[1]+agentScores[3])/2.)+"\n");
											myWriter.write("UBFM score:"+Double.toString(50f+(agentScores[0]+agentScores[2])/4.-(agentScores[1]+agentScores[3])/4.)+"\n");
											myWriter.close();
										}
										catch (IOException e)
										{
									    	System.out.println("An error occurred.");
									    	e.printStackTrace();
									    }
										
									}
									
									return agentScores;
								}
								catch (final Exception e)
								{
									e.printStackTrace();
									
									return agentScores;
								}
								finally
								{
									latch.countDown();
								}
							}
						)
					);
				}
			}
			
			latch.await();  // wait for all trials to finish
			
			System.out.println("Games done.");
			
			executor.shutdown();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Compares two agents on a given game. Writes the results in the array resultsArray.
	 * The format of the result is the following:
	 * {AI1 win rate as 1st player, AI2 win rate as 1st player, AI1 win rate as 2nd player, AI2 win rate as 2nd player}
	 * @param game
	 * @param AI1
	 * @parama AI2
	 * @param resultsArray : array of size 4
	 * @param nbTrials
	 * @param configuration
	 */
	private void compareAgents
	(
		final Game game,
		final AI AI1,
		final AI AI2,
		final double[] resultsArray,
		final int nbTrials,
		final String[] configuration
	)
	{
		
		for (int i=0; i<nbTrials; i++)
		{
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			
			final List<AI> agents = new ArrayList<>();
			agents.add(null);
			if (i%2 == 0) {
				agents.add(AI1);
				agents.add(AI2);
			}
			else {
				agents.add(AI2);
				agents.add(AI1);
			}
	
			game.start(context);
	
			AI1.initAI(game, i%2+1);
			AI2.initAI(game, (i+1)%2+1);
			
			if (debugDisplays)
				System.out.println("launching a playout");
			
			game.playout(context, agents, thinkingTime, null, -1, 200, ThreadLocalRandom.current());
			
			if (debugDisplays)
				System.out.println("a game is over");
			
			if (RankUtils.agentUtilities(context)[1+i%2]==1)
				resultsArray[0+(i%2)*2] += 2 * 100 / nbTrials;
			if (RankUtils.agentUtilities(context)[2-i%2]==1)
				resultsArray[1+(i%2)*2] += 2 * 100 / nbTrials;
		}
		
		return;
	}
	
	public static String configurationToString(String[] configuration)
	{
		StringBuffer res = new StringBuffer();
		
		for (int i=0; i<configuration.length; i++)
			res.append(configuration[i].replace(".", "p")+"_");
		res.deleteCharAt(res.length()-1);
		return res.toString();
	}
	
	public static void main(String[] args)
	{
		evaluator = new EvaluateAllUBFMs();
		if (args.length>0)
			evaluator.gameName = args[0];
		else
			evaluator.gameName = "Breakthrough"; // by default
		
		if (args.length>1)
			if (args[1] == "eval heuristics")
				evaluator.compareHeuristics = true;
		
		evaluator.runExperiment();
	}
}
