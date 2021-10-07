package metrics.moveBased;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.CountMap;
import common.DistanceUtils;
import common.LudRul;
import common.Score;
import common.TrialLoader;
import common.trial_loader.AgentSetting;
import game.Game;
import metrics.DistanceMetric;
import metrics.MoveBased;
import metrics.individual.JensenShannonDivergence;
import metrics.suffix_tree.Letteriser;
import other.trial.Trial;

/**
 * Just like JensenShannonDivergence, but this time it focuses not on Ludemes,
 * but instead on the movetypes of respective trials.
 * 
 * @author Markus
 *
 */
public class MoveTypeJensonShannon implements DistanceMetric,MoveBased
{
	final int numPlayouts;// = 80;
	final int numMaxMoves;// = 200;

	static HashMap<LudRul, Trial[]> storedTrials = new HashMap<>();
	static HashMap<LudRul, TreeMap<String, Double>> storedFrequencies = new HashMap<>();
	private final Letteriser letteriser;
	public MoveTypeJensonShannon(final Letteriser letteriser, final int numPlayouts, final int numMaxMoves)
	{
		this.numPlayouts = numPlayouts;
		this.numMaxMoves = numMaxMoves;
		this.letteriser = letteriser;
	}
	
	@Override
	public String getName()
	{
		return "MoveTypeJensonShannon_" + letteriser.toString() + "_" + numPlayouts + "_" + numMaxMoves;
	}

	@Override
	public void releaseResources()
	{
		storedTrials.clear();
		storedFrequencies.clear();
	}

	@Override
	public Score distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		TreeMap<String, Double> distA = storedFrequencies.get(candidate);
		TreeMap<String, Double> distB = storedFrequencies.get(gameToCompareWith);
		if (distA==null) {
			distA = getActionDistribution(candidate);
			storedFrequencies.put(candidate, distA);
		}
		if (distB==null) {
			distB = getActionDistribution(gameToCompareWith);
			storedFrequencies.put(gameToCompareWith, distB);
		}
		final double dist = JensenShannonDivergence.jensenShannonDivergence(distA, distB);

		return new Score(dist);
	}

	public TreeMap<String, Double> getActionDistribution(final LudRul game)
	{
		final Trial[] trials = TrialLoader.lazyloadTrials(game, AgentSetting.random, numPlayouts, numMaxMoves, false);
		final Map<String, Integer> frequency = getFrequencies(letteriser,game.getGame(),trials);
		final TreeMap<String, Double> distribution = JensenShannonDivergence.frequencyToDistribution(frequency);
		
		return distribution;
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{

		final Trial[] trialsA = DistanceUtils.generateRandomTrialsFromGame(gameA, numPlayouts, numMaxMoves);
		final Trial[] trialsB = DistanceUtils.generateRandomTrialsFromGame(gameB, numPlayouts, numMaxMoves);

		return distance(gameA, gameB,trialsA, trialsB);
	}

	public Score distance(final Game gameA, final Game gameB, final Trial[] trialsA, final Trial[] trialsB)
	{

		final Map<String, Integer> frequencyA = getFrequencies(letteriser,gameA,trialsA);
		final Map<String, Integer> frequencyB = getFrequencies(letteriser,gameB,trialsB);
		final TreeMap<String, Double> distributionA = JensenShannonDivergence.frequencyToDistribution(frequencyA);
		final TreeMap<String, Double> distributionB = JensenShannonDivergence.frequencyToDistribution(frequencyB);

		final double dist = JensenShannonDivergence.jensenShannonDivergence(distributionA, distributionB);

		return new Score(dist);

	}

	/**
	 * Counts the frequencies of the action types within the trial
	 * 
	 * @return A map of word frequency.
	 */
	public static Map<String, Integer> getFrequencies(final Letteriser let, final Game g,final Trial[] trials)
	{
		final String[][] words = let.getWords(g, trials);	
		final CountMap<String> cm = new CountMap<>();
		
		for (final String[] strings : words)
		{
			for (final String string : strings)
			{
				cm.addInstance(string);
			}
		}
		
		return cm.getHashMap();
	}

	@Override
	public Score distance(final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns, final double thinkTime, final String AIName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(final String description1, final String description2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new MoveTypeJensonShannon(Letteriser.lowRes, 40, 60);
	}

	@Override
	public DistanceMetric showUserSelectionDialog()
	{
		final MoveBased.Inits inits = MoveBased.showUserPlayoutAndMaxMovesSettings();
		return new MoveTypeJensonShannon(inits.letteriser, inits.numPlayouts, inits.numMaxMoves);
	}

	@Override
	public boolean hasUserSelectionDialog() {
		return true;
	}
}
