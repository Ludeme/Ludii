package supplementary.experiments.heuristicWeightTuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	void test()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");
		final List<HeuristicTerm> heuristicTerms = initialHeuristicTerms(game);
		final List<HeuristicSampling> allAgents = initialAgents(heuristicTerms);
		final List<TIntArrayList> allAgentIndexCombinations = allAgentIndexCombinations(game, allAgents);
		
		final List<Double> allAgentWinRates = new ArrayList<Double>(Collections.nCopies(allAgents.size(), 0.0));
		final List<Double> allAgentNumEvaluations = new ArrayList<Double>(Collections.nCopies(allAgents.size(), 0.0));
		
		try
		{
			System.out.println(allAgentIndexCombinations.size());
			System.out.println(allAgents.size());
			
			for (final TIntArrayList agentIndices : allAgentIndexCombinations)
			{
				System.out.println(agentIndices);
				
				final List<AI> agents = new ArrayList<>();
				for (final int i : agentIndices.toArray())
					agents.add(allAgents.get(i));
				
				final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
				for (int i = 0; i < agentMeanWinRates.size(); i++)
				{
					System.out.println(agentMeanWinRates);
					allAgentWinRates.set(agentIndices.get(i), allAgentWinRates.get(agentIndices.get(i)) + agentMeanWinRates.get(i));
					allAgentNumEvaluations.set(agentIndices.get(i), allAgentNumEvaluations.get(agentIndices.get(i))+1);
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		for (int i = 0; i < allAgents.size(); i++)
			System.out.println(allAgents.get(i).heuristics()+ " : " + allAgentWinRates.get(i)/allAgentNumEvaluations.get(i));
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 */
	final static ArrayList<Double> compareAgents(final Game game, final List<AI> agents) throws Exception
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
	
	final static List<TIntArrayList> allAgentIndexCombinations(final Game game, final List<HeuristicSampling> allAgents)
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
	
	public static List<HeuristicTerm> initialHeuristicTerms(final Game game)
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
	
	public static List<HeuristicSampling> initialAgents(final List<HeuristicTerm> heuristicTerms)
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
