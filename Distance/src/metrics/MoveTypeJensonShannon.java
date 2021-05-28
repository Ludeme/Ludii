package metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.DistanceUtils;
import common.LudRul;
import common.Score;
import game.Game;
import other.action.Action;
import other.move.Move;
import other.trial.Trial;

/**
 * Just like JensenShannonDivergence, but this time it focuses not on Ludemes,
 * but instead on the movetypes of respective trials.
 * 
 * @author Markus
 *
 */
public class MoveTypeJensonShannon implements DistanceMetric
{
	final int numPlayouts = 30;
	final int numMaxMoves = 40;

	static HashMap<LudRul, Trial[]> storedTrials = new HashMap<>();

	@Override
	public void releaseResources()
	{
		storedTrials.clear();
	}

	@Override
	public Score distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		Trial[] trialsA = storedTrials.get(candidate);
		if (trialsA == null)
		{
			trialsA = DistanceUtils.getRandomTrialsFromGame(candidate.getGame(), numPlayouts, numMaxMoves);
			storedTrials.put(candidate, trialsA);
		}
		Trial[] trialsB = storedTrials.get(gameToCompareWith);
		if (trialsB == null)
		{
			trialsB = DistanceUtils.getRandomTrialsFromGame(candidate.getGame(), numPlayouts, numMaxMoves);
			storedTrials.put(gameToCompareWith, trialsB);
		}

		return distance(trialsA, trialsB);
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{

		final Trial[] trialsA = DistanceUtils.getRandomTrialsFromGame(gameA, numPlayouts, numMaxMoves);
		final Trial[] trialsB = DistanceUtils.getRandomTrialsFromGame(gameB, numPlayouts, numMaxMoves);

		return distance(trialsA, trialsB);
	}

	public Score distance(final Trial[] trialsA, final Trial[] trialsB)
	{

		final Map<String, Integer> frequencyA = getFrequencies(trialsA);
		final Map<String, Integer> frequencyB = getFrequencies(trialsB);
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
	public Map<String, Integer> getFrequencies(final Trial[] trials)
	{
		final Map<String, Integer> map = new HashMap<>();

		// iterate through all actions and increase counter in Hashmap.
		for (final Trial trial : trials)
		{
			for (final Move m : trial.generateCompleteMovesList())
			{
				for (final Action a : m.actions())
				{
					final Class<? extends Action> className = a.getClass();
					final String word = className.toString();

					final Integer n = map.get(word);
					if (n == null)
						map.put(word, Integer.valueOf(1));
					else
						map.put(word, Integer.valueOf(n.intValue() + 1));

				}
			}
		}

		return map;
	}

	@Override
	public Score distance
	(
		final Game gameA, final List<Game> gameB, final int numberTrials, 
		final int maxTurns, final double thinkTime, final String AIName
	)
	{
		return null;
	}

}
