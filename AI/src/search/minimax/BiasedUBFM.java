package search.minimax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.Game;
import gnu.trove.list.array.TLongArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import utils.data_structures.ScoredIndex;

/**
 * AI based on Unbounded Best-First Minimax, which uses the action evaluation to select a small number 
 * of actions that will really be simulated (the most promising ones). If selectionEpsilon != 0, then any other move can still be randomly 
 * picked for exploration with a probability of selectionEpsilon.
 * 
 * @author cyprien
 */
public class BiasedUBFM extends UBFM
{
	
	/** Number of moves that are really evaluated with the heuristics at each step of the exploration. */
	private int nbStateEvaluationsPerNode = 6;
	
	//-------------------------------------------------------------------------

	/** A learned policy to use in Selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;

	//-------------------------------------------------------------------------

	public static BiasedUBFM createBiasedUBFM ()
	{
		return new BiasedUBFM();
	}
	
	/**
	 * Constructor:
	 */
	public BiasedUBFM ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Biased UBFM";
	}

	/**
	 * Constructor
	 * @param heuristics
	 */
	public BiasedUBFM(final Heuristics heuristics)
	{
		super(heuristics);
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Biased UBFM";
	}
	
	//-------------------------------------------------------------------------

	@Override
	protected FVector estimateMovesValues
	(
		final FastArrayList<Move> legalMoves,
		final Context context,
		final int maximisingPlayer,
		final TLongArrayList nodeHashes,
		final int depth,
		final long stopTime
	)
	{
		final int numLegalMoves = legalMoves.size();
		final Game game = context.game();
		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		
		final List<ScoredIndex> consideredMoveIndices = new ArrayList<ScoredIndex>(numLegalMoves);
		
		for (int i = 0; i < numLegalMoves; ++i)
		{
			final Move m = legalMoves.get(i);
			
			final float actionValue = learnedSelectionPolicy.computeLogit(context,m);
			
			consideredMoveIndices.add(new ScoredIndex(i,actionValue));
		}
		Collections.sort(consideredMoveIndices);

		final FVector moveScores = new FVector(numLegalMoves);
		for (int i = 0; i < numLegalMoves; ++i)
		{
			// filling default score for each moves:
			moveScores.set(i, (mover==maximisingPlayer)? -BETA_INIT+1: BETA_INIT-1);
		}
		
		for (int k = 0; k < Math.min(nbStateEvaluationsPerNode, numLegalMoves); k++)
		{
			final int i = consideredMoveIndices.get(k).index;
			
			final Move m = legalMoves.get(i);
			final Context contextCopy = new TempContext(context);
			
			game.apply(contextCopy, m);

			nodeHashes.add(contextCopy.state().fullHash(contextCopy));
			final float heuristicScore = getContextValue(contextCopy, maximisingPlayer, nodeHashes,depth);
			nodeHashes.removeAt(nodeHashes.size()-1);
			
			moveScores.set(i, heuristicScore);
			
			if ((System.currentTimeMillis() >= stopTime) || wantsInterrupt)
			{
				break;
			}
		}
		
		return moveScores;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game,  playerID);
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
			learnedSelectionPolicy.initAI(game, playerID);
		
		return;
	}

	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		if (game.hasSubgames())		// Cant properly init most heuristics
			return false;
		
		if (!(game.isAlternatingMoveGame()))
			return false;
		
		return ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null));
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets the learned policy to use in Selection phase
	 * @param policy The policy.
	 */
	public void setLearnedSelectionPolicy(final SoftmaxPolicy policy)
	{
		learnedSelectionPolicy = policy;
	}
	
	/**
	 * Sets the number of moves that will be really evaluated with a simulation and a call to the heuristicValue function.
	 * @param value
	 */
	public void setNbStateEvaluationsPerNode(final int value)
	{
		nbStateEvaluationsPerNode = value;
	}
}
