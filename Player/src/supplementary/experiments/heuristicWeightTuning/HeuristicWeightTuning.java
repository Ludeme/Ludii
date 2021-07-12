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
	final static double initialWinRateThreshold = 0.55;
	
	//-------------------------------------------------------------------------
	
	static class HeuristicStats
	{
		private double heuristicWinRateSum = 0.0;
		private int numComparisons = 0;
		
		public double heuristicWinRate()
		{
			return heuristicWinRateSum/numComparisons;
		}
		
		public void addHeuristicWinRate(final double winRate)
		{
			heuristicWinRateSum += winRate;
			numComparisons++;
		}
	}
	
	//-------------------------------------------------------------------------
	
	private static void test()
	{
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud");

		Map<Heuristics, HeuristicStats> candidateHeuristics = initialHeuristics(game);
		candidateHeuristics = intialCandidatePruning(game, candidateHeuristics, true);
		
		for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, candidateHeuristic.getKey());
		
		for (int i = 0; i < numGenerations; i++)
		{
			System.out.println("Generation " + i);
			candidateHeuristics = evolveCandidateHeuristics(game, candidateHeuristics);
		}
		
		for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
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
	private static Map<Heuristics, HeuristicStats> intialCandidatePruning(final Game game, final Map<Heuristics, HeuristicStats> originalCandidateHeuristics, final boolean againstNullHeuristic) 
	{
		Map<Heuristics, HeuristicStats> candidateHeuristics = originalCandidateHeuristics;
		
		if (againstNullHeuristic)
		{
			// Initial comparison against Null heuristic.
			for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
			{
				System.out.println(candidateHeuristic);
				final Map<Heuristics, HeuristicStats> agentList = new HashMap<>();
				agentList.put(new Heuristics(new NullHeuristic()), new HeuristicStats());
				agentList.put(candidateHeuristic.getKey(), candidateHeuristic.getValue());
				candidateHeuristics.put(candidateHeuristic.getKey(), evaluateCandidateHeuristicsAgainstEachOther(game, agentList, null).get(candidateHeuristic.getKey()));
			}
		}
		else
		{
			// Initial comparison against each other.
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, null);
		}
		
		// Remove any entries that have below % win-rate.
		candidateHeuristics.entrySet().removeIf(e -> e.getValue().heuristicWinRate() < initialWinRateThreshold);
		
		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Evolves the given set of candidate heuristics to create new candidate offspring.
	 */
	private static final Map<Heuristics, HeuristicStats> evolveCandidateHeuristics(final Game game, final Map<Heuristics, HeuristicStats> candidateHeuristics) 
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
			
			candidateHeuristics.put(newHeuristicA, new HeuristicStats());
			candidateHeuristics.put(newHeuristicB, new HeuristicStats());
			candidateHeuristics.put(newHeuristicC, new HeuristicStats());
			
			final double newHeuristicAWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newHeuristicA).get(newHeuristicA).heuristicWinRate();
			final double newHeuristicBWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newHeuristicB).get(newHeuristicB).heuristicWinRate();
			final double newHeuristicCWeight = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, newHeuristicC).get(newHeuristicC).heuristicWinRate();
			
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
	private static Heuristics[] tournamentSelection(final Map<Heuristics, HeuristicStats> candidates)
	{
		// selected parent candidates.
		final Heuristics[] selectedCandidates = new Heuristics[2];
		
		if (candidates.size() < 2)
			System.out.println("ERROR, candidates must be at least size 2.");
		
		// Select a set of k random candidates;
		final int k = Math.max((int) (candidates.keySet().size()/tournamentSelectionPercentage), 2);
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
		for (final Map.Entry<Heuristics, HeuristicStats> candidate : candidates.entrySet())
		{
			if (selectedCandidateIndices.contains(counter))
			{
				if (candidate.getValue().heuristicWinRate() > highestWinRate)
				{
					selectedCandidates[1] = Heuristics.copy(selectedCandidates[0]);
					secondHighestWinRate = highestWinRate;
					
					selectedCandidates[0] = Heuristics.copy(candidate.getKey());
					highestWinRate = candidate.getValue().heuristicWinRate();
				}
				else if (candidate.getValue().heuristicWinRate() > secondHighestWinRate)
				{
					selectedCandidates[1] = Heuristics.copy(candidate.getKey());
					secondHighestWinRate = candidate.getValue().heuristicWinRate();
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
	private static Map<Heuristics, HeuristicStats> evaluateCandidateHeuristicsAgainstEachOther(final Game game, final Map<Heuristics, HeuristicStats> candidateHeuristics, final Heuristics requiredHeuristic) 
	{
		final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
		final List<TIntArrayList> allIndexCombinations = allHeuristicIndexCombinations(game.players().count(), allHeuristics, requiredHeuristic, sampleSize);
		final List<HeuristicSampling> allAgents = createAgents(allHeuristics);
		
		System.out.println("number of pairups: " + allIndexCombinations.size());
		System.out.println("number of agents: " + allAgents.size());
		
		// Perform initial comparison across all agents/heuristics
		int counter = 1;
		for (final TIntArrayList agentIndices : allIndexCombinations)
		{
			System.out.println(counter + "/" + allIndexCombinations.size());
			counter++;
			
			final List<AI> agents = new ArrayList<>();
			for (final int i : agentIndices.toArray())
				agents.add(allAgents.get(i));
			
			final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
			for (int i = 0; i < agentMeanWinRates.size(); i++)
				candidateHeuristics.get(allHeuristics.get(agentIndices.get(i))).addHeuristicWinRate(agentMeanWinRates.get(i));
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
	private static List<TIntArrayList> allHeuristicIndexCombinations(final int numPlayers, final List<Heuristics> allHeuristics, final Heuristics requiredHeuristic, final int samepleSize)
	{		
		final int numHeuristics = allHeuristics.size();

		final TIntArrayList heuristicIndices = new TIntArrayList(numHeuristics);
		for (int i = 0; i < numHeuristics; ++i)
			heuristicIndices.add(i);
		
		List<TIntArrayList> allHeuristicIndexCombinations = new ArrayList<TIntArrayList>();
		FindBestBaseAgentScriptsGen.generateAllCombinations(heuristicIndices, numPlayers, 0, new int[numPlayers], allHeuristicIndexCombinations);
		
		if (requiredHeuristic != null)
		{
			final int requiredHeuristicIndex = allHeuristics.indexOf(requiredHeuristic);
			final List<TIntArrayList> allHeuristicIndexCombinationsNew = new ArrayList<TIntArrayList>();
			for (final TIntArrayList heuristicIndexCombination : allHeuristicIndexCombinations)
				if (heuristicIndexCombination.contains(requiredHeuristicIndex))
					allHeuristicIndexCombinationsNew.add(heuristicIndexCombination);
			allHeuristicIndexCombinations = allHeuristicIndexCombinationsNew;
		}
		
		if (samepleSize > 0 && samepleSize <= allHeuristicIndexCombinations.size())
		{
			Collections.shuffle(allHeuristicIndexCombinations);
			return allHeuristicIndexCombinations.subList(0, samepleSize);
		}	
		
		return allHeuristicIndexCombinations;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of all initial heuristics.
	 */
	private static Map<Heuristics, HeuristicStats> initialHeuristics(final Game game)
	{
		final Map<Heuristics, HeuristicStats> initialHeuristics = new HashMap<>();
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
		
		for (final HeuristicTerm h : heuristicTerms)
			initialHeuristics.put(new Heuristics(h), new HeuristicStats());
		
		return initialHeuristics;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of all initial HeuristicSampling agents, one for each provided heuristic.
	 */
	private static List<HeuristicSampling> createAgents(final List<Heuristics> heuristics)
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
