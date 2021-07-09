package supplementary.experiments.heuristicWeightTuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

	final static int numTrialsPerPermutation = 10;
	
	//-------------------------------------------------------------------------
	
	private static void test()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");

		Map<Heuristics, Double> candidateHeuristics = new HashMap<Heuristics, Double>();
		final List<HeuristicTerm> heuristicTerms = initialHeuristicTerms(game);
		for (final HeuristicTerm h : heuristicTerms)
			candidateHeuristics.put(new Heuristics(h), -1.0);

		try
		{
			// Initial comparison
			candidateHeuristics = evaluateCandidateHeuristics(game, candidateHeuristics);
			
			// remove any entries that have below 50% win-rate
			candidateHeuristics.entrySet().removeIf(e -> e.getValue() < 0.5);
			
			candidateHeuristics = evaluateCandidateHeuristics(game, candidateHeuristics);
			
			final Heuristics[] newCandidates = getNewCandidates(candidateHeuristics);
			
			for (final Heuristics newCandidate : newCandidates)
				candidateHeuristics.put(newCandidate, -1.0);
			
			candidateHeuristics = evaluateCandidateHeuristics(game, candidateHeuristics);
		 
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
	
	private static Heuristics[] getNewCandidates(final Map<Heuristics, Double> candidateHeuristics) 
	{
//		final HeuristicTerm[] parentA = tournamentSelection(candidateHeuristics).heuristicTerms();
//		final HeuristicTerm[] parentB = tournamentSelection(candidateHeuristics).heuristicTerms();
		final Heuristics[] parentHeuristics = tournamentSelection(candidateHeuristics);
		final HeuristicTerm[] parentA = parentHeuristics[0].heuristicTerms();
		final HeuristicTerm[] parentB = parentHeuristics[1].heuristicTerms();
		
		final HeuristicTerm[] parentBHalved = new HeuristicTerm[parentB.length];
		final HeuristicTerm[] parentBDoubled = new HeuristicTerm[parentB.length];
		
		for (int i = 0; i < parentBHalved.length; i++)
		{
			final HeuristicTerm halvedHeuristicTerm = parentB[i].copy();
			halvedHeuristicTerm.setWeight((float) (parentB[i].weight()/2.0));
			parentBHalved[i] = halvedHeuristicTerm;
		}
		for (int i = 0; i < parentBDoubled.length; i++)
		{
			final HeuristicTerm doubledHeuristicTerm = parentB[i].copy();
			doubledHeuristicTerm.setWeight((float) (parentB[i].weight()*2.0));
			parentBDoubled[i] = doubledHeuristicTerm;
		}
			
		final HeuristicTerm[] parentsCombined = new HeuristicTerm[parentA.length + parentB.length];
		System.arraycopy(parentA, 0, parentsCombined, 0, parentA.length);
        System.arraycopy(parentB, 0, parentsCombined, parentA.length, parentB.length);
        
        final HeuristicTerm[] parentsCombinedHalved = new HeuristicTerm[parentA.length + parentBHalved.length];
		System.arraycopy(parentA, 0, parentsCombinedHalved, 0, parentA.length);
        System.arraycopy(parentBHalved, 0, parentsCombinedHalved, parentA.length, parentBHalved.length);
        
        final HeuristicTerm[] parentsCombinedDoubled = new HeuristicTerm[parentA.length + parentBDoubled.length];
		System.arraycopy(parentA, 0, parentsCombinedDoubled, 0, parentA.length);
        System.arraycopy(parentBDoubled, 0, parentsCombinedDoubled, parentA.length, parentBDoubled.length);
        
        final Heuristics[] parentsCombinedHeuristics = new Heuristics[3];
        parentsCombinedHeuristics[0] = new Heuristics(parentsCombined);
        parentsCombinedHeuristics[1] = new Heuristics(parentsCombinedHalved);
        parentsCombinedHeuristics[2] = new Heuristics(parentsCombinedDoubled);
        
		return parentsCombinedHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Selects two random individuals from the set of candidates, with probability based on its win-rate.
	 */
	private static Heuristics[] tournamentSelection(final Map<Heuristics, Double> candidates)
	{
		final int k = candidates.keySet().size()/2 + 1;
		
		final Heuristics[] selectedCandidates = new Heuristics[2];
		
		if (candidates.keySet().size() < 2)
			System.out.println("ERROR. Must have at least two candidates.");
		
		final Set<Integer> selectedCandidateIndices = new HashSet<>();
		while (selectedCandidateIndices.size() < k)
		{
			final int randomNum = ThreadLocalRandom.current().nextInt(0, candidates.keySet().size() + 1);
			selectedCandidateIndices.add(randomNum);
		}
		
		Heuristics selectedCandidateA = null;
		double highestWinRate = -1.0;
		int counter = 0;
		for (final Map.Entry<Heuristics,Double> candidate : candidates.entrySet())
		{
			if (selectedCandidateIndices.contains(counter))
			{
				if (candidate.getValue() > highestWinRate)
				{
					selectedCandidateA = candidate.getKey();
					highestWinRate = candidate.getValue();
				}
			}
			counter++;
		}
		
		Heuristics selectedCandidateB = null;
		highestWinRate = -1.0;
		counter = 0;
		for (final Map.Entry<Heuristics,Double> candidate : candidates.entrySet())
		{
			if (selectedCandidateIndices.contains(counter))
			{
				if (candidate.getValue() > highestWinRate && !candidate.getKey().equals(selectedCandidateA))
				{
					selectedCandidateB = candidate.getKey();
					highestWinRate = candidate.getValue();
				}
			}
			counter++;
		}
		
		selectedCandidates[0] = selectedCandidateA;
		selectedCandidates[1] = selectedCandidateB;

		return selectedCandidates;
	}
	
	/**
	 * Selects a random individual from the set of candidates, with probability based on its win-rate.
	 */
//	private static Heuristics tournamentSelection(final Map<Heuristics, Double> candidates)
//	{
//		final double random = Math.random() * candidates.values().stream().mapToDouble(f -> f.doubleValue()).sum();
//		double acumulatedChance = 0.0;
//		
//		for (final Map.Entry<Heuristics,Double> candidate : candidates.entrySet())
//		{
//			acumulatedChance += candidate.getValue();
//	        if (acumulatedChance >= random) 
//	            return candidate.getKey();
//		}
//		
//		System.out.println("SHOULDN'T REACH HERE");
//		return null;
//	}

	//-------------------------------------------------------------------------

	private static Map<Heuristics, Double> evaluateCandidateHeuristics(final Game game, final Map<Heuristics, Double> candidateHeuristics) 
	{
		try
		{
			final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
			final List<HeuristicSampling> allAgents = initialAgents(allHeuristics);
			final List<TIntArrayList> allAgentIndexCombinations = allAgentIndexCombinations(game, allAgents);
			
			final List<Double> allAgentWinRates = new ArrayList<Double>(Collections.nCopies(allAgents.size(), 0.0));
			final int numEvaluationsPerAgent = allAgentIndexCombinations.size() * game.players().count() / allAgents.size(); 
			//binom(allAgents.size()-1, game.players().count()-1);
			
			System.out.println("number of pairups: " + allAgentIndexCombinations.size());
			System.out.println("number of agents: " + allAgents.size());
			System.out.println("number of pairups per agent: " + numEvaluationsPerAgent);
			
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
			
			for (int i = 0; i < allAgents.size(); i++)
			{
				final Heuristics agentHeuristics = allAgents.get(i).heuristics();
				final double averageWinRate = allAgentWinRates.get(i)/numEvaluationsPerAgent;
				candidateHeuristics.put(agentHeuristics, averageWinRate);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		return candidateHeuristics;
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
	
	private static List<HeuristicSampling> initialAgents(final List<Heuristics> heuristics)
	{
		final List<HeuristicSampling> allAgents = new ArrayList<>();
		
		for (final Heuristics h : heuristics)
			allAgents.add(new HeuristicSampling(h));
		
		return allAgents;
	}
	
	//-------------------------------------------------------------------------
	
//	private static Map<Heuristics, Double> sortCandidateHeuristics(final Map<Heuristics, Double> unsortedMap) 
//	{
//		final Map<Heuristics, Double> sortedMap = new HashMap<Heuristics, Double>();
//		unsortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
//		return sortedMap;
//	}
	
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
		test();
	}

	//-------------------------------------------------------------------------

}
