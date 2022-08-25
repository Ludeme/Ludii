package supplementary.experiments.heuristicWeightTuning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import main.collections.ListUtils;
import main.math.statistics.Stats;
import metadata.ai.heuristics.HeuristicUtil;
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
import metadata.ai.heuristics.terms.ThreatenedMaterial;
import metadata.ai.heuristics.terms.ThreatenedMaterialMultipleCount;
import metadata.ai.heuristics.terms.UnthreatenedMaterial;
import metadata.ai.misc.Pair;
import other.AI;
import other.GameLoader;
import search.flat.HeuristicSampling;
import supplementary.experiments.EvalGamesSet;

//-----------------------------------------------------------------------------

/**
 * Experiments to tune the weights of heuristics
 * 
 * @author matthew.stephenson and Dennis Soemers and cambolbro
 */
public class HeuristicWeightTuning
{
	// Percentage of population that is chosen for tournament selection.
	final static double tournamentSelectionPercentage = 10.0;
	
	// Number of generations before stopping.
	final static int numGenerations = 100;
	
	// Number of trials per agent comparison.
	final static int numTrialsPerComparison = 100;
	
	// Number of samples when evaluating an agent.
	final static int sampleSize = 100;
	
	// Thread executor (maximum number of threads possible)
	final static int numThreads = Runtime.getRuntime().availableProcessors();
	final static ExecutorService executor = Executors.newFixedThreadPool(numThreads);
	
	// Minimum win-rate against Null heuristic to survive initial pruning.
	final static double initialWinRateThreshold = 0.55;
	
	// Try removing heuristic terms which don't pass the improvement requirement.
	final static boolean tryHeuristicRemoval = true;
	final static double heuristicRemovalImprovementRquirement = -0.01;
	
	// Normalises all weights on heuristic between -1 and 1.
	final static boolean normaliseHeuristicWeights = true;
	
	// Simplifies heuristic weights by combining them.
	final static boolean simplifyHeuristicWeights = true;
	
	// Fraction value for heuristic sampling agents.
	final static int HeuristicSamplingAgentFraction = 4;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Heuristic statistics object.
	 */
	static class HeuristicStats implements Comparable<HeuristicStats>
	{
		private double heuristicWinRateSum = 0.0;
		private int numComparisons = 0;
		
		public Double heuristicWinRate()
		{
			return Double.valueOf(heuristicWinRateSum / numComparisons);
		}
		
		public void addHeuristicWinRate(final double winRate)
		{
			heuristicWinRateSum += winRate;
			numComparisons++;
		}

		@Override
		public int compareTo(final HeuristicStats arg0) 
		{
			return heuristicWinRate().compareTo(arg0.heuristicWinRate());
		}
	}
	
	//-------------------------------------------------------------------------
	
	private static void test()
	{
		//final Game game = GameLoader.loadGameFromName("Chess.lud");
		//final Game game = GameLoader.loadGameFromName("Tic-Tac-Mo.lud");
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud");
		//final Game game = GameLoader.loadGameFromName("Tablut.lud", Arrays.asList("Play Rules/King Flanked"));

		System.out.println("--PERFORMING INITIAL HEURISTIC PRUNING--\n");
		LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics = initialHeuristics(game);
		candidateHeuristics = intialCandidatePruning(game, candidateHeuristics, true);
		
		System.out.println("--DETERMINING INITIAL HEURISTIC WEIGHTS--\n");
		for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, candidateHeuristic.getKey());
		
		for (int i = 1; i <= numGenerations; i++)
		{
			System.out.println("\nGENERATION " + i + "\n");
			candidateHeuristics = evolveCandidateHeuristics(game, candidateHeuristics);
			
			// Store the current candidate heuristics to a text file after each generation.
			candidateHeuristics = sortCandidateHeuristics(candidateHeuristics);
			final File resultDirectory = new File("HWT_results");
			if (!resultDirectory.exists())
				resultDirectory.mkdirs();
			try (PrintWriter out = new PrintWriter(resultDirectory + "/results_" + game.name() + "_" + i + ".txt"))
			{
				for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
				{
					out.println("-------------------------------");
					out.println(candidateHeuristic.getKey());
					out.println(candidateHeuristic.getValue().heuristicWinRate());
					out.println("-------------------------------");
				}
			} 
			catch (final FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		
		// Determine the best heuristic after all generations are complete.
		final Heuristics bestHeuristicFound = candidateHeuristics.entrySet().iterator().next().getKey();
		System.out.println(bestHeuristicFound);
		
		// Compare best heuristic against the Null heuristic
		final List<Heuristics> heuristics = new ArrayList<>();
		heuristics.add(bestHeuristicFound);
		heuristics.add(new Heuristics(new NullHeuristic()));
		ArrayList<Double> agentMeanWinRates = compareHeuristics(game, heuristics);
		System.out.println("Performance against Null heuristic: " + agentMeanWinRates.get(0));
		
		// Compare the best heuristic against the default (metadata) HeuristicSampling agent.
		heuristics.clear();
		heuristics.add(bestHeuristicFound);
		heuristics.add(null);
		agentMeanWinRates = compareHeuristics(game, heuristics);
		System.out.println("Performance against default HeuristicSampling agent : " + agentMeanWinRates.get(0));

		bestHeuristicFound.toFile(game, "cameronMathew.heu");
		System.out.println("DONE!");
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
	private static LinkedHashMap<Heuristics, HeuristicStats> intialCandidatePruning(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> originalCandidateHeuristics, final boolean againstNullHeuristic) 
	{
		LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics = originalCandidateHeuristics;

		System.out.println("Num initial heuristics: " + candidateHeuristics.size());
		
		if (againstNullHeuristic)
		{
			// Initial comparison against Null heuristic.
			for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
			{
				System.out.println(candidateHeuristic.getKey());
				final LinkedHashMap<Heuristics, HeuristicStats> agentList = new LinkedHashMap<>();
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
		
		// Remove any entries that have below required win-rate.
		candidateHeuristics.entrySet().removeIf(e -> e.getValue().heuristicWinRate().doubleValue() < initialWinRateThreshold);
		
		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Evolves the given set of candidate heuristics to create new candidate offspring.
	 */
	private static final LinkedHashMap<Heuristics, HeuristicStats> evolveCandidateHeuristics(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics) 
	{
		final Heuristics[] parentHeuristics = tournamentSelection(candidateHeuristics);
		final HeuristicTerm[] parentA = parentHeuristics[0].heuristicTerms();
		final HeuristicTerm[] parentB = parentHeuristics[1].heuristicTerms();
		
		final List<LinkedHashMap<Heuristics, HeuristicStats>> allCandidateHeuristics = new ArrayList<>();
		final List<Heuristics> allHeuristics = new ArrayList<>();
		
		allHeuristics.add(combineHeuristicTerms(parentA, parentB));									// Regular
		allHeuristics.add(combineHeuristicTerms(parentA, HeuristicUtil.multiplyHeuristicTerms(parentB, 0.5)));	// Double
		allHeuristics.add(combineHeuristicTerms(parentA, HeuristicUtil.multiplyHeuristicTerms(parentB, 2.0)));	// Half
		
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(0)));
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(1)));
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(2)));
		
		// Record best candidate's results from evaluation
		LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristicsBest = null;
		Heuristics newHeuristicBest = null;
		double newHeuristicBestWeight = -1;
		for (int i = 0; i < allHeuristics.size(); i++)
		{
			final double heurisitcWinRate = allCandidateHeuristics.get(i).get(allHeuristics.get(i)).heuristicWinRate().doubleValue();
			if (heurisitcWinRate > newHeuristicBestWeight)
			{
				candidateHeuristicsBest = allCandidateHeuristics.get(i);
				newHeuristicBest = allHeuristics.get(i);
				newHeuristicBestWeight = heurisitcWinRate;
			}
		}
		
		// Remove any unnecessary heuristic terms from the best heuristic.
		if (tryHeuristicRemoval)
			candidateHeuristicsBest = tryRemovingHeuristicTerms(game, candidateHeuristics, candidateHeuristicsBest, newHeuristicBest, newHeuristicBestWeight);
			
		return candidateHeuristicsBest;
	}
	
	//-------------------------------------------------------------------------
	
	private static LinkedHashMap<Heuristics, HeuristicStats> tryRemovingHeuristicTerms(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristicsBestOri, final Heuristics newHeuristicBestOri, final double newHeuristicBestWeightOri)
	{
		LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristicsBest = candidateHeuristicsBestOri;
		Heuristics newHeuristicBest = newHeuristicBestOri;
		double newHeuristicBestWeight = newHeuristicBestWeightOri;
				
		boolean changeMade = true;
		while(changeMade)
		{
			changeMade = false;
			final int numHeuristicTerms = newHeuristicBest.heuristicTerms().length;
			for (int i = 0; i < numHeuristicTerms; i++)
			{
				final ArrayList<HeuristicTerm> heuristicsMinusOneTerm = new ArrayList<HeuristicTerm>();
				for (int j = 0; j < numHeuristicTerms; j++)
				{
					if (i == j)
						System.out.println("Evaluating without " + newHeuristicBest.heuristicTerms()[j]);
					else
						heuristicsMinusOneTerm.add(newHeuristicBest.heuristicTerms()[j]);
				}

				final Heuristics heuristicMinusOne = new Heuristics(heuristicsMinusOneTerm.toArray(new HeuristicTerm[0]));
				final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristicsMinusOneWeight = addAndEvaluateHeuristic(game, candidateHeuristics, heuristicMinusOne);
				final double newHeuristicMinusOneWeight = candidateHeuristicsMinusOneWeight.get(heuristicMinusOne).heuristicWinRate().doubleValue();
				
				if (newHeuristicMinusOneWeight > newHeuristicBestWeight + heuristicRemovalImprovementRquirement)
				{
					candidateHeuristicsBest = candidateHeuristicsMinusOneWeight;
					newHeuristicBest = heuristicMinusOne;
					newHeuristicBestWeight = newHeuristicMinusOneWeight;
					changeMade = true;
					break;
				}
			}
		}
		
		return candidateHeuristicsBest;
	}
	
	//-------------------------------------------------------------------------
	
	private static LinkedHashMap<Heuristics, HeuristicStats> addAndEvaluateHeuristic(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, final Heuristics heuristic) 
	{
		final LinkedHashMap<Heuristics, HeuristicStats> newcandidateHeuristics = copyCandidateHeuristics(candidateHeuristics);
		if (!newcandidateHeuristics.containsKey(heuristic))
			newcandidateHeuristics.put(heuristic, new HeuristicStats());
		return evaluateCandidateHeuristicsAgainstEachOther(game, newcandidateHeuristics, heuristic);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Copies an existing candidateHeuristics map.
	 */
	public static LinkedHashMap<Heuristics, HeuristicStats> copyCandidateHeuristics(final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics)
	{
		final LinkedHashMap<Heuristics, HeuristicStats> copy = new LinkedHashMap<>();
		for (final Map.Entry<Heuristics, HeuristicStats> entry : candidateHeuristics.entrySet())
	        copy.put(entry.getKey(), entry.getValue());
		return copy;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Combines two arrays of heuristicTerms together.
	 */
	private static Heuristics combineHeuristicTerms(final HeuristicTerm[] heuristicTermsA, final HeuristicTerm[] heuristicTermsB)
	{
		final ArrayList<HeuristicTerm> heuristicTermsCombined = new ArrayList<>(Arrays.asList(heuristicTermsA));
		for (final HeuristicTerm termB : heuristicTermsB)
		{
			boolean termAdded = false;
			
			for (int i = 0; i < heuristicTermsCombined.size(); i++)
			{
				final HeuristicTerm termA = heuristicTermsCombined.get(i);
				
				if (termA.canBeMerged(termB))
				{
					termA.merge(termB);
					heuristicTermsCombined.set(i, termA);
					termAdded = true;
					break;
				}
			}
			
			if (!termAdded)
				heuristicTermsCombined.add(termB);
		}
		
		Heuristics combinedHeuristic = new Heuristics(heuristicTermsCombined.toArray(new HeuristicTerm[0]));
		
		if (normaliseHeuristicWeights)
			combinedHeuristic = HeuristicUtil.normaliseHeuristic(combinedHeuristic);
		
		if (simplifyHeuristicWeights)
			for (final HeuristicTerm term : combinedHeuristic.heuristicTerms())
				term.simplify();

        return combinedHeuristic;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Selects two random individuals from the set of candidates, with probability based on its win-rate.
	 */
	private static Heuristics[] tournamentSelection(final LinkedHashMap<Heuristics, HeuristicStats> candidates)
	{
		// selected parent candidates.
		final Heuristics[] selectedCandidates = new Heuristics[2];
		
		if (candidates.size() < 2)
			System.out.println("ERROR, candidates must be at least size 2.");
		
		// Select a set of k random candidates;
		final int k = Math.max((int) Math.ceil(candidates.keySet().size()/tournamentSelectionPercentage), 2);
		final Set<Integer> selectedCandidateIndices = new HashSet<>();
		while (selectedCandidateIndices.size() < k)
		{
			final int randomNum = ThreadLocalRandom.current().nextInt(0, candidates.keySet().size());
			selectedCandidateIndices.add(Integer.valueOf(randomNum));
		}
			
		// Select the two best candidates from our random candidate set.
		double highestWinRate = -1.0;
		double secondHighestWinRate = -1.0;
		int counter = 0;
		for (final Map.Entry<Heuristics, HeuristicStats> candidate : candidates.entrySet())
		{
			if (selectedCandidateIndices.contains(Integer.valueOf(counter)))
			{
				if (candidate.getValue().heuristicWinRate().doubleValue() > highestWinRate)
				{
					selectedCandidates[1] = Heuristics.copy(selectedCandidates[0]);
					secondHighestWinRate = highestWinRate;
					
					selectedCandidates[0] = Heuristics.copy(candidate.getKey());
					highestWinRate = candidate.getValue().heuristicWinRate().doubleValue();
				}
				else if (candidate.getValue().heuristicWinRate().doubleValue() > secondHighestWinRate)
				{
					selectedCandidates[1] = Heuristics.copy(candidate.getKey());
					secondHighestWinRate = candidate.getValue().heuristicWinRate().doubleValue();
				} 
			}
			counter++;
		}

		return selectedCandidates;
	}
	
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

	//-------------------------------------------------------------------------

	/**
	 * Evaluates a set of heuristics against each other, updating their associated win-rates.
	 */
	private static LinkedHashMap<Heuristics, HeuristicStats> evaluateCandidateHeuristicsAgainstEachOther(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, final Heuristics requiredHeuristic) 
	{
		final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
		final List<TIntArrayList> allIndexCombinations = allHeuristicIndexCombinations(game.players().count(), allHeuristics, requiredHeuristic, sampleSize);
		
		System.out.println("number of pairups: " + allIndexCombinations.size());
		System.out.println("number of agents: " + allHeuristics.size());

		try
		{
			// Run trials concurrently
			final CountDownLatch latch = new CountDownLatch(allIndexCombinations.size());
			
			for (final TIntArrayList agentIndices : allIndexCombinations)
			{
				executor.submit
				(
					() -> 
					{
						final List<Heuristics> selectedHeuristiscs = new ArrayList<>();
						for (final int i : agentIndices.toArray())
							selectedHeuristiscs.add(Heuristics.copy(allHeuristics.get(i)));
							
						final ArrayList<Double> agentMeanWinRates = compareHeuristics(game, selectedHeuristiscs);
						for (int i = 0; i < agentMeanWinRates.size(); i++)
							candidateHeuristics.get(allHeuristics.get(agentIndices.get(i))).addHeuristicWinRate(agentMeanWinRates.get(i).doubleValue());

						System.out.print(".");
						
						latch.countDown();					
					}
				);
			}
			
			latch.await();  // wait for all trials to finish
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}	
		
		System.out.println("\n");
		
		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Compares a set of agents on a given game.
	 */
	private static ArrayList<Double> compareHeuristics(final Game game, final List<Heuristics> heuristics)
	{
		final ArrayList<Double> agentMeanWinRates = new ArrayList<>();

		final List<AI> agents = new ArrayList<>();
		for (final Heuristics h : heuristics)
		{
			if (h == null)
				agents.add(new HeuristicSampling(HeuristicSamplingAgentFraction));
			else
				agents.add(new HeuristicSampling(h, HeuristicSamplingAgentFraction));
		}

		final EvalGamesSet gamesSet = 
				new EvalGamesSet()
				.setGameName(game.name() + ".lud")
				.setAgents(agents)
				.setWarmingUpSecs(0)
				.setNumGames(numTrialsPerComparison)
				.setPrintOut(false)
				.setRoundToNextPermutationsDivisor(true)
				.setRotateAgents(true);
		
		gamesSet.startGames(game);
		
		for (final Stats agentStats : gamesSet.resultsSummary().agentPoints())
		{
			agentStats.measure();
			agentMeanWinRates.add(Double.valueOf(agentStats.mean()));
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
		ListUtils.generateAllCombinations(heuristicIndices, numPlayers, 0, new int[numPlayers], allHeuristicIndexCombinations);
		
		// Only select heuristic combinations that includes our required heuristic. Also remove combinations with duplicates to prevent potential issues.
		if (requiredHeuristic != null)
		{
			final int requiredHeuristicIndex = allHeuristics.indexOf(requiredHeuristic);
			final List<TIntArrayList> allHeuristicIndexCombinationsNew = new ArrayList<TIntArrayList>();
			for (final TIntArrayList heuristicIndexCombination : allHeuristicIndexCombinations)
				if (heuristicIndexCombination.contains(requiredHeuristicIndex) && !containsDuplicates(heuristicIndexCombination))
					allHeuristicIndexCombinationsNew.add(heuristicIndexCombination);
			allHeuristicIndexCombinations = allHeuristicIndexCombinationsNew;
		}
		
		// Select a random number of combinations based on our desired sample size.
		if (samepleSize > 0 && samepleSize <= allHeuristicIndexCombinations.size())
		{
			Collections.shuffle(allHeuristicIndexCombinations);
			return allHeuristicIndexCombinations.subList(0, samepleSize);
		}	
		
		return allHeuristicIndexCombinations;
	}
	
	/**
	 * @return True if duplicate values are present in list
	 */
	private static boolean containsDuplicates(final TIntArrayList list)
	{
		final TIntHashSet set = new TIntHashSet();
		for (int i = 0; i < list.size(); i++)
		{
			if (!set.add(list.getQuick(i)))
				return true;
		}
		return false;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of all initial heuristics.
	 */
	private static LinkedHashMap<Heuristics, HeuristicStats> initialHeuristics(final Game game)
	{
		final LinkedHashMap<Heuristics, HeuristicStats> initialHeuristics = new LinkedHashMap<>();
		final List<HeuristicTerm> heuristicTerms = new ArrayList<>();
		
		// All possible initial component pair combinations.
		final List<Pair[]> allComponentPairsCombinations = new ArrayList<>();
		for (int i = 0; i < game.equipment().components().length-1; i++)
		{
			final Pair[] componentPairs  = new Pair[game.equipment().components().length-1];
			for (int j = 0; j < game.equipment().components().length-1; j++)
			{
				if (j == i)
					componentPairs[j] = new Pair(game.equipment().components()[j+1].name(), Float.valueOf(1));
				else
					componentPairs[j] = new Pair(game.equipment().components()[j+1].name(), Float.valueOf(0));
			}
			allComponentPairsCombinations.add(componentPairs);
		}
		
		for (float weight = -1f; weight < 2; weight+=2)
		{
			if (LineCompletionHeuristic.isApplicableToGame(game))
				heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(weight), null));
			
			if (MobilitySimple.isApplicableToGame(game))
				heuristicTerms.add(new MobilitySimple(null, Float.valueOf(weight)));
			
			if (Influence.isApplicableToGame(game))
				heuristicTerms.add(new Influence(null, Float.valueOf(weight)));
			
			if (OwnRegionsCount.isApplicableToGame(game))
				heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(weight)));
			
			if (PlayerSiteMapCount.isApplicableToGame(game))
				heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(weight)));
			
			if (Score.isApplicableToGame(game))
				heuristicTerms.add(new Score(null, Float.valueOf(weight)));
			
			if (CentreProximity.isApplicableToGame(game))
			{
				heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
			}
			
			if (ComponentValues.isApplicableToGame(game))
			{
				heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), null, null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), componentPairs, null));
			}
				
			if (CornerProximity.isApplicableToGame(game))
			{
				heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), componentPairs));
			}
		
			if (Material.isApplicableToGame(game))
			{
				heuristicTerms.add(new Material(null, Float.valueOf(weight), null, null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new Material(null, Float.valueOf(weight), componentPairs, null));
			}
		
			if (SidesProximity.isApplicableToGame(game))
			{
				heuristicTerms.add(new SidesProximity(null, Float.valueOf(weight), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
			}
			
			if (PlayerRegionsProximity.isApplicableToGame(game))
			{
				for (int p = 1; p <= game.players().count(); ++p)
				{
					heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), null));
					for (final Pair[] componentPairs : allComponentPairsCombinations)
						heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), componentPairs));
				}
			}
			
			if (RegionProximity.isApplicableToGame(game))
			{
				for (int i = 0; i < game.equipment().regions().length; ++i)
				{
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), null));
					for (final Pair[] componentPairs : allComponentPairsCombinations)
						heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), componentPairs));
				}
			}
			
			if (UnthreatenedMaterial.isApplicableToGame(game)) {
				for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new UnthreatenedMaterial(null, Float.valueOf(weight), componentPairs));
			}
			if (ThreatenedMaterial.isApplicableToGame(game)) {
				for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new ThreatenedMaterial(null, Float.valueOf(weight), componentPairs));
			}
			
			if (ThreatenedMaterialMultipleCount.isApplicableToGame(game)) {
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new ThreatenedMaterialMultipleCount(null, Float.valueOf(weight), componentPairs));
			}
		}
				
		for (final HeuristicTerm h : heuristicTerms)
			initialHeuristics.put(new Heuristics(h), new HeuristicStats());
		
		return initialHeuristics;
	}
	
	//-------------------------------------------------------------------------
	
	private static LinkedHashMap<Heuristics, HeuristicStats> sortCandidateHeuristics(final LinkedHashMap<Heuristics, HeuristicStats> unsortedMap) 
	{
		final LinkedHashMap<Heuristics, HeuristicStats> sortedMap = new LinkedHashMap<>();
		unsortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		return sortedMap;
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		test();
	}
	
	//-------------------------------------------------------------------------

}
