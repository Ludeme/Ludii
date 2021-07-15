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
	// Percentage of population that is chosen for tournament selection.
	final static double tournamentSelectionPercentage = 10.0;
	
	// Number of generations before stopping.
	final static int numGenerations = 100;
	
	// Number of trials per agent comparison.
	final static int numTrialsPerComparison = 20;
	
	// Number of samples when evaluating an agent.
	final static int sampleSize = 50;
	
	// Minimum win-rate against Null heuristic to surivive initial pruning.
	final static double initialWinRateThreshold = 0.55;
	
	//-------------------------------------------------------------------------
	
	static class HeuristicStats implements Comparable<HeuristicStats>
	{
		private double heuristicWinRateSum = 0.0;
		private int numComparisons = 0;
		
		public Double heuristicWinRate()
		{
			return heuristicWinRateSum/numComparisons;
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
		//final Game game = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");
		//final Game game = GameLoader.loadGameFromName("Tic-Tac-Mo.lud");
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud");
		final Game game = GameLoader.loadGameFromName("Halma.lud", Arrays.asList("Board Size/6x6"));

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
					for (final HeuristicTerm term : candidateHeuristic.getKey().heuristicTerms())
						term.simplify();
					
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
		
		// Print win-rate against null for the best heuristic found.
		candidateHeuristics = sortCandidateHeuristics(candidateHeuristics);
		for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
		{
			for (final HeuristicTerm term : candidateHeuristic.getKey().heuristicTerms())
				term.simplify();
			
			System.out.println(candidateHeuristic.getKey());
			
			final List<AI> agents = new ArrayList<>();
			agents.add(new HeuristicSampling(candidateHeuristic.getKey(), 4));
			agents.add(new HeuristicSampling());
			final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
			System.out.println(agentMeanWinRates.get(0));
			
			break;
		}

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
		candidateHeuristics.entrySet().removeIf(e -> e.getValue().heuristicWinRate() < initialWinRateThreshold);
		
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
		
		final HeuristicTerm[] parentBHalved = multiplyHeuristicTerms(parentB, 0.5);
		final HeuristicTerm[] parentBDoubled = multiplyHeuristicTerms(parentB, 2.0);
			
		final Heuristics newHeuristicA = new Heuristics(combineHeuristicTerms(parentA, parentB));
		final Heuristics newHeuristicB = new Heuristics(combineHeuristicTerms(parentA, parentBHalved));
		final Heuristics newHeuristicC = new Heuristics(combineHeuristicTerms(parentA, parentBDoubled));
		
		final LinkedHashMap<Heuristics, HeuristicStats> newcandidateHeuristicsA = copyCandidateHeuristics(candidateHeuristics);
		final LinkedHashMap<Heuristics, HeuristicStats> newcandidateHeuristicsB = copyCandidateHeuristics(candidateHeuristics);
		final LinkedHashMap<Heuristics, HeuristicStats> newcandidateHeuristicsC = copyCandidateHeuristics(candidateHeuristics);
		
		if (!newcandidateHeuristicsA.containsKey(newHeuristicA))
			newcandidateHeuristicsA.put(newHeuristicA, new HeuristicStats());
		if (!newcandidateHeuristicsB.containsKey(newHeuristicB))
			newcandidateHeuristicsB.put(newHeuristicB, new HeuristicStats());
		if (!newcandidateHeuristicsC.containsKey(newHeuristicC))
			newcandidateHeuristicsC.put(newHeuristicC, new HeuristicStats());
		
		final double newHeuristicAWeight = evaluateCandidateHeuristicsAgainstEachOther(game, newcandidateHeuristicsA, newHeuristicA).get(newHeuristicA).heuristicWinRate();
		final double newHeuristicBWeight = evaluateCandidateHeuristicsAgainstEachOther(game, newcandidateHeuristicsB, newHeuristicB).get(newHeuristicB).heuristicWinRate();
		final double newHeuristicCWeight = evaluateCandidateHeuristicsAgainstEachOther(game, newcandidateHeuristicsC, newHeuristicC).get(newHeuristicC).heuristicWinRate();
		
		if (newHeuristicAWeight >= newHeuristicBWeight && newHeuristicAWeight >= newHeuristicCWeight)
			return newcandidateHeuristicsA;
		else if (newHeuristicBWeight >= newHeuristicAWeight && newHeuristicBWeight >= newHeuristicCWeight)
			return newcandidateHeuristicsB;
		else
			return newcandidateHeuristicsC;
	}
	
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
	
	/**
	 * Multiplies the weights for an array of heuristicTerms by the specified multiplier.
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
		
        return heuristicTermsCombined.toArray(new HeuristicTerm[0]);
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
	private static LinkedHashMap<Heuristics, HeuristicStats> evaluateCandidateHeuristicsAgainstEachOther(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, final Heuristics requiredHeuristic) 
	{
		final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
		final List<TIntArrayList> allIndexCombinations = allHeuristicIndexCombinations(game.players().count(), allHeuristics, requiredHeuristic, sampleSize);
		final List<HeuristicSampling> allAgents = createAgents(allHeuristics);
		
		System.out.println("number of pairups: " + allIndexCombinations.size());
		System.out.println("number of agents: " + allAgents.size());
		
		// Perform initial comparison across all agents/heuristics
		for (final TIntArrayList agentIndices : allIndexCombinations)
		{
			System.out.print(".");
			
			final List<AI> agents = new ArrayList<>();
			for (final int i : agentIndices.toArray())
				agents.add(allAgents.get(i));
			
			final ArrayList<Double> agentMeanWinRates = compareAgents(game, agents);
			for (int i = 0; i < agentMeanWinRates.size(); i++)
				candidateHeuristics.get(allHeuristics.get(agentIndices.get(i))).addHeuristicWinRate(agentMeanWinRates.get(i));
		}

		System.out.println("\n");
		
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
				.setNumGames(numTrialsPerComparison)
				.setPrintOut(false)
				.setRoundToNextPermutationsDivisor(true)
				.setRotateAgents(true);
		
		gamesSet.startGames(game);
		
		final ArrayList<Double> agentMeanWinRates = new ArrayList<>();
		for (final Stats agentStats : gamesSet.resultsSummary().agentPoints())
		{
			agentStats.measure();
			agentMeanWinRates.add(agentStats.mean());
		}
		
		return agentMeanWinRates;
	}
	
//	private static ArrayList<Double> compareAgents(final Game game, final List<AI> agents)
//	{
//		final ArrayList<Double> agentMeanWinRates = new ArrayList<>();
//		
//		try
//		{
//			final int numTrials = numTrialsPerComparison;
//					
//			AI aiA = null;
//			AI aiB = null;
//					
//			// Run trials concurrently
//			final ExecutorService executor = Executors.newFixedThreadPool(numTrials);
//			final List<Future<TrialRecord>> futures = new ArrayList<>(numTrials);
//			
//			final CountDownLatch latch = new CountDownLatch(numTrials);
//				
//			for (int t = 0; t < numTrials; t++)
//			{
//				final int starter = t % 2;
//				
//				final List<AI> ais = new ArrayList<>();
//				ais.add(null);  // null placeholder for player 0
//				
//				aiA = agents.get(0);
//				aiB = agents.get(1);
//				
//				if (t % 2 == 0)
//				{
//					ais.add(aiA);
//					ais.add(aiB);
//				}
//				else
//				{
//					ais.add(aiB);
//					ais.add(aiA);
//				}
//				
//				futures.add
//				(
//					executor.submit
//					(
//						() -> 
//						{
//							final Trial trial = new Trial(game);
//							final Context context = new Context(game, trial);
//					
//							game.start(context);
//		
//							for (int p = 1; p <= game.players().count(); ++p)
//								ais.get(p).initAI(game, p);
//		
//							final Model model = context.model();
//							while (!trial.over())
//								model.startNewStep(context, ais, -1, -1, 1, 0);
//		
//							latch.countDown();
//							System.out.println(latch.getCount());
//					
//							return new TrialRecord(starter, trial);
//						}
//					)
//				);
//			}
//			
//			latch.await();  // wait for all trials to finish
//			
//			// Accumulate wins per player		
//			final double[] results = new double[Constants.MAX_PLAYERS + 1];
//	
//			for (int t = 0; t < numTrials; t++)
//			{
//				final TrialRecord trialRecord = futures.get(t).get();
//				final Trial trial = trialRecord.trial();
//	
//				final int result = trial.status().winner();  //futures.get(t).get().intValue();
//				if (result == 0)
//				{
//					// Draw: share win
//					results[0] += 0.5;
//					results[1] += 0.5;
//				}
//				else
//				{
//					// Reward winning AI
//					if (trialRecord.starter() == 0)
//					{
//						if (result == 1)
//							results[0]++;
//						else
//							results[1]++;
//					}
//					else 
//					{
//						if (result == 1)
//							results[1]++;
//						else
//							results[0]++;
//					}
//				}
//				
//				//System.out.println(trialRecord.starter() + " => " + trial.status().winner());
//			}
//					
//			//System.out.println("\naiA=" + results[0] + ", aiB=" + results[1] + ".");
//			System.out.println("aiA success rate " + results[0] / numTrials * 100 + "%.");  //+ ", aiB=" + results[1] + ".");
//			
//			agentMeanWinRates.add(results[0] / numTrials);
//			agentMeanWinRates.add(results[1] / numTrials);
//			
//			executor.shutdown();
//		}
//		catch (final Exception e)
//		{
//			e.printStackTrace();
//		}
//		
//		return agentMeanWinRates;
//	}
	
//	private static ArrayList<Double> compareAgents(final Game game, final List<AI> agents)
//	{
//		final ArrayList<Double> agentMeanWinRates = new ArrayList<>();
//		
//		try
//		{
//			final int numTrials = numTrialsPerComparison;
//					
//			AI aiA = null;
//			AI aiB = null;
//					
//			// Run trials concurrently
//			final ExecutorService executor = Executors.newFixedThreadPool(numTrials);
//			final List<TrialRecord> futures = new ArrayList<>(numTrials);
//			
//			final CountDownLatch latch = new CountDownLatch(numTrials);
//				
//			for (int t = 0; t < numTrials; t++)
//			{
//				final int starter = t % 2;
//				
//				final List<AI> ais = new ArrayList<>();
//				ais.add(null);  // null placeholder for player 0
//				
//				aiA = agents.get(0);
//				aiB = agents.get(1);
//				
//				if (t % 2 == 0)
//				{
//					ais.add(aiA);
//					ais.add(aiB);
//				}
//				else
//				{
//					ais.add(aiB);
//					ais.add(aiA);
//				}
//				
//	
//							final Trial trial = new Trial(game);
//							final Context context = new Context(game, trial);
//					
//							game.start(context);
//		
//							for (int p = 1; p <= game.players().count(); ++p)
//								ais.get(p).initAI(game, p);
//		
//							final Model model = context.model();
//							while (!trial.over())
//								model.startNewStep(context, ais, -1, -1, 1, 0);
//		
//							latch.countDown();
//							//System.out.println(latch.getCount());
//					
//							futures.add(new TrialRecord(starter, trial));
//	
//			}
//			
//			latch.await();
//
//			
//			// Accumulate wins per player		
//			final double[] results = new double[Constants.MAX_PLAYERS + 1];
//	
//			for (int t = 0; t < numTrials; t++)
//			{
//				final TrialRecord trialRecord = futures.get(t);
//				final Trial trial = trialRecord.trial();
//	
//				final int result = trial.status().winner();  //futures.get(t).get().intValue();
//				if (result == 0)
//				{
//					// Draw: share win
//					results[0] += 0.5;
//					results[1] += 0.5;
//				}
//				else
//				{
//					// Reward winning AI
//					if (trialRecord.starter() == 0)
//					{
//						if (result == 1)
//							results[0]++;
//						else
//							results[1]++;
//					}
//					else 
//					{
//						if (result == 1)
//							results[1]++;
//						else
//							results[0]++;
//					}
//				}
//				
//				//System.out.println(trialRecord.starter() + " => " + trial.status().winner());
//			}
//					
//			//System.out.println("\naiA=" + results[0] + ", aiB=" + results[1] + ".");
//			//System.out.println("aiA success rate " + results[0] / numTrials * 100 + "%.");  //+ ", aiB=" + results[1] + ".");
//			
//			agentMeanWinRates.add(results[0] / numTrials);
//			agentMeanWinRates.add(results[1] / numTrials);
//			
//			executor.shutdown();
//		}
//		catch (final Exception e)
//		{
//			e.printStackTrace();
//		}
//		
//		return agentMeanWinRates;
//	}
	
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
		final Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < list.size(); i++)
			set.add(list.get(i));
		return set.size() < list.size();
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
					componentPairs[j] = new Pair(game.equipment().components()[j+1].name(), 1f);
				else
					componentPairs[j] = new Pair(game.equipment().components()[j+1].name(), 0f);
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
			allAgents.add(new HeuristicSampling(h, 4));
		
		return allAgents;
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
