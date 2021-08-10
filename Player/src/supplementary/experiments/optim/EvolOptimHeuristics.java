package supplementary.experiments.optim;

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
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
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
	
	// Percentage of population that is chosen for tournament selection.
	private double tournamentSelectionPercentage = 10.0;
	
	// Number of generations before stopping.
	private int numGenerations = 100;
	
	// Number of trials per agent comparison.
	private int numTrialsPerComparison = 100;
	
	// Number of samples when evaluating an agent.
	private int sampleSize = 100;
	
	// Try removing heuristic terms which don't pass the improvement requirement.
	private boolean tryHeuristicRemoval = true;
	private double heuristicRemovalImprovementRequirement = -0.01;
	
	// Normalises all weights on heuristic between -1 and 1.
	private boolean normaliseHeuristicWeights = true;
	
	// Simplifies heuristic weights by combining them.
	private boolean simplifyHeuristicWeights = true;
	
	// Fraction value for heuristic sampling agents.
	private int HeuristicSamplingAgentFraction = 4;
	
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

		LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics = initialHeuristics(game);
		
		System.out.println("--DETERMINING INITIAL HEURISTIC WEIGHTS--\n");
		for (final Map.Entry<Heuristics, HeuristicStats> candidateHeuristic : candidateHeuristics.entrySet())
			candidateHeuristics = evaluateCandidateHeuristicsAgainstEachOther(game, candidateHeuristics, candidateHeuristic.getKey());
		
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
	private final LinkedHashMap<Heuristics, HeuristicStats> evolveCandidateHeuristics(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics) 
	{
		final Heuristics[] parentHeuristics = tournamentSelection(candidateHeuristics);
		final HeuristicTerm[] parentA = parentHeuristics[0].heuristicTerms();
		final HeuristicTerm[] parentB = parentHeuristics[1].heuristicTerms();
		
		final List<LinkedHashMap<Heuristics, HeuristicStats>> allCandidateHeuristics = new ArrayList<>();
		final List<Heuristics> allHeuristics = new ArrayList<>();
		
		allHeuristics.add(combineHeuristicTerms(parentA, parentB));									// Regular
		allHeuristics.add(combineHeuristicTerms(parentA, multiplyHeuristicTerms(parentB, 0.5)));	// Double
		allHeuristics.add(combineHeuristicTerms(parentA, multiplyHeuristicTerms(parentB, 2.0)));	// Half
		
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(0)));
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(1)));
		allCandidateHeuristics.add(addAndEvaluateHeuristic(game, candidateHeuristics, allHeuristics.get(2)));
		
		// Record best candidate's results from evaluation
		LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristicsBest = null;
		Heuristics newHeuristicBest = null;
		double newHeuristicBestWeight = -1;
		for (int i = 0; i < allHeuristics.size(); i++)
		{
			final double heurisitcWinRate = allCandidateHeuristics.get(i).get(allHeuristics.get(i)).heuristicWinRate();
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
	
	private LinkedHashMap<Heuristics, HeuristicStats> tryRemovingHeuristicTerms(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristicsBestOri, final Heuristics newHeuristicBestOri, final double newHeuristicBestWeightOri)
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
				final double newHeuristicMinusOneWeight = candidateHeuristicsMinusOneWeight.get(heuristicMinusOne).heuristicWinRate();
				
				if (newHeuristicMinusOneWeight > newHeuristicBestWeight + heuristicRemovalImprovementRequirement)
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
	
	private LinkedHashMap<Heuristics, HeuristicStats> addAndEvaluateHeuristic(final Game game, final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, final Heuristics heuristic) 
	{
		final LinkedHashMap<Heuristics, HeuristicStats> newcandidateHeuristics = copyCandidateHeuristics(candidateHeuristics);
		if (!newcandidateHeuristics.containsKey(heuristic))
			newcandidateHeuristics.put(heuristic, new HeuristicStats());
		return evaluateCandidateHeuristicsAgainstEachOther(game, newcandidateHeuristics, heuristic);
	}

	/**
	 * Copies an existing candidateHeuristics map.
	 */
	public LinkedHashMap<Heuristics, HeuristicStats> copyCandidateHeuristics(final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics)
	{
		final LinkedHashMap<Heuristics, HeuristicStats> copy = new LinkedHashMap<>();
		for (final Map.Entry<Heuristics, HeuristicStats> entry : candidateHeuristics.entrySet())
	        copy.put(entry.getKey(), entry.getValue());
		return copy;
	}
	
	/**
	 * Multiplies the weights for an array of heuristicTerms by the specified multiplier.
	 */
	private HeuristicTerm[] multiplyHeuristicTerms(final HeuristicTerm[] heuristicTerms, final double multiplier)
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
	private Heuristics normaliseHeuristic(final Heuristics heuristic)
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
	private Heuristics[] tournamentSelection(final LinkedHashMap<Heuristics, HeuristicStats> candidates)
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
	 * Evaluates a set of heuristics against each other, updating their associated win-rates.
	 */
	private LinkedHashMap<Heuristics, HeuristicStats> evaluateCandidateHeuristicsAgainstEachOther
	(
		final Game game, 
		final LinkedHashMap<Heuristics, HeuristicStats> candidateHeuristics, 
		final Heuristics requiredHeuristic
	)
	{
		final List<Heuristics> allHeuristics = new ArrayList<>(candidateHeuristics.keySet());
		final List<TIntArrayList> allIndexCombinations = allHeuristicIndexCombinations(game.players().count(), allHeuristics, requiredHeuristic);
		
		System.out.println("number of pairups: " + allIndexCombinations.size());
		System.out.println("number of agents: " + allHeuristics.size());

		for (final TIntArrayList agentIndices : allIndexCombinations)
		{
			final List<Heuristics> selectedHeuristiscs = new ArrayList<>();
			for (final int i : agentIndices.toArray())
				selectedHeuristiscs.add(Heuristics.copy(allHeuristics.get(i)));

			final TDoubleArrayList agentMeanWinRates = compareHeuristics(game, selectedHeuristiscs);
			for (int i = 0; i < agentMeanWinRates.size(); i++)
				candidateHeuristics.get(allHeuristics.get(agentIndices.get(i))).addHeuristicWinRate(agentMeanWinRates.get(i));

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
			agentMeanWinRates.add(agentStats.mean());
		}

		return agentMeanWinRates;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Provides a list of TIntArrayList, describing all combinations of agent indices from allHeuristics.
	 */
	private List<TIntArrayList> allHeuristicIndexCombinations
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
		if (sampleSize > 0 && sampleSize <= allHeuristicIndexCombinations.size())
		{
			Collections.shuffle(allHeuristicIndexCombinations);
			return allHeuristicIndexCombinations.subList(0, sampleSize);
		}	
		
		return allHeuristicIndexCombinations;
	}
	
	/**
	 * @return True if duplicate values are present in list
	 */
	private boolean containsDuplicates(final TIntArrayList list)
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
	 * @param game
	 */
	private static LinkedHashMap<Heuristics, HeuristicStats> initialHeuristics(final Game game)
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
		}
				
		for (final HeuristicTerm h : heuristicTerms)
			initialHeuristics.put(new Heuristics(h), new HeuristicStats());
		
		return initialHeuristics;
	}
	
	//-------------------------------------------------------------------------
	
	private LinkedHashMap<Heuristics, HeuristicStats> sortCandidateHeuristics(final LinkedHashMap<Heuristics, HeuristicStats> unsortedMap) 
	{
		final LinkedHashMap<Heuristics, HeuristicStats> sortedMap = new LinkedHashMap<>();
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
				.withDefault(new ArrayList<String>(0))
				.withNumVals("*")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--ruleset")
				.help("Ruleset to compile.")
				.withDefault("")
				.withNumVals(1)
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
		
		final String outDirFilepath = argParse.getValueString("--out-dir");
		if (outDirFilepath != null)
			experiment.outDir = new File(outDirFilepath);
		else
			experiment.outDir = null;
		
		experiment.runOptim();
	}
	
	//-------------------------------------------------------------------------

}
