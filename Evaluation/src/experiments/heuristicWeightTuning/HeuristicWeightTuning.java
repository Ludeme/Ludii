package experiments.heuristicWeightTuning;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.equipment.other.Regions;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Influence;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import other.GameLoader;
import search.minimax.HeuristicSampling;

//-----------------------------------------------------------------------------

/**
 * Experiments to tune the weights of heuristics
 * 
 * @author matthew.stephenson and cambolbro
 */
public class HeuristicWeightTuning
{

	//-------------------------------------------------------------------------
	
	void test()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");
		final List<HeuristicTerm> heuristicTerms = initialHeuristicTerms(game);
		final List<HeuristicSampling> allAgents = initialAgents(heuristicTerms);
		
		try
		{
			
			
			//lengthHS(game, 2, false);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @param game Single game object shared between threads.
//	 */
//	final void lengthHS
//	(
//		final Game game, 
//		final int fraction, final boolean continuation
//	) throws Exception
//	{
//		final int MaxTrials = 100;
//				
//		final long startAt = System.nanoTime();
//				
//		AI aiA = null;
//		AI aiB = null;
//		
//		//System.out.println("\nHS (1/" + fraction + ")" + (continuation ? "*" : "") + ".");
//		final String label = "HS 1/" + fraction + (continuation ? "" : "-");
//		System.out.println("\n" + label + ":");
//				
//		// Run trials concurrently
//		final ExecutorService executor = Executors.newFixedThreadPool(MaxTrials);
//		final List<Future<TrialRecord>> futures = new ArrayList<>(MaxTrials);
//		
//		final CountDownLatch latch = new CountDownLatch(MaxTrials);
//			
//		for (int t = 0; t < MaxTrials; t++)
//		{
//			final int starter = t % 2;
//			
//			final List<AI> ais = new ArrayList<>();
//			ais.add(null);  // null placeholder for player 0
//			
//			final String heuristicsFilePath = "src/experiments/fastGameLengths/Heuristics_" + game.name() + "_Good.txt";
//			aiA = new HeuristicSampling(heuristicsFilePath);
//			aiB = new HeuristicSampling(heuristicsFilePath);
//				
//			((HeuristicSampling)aiA).setThreshold(fraction);
//			((HeuristicSampling)aiB).setThreshold(fraction);
//
//			((HeuristicSampling)aiA).setContinuation(continuation);
//			((HeuristicSampling)aiB).setContinuation(continuation);
//			
//			if (t % 2 == 0)
//			{
//				ais.add(aiA);
//				ais.add(aiB);
//			}
//			else
//			{
//				ais.add(aiB);
//				ais.add(aiA);
//			}
//			
//			futures.add
//			(
//				executor.submit
//				(
//					() -> 
//					{
//						final Trial trial = new Trial(game);
//						final Context context = new Context(game, trial);
//				
//						game.start(context);
//	
//						for (int p = 1; p <= game.players().count(); ++p)
//							ais.get(p).initAI(game, p);
//	
//						final Model model = context.model();
//						while (!trial.over())
//							model.startNewStep(context, ais, -1, -1, 1, 0);
//	
//						latch.countDown();
//				
//						return new TrialRecord(starter, trial);
//					}
//				)
//			);
//		}
//		
//		latch.await();  // wait for all trials to finish
//		
//		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
//		System.out.println("Heuristic Sampling (1/" + fraction + ") " + secs + "s (" + (secs / MaxTrials) + "s per game).");
//		
//		executor.shutdown();
//	}
	
	//-------------------------------------------------------------------------
	
	public List<HeuristicTerm> initialHeuristicTerms(final Game game)
	{
		final List<HeuristicTerm> heuristicTerms = new ArrayList<>();
		
		heuristicTerms.add(new CentreProximity(null, Float.valueOf(1.f), null));
		heuristicTerms.add(new CentreProximity(null, Float.valueOf(-1.f), null));
		heuristicTerms.add(new ComponentValues(null, Float.valueOf(1.f), null, null));
		heuristicTerms.add(new ComponentValues(null, Float.valueOf(-1.f), null, null));
		heuristicTerms.add(new CornerProximity(null, Float.valueOf(1.f), null));
		heuristicTerms.add(new CornerProximity(null, Float.valueOf(-1.f), null));
		heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(1.f), null));
		heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(-1.f), null));
		heuristicTerms.add(new Material(null, Float.valueOf(1.f), null, null));
		heuristicTerms.add(new Material(null, Float.valueOf(-1.f), null, null));
		heuristicTerms.add(new MobilitySimple(null, Float.valueOf(1.f)));
		heuristicTerms.add(new MobilitySimple(null, Float.valueOf(-1.f)));
		heuristicTerms.add(new Influence(null, Float.valueOf(1.f)));
		heuristicTerms.add(new Influence(null, Float.valueOf(-1.f)));
		heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(1.f)));
		heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(-1.f)));
		heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(1.f)));
		heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(-1.f)));
		heuristicTerms.add(new Score(null, Float.valueOf(1.f)));
		heuristicTerms.add(new Score(null, Float.valueOf(-1.f)));
		heuristicTerms.add(new SidesProximity(null, Float.valueOf(1.f), null));
		heuristicTerms.add(new SidesProximity(null, Float.valueOf(-1.f), null));
		
		final Regions[] regions = game.equipment().regions();
		for (int p = 1; p <= game.players().count(); ++p)
		{
			heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(1.f), Integer.valueOf(p), null));
			heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(-1.f), Integer.valueOf(p), null));
		}
		for (int i = 0; i < regions.length; ++i)
		{
			heuristicTerms.add(new RegionProximity(null, Float.valueOf(1.f), Integer.valueOf(i), null));
			heuristicTerms.add(new RegionProximity(null, Float.valueOf(-1.f), Integer.valueOf(i), null));
		}
		
		return heuristicTerms;
	}
	
	//-------------------------------------------------------------------------
	
	public List<HeuristicSampling> initialAgents(final List<HeuristicTerm> heuristicTerms)
	{
		final List<HeuristicSampling> allAgents = new ArrayList<>();
		
		for (final HeuristicTerm h : heuristicTerms)
			allAgents.add(new HeuristicSampling(new Heuristics(h)));
		
		return allAgents;
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final HeuristicWeightTuning sd = new HeuristicWeightTuning();
		sd.test();
	}

	//-------------------------------------------------------------------------

}
