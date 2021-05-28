package metrics;

import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import main.StringRoutines;

/**
 * Example distance metric. Returns Levenshtein distance (i.e. edit distance)
 * between expanded game descriptions as a ratio of string length.
 * 
 * @author cambolbro
 */
public class Levenshtein implements DistanceMetric
{
	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		return distance(gameA.description().expanded(),
				gameB.description().expanded());

	}

	private static Score distance(final String expandedA, final String expandedB)
	{
		// Expect around 10,000 edits for the general case
		final int edits = StringRoutines.levenshteinDistance(expandedA,
				expandedB);
		final int maxLength = Math.max(expandedA.length(), expandedB.length());
		final double score = (double) edits / maxLength;
		return new Score(score);
	}

	@Override
	public Score
			distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		return distance(candidate.getDescriptionExpanded(),
				gameToCompareWith.getDescriptionExpanded());
	}

	@Override
	public Score distance
	(
		final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns,
		final double thinkTime, final String AIName
	)
	{
		return null;
	}
}
