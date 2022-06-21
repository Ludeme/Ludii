package search.minimax;

import game.Game;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import utils.data_structures.transposition_table.TranspositionTable;
import utils.data_structures.transposition_table.TranspositionTable.ABTTData;

/**
 * AI based on Unbounded Best-First Search, using trained action evaluations to complete the heuristic scores.
 * 
 * @author cyprien
 *
 */

public class HybridBFS extends BestFirstSearch
{
	
	//-------------------------------------------------------------------------
	
	/** An epsilon parameter to give to the selection policy which hopefully is not chaging anything*/
	private final float epsilon = 0f;

	/** A learned policy to use in Selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;
	
	/** Number of playouts for each state's evaluation */
	protected int nbPlayoutsPerEvaluation = 10;
	
	/** A MCTS used only as an argument for running the playouts (seems like they are not used by the function for linear playout strategies)*/
	protected MCTS MCTS_Slave = null;
	
	//-------------------------------------------------------------------------

	/** For analysis report: */
	private int nbPlayoutsDone;
	
	//-------------------------------------------------------------------------
	
	public static HybridBFS createHybridBFS ()
	{
		return new HybridBFS();
	}
	
	/**
	 * Constructor:
	 */
	
	public HybridBFS ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(epsilon));
		MCTS_Slave = MCTS.createUCT();
		friendlyName = "Hybrid BFS";
		
		return;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected Move BFSSelection
	(
			final Game game,
			final Context context,
			final double maxSeconds,
			final int depthLimit
	)
	{
		nbPlayoutsDone = 0;
		
		final Move res = super.BFSSelection(game, context, maxSeconds,depthLimit);
		
		analysisReport += "("+Integer.toString(nbPlayoutsDone)+" playouts done)";
		
		return res;
	}
	
	@Override
	protected float getContextValue
	(
		final Context context,
		final int maximisingPlayer,
		final int previousMover,
		final float inAlpha,
		final float inBeta
	)
	{
		boolean valueRetrievedFromMemory = false;
		float heuristicScore = 0;
		
		final long zobrist = context.state().fullHash();
		final ABTTData tableData;
		if (transpositionTable != null)
		{
			tableData = transpositionTable.retrieve(zobrist);
			
			if (tableData != null)
			{
				// Already searched for data in TT, use results
				switch(tableData.valueType)
				{
				case TranspositionTable.EXACT_VALUE:
					heuristicScore = tableData.value;
					valueRetrievedFromMemory = true;
					break;
				case TranspositionTable.LOWER_BOUND:
					if ((previousMover == maximisingPlayer)&&(tableData.value > inBeta)) {
						heuristicScore = tableData.value;
						// the value wouldn't be exact but we don't care since the principal path will change
						valueRetrievedFromMemory = true;
					};
					break;
				case TranspositionTable.UPPER_BOUND:
					if ((previousMover != maximisingPlayer)&&(tableData.value < inAlpha)) {
						heuristicScore = tableData.value;
						valueRetrievedFromMemory = true;
					};
					break;
				case TranspositionTable.INVALID_VALUE:
					System.err.println("INVALID TRANSPOSITION TABLE DATA: INVALID VALUE");
					break;
				default:
					// bounds are not used up to this point
					break;
				}
			}
		}
		
		// Only compute heuristicScore if we didn't have a score registered in the TT
		if (!valueRetrievedFromMemory) {
			
			if (context.trial().over() || !context.active(maximisingPlayer))
			{
				// terminal node (at least for maximising player)
				heuristicScore = (float) RankUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT;
			}
			else
			{
				double scoreMean = 0.;
				
				for (int i = 0; i<nbPlayoutsPerEvaluation; i++)
				{
					final Context contextCopy = copyContext(context);//check that it is not done again
					
					nbPlayoutsDone += 1;
					
					learnedSelectionPolicy.runPlayout(MCTS_Slave, contextCopy);
					
					scoreMean += RankUtils.agentUtilities(contextCopy)[maximisingPlayer] * 20 / nbPlayoutsPerEvaluation;
				}
				
				heuristicScore = (float) scoreMean;
			}
			
			if (transpositionTable != null)
			{
				transpositionTable.store(null, zobrist, heuristicScore, 0, TranspositionTable.EXACT_VALUE);
			}
			
			nbStatesEvaluated += 1;
		};

		minHeuristicEval = Math.min(minHeuristicEval, heuristicScore);
		maxHeuristicEval = Math.max(maxHeuristicEval, heuristicScore);
		
		return heuristicScore;
	}
;
	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game,  playerID);
		
		// Initialise feature sets for selection policy
		if (learnedSelectionPolicy != null)
		{
			learnedSelectionPolicy.initAI(game, playerID);
		}
		
		return;
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
}
