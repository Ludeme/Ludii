package supplementary.experiments.optim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.collections.ListUtils;
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

//-----------------------------------------------------------------------------

/**
 * Experiments to optimise heuristics using a simple evolutionary-inspired approach
 * 
 * @author Dennis Soemers and matthew.stephenson
 */
public class EvolOptimHeuristics
{
	
	/** Name of game to compile */
	private String gameName = null;
	
	/** List of options to compile game with (ignored if ruleset is provided) */
	private List<String> gameOptions = null;
	
	/** Ruleset to compile game with */
	private String ruleset = null;
	
	/** List of names of heuristics we'd like to skip */
	private List<String> skipHeuristics = null;
	
	// Percentage of population that is chosen for tournament selection.
	private double tournamentSelectionPercentage = 10.0;
	
	// Number of generations before stopping.
	private int numGenerations = 100;
	
	// Number of trials per agent comparison.
	private int numTrialsPerComparison = 100;
	
	/** Max number of combinations of opponents we sample for evaluating a heuristic */
	private int opponentsSampleSize = 100;
	
	// Normalises all weights on heuristic between -1 and 1.
	private boolean normaliseHeuristicWeights = true;
	
	// Simplifies heuristic weights by combining them.
	private boolean simplifyHeuristicWeights = true;
	
	// Fraction value for heuristic sampling agents.
	private int heuristicSamplingAgentFraction = 4;
	
	/** Directory to write output to */
	private File outDir = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private EvolOptimHeuristics()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs our evolutionary optimisation of heuristics
	 */
	private void runOptim()
	{
		if (outDir == null)
			System.err.println("Warning: no outDir specified!");
		else if (!outDir.exists())
			outDir.mkdirs();
		
		final Game game;
		
		if (ruleset != null && !ruleset.equals(""))
			game = GameLoader.loadGameFromName(gameName, ruleset);
		else
			game = GameLoader.loadGameFromName(gameName, gameOptions);

		Map<Heuristics, HeuristicStats> candidateHeuristics = initialHeuristics(game);
		
		System.out.println("--DETERMINING INITIAL HEURISTIC WEIGHTS--\n");
		for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
			candidateHeuristics = evaluateCandidateHeuristicsAgainstOthers(game, candidateHeuristics, candidateHeuristic.getKey());
		
		for (int i = 1; i <= numGenerations; i++)
		{
			System.out.println("\nGENERATION " + i + "\n");
			candidateHeuristics = evolveCandidateHeuristics(game, candidateHeuristics);
			
			// Store the current candidate heuristics to a text file after each generation.
			candidateHeuristics = sortCandidateHeuristics(candidateHeuristics);

			if (outDir != null)
			{
				try (PrintWriter out = new PrintWriter(outDir + "/results_" + game.name() + "_" + i + ".txt"))
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
		}
		
		// Determine the best heuristic after all generations are complete.
		final Heuristics bestHeuristicFound = candidateHeuristics.entrySet().iterator().next().getKey();
		System.out.println(bestHeuristicFound);
		
		// Compare best heuristic against the Null heuristic
		final List<Heuristics> heuristics = new ArrayList<>();
		heuristics.add(bestHeuristicFound);
		heuristics.add(new Heuristics(new NullHeuristic()));
		TDoubleArrayList agentMeanWinRates = compareHeuristics(game, heuristics);
		System.out.println("Performance against Null heuristic: " + agentMeanWinRates.getQuick(0));
		
		// Compare the best heuristic against the default (metadata) HeuristicSampling agent.
		heuristics.clear();
		heuristics.add(bestHeuristicFound);
		heuristics.add(null);
		agentMeanWinRates = compareHeuristics(game, heuristics);
		System.out.println("Performance against default HeuristicSampling agent : " + agentMeanWinRates.getQuick(0));

		System.out.println("DONE!");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Evolves the given set of candidate heuristics to create new candidate offspring.
	 */
	private final Map<Heuristics, HeuristicStats> evolveCandidateHeuristics
	(
		final Game game, 
		final Map<Heuristics, HeuristicStats> candidateHeuristics
	)
	{
		final Heuristics[] parentHeuristics = tournamentSelection(candidateHeuristics);
		final HeuristicTerm[] parentA = parentHeuristics[0].heuristicTerms();
		final HeuristicTerm[] parentB = parentHeuristics[1].heuristicTerms();
		
		final List<Map<Heuristics, HeuristicStats>> allCandidateHeuristics = new ArrayList<>();
		final List<Heuristics> allHeuristics = new ArrayList<>();
		
		allHeuristics.add(combineHeuristicTerms(parentA, parentB));									// Regular
		allHeuristics.add(combineHeuristicTerms(parentA, multiplyHeuristicTerms(parentB, 0.5)));	// Double
		allHeuristics.add(combineHeuristicTerms(parentA, multiplyHeuristicTerms(parentB, 2.0)));	// Half
		
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(0)));
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(1)));
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(2)));
		
		// Record best candidate's results from evaluation
		Map<Heuristics, HeuristicStats> candidateHeuristicsBest = null;
		double newHeuristicBestWeight = -1;
		for (int i = 0; i < allHeuristics.size(); i++)
		{
			final double heurisitcWinRate = allCandidateHeuristics.get(i).get(allHeuristics.get(i)).heuristicWinRate();
			if (heurisitcWinRate > newHeuristicBestWeight)
			{
				candidateHeuristicsBest = allCandidateHeuristics.get(i);
				newHeuristicBestWeight = heurisitcWinRate;
			}
		}
		
		return candidateHeuristicsBest;
	}

	private Map<Heuristics, HeuristicStats> addAndEvaluateHeuristic
	(
		final Game game, 
		final Map<Heuristics, HeuristicStats> candidateHeuristics, 
		final Heuristics heuristic
	)
	{
		final Map<Heuristics, HeuristicStats> newcandidateHeuristics = copyCandidateHeuristics(candidateHeuristics);
		if (!newcandidateHeuristics.containsKey(heuristic))
			newcandidateHeuristics.put(heuristic, new HeuristicStats());
		return evaluateCandidateHeuristicsAgainstOthers(game, newcandidateHeuristics, heuristic);
	}

	/**
	 * Copies an existing candidateHeuristics map.
	 * @return The copy
	 */
	private static Map<Heuristics, HeuristicStats> copyCandidateHeuristics(final Map<Heuristics, HeuristicStats> candidateHeuristics)
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
	private Heuristics combineHeuristicTerms(final HeuristicTerm[] heuristicTermsA, final HeuristicTerm[] heuristicTermsB)
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
			combinedHeuristic = normaliseHeuristic(combinedHeuristic);
		
		if (simplifyHeuristicWeights)
			for (final HeuristicTerm term : combinedHeuristic.heuristicTerms())
				term.simplify();

        return combinedHeuristic;
	}
	
	/**
	 * Normalises all weights on heuristic between -1 and 1.
	 */
	private static Heuristics normaliseHeuristic(final Heuristics heuristic)
	{
		double maxWeight = 0.0;
		for (final HeuristicTerm term : heuristic.heuristicTerms())
			maxWeight = Math.max(maxWeight, term.maxAbsWeight());
		return new Heuristics(multiplyHeuristicTerms(heuristic.heuristicTerms(), 1.0/maxWeight));
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Selects two random individuals from the set of candidates, with probability based on its win-rate.
	 */
	private Heuristics[] tournamentSelection(final Map<Heuristics, HeuristicStats> candidates)
	{
		// selected parent candidates.
		final Heuristics[] selectedCandidates = new Heuristics[2];
		
		if (candidates.size() < 2)
			System.out.println("ERROR, candidates must be at least size 2.");
		
		// Select a set of k random candidates;
		final int k = Math.max((int) Math.ceil(candidates.keySet().size()/tournamentSelectionPercentage), 2);
		final TIntSet selectedCandidateIndices = new TIntHashSet();
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
	
	/**
	 * Selects a random individual from the set of candidates, with probability based on its win-rate.
	 */
//	private Heuristics tournamentSelection(final Map<Heuristics, Double> candidates)
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
	 * Evaluates a heuristic against (a sample of) all others, updating their associated win-rates.
	 */
	private Map<Heuristics, HeuristicStats> evaluateCandidateHeuristicsAgainstOthers
	(
		final Game game, 
		final Map<Heuristics, HeuristicStats> candidateHeuristics, 
		final Heuristics requiredHeuristic
	)
	{
		final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
		final List<TIntArrayList> allIndexCombinations = allHeuristicIndexCombinationsWithRequired(game.players().count(), allHeuristics, requiredHeuristic);
		
		System.out.println("number of pairups: " + allIndexCombinations.size());
		System.out.println("number of agents: " + allHeuristics.size());

		for (final TIntArrayList agentIndices : allIndexCombinations)
		{
			final List<Heuristics> selectedHeuristiscs = new ArrayList<>(agentIndices.size());
			for (int i = 0; i < agentIndices.size(); ++i)
				selectedHeuristiscs.add(Heuristics.copy(allHeuristics.get(agentIndices.getQuick(i))));

			final TDoubleArrayList agentMeanWinRates = compareHeuristics(game, selectedHeuristiscs);
			for (int i = 0; i < agentMeanWinRates.size(); i++)
				candidateHeuristics.get(allHeuristics.get(agentIndices.getQuick(i))).addHeuristicWinRate(agentMeanWinRates.getQuick(i));

			System.out.print(".");
		}
		
		System.out.println("\n");
		
		return candidateHeuristics;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Compares a set of agents on a given game.
	 */
	private TDoubleArrayList compareHeuristics(final Game game, final List<Heuristics> heuristics)
	{
		final TDoubleArrayList agentMeanWinRates = new TDoubleArrayList();

		final List<AI> agents = new ArrayList<>();
		for (final Heuristics h : heuristics)
		{
			if (h == null)
				agents.add(new HeuristicSampling(heuristicSamplingAgentFraction));
			else
				agents.add(new HeuristicSampling(h, heuristicSamplingAgentFraction));
		}

		final EvalGamesSet gamesSet = 
				new EvalGamesSet()
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
			agentMeanWinRates.add(agentStats.mean());
		}

		return agentMeanWinRates;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of TIntArrayList, describing all combinations of agent indices from allHeuristics,
	 * such that every combination includes the index of the given required heuristic.
	 * 
	 * @param numPlayers
	 * @param allHeuristics
	 * @param requiredHeuristic
	 * @return
	 */
	private List<TIntArrayList> allHeuristicIndexCombinationsWithRequired
	(
		final int numPlayers, 
		final List<Heuristics> allHeuristics, 
		final Heuristics requiredHeuristic
	)
	{		
		final int numHeuristics = allHeuristics.size();

		final TIntArrayList heuristicIndices = new TIntArrayList(numHeuristics);
		for (int i = 0; i < numHeuristics; ++i)
			heuristicIndices.add(i);
		
		final List<TIntArrayList> allHeuristicIndexCombinations = new ArrayList<TIntArrayList>();
		ListUtils.generateAllCombinations(heuristicIndices, numPlayers, 0, new int[numPlayers], allHeuristicIndexCombinations);
		
		// Only keep heuristic combinations that include our required heuristic.
		final int requiredHeuristicIndex = allHeuristics.indexOf(requiredHeuristic);
		ListUtils.removeSwapIf(allHeuristicIndexCombinations, (l) -> {return !l.contains(requiredHeuristicIndex);});
		
		// Select a random number of combinations based on our desired sample size.
		if (opponentsSampleSize > 0 && opponentsSampleSize <= allHeuristicIndexCombinations.size())
		{
			Collections.shuffle(allHeuristicIndexCombinations);
			return allHeuristicIndexCombinations.subList(0, opponentsSampleSize);
		}	
		
		return allHeuristicIndexCombinations;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of all initial heuristics.
	 * @param game
	 */
	private LinkedHashMap<Heuristics, HeuristicStats> initialHeuristics(final Game game)
	{
		final LinkedHashMap<Heuristics, HeuristicStats> initialHeuristics = new LinkedHashMap<>();
		final List<HeuristicTerm> heuristicTerms = new ArrayList<>();
		
		// All possible initial component pair combinations.
		final List<Pair[]> allComponentPairsCombinations = new ArrayList<>();
		for (int i = 0; i < game.equipment().components().length - 1; i++)
		{
			final Pair[] componentPairs  = new Pair[game.equipment().components().length - 1];
			for (int j = 0; j < game.equipment().components().length - 1; j++)
			{
				if (j == i)
					componentPairs[j] = new Pair(game.equipment().components()[j + 1].name(), Float.valueOf(1.f));
				else
					componentPairs[j] = new Pair(game.equipment().components()[j + 1].name(), Float.valueOf(0.f));
			}
			allComponentPairsCombinations.add(componentPairs);
		}
		
		for (final float weight : new float[]{-1.f, 1.f})
		{
			if (LineCompletionHeuristic.isApplicableToGame(game) && !skipHeuristics.contains("LineCompletionHeuristic"))
				heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(weight), null));
			
			if (MobilitySimple.isApplicableToGame(game) && !skipHeuristics.contains("MobilitySimple"))
				heuristicTerms.add(new MobilitySimple(null, Float.valueOf(weight)));
			
			if (Influence.isApplicableToGame(game) && !skipHeuristics.contains("Influence"))
				heuristicTerms.add(new Influence(null, Float.valueOf(weight)));
			
			if (OwnRegionsCount.isApplicableToGame(game) && !skipHeuristics.contains("OwnRegionsCount"))
				heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(weight)));
			
			if (PlayerSiteMapCount.isApplicableToGame(game) && !skipHeuristics.contains("PlayerSiteMapCount"))
				heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(weight)));
			
			if (Score.isApplicableToGame(game) && !skipHeuristics.contains("Score"))
				heuristicTerms.add(new Score(null, Float.valueOf(weight)));
			
			if (CentreProximity.isApplicableToGame(game) && !skipHeuristics.contains("CentreProximity"))
			{
				heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
			}
			
			if (ComponentValues.isApplicableToGame(game) && !skipHeuristics.contains("ComponentValues"))
			{
				heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), null, null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), componentPairs, null));
			}
				
			if (CornerProximity.isApplicableToGame(game) && !skipHeuristics.contains("CornerProximity"))
			{
				heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), componentPairs));
			}
		
			if (Material.isApplicableToGame(game) && !skipHeuristics.contains("Material"))
			{
				heuristicTerms.add(new Material(null, Float.valueOf(weight), null, null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new Material(null, Float.valueOf(weight), componentPairs, null));
			}
		
			if (SidesProximity.isApplicableToGame(game) && !skipHeuristics.contains("SidesProximity"))
			{
				heuristicTerms.add(new SidesProximity(null, Float.valueOf(weight), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
			}
			
			if (PlayerRegionsProximity.isApplicableToGame(game) && !skipHeuristics.contains("PlayerRegionsProximity"))
			{
				for (int p = 1; p <= game.players().count(); ++p)
				{
					heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), null));
					for (final Pair[] componentPairs : allComponentPairsCombinations)
						heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), componentPairs));
				}
			}
			
			if (RegionProximity.isApplicableToGame(game) && !skipHeuristics.contains("RegionProximity"))
			{
				for (int i = 0; i < game.equipment().regions().length; ++i)
				{
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), null));
					for (final Pair[] componentPairs : allComponentPairsCombinations)
						heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), componentPairs));
				}
			}
		}
				
		for (final HeuristicTerm h : heuristicTerms)
			initialHeuristics.put(new Heuristics(h), new HeuristicStats());
		
		return initialHeuristics;
	}
	
	//-------------------------------------------------------------------------
	
	private static Map<Heuristics, HeuristicStats> sortCandidateHeuristics(final Map<Heuristics, HeuristicStats> unsortedMap) 
	{
		final Map<Heuristics, HeuristicStats> sortedMap = new LinkedHashMap<>();
		unsortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		return sortedMap;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Heuristic statistics object.
	 */
	protected static class HeuristicStats implements Comparable<HeuristicStats>
	{
		private double heuristicWinRateSum = 0.0;
		private int numComparisons = 0;
		
		public double heuristicWinRate()
		{
			return heuristicWinRateSum / numComparisons;
		}
		
		public void addHeuristicWinRate(final double winRate)
		{
			heuristicWinRateSum += winRate;
			numComparisons++;
		}

		@Override
		public int compareTo(final HeuristicStats other) 
		{
			return Double.compare(heuristicWinRate(), other.heuristicWinRate());
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Run the evolutionary optimisation of heuristics
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Runs evolutionary optimisation of heuristics for a single game."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to play. Should end with \".lud\".")
				.withDefault("/Tic-Tac-Toe.lud")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--game-options")
				.help("Game Options to load.")
				.withDefault(new ArrayList<String>())
				.withNumVals("*")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--ruleset")
				.help("Ruleset to compile.")
				.withDefault("")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--skip-heuristics")
				.help("List of heuristics to skip.")
				.withDefault(new ArrayList<String>())
				.withNumVals("*")
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--out-dir", "--output-directory")
				.help("Filepath for output directory")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final EvolOptimHeuristics experiment = new EvolOptimHeuristics();
		
		experiment.gameName = argParse.getValueString("--game");
		experiment.gameOptions = (List<String>) argParse.getValue("--game-options"); 
		experiment.ruleset = argParse.getValueString("--ruleset");
		
		experiment.skipHeuristics = (List<String>) argParse.getValue("--skip-heuristics");
		
		final String outDirFilepath = argParse.getValueString("--out-dir");
		if (outDirFilepath != null)
			experiment.outDir = new File(outDirFilepath);
		else
			experiment.outDir = null;
		
		experiment.runOptim();
	}
	
	//-------------------------------------------------------------------------

}
