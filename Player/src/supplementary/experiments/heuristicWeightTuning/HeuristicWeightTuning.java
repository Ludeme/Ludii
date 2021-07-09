package supplementary.experiments.heuristicWeightTuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import game.equipment.other.Regions;
import gnu.trove.list.array.TIntArrayList;
import main.math.Stats;
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
import other.AI;
import other.GameLoader;
import search.minimax.HeuristicSampling;
import supplementary.experiments.EvalGamesSet;
import supplementary.experiments.scripts.FindBestBaseAgentScriptsGen;

//-----------------------------------------------------------------------------

/**
 * Experiments to tune the weights of heuristics
 * 
 * @author matthew.stephenson and cambolbro
 */
public class HeuristicWeightTuning
{

	final static int numTrialsPerPermutation = 100;
	
	//-------------------------------------------------------------------------
	
	private void test()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");
		final List<HeuristicTerm> heuristicTerms = initialHeuristicTerms(game);
		final List<HeuristicSampling> allAgents = initialAgents(heuristicTerms);
		final List<TIntArrayList> allAgentIndexCombinations = allAgentIndexCombinations(game, allAgents);
		
		final List<Double> allAgentWinRates = new ArrayList<Double>(Collections.nCopies(allAgents.size(), 0.0));
		final int numEvaluationsPerAgent = allAgentIndexCombinations.size() * game.players().count() / allAgents.size(); 
		//binom(allAgents.size()-1, game.players().count()-1);
		
		System.out.println("number of pairups: " + allAgentIndexCombinations.size());
		System.out.println("number of agents: " + allAgents.size());
		System.out.println("number of pairups per agent: " + numEvaluationsPerAgent);
		
		try
		{
			// Perform initial comparison across all agents/heuristics
			int counter = 1;
			for (final TIntArrayList agentIndices : allAgentIndexCombinations)
			{
				System.out.println(counter + "/" + allAgentIndexCombinations.size());
				counter++;
				
				final List<AI> agents = new ArrayList<>();
				for (final int i : agentIndices.toArray())
					agents.add(allAgents.get(i));
				
				final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
				for (int i = 0; i < agentMeanWinRates.size(); i++)
					allAgentWinRates.set(agentIndices.get(i), allAgentWinRates.get(agentIndices.get(i)) + agentMeanWinRates.get(i));
			}
			
			// Perform initial comparison against Naive.
//			int counter = 1;
//			for (final TIntArrayList agentIndices : allAgentIndexCombinations)
//			{
//				System.out.println(counter + "/" + allAgentIndexCombinations.size());
//				counter++;
//				
//				final List<AI> agents = new ArrayList<>();
//				for (final int i : agentIndices.toArray())
//					agents.add(allAgents.get(i));
//				
//				final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
//				for (int i = 0; i < agentMeanWinRates.size(); i++)
//					allAgentWinRates.set(agentIndices.get(i), allAgentWinRates.get(agentIndices.get(i)) + agentMeanWinRates.get(i));
//			}
			
			// Remove any heuristics that score less than 50% win-rate
			final Map<Heuristics, Double> candidateHeuristics = new HashMap<Heuristics, Double>();
			for (int i = 0; i < allAgents.size(); i++)
			{
				final Heuristics agentHeuristics = allAgents.get(i).heuristics();
				final double averageWinRate = allAgentWinRates.get(i)/numEvaluationsPerAgent;
				if (averageWinRate >= 0.5)
					candidateHeuristics.put(agentHeuristics, averageWinRate);
			}
			
			for (final Map.Entry<Heuristics,Double> candidateHeuristic : candidateHeuristics.entrySet())
			{
				System.out.println("-------------------------------");
				System.out.println(candidateHeuristic.getKey());
				System.out.println(candidateHeuristic.getValue());
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 */
	private static ArrayList<Double> compareAgents(final Game game, final List<AI> agents) throws Exception
	{
		final EvalGamesSet gamesSet = 
				new EvalGamesSet()
				.setGameName(game.name() + ".lud")
				.setAgents(agents)
				.setWarmingUpSecs(0)
				.setNumGames(numTrialsPerPermutation * game.players().count())
				.setPrintOut(false)
				.setRotateAgents(true);
		
		gamesSet.startGames();
		
		final ArrayList<Double> agentMeanWinRates = new ArrayList<>();
		for (final Stats agentStats : gamesSet.resultsSummary().agentPoints())
		{
			agentStats.measure();
			agentMeanWinRates.add(agentStats.mean());
		}
		
		return agentMeanWinRates;
	}
	
	//-------------------------------------------------------------------------
	
	private static List<TIntArrayList> allAgentIndexCombinations(final Game game, final List<HeuristicSampling> allAgents)
	{		
		final int numAgents = allAgents.size();
		final int numPlayers = game.players().count();
		
		final TIntArrayList agentIndices = new TIntArrayList(numAgents);
		for (int i = 0; i < numAgents; ++i)
			agentIndices.add(i);
		
		final List<TIntArrayList> allAgentIndexCombinations = new ArrayList<TIntArrayList>();
		FindBestBaseAgentScriptsGen.generateAllCombinations(agentIndices, numPlayers, 0, new int[numPlayers], allAgentIndexCombinations);
		
		return allAgentIndexCombinations;
	}

	//-------------------------------------------------------------------------
	
	private static List<HeuristicTerm> initialHeuristicTerms(final Game game)
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
	
	private static List<HeuristicSampling> initialAgents(final List<HeuristicTerm> heuristicTerms)
	{
		final List<HeuristicSampling> allAgents = new ArrayList<>();
		
		for (final HeuristicTerm h : heuristicTerms)
			allAgents.add(new HeuristicSampling(new Heuristics(h)));
		
		return allAgents;
	}
	
	//-------------------------------------------------------------------------
	
//	private static int binom(final int N, final int K) 
//	{
//		int ret = 1;
//	    for (int k = 0; k < K; k++) 
//	        ret = ret * (N-k) / (k+1);
//	    return ret;
//	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final HeuristicWeightTuning sd = new HeuristicWeightTuning();
		sd.test();
	}

	//-------------------------------------------------------------------------

}
