package supplementary.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.math.statistics.Stats;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.trial.Trial;
//import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.HybridUBFM;
import search.minimax.LazyUBFM;
import search.minimax.NaiveActionBasedSelection;
import search.minimax.UBFM;
import search.minimax.BiasedUBFM;
import utils.RandomAI;

/**
 * Class to run experiments and compare an Unbounded Best-First Minimax agent
 * to an Iterative Deepening Alpha-Beta agent.
 * 
 * @author cyprien
 *
 */

public class EvaluateUBFM

{
	
	/** Activation of some displays to help debugging if true: */
	public static boolean debugDisplays = false;
	
	private static EvaluateUBFM evaluateUBFM = null;
	
	/** Number of trials that will be played to compare the agents: */
	private static int numTrialsPerComparison = 100;
	
	/** Time for the AI to think in the simulations (seconds): */
	private static double thinkingTime = 1;
	
	/** Game played: */
	private static final String gameName = "Reversi";
	
	/** Name of the file in which the results will be written: */
	private String outputFile = "comparison_output.sav";
	
	/** Text output: */
	private StringBuffer textOutput = new StringBuffer();
	
	//-------------------------------------------------------------------------

	// Thread executor (maximum number of threads possible)
	final static int numThreads = Runtime.getRuntime().availableProcessors();
	final ExecutorService executor = Executors.newFixedThreadPool(numThreads); // note: was static on the previous file
	
	/** Main function, running the experiment */
	public void runExperiment()
	{
		System.out.println("Launching experiment comparing UBFM to Iterative deepening...");

		output("Game: "+gameName);
		
		final Game game = GameLoader.loadGameFromName(gameName+".lud");
		
		//final HeuristicTerm materialHeuristic = new Material(null, Float.valueOf(1.f), null, null);
		//final HeuristicTerm[] heuristicsTerms = {materialHeuristic};
		//final Heuristics heuristics = new Heuristics(heuristicsTerms);
		
		final Stats resultsAgent1asFirst = new Stats("Results of agent 1 (as first player)");
		final Stats resultsAgent1asSecond = new Stats("Results of agent 1 (as second player)");
		
		int nbDraws = 0;
		
		//final ArrayList<Double> resultsAgent2 = new ArrayList<Double>(numTrialsPerComparison);
		
		output("\n");
		
		try
		{
			// Run trials concurrently
			final CountDownLatch latch = new CountDownLatch(numTrialsPerComparison);

			final List<Future<Double>> futures = new ArrayList<Future<Double>>(numTrialsPerComparison);
			
			for (int n = 0; n < numTrialsPerComparison; ++n)
			{	
				
				final int m = n;
				futures.add(
					executor.submit
					(
						() -> 
						{
							try
							{
								final LazyUBFM UBFM_AI = new NaiveActionBasedSelection();
								UBFM_AI.setSelectionPolicy(UBFM.SelectionPolicy.SAFEST);
								//final AI UBFM_AI = new AlphaBetaSearch(heuristics);
								final AI alphaBetaAI = new AlphaBetaSearch();
								
								UBFM_AI.debugDisplay = false;
								UBFM_AI.savingSearchTreeDescription = false;
								UBFM_AI.setActionEvaluationWeight(20f);
								
								final Float[] agentScores = new Float[]{0f,0f};
								
								if (m%2==0)
									compareAgents(game, UBFM_AI, alphaBetaAI, agentScores);
								else
									compareAgents(game, alphaBetaAI, UBFM_AI, agentScores);
								
								latch.countDown();
								output(".");
								
								// agent utilities are converted to a score between 0 and 1
								return (double) agentScores[m%2]*0.5 + 0.5;
							}
							catch (final Exception e)
							{
								e.printStackTrace();
								
								return (double) 0;
							}
						}
					)
				);
			}
			
			latch.await();  // wait for all trials to finish
			
			System.out.println("Games done.");
			
			double result;
			for (int n = 0; n < numTrialsPerComparison; ++n)
			{	
				result = futures.get(n).get();
				if (n%2==0)
					resultsAgent1asFirst.addSample(result);
				else
					resultsAgent1asSecond.addSample(result);
				if (result==0.5)
					nbDraws += 1;				
				if (debugDisplays) System.out.println("Score of agent 1 in game "+n+" is "+futures.get(n).get());
			}
			
			resultsAgent1asFirst.measure();
			resultsAgent1asSecond.measure();
			
			output("\nWin rate of agent 1 (UBFM) as 1st player");
			output(resultsAgent1asFirst.toString());
			
			output("\nWin rate of agent 1 (UBFM) as 2nd player");
			output(resultsAgent1asSecond.toString());
			
			output("\nNumber of draws: ");
			output(Double.toString(nbDraws));
			
			output("\nOverall mean: ");
			output(Double.toString((resultsAgent1asFirst.mean()+resultsAgent1asSecond.mean())/2));
			
			executor.shutdown();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Compares a two of agents on a given game. Writes the results in the array resultsArray.
	 */
	private static void compareAgents(final Game game, final AI AI1, final AI AI2, final Float[] resultsArray)
	{
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		final List<AI> agents = new ArrayList<>();
		agents.add(null);
		
		agents.add(AI1);
		agents.add(AI2);

		game.start(context);

		AI1.initAI(game, 1);
		AI2.initAI(game, 2);
		
		if (debugDisplays) System.out.println("launching a playout");
		
		game.playout(context, agents, thinkingTime, null, -1, 200, ThreadLocalRandom.current());
		
		if (debugDisplays) System.out.println("a game is over");
		
		resultsArray[0] += (float) RankUtils.agentUtilities(context)[1];
		resultsArray[1] += (float) RankUtils.agentUtilities(context)[2];
		
		return;
	}
	
	private void output(String text)
	{
		System.out.print(text);
		
		textOutput.append(text);
		
		try {
	      FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/"+outputFile);
	      myWriter.write(textOutput.toString());
	      myWriter.close();
	    } catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
		
		return;
	}
	
	public static void main(String[] args)
	{
		evaluateUBFM = new EvaluateUBFM();
		
		evaluateUBFM.runExperiment();
	}
}
