package metrics;

import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;

/**
 * Interface for distance metric classes.
 * 
 * @author cambolbro
 */
public interface DistanceMetric
{
	/**
	 * @return Estimated distance between two games.
	 */
	public Score distance(final Game gameA, final Game gameB);

	/**
	 * Compares the distance and uses the lightweight representation of a game.
	 * 
	 * @return Estimated distance between two games.
	 */
	public Score distance(LudRul gameA, LudRul gameB);

	/**
	 * Releases resources if used, like frequency maps, etc.
	 */
	public default void releaseResources()
	{
		// TODO
	}
	
	/**
	 * @return Estimated minimum distance gameA and the games listed in gameB.
	 */
	public Score distance
	(
		final Game gameA, final List<Game> gameB, final int numberTrials, 
		final int maxTurns, final double thinkTime, final String AIName
	);
}
