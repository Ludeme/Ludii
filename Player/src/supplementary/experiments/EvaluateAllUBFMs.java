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
import game.types.play.RoleType;
import metadata.ai.features.Features;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.trial.Trial;
import search.mcts.MCTS;
//import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.HybridUBFM;
import search.minimax.LazyUBFM;
import search.minimax.UBFM;
import search.minimax.BiasedUBFM;

/**
 * Class to run experiments and compare an Unbounded Best-First Minimax agent
 * to an Iterative Deepening Alpha-Beta agent.
 * 
 * @author cyprien
 */

public class EvaluateAllUBFMs
{
	
	/** Activation of some displays to help debugging if true: */
	public static boolean debugDisplays = false;
	
	/** Number of trials that will be played to compare the agents: (must be even)*/
	private static int numTrialsPerComparison = 2;
	
	/** Time for the AI to think in the simulations (seconds): */
	private static double thinkingTime = 1;
	
	//-------------------------------------------------------------------------
	
	private static EvaluateAllUBFMs evaluator = null;
	
	/** Game played (first arg of main): */
	public String gameName;
	
	/** The different configurations to test: */
	private List<String[]> configurations;
	
	//-------------------------------------------------------------------------

	// Thread executor (maximum number of threads possible)
	final static int numThreads = 55;
	final ExecutorService executor = Executors.newFixedThreadPool(numThreads); // note: was static on the previous file
	
	//-------------------------------------------------------------------------
	
	/** Main function, running the experiment */
	public void runExperiment()
	{
		System.out.println("Launching experiment including plenty of comparisons in "+gameName+"...");
		
		final Game game = GameLoader.loadGameFromName(gameName+".lud");
		
		configurations = new ArrayList<String[]>(66);
		for (String epsilon : new String[] {"0", "0.1", "0.2", "0.4","1"})
		{
			
			configurations.add(new String[] {"BestFirstSearch",epsilon});
			
			boolean featuresAvailable = false;
			
			if (game.metadata().ai().features() != null)
			{
				final Features featuresMetadata = game.metadata().ai().features();
				if (featuresMetadata.featureSets().length == 1 && featuresMetadata.featureSets()[0].role() == RoleType.Shared)
					featuresAvailable = true;
			}
			else if (game.metadata().ai().trainedFeatureTrees() != null)
			{
				featuresAvailable = true;
			}
			
			if (featuresAvailable)
			{
				for (String weight : new String[] {"0.1","0.2","0.3","0.5"})
					configurations.add(new String[] {"LazyBFS",epsilon,weight});
				
				for (String n : new String[] {"2","3","6"})
					configurations.add(new String[] {"BiasedBFS",epsilon,n});
				
				for (String weight : new String[] {"0.2","0.5","0.9"})
					configurations.add(new String[] {"HybridBFS",epsilon,weight});
			};
		}
		
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		try
		{
			// Run trials concurrently
			final CountDownLatch latch = new CountDownLatch(configurations.size());

			final List<Future<Double[]>> futures = new ArrayList<Future<Double[]>>(numTrialsPerComparison);
			
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
								try
								{
									LocalDateTime beginTime = LocalDateTime.now();  
									
									final UBFM UBFM_AI;
									switch (configuration[0])
									{
									case "BestFirstSearch":
										UBFM_AI = new UBFM();
										break;
									case "LazyBFS":
										final LazyUBFM LazyAI = new LazyUBFM();
										UBFM_AI = LazyAI;
										LazyAI.setActionEvaluationWeight(Float.parseFloat(configuration[2]));
										break;
									case "HybridBFS":
										final HybridUBFM HybridAI = new HybridUBFM();
										UBFM_AI = HybridAI;
										HybridAI.setHeuristicScoreWeight(Float.parseFloat(configuration[2]));
										break;
									case "BiasedBFS":
										final BiasedUBFM BiasedAI = new BiasedUBFM();
										UBFM_AI = BiasedAI;
										BiasedAI.setNbStateEvaluationsPerNode(Integer.parseInt(configuration[2]));
										break;
									default:
										throw new RuntimeException("Configuration not understood");
									}
									
									UBFM_AI.setSelectionEpsilon(Float.parseFloat(configuration[1]));
									UBFM_AI.setSelectionPolicy(UBFM.SelectionPolicy.SAFEST);
									
									if (gameName == "Chess")
										UBFM_AI.setTTReset(true);
									
									final AI opponentAI;
									switch (opponent)
									{
									case "AlphaBeta":
										opponentAI = new AlphaBetaSearch();
										break;
									case "UCT":
										opponentAI = MCTS.createUCT();
										break;
									default:
										throw new RuntimeException("Unkown opponent");
									}
									
									UBFM_AI.debugDisplay = false;
									UBFM_AI.savingSearchTreeDescription = false;
									
									final Double[] agentScores = new Double[]{0.,0.,0.,0.};
									
									compareAgents(game, UBFM_AI, opponentAI, agentScores, numTrialsPerComparison,configuration);
	
									try {
										File directory = new File(String.valueOf("/home/cyprien/M1/Internship//data/"+gameName+"/"+opponent+"/"));
										directory.mkdirs();
										FileWriter myWriter = new FileWriter("/home/cyprien/M1/Internship/data/"+gameName+"/"+opponent+"/"+configurationToString(configuration)+".sav");
										myWriter.write("Results of the duel between "+configurationToString(configuration)+" against "+opponent+":\n");
										myWriter.write("(thinking time:"+Double.toString(thinkingTime)+", numberOfPlayouts:"+Integer.toString(numTrialsPerComparison)+")\n\n");
										myWriter.write("(begin time "+dtf.format(beginTime)+", end time "+dtf.format(LocalDateTime.now())+")\n");
										myWriter.write("UBFM WR as 1st player:"+Double.toString(agentScores[0])+"\n");
										myWriter.write("Opponent WR as 1st player:"+Double.toString(agentScores[1])+"\n");
										myWriter.write("UBFM WR as 2nd player:"+Double.toString(agentScores[2])+"\n");
										myWriter.write("Opponent WR as 2nd player:"+Double.toString(agentScores[3])+"\n");
										myWriter.write("\n");
										myWriter.write("UBFM WR average:"+Double.toString((agentScores[2]+agentScores[0])/2.)+"\n");
										myWriter.write("Opponent WR average:"+Double.toString((agentScores[1]+agentScores[3])/2.)+"\n");
										myWriter.close();
									} catch (IOException e) {
								    	System.out.println("An error occurred.");
								    	e.printStackTrace();
								    }
									
									return agentScores;
									
								}
								catch (final Exception e)
								{
									e.printStackTrace();
									
									return new Double[] {0.,0.,0.,0.};
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
	 * Compares a two of agents on a given game. Writes the results in the array resultsArray.
	 */
	private void compareAgents
	(
			final Game game,
			final AI AI1,
			final AI AI2,
			final Double[] resultsArray,
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
			
			if (debugDisplays) System.out.println("launching a playout");
			
			game.playout(context, agents, thinkingTime, null, -1, 200, ThreadLocalRandom.current());
			
			if (debugDisplays) System.out.println("a game is over");
			
			resultsArray[0+(i%2)*2] += (RankUtils.agentUtilities(context)[1+i%2] + 1)*100 / nbTrials;
			resultsArray[1+(i%2)*2] += (RankUtils.agentUtilities(context)[1+(i+1)%2] + 1)*100 / nbTrials;
		}
		
		return;
	}
	
	public static String configurationToString(String[] configuration)
	{
		StringBuffer res = new StringBuffer();
		
		for (int i=0; i<configuration.length; i++)
			res.append(configuration[i].replace(".","p")+"_");
		res.deleteCharAt(res.length()-1);
		return res.toString();
	}
	
	public static void main(String[] args)
	{
		evaluator = new EvaluateAllUBFMs();
		if (args.length>0)
			evaluator.gameName = args[0];
		else
			evaluator.gameName = "Brazilian Draughts";
		evaluator.runExperiment();
	}
}
