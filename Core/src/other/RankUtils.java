package other;

import other.context.Context;

/**
 * Some utility methods for working with rankings (and converting them to utilities)
 *
 * @author Dennis Soemers
 */
public class RankUtils
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private RankUtils()
	{
		// Do not instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Converts a rank >= 1 into a utility value in [-1, 1].
	 * 
	 * @param rank
	 * @param numPlayers Total number of players in the game (active + inactive ones)
	 * @return Utility for the given rank
	 */
	public static double rankToUtil(final double rank, final int numPlayers)
	{
		if (numPlayers == 1)
		{
			// a single-player game
			return 2.0 * rank - 1.0;
		}
		else
		{
			// two or more players
			return 1.0 - ((rank - 1.0) * (2.0 / (numPlayers - 1)));
		}
	}
	
	/**
	 * Computes a vector of utility values for all players based on the player
	 * rankings in the given context.
	 * 
	 * For players who do not yet have an established ranking, a "draw" utility 
	 * will be returned.
	 * 
	 * For players who do have an established ranking, the utility value will lie
	 * in [-1, 1] based on the ranking; 1.0 for top ranking, -1.0 for bottom ranking,
	 * 0.0 for middle ranking (e.g. second player out of three), etc.
	 * 
	 * The returned array will have a length equal to the number of players plus one,
	 * such that it can be indexed directly by player number.
	 * 
	 * @param context
	 * @return The utilities.
	 */
	public static double[] utilities(final Context context)
	{
		final double[] ranking = context.trial().ranking();
		final double[] utilities = new double[ranking.length];
		final int numPlayers = ranking.length - 1;
		//System.out.println("ranking = " + Arrays.toString(ranking));
		
		for (int p = 1; p < ranking.length; ++p)
		{
			double rank = ranking[p];
			if (numPlayers > 1 && rank == 0.0)
			{
				// looks like a playout didn't terminate yet; assign "draw" ranks
				rank = context.computeNextDrawRank();
			}
			
			utilities[p] = rankToUtil(rank, numPlayers);
			assert (utilities[p] >= -1.0 && utilities[p] <= 1.0);
		}
		
//		System.out.println("ranking = " + Arrays.toString(ranking));
//		System.out.println("utilities = " + Arrays.toString(utilities));
		
		return utilities;
	}
	
	/**
	 * Computes a vector of utilities, like above, but now for agents. In states
	 * where a player-swap has occurred, the utilities will also be swapped, such
	 * that the utility values can be indexed by original-agent-index rather than
	 * role / colour / player index.
	 * 
	 * @param context
	 * @return The agent utilities.
	 */
	public static double[] agentUtilities(final Context context)
	{
		final double[] utils = utilities(context);
		final double[] agentUtils = new double[utils.length];
		
		for (int p = 1; p < utils.length; ++p)
		{
			agentUtils[p] = utils[context.state().playerToAgent(p)];
		}
		
		return agentUtils;
	}
	
	//-------------------------------------------------------------------------

}
