package search.flat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import utils.AIUtils;

/**
 * A simple Flat Monte-Carlo AI.
 * 
 * @author Dennis Soemers
 */
public class FlatMonteCarlo extends AI 
{
	
	//-------------------------------------------------------------------------
	
	/** Our player index */
	protected int player = -1;
	
	/** Sums of scores of the last search we ran */
	protected int[] lastScoreSums = null;
	
	/** Visit counts of the last search we ran */
	protected int[] lastVisitCounts = null;
	
	/** List of legal actions for which we ran last search */
	protected FastArrayList<Move> lastActionList = null;
	
	/** We'll automatically return our move after at most this number of seconds if we only have one move */
	protected double autoPlaySeconds = 0.5;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public FlatMonteCarlo()
	{
		friendlyName = "Flat MC";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		final long startTime = System.currentTimeMillis();
		long stopTime = (maxSeconds > 0.0) ? startTime + (long) (maxSeconds * 1000) : Long.MAX_VALUE;
		final int maxIts = (maxIterations >= 0) ? maxIterations : Integer.MAX_VALUE;
		
		FastArrayList<Move> legalMoves = game.moves(context).moves();
		//System.out.println("legal moves for all players = " + legalMoves);

		if (!game.isAlternatingMoveGame())
			legalMoves = AIUtils.extractMovesForMover(legalMoves, player);
		
		//System.out.println("legal moves for player " + player + " = " + legalMoves);
		final int numActions = legalMoves.size();
		
		if (numActions == 1)
		{
			// play faster if we only have one move available anyway
			if (autoPlaySeconds >= 0.0 && autoPlaySeconds < maxSeconds)
				stopTime = startTime + (long) (autoPlaySeconds * 1000);
		}
		
		final int[] sumScores = new int[numActions];
		final int[] numVisits = new int[numActions];
				
		int numIterations = 0;

		// Simulate until we have to stop
		while (numIterations < maxIts && System.currentTimeMillis() < stopTime)
		{
			final Context copyContext = copyContext(context);
			final Model model = copyContext.model();
			
			model.startNewStep(copyContext, null, 1.0, -1, -1, 0.0, false, false, false);
			
			final int firstAction = ThreadLocalRandom.current().nextInt(numActions);
			model.applyHumanMove(copyContext, legalMoves.get(firstAction), player);
			
			if (!model.isReady())
			{
				// this means we're in a simultaneous-move game, randomly select actions for opponents
				model.randomStep(copyContext, null, null);
			}
			
			if (!copyContext.trial().over())
			{
				copyContext.game().playout
				(
					copyContext, 
					null, 
					1.0, 
					null,  
					0, 
					-1,
					ThreadLocalRandom.current()
				);
			}
			
			numVisits[firstAction] += 1;
			
			final double[] utilities = RankUtils.utilities(copyContext);
			sumScores[firstAction] += utilities[player];
			
			++numIterations;
		}
		
		final List<Move> bestActions = new ArrayList<Move>();
        double maxAvgScore = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < numActions; ++i) 
        {
            final double avgScore = numVisits[i] == 0 ? -100.0 : (double) sumScores[i] / numVisits[i];
            //System.out.println("avgScore for " + legalMoves.get(i) + " = " + avgScore);

            if (avgScore > maxAvgScore)
            {
            	maxAvgScore = avgScore;
            	bestActions.clear();
            	bestActions.add(legalMoves.get(i));
            }
            else if (avgScore == maxAvgScore)
            {
            	bestActions.add(legalMoves.get(i));
            }
        }
        
        lastScoreSums = sumScores;
        lastVisitCounts = numVisits;
        lastActionList = new FastArrayList<Move>(legalMoves);
        
        //System.out.println("returning best action");
        return bestActions.get(ThreadLocalRandom.current().nextInt(bestActions.size()));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		this.player = playerID;
		lastScoreSums = null;
		lastVisitCounts = null;
		lastActionList = null;
	}
	
	/**
	 * @return Sums of scores of last search
	 */
	public int[] lastScoreSums()
	{
		return lastScoreSums;
	}
	
	/**
	 * @return Visit counts of last search
	 */
	public int[] lastVisitCounts()
	{
		return lastVisitCounts;
	}
	
	/**
	 * @return List of legal actions of last search
	 */
	public FastArrayList<Move> lastActionList()
	{
		return lastActionList;
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isDeductionPuzzle())
			return false;
		
		return true;
	}
	
	@Override
	public AIVisualisationData aiVisualisationData()
	{
		if (lastActionList == null)
			return null;
		
		final FVector aiDistribution = new FVector(lastActionList.size());
		final FVector valueEstimates = new FVector(lastActionList.size());
		final FastArrayList<Move> moves = new FastArrayList<>();

		for (int i = 0; i < lastActionList.size(); ++i)
		{
			aiDistribution.set(i, (float) ((double) lastScoreSums[i] / lastVisitCounts[i]));
			valueEstimates.set(i, (float) ((double) lastScoreSums[i] / lastVisitCounts[i]));
			moves.add(lastActionList.get(i));
		}
		
		return new AIVisualisationData(aiDistribution, valueEstimates, moves);
	}
	
	//-------------------------------------------------------------------------

}
