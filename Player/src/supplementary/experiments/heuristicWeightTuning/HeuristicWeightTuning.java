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
	final static int numGenerations = 100;
	final static int numCrossoversEachGeneration = 100;
	final static double tournamentSelectionPercentage = 10.0;
	final static int sampleSize = 10;
	
	//-------------------------------------------------------------------------
	
	private static void test()
	{
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud");

		Map<Heuristics, Double> candidateHeuristics = new HashMap<Heuristics, Double>();
		final List<HeuristicTerm> heuristicTerms = initialHeuristicTerms(game);
		for (final HeuristicTerm h : heuristicTerms)
			candidateHeuristics.put(new Heuristics(h), -1.0);


		candidateHeuristics = intialCandidatePruning(game, candidateHeuristics, true);
		candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, null);
		
		for (int i = 0; i < numGenerations; i++)
		{
			System.out.println("Generation " + i);
			
			// Only compare and update the value of the new heuristic.
			candidateHeuristics = evolveCandidateHeuristics(game, candidateHeuristics);
			
			// Only compare and update the value of the new heuristic.
//			final List<Heuristics> newCandidates = evolveCandidateHeuristics(game, candidateHeuristics);
//			for (final Heuristics newCandidate : newCandidates)
//			{
//				final double newCandidateWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newCandidate).get(newCandidate);
//				candidateHeuristics.put(newCandidate, newCandidateWeight);
//			}

			// Update the values of all heuristics again.
//				final List<Heuristics> newCandidates = evolveCandidateHeuristics(candidateHeuristics);
//				for (final Heuristics newCandidate : newCandidates)
//					candidateHeuristics.put(newCandidate, -1.0);
//				candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics);
		}
		
		for (final Map.Entry<Heuristics,Double> candidateHeuristic : candidateHeuristics.entrySet())
		{
			System.out.println("-------------------------------");
			System.out.println(candidateHeuristic.getKey());
			System.out.println(candidateHeuristic.getValue());
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
			for (final Map.Entry<Heuristics,Double> candidateHeuristic : candidateHeuristics.entrySet())
			{
				System.out.println(candidateHeuristic);
				final Map<Heuristics, Double> agentList = new HashMap<>();
				agentList.put(new Heuristics(new NullHeuristic()), -1.0);
				agentList.put(candidateHeuristic.getKey(), candidateHeuristic.getValue());
				candidateHeuristics.put(candidateHeuristic.getKey(), evaluateCandidateHeuristicsAgainstEachOther(game, agentList, null).get(candidateHeuristic.getKey()));
			}
		}
		else
		{
			// Initial comparison against each other.
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, null);
		}
		
		// Remove any entries that have below 50% win-rate.
		candidateHeuristics.entrySet().removeIf(e -> e.getValue() < 0.55);
		
		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Evolves the given set of candidate heuristics to create new candidate offspring.
	 */
	private static final Map<Heuristics, Double> evolveCandidateHeuristics(final Game game, final Map<Heuristics, Double> candidateHeuristics) 
	{
		for (int i = 0; i < numCrossoversEachGeneration; i++)
		{
			final Heuristics[] parentHeuristics = tournamentSelection(candidateHeuristics);
			final HeuristicTerm[] parentA = parentHeuristics[0].heuristicTerms();
			final HeuristicTerm[] parentB = parentHeuristics[1].heuristicTerms();
			
			final HeuristicTerm[] parentBHalved = multiplyHeuristicTerms(parentB, 0.5);
			final HeuristicTerm[] parentBDoubled = multiplyHeuristicTerms(parentB, 2.0);
				
			final Heuristics newHeuristicA = new Heuristics(combineHeuristicTerms(parentA, parentB));
			final Heuristics newHeuristicB = new Heuristics(combineHeuristicTerms(parentA, parentBHalved));
			final Heuristics newHeuristicC = new Heuristics(combineHeuristicTerms(parentA, parentBDoubled));
			
			candidateHeuristics.put(newHeuristicA, -1.0);
			candidateHeuristics.put(newHeuristicB, -1.0);
			candidateHeuristics.put(newHeuristicC, -1.0);
			
			final double newHeuristicAWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newHeuristicA).get(newHeuristicA);
			final double newHeuristicBWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newHeuristicB).get(newHeuristicB);
			final double newHeuristicCWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newHeuristicC).get(newHeuristicC);
			
			if (newHeuristicAWeight >= newHeuristicBWeight && newHeuristicAWeight >= newHeuristicCWeight)
			{
				candidateHeuristics.remove(newHeuristicB);
				candidateHeuristics.remove(newHeuristicC);
			}
			else if (newHeuristicBWeight >= newHeuristicAWeight && newHeuristicBWeight >= newHeuristicCWeight)
			{
				candidateHeuristics.remove(newHeuristicA);
				candidateHeuristics.remove(newHeuristicC);
			}
			else
			{
				candidateHeuristics.remove(newHeuristicA);
				candidateHeuristics.remove(newHeuristicB);
			}
		}
        
		return candidateHeuristics;
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
		
		if (candidates.size() < 2)
			System.out.println("ERROR, candidates must be at least size 2.");
		
		// Select a set of k random candidates;
		final int k = (int) (candidates.keySet().size()/tournamentSelectionPercentage);
		final Set<Integer> selectedCandidateIndices = new HashSet<>();
		while (selectedCandidateIndices.size() < k)
		{
			final int randomNum = ThreadLocalRandom.current().nextInt(0, candidates.keySet().size() + 1);
			selectedCandidateIndices.add(randomNum);
		}
		
		// Check that there at least two possible candidates
		if (k < 2)
			System.out.println("ERROR, k must be at least 2.");
			
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
	private static Map<Heuristics, Double> evaluateCandidateHeuristicsAgainstEachOther(final Game game, final Map<Heuristics, Double> candidateHeuristics, final Heuristics requiredHeuristic) 
	{

		final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
		final List<HeuristicSampling> allAgents = initialAgents(allHeuristics);
		final List<TIntArrayList> allAgentIndexCombinations = allAgentIndexCombinations(game.players().count(), allAgents, sampleSize);
		
		final List<Double> allAgentWinRates = new ArrayList<Double>(Collections.nCopies(allAgents.size(), 0.0));
		final int numEvaluationsPerAgent = allAgentIndexCombinations.size() * game.players().count() / allAgents.size(); 
		//binom(allAgents.size()-1, game.players().count()-1);
		
		int requiredHeuristicIndex = -1;
		if (requiredHeuristic != null)
			requiredHeuristicIndex = allHeuristics.indexOf(requiredHeuristic);
		
		System.out.println("number of pairups: " + allAgentIndexCombinations.size());
		System.out.println("number of agents: " + allAgents.size());
		System.out.println("number of pairups per agent: " + numEvaluationsPerAgent);
		
		// Perform initial comparison across all agents/heuristics
		int counter = 1;
		for (final TIntArrayList agentIndices : allAgentIndexCombinations)
		{
			if (requiredHeuristicIndex == -1 || agentIndices.contains(requiredHeuristicIndex))
			{
				System.out.println(counter + "/" + allAgentIndexCombinations.size());
				counter++;
				
				final List<AI> agents = new ArrayList<>();
				for (final int i : agentIndices.toArray())
					agents.add(allAgents.get(i));
				
				final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
				for (int i = 0; i < agentMeanWinRates.size(); i++)
					if (requiredHeuristicIndex == -1 || requiredHeuristicIndex == i)
						allAgentWinRates.set(agentIndices.get(i), allAgentWinRates.get(agentIndices.get(i)) + agentMeanWinRates.get(i));
			}
		}
		
		for (int i = 0; i < allAgents.size(); i++)
		{
			final Heuristics agentHeuristics = allAgents.get(i).heuristics();
			final double averageWinRate = allAgentWinRates.get(i)/numEvaluationsPerAgent;
			candidateHeuristics.put(agentHeuristics, averageWinRate);
		}

		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------

	/**
	 * Compares a set of agents on a given game.
	 */
	private static ArrayList<Double> compareAgents(final Game game, final List<AI> agents)
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
	private static List<TIntArrayList> allAgentIndexCombinations(final int numPlayers, final List<HeuristicSampling> allAgents, final int samepleSize)
	{		
		final int numAgents = allAgents.size();
		
		final TIntArrayList agentIndices = new TIntArrayList(numAgents);
		for (int i = 0; i < numAgents; ++i)
			agentIndices.add(i);
		
		final List<TIntArrayList> allAgentIndexCombinations = new ArrayList<TIntArrayList>();
		FindBestBaseAgentScriptsGen.generateAllCombinations(agentIndices, numPlayers, 0, new int[numPlayers], allAgentIndexCombinations);
		
		if (samepleSize > 0 && samepleSize <= allAgentIndexCombinations.size())
		{
			final List<TIntArrayList> allAgentIndexCombinationsSampled = new ArrayList<TIntArrayList>();
			Collections.shuffle(allAgentIndexCombinations);
			for (int i = 0; i < samepleSize; i++)
				allAgentIndexCombinationsSampled.add(allAgentIndexCombinations.get(i));
			return allAgentIndexCombinationsSampled;
		}	
		
		return allAgentIndexCombinations;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of all initial heuristics.
	 */
	private static List<HeuristicTerm> initialHeuristicTerms(final Game game)
	{
		final List<HeuristicTerm> heuristicTerms = new ArrayList<>();
		
		// All possible initial component pair combinations.
		final List<Pair[]> allComponentPairsCombinations = new ArrayList<>();
		for (int i = 0; i < game.equipment().components().length; i++)
		{
			final Pair[] componentPairs  = new Pair[game.equipment().components().length];
			for (int j = 0; j < game.equipment().components().length; j++)
			{
				if (j == i)
					componentPairs[j] = new Pair(game.equipment().components()[j].name(), 1f);
				else
					componentPairs[j] = new Pair(game.equipment().components()[j].name(), 0f);
			}
			allComponentPairsCombinations.add(componentPairs);
		}
		
		for (float weight = -1f; weight < 2; weight+=2)
		{
			heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(weight), null));
			heuristicTerms.add(new MobilitySimple(null, Float.valueOf(weight)));
			heuristicTerms.add(new Influence(null, Float.valueOf(weight)));
			heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(weight)));
			heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(weight)));
			heuristicTerms.add(new Score(null, Float.valueOf(weight)));
			
			heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
			
			heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), null, null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), componentPairs, null));
			
			heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), componentPairs));
		
			heuristicTerms.add(new Material(null, Float.valueOf(weight), null, null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new Material(null, Float.valueOf(weight), componentPairs, null));
		
			heuristicTerms.add(new SidesProximity(null, Float.valueOf(weight), null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
			
			for (int p = 1; p <= game.players().count(); ++p)
			{
				heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), componentPairs));
			}
			
			for (int i = 0; i < game.equipment().regions().length; ++i)
			{
				heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), componentPairs));
			}
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
