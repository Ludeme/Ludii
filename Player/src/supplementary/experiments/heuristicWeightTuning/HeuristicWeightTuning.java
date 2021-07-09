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
import metadata.ai.heuristics.terms.NullHeuristic;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import metadata.ai.misc.Pair;
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
	final static int numGenerations = 3;
	final static int numCrossoversEachGeneration = 3;
	
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
			candidateHeuristics = intialCandidatePruning(game, candidateHeuristics, false);
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics);
			
			for (int i = 0; i < numGenerations; i++)
			{
				System.out.println("Generation " + i);
				
				// Only compare and update the value of the new heuristic.
//				final List<Heuristics> newCandidates = evolveCandidateHeuristics(candidateHeuristics);
//				for (final Heuristics newCandidate : newCandidates)
//				{
//					final double newCandidateWeight = evaluateCandidateHeuristicAgaintOthers(game, newCandidate, new ArrayList<>(candidateHeuristics.keySet()));
//					candidateHeuristics.put(newCandidate, newCandidateWeight);
//				}

				// Update the values of all heuristics again.
				final List<Heuristics> newCandidates = evolveCandidateHeuristics(candidateHeuristics);
				for (final Heuristics newCandidate : newCandidates)
					candidateHeuristics.put(newCandidate, -1.0);
				candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics);
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
	 * Prunes the initial set of all candidate heuristics
	 * 
	 * @param game
	 * @param originalCandidateHeuristics		Set of all initial heuristics.
	 * @param againstNullHeuristic				If the comparison should be done against the Null heuristic rather than each other.
	 * @return
	 */
	private static Map<Heuristics, Double> intialCandidatePruning(final Game game, final Map<Heuristics, Double> originalCandidateHeuristics, final boolean againstNullHeuristic) 
	{
		Map<Heuristics, Double> candidateHeuristics = originalCandidateHeuristics;
		
		if (againstNullHeuristic)
		{
			// Initial comparison against Null heuristic.
			final List<Heuristics> opponentAgentList = new ArrayList<>();
			opponentAgentList.add(new Heuristics(new NullHeuristic()));
			for (final Map.Entry<Heuristics,Double> candidateHeuristic : candidateHeuristics.entrySet())
			{
				System.out.println(candidateHeuristic);
				candidateHeuristics.put(candidateHeuristic.getKey(), evaluateCandidateHeuristicAgaintOthers(game, candidateHeuristic.getKey(), opponentAgentList));
			}
		}
		else
		{
			// Initial comparison against each other.
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics);
		}
		
		// Remove any entries that have below 50% win-rate.
		candidateHeuristics.entrySet().removeIf(e -> e.getValue() < 0.5);
		
		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Evolves the given set of candidate heuristics to create new candidate offspring.
	 */
	private static List<Heuristics> evolveCandidateHeuristics(final Map<Heuristics, Double> candidateHeuristics) 
	{
		final List<Heuristics> parentsCombinedHeuristics = new ArrayList<>();
		
		for (int i = 0; i < numCrossoversEachGeneration; i++)
		{
			final Heuristics[] parentHeuristics = tournamentSelection(candidateHeuristics);
			final HeuristicTerm[] parentA = parentHeuristics[0].heuristicTerms();
			final HeuristicTerm[] parentB = parentHeuristics[1].heuristicTerms();
			
			final HeuristicTerm[] parentBHalved = multiplyHeuristicTerms(parentB, 0.5);
			final HeuristicTerm[] parentBDoubled = multiplyHeuristicTerms(parentB, 2.0);
				
			parentsCombinedHeuristics.add(new Heuristics(combineHeuristicTerms(parentA, parentB)));
			parentsCombinedHeuristics.add(new Heuristics(combineHeuristicTerms(parentA, parentBHalved)));
			parentsCombinedHeuristics.add(new Heuristics(combineHeuristicTerms(parentA, parentBDoubled)));
		}
        
		return parentsCombinedHeuristics;
	}
	
	/**
	 * Multiplies the weights on an array of heuristicTerms by the specified multiplier.
	 */
	private static HeuristicTerm[] multiplyHeuristicTerms(final HeuristicTerm[] heuristicTerms, final double multiplier)
	{
		final HeuristicTerm[] heuristicTermsMultiplied = new HeuristicTerm[heuristicTerms.length];
		for (int i = 0; i < heuristicTermsMultiplied.length; i++)
		{
			final HeuristicTerm halvedHeuristicTerm = heuristicTerms[i].copy();
			halvedHeuristicTerm.setWeight((float) (heuristicTerms[i].weight()*multiplier));
			heuristicTermsMultiplied[i] = halvedHeuristicTerm;
		}
		return heuristicTermsMultiplied;
	}
	
	/**
	 * Combines two arrays of heuristicTerms together.
	 */
	private static HeuristicTerm[] combineHeuristicTerms(final HeuristicTerm[] heuristicTermsA, final HeuristicTerm[] heuristicTermsB)
	{
		final HeuristicTerm[] heuristicTermsCombined = new HeuristicTerm[heuristicTermsA.length + heuristicTermsB.length];
		System.arraycopy(heuristicTermsA, 0, heuristicTermsCombined, 0, heuristicTermsA.length);
        System.arraycopy(heuristicTermsB, 0, heuristicTermsCombined, heuristicTermsA.length, heuristicTermsB.length);
        return heuristicTermsCombined;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Selects two random individuals from the set of candidates, with probability based on its win-rate.
	 */
	private static Heuristics[] tournamentSelection(final Map<Heuristics, Double> candidates)
	{
		// selected parent candidates.
		final Heuristics[] selectedCandidates = new Heuristics[2];
		
		// Select a set of k random candidates;
		final int k = candidates.keySet().size()/2 + 1;
		final Set<Integer> selectedCandidateIndices = new HashSet<>();
		while (selectedCandidateIndices.size() < k)
		{
			final int randomNum = ThreadLocalRandom.current().nextInt(0, candidates.keySet().size() + 1);
			selectedCandidateIndices.add(randomNum);
		}
		
		// Select the two best candidates from our random candidate set.
		double highestWinRate = -1.0;
		double secondHighestWinRate = -1.0;
		int counter = 0;
		for (final Map.Entry<Heuristics,Double> candidate : candidates.entrySet())
		{
			if (selectedCandidateIndices.contains(counter))
			{
				if (candidate.getValue() > highestWinRate)
				{
					selectedCandidates[0] = candidate.getKey();
					highestWinRate = candidate.getValue();
				}
				else if (candidate.getValue() > secondHighestWinRate)
				{
					selectedCandidates[1] = candidate.getKey();
					secondHighestWinRate = candidate.getValue();
				} 
			}
			counter++;
		}

		return selectedCandidates;
	}

	//-------------------------------------------------------------------------

	/**
	 * Evaluates a set of heuristics against each other, updating their associated win-rates.
	 */
	private static Map<Heuristics, Double> evaluateCandidateHeuristicsAgainstEachOther(final Game game, final Map<Heuristics, Double> candidateHeuristics) 
	{
		try
		{
			final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
			final List<HeuristicSampling> allAgents = initialAgents(allHeuristics);
			final List<TIntArrayList> allAgentIndexCombinations = allAgentIndexCombinations(game.players().count(), allAgents);
			
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
	 * Evaluates a given heuristic against a set of opponentHeuristics. Returns the average win-rate of the given heuristic.
	 */
	private static Double evaluateCandidateHeuristicAgaintOthers(final Game game, final Heuristics candidateHeuristic, final List<Heuristics> opponentHeuristics) 
	{
		final List<HeuristicSampling> allOpponentAgents = initialAgents(opponentHeuristics);
		final List<TIntArrayList> allAgentIndexCombinations = allAgentIndexCombinations(game.players().count()-1, allOpponentAgents);
		
		double averageWinRate = 0.0;
		final int numEvaluations = allAgentIndexCombinations.size() * game.players().count();
		
		System.out.println("number of pairups: " + allAgentIndexCombinations.size());
		System.out.println("number of agents: " + allOpponentAgents.size());
		
		try
		{
			for (int playerIndex = 0; playerIndex < game.players().count(); playerIndex++)
			{
				for (final TIntArrayList agentIndices : allAgentIndexCombinations)
				{
					final List<AI> agents = new ArrayList<>();
					for (final int i : agentIndices.toArray())
						agents.add(allOpponentAgents.get(i));
					agents.add(playerIndex, new HeuristicSampling(candidateHeuristic));
					
					final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
					averageWinRate += agentMeanWinRates.get(playerIndex);
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
			
		return averageWinRate/numEvaluations;
	}

	//-------------------------------------------------------------------------

	/**
	 * Compares a set of agents on a given game.
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
	
	/**
	 * Provides a list of TIntArrayList, describing all combinations of agent indices from allAgents.
	 */
	private static List<TIntArrayList> allAgentIndexCombinations(final int numPlayers, final List<HeuristicSampling> allAgents)
	{		
		final int numAgents = allAgents.size();
		
		final TIntArrayList agentIndices = new TIntArrayList(numAgents);
		for (int i = 0; i < numAgents; ++i)
			agentIndices.add(i);
		
		final List<TIntArrayList> allAgentIndexCombinations = new ArrayList<TIntArrayList>();
		FindBestBaseAgentScriptsGen.generateAllCombinations(agentIndices, numPlayers, 0, new int[numPlayers], allAgentIndexCombinations);
		
		return allAgentIndexCombinations;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of all initial heuristics.
	 */
	private static List<HeuristicTerm> initialHeuristicTerms(final Game game)
	{
		final List<HeuristicTerm> heuristicTerms = new ArrayList<>();
		
		
		heuristicTerms.add(new CentreProximity(null, Float.valueOf(1.f), null));
		heuristicTerms.add(new CentreProximity(null, Float.valueOf(-1.f), null));
		for (int i = 0; i < game.equipment().components().length; i++)
		{
			final Pair[] componentPairs  = new Pair[game.equipment().components().length];
			componentPairs[i] = new Pair(game.equipment().components()[i].name(), 1f);
			heuristicTerms.add(new CentreProximity(null, Float.valueOf(1.f), componentPairs));
		}
		

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
	
	/**
	 * Provides a list of all initial HeuristicSampling agents, one for each provided heuristic.
	 */
	private static List<HeuristicSampling> initialAgents(final List<Heuristics> heuristics)
	{
		final List<HeuristicSampling> allAgents = new ArrayList<>();
		
		for (final Heuristics h : heuristics)
			allAgents.add(new HeuristicSampling(h));
		
		return allAgents;
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		test();
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

}
