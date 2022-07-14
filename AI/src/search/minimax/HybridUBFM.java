package search.minimax;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TLongArrayList;
import other.RankUtils;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import utils.data_structures.transposition_table.TranspositionTableUBFM;
import utils.data_structures.transposition_table.TranspositionTableUBFM.UBFMTTData;

/**
 * AI based on Unbounded Best-First Search, using trained action evaluations to complete the heuristic scores with informed playouts.
 * Can also work with no trained features, and will then execute random playouts.
 * 
 * (the formula for the evaluation of a context is v(s) = h(s) * x + (1-x) * p(s) * max(abs(h))
 *  where h is the heuristic score, x is heiristicScoreWeight parameter, p(s) is the average
 *  ranking utility obtained in the playouts (between -1 and 1) and max(abs(h)) is the 
 *  maximum absolute value of heuristics observed up to now. An effect of this choice is that 
 *  the playouts will have less impact when the AI doesn't know much about the heuristics range.)
 *
 * 
 * @author cyprien
 */

public class HybridUBFM extends UBFM
{
	
	/** An epsilon parameter to give to the selection policy */
	private final float epsilon = 0.5f;
	
	/** Number of playouts for each state's evaluation */
	protected int nbPlayoutsPerEvaluation = 10;
	
	/** Weight of heuristics score in state evaluation */
	protected float heuristicScoreWeight = 0.5f;
	
	//-------------------------------------------------------------------------

	/** A learned policy to use in the selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;
	
	/** For analysis report: */
	private int nbPlayoutsDone;
	
	/** Maximum absolute value recorded for heuristic scores */
	protected float maxAbsHeuristicScore = 0f;
	
	//-------------------------------------------------------------------------
	
	public static HybridUBFM createHybridUBFM ()
	{
		return new HybridUBFM();
	}
	
	/**
	 * Constructor:
	 */
	public HybridUBFM ()
	{
		super();
		friendlyName = "Hybrid UBFM";
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
		final TLongArrayList nodeHashes,
		final int depth
	)
	{
		final State state = context.state();
		final long zobrist = state.fullHash(context);
		final int newMover = state.playerToAgent(state.mover());
		
		boolean valueRetrievedFromMemory = false;
		float contextScore = Float.NaN;
		
		final UBFMTTData tableData;
		if (transpositionTable != null)
		{
			tableData = transpositionTable.retrieve(zobrist);
			
			if (tableData != null)
			{
				// Already searched for data in TT, use results
				switch(tableData.valueType)
				{
				case TranspositionTableUBFM.EXACT_VALUE:
					contextScore = tableData.value;
					valueRetrievedFromMemory = true;
					break;
				case TranspositionTableUBFM.INVALID_VALUE:
					System.err.println("INVALID TRANSPOSITION TABLE DATA: INVALID VALUE");
					break;
				default:
					System.err.println("INVALID TRANSPOSITION TABLE DATA: INVALID VALUE");
					break;
				}
			}
		}
		
		// Only compute heuristicScore if we didn't have a score registered in the TT
		if (!valueRetrievedFromMemory)
		{
			if (context.trial().over() || !context.active(maximisingPlayer))
			{
				// terminal node (at least for maximising player)
				contextScore = (float) RankUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT;
			}
			else
			{
				float scoreMean = 0f;
				
				float heuristicScore = heuristicValueFunction().computeValue(
						context, maximisingPlayer, ABS_HEURISTIC_WEIGHT_THRESHOLD);
			
				for (final int opp : opponents(maximisingPlayer))
				{
					if (context.active(opp))
						heuristicScore -= heuristicValueFunction().computeValue(context, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
					else if (context.winners().contains(opp))
						heuristicScore -= PARANOID_OPP_WIN_SCORE;
				}
				
				for (int i = 0; i<nbPlayoutsPerEvaluation; i++)
				{
					final Context contextCopy = new TempContext(context);
					
					nbPlayoutsDone += 1;
					
					if (learnedSelectionPolicy != null)
						learnedSelectionPolicy.runPlayout(null, contextCopy);
					else
						context.game().playout(contextCopy, null, 1.0, null, 0, 200, ThreadLocalRandom.current()); // arbitrary max 200 moves 
					
					scoreMean += RankUtils.agentUtilities(contextCopy)[maximisingPlayer] * maxAbsHeuristicScore / nbPlayoutsPerEvaluation;
				}
				contextScore = heuristicScore * heuristicScoreWeight + scoreMean * (1f - heuristicScoreWeight);
				
				if (debugDisplay)
					if (ThreadLocalRandom.current().nextFloat()<0.1)
						System.out.printf("heuristic score is %.5g while avg score is %.5g -> final value is %.5g\n",heuristicScore,scoreMean,contextScore);

				
				minHeuristicEval = Math.min(minHeuristicEval, heuristicScore);
				maxHeuristicEval = Math.max(maxHeuristicEval, heuristicScore);
				
				maxAbsHeuristicScore = Math.max(maxAbsHeuristicScore, Math.abs(heuristicScore));
			
			}

			if (transpositionTable != null)
				transpositionTable.store(null, zobrist, contextScore, depth, TranspositionTableUBFM.EXACT_VALUE, null);
			
			nbStatesEvaluated += 1;
		};

		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfNodeHashes(nodeHashes)+","+Float.toString(contextScore)+","+((newMover==maximisingPlayer)? 1: 2)+"),\n");
		
		
		return contextScore;
	}

	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game,  playerID);
		
		if ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null))
		{
			setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(epsilon));
			learnedSelectionPolicy.initAI(game, playerID);
		}
		maxAbsHeuristicScore = 0;
		
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
	
	/**
	 * Sets the number of playouts per context evaluations.
	 * @param n
	 */
	public void setPlayoutsPerEvaluation(final int n)
	{
		nbPlayoutsPerEvaluation = n;
	}
	
	/**
	 * Set the weight of the heuristic evaluation function in the evaluation of a move.
	 * @param value
	 */
	public void setHeuristicScoreWeight(final float value)
	{
		heuristicScoreWeight = value;
	}
}
