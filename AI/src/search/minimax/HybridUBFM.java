package search.minimax;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import game.types.play.RoleType;
import metadata.ai.features.Features;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import utils.data_structures.transposition_table.TranspositionTableBFS;
import utils.data_structures.transposition_table.TranspositionTableBFS.BFSTTData;

/**
 * AI based on Unbounded Best-First Search, using trained action evaluations to complete the heuristic scores with informed playouts.
 * Can also work with no trained features, and will then execute random playouts.
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
	
	/** Coeficient to make the playout evaluation comparable to the heuristic score (arbitrarily chosen)*/
	protected float normalisingCoeficient = 10f;
	
	/** A MCTS used only as an argument for running the playouts (seems like they are not used by the function for linear playout strategies)*/
	protected MCTS MCTS_Slave = null;
	
	//-------------------------------------------------------------------------

	/** A learned policy to use in the selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;
	
	/** For analysis report: */
	private int nbPlayoutsDone;
	
	//-------------------------------------------------------------------------
	
	public static HybridUBFM createHybridBFS ()
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
		final List<Long> nodeHashes,
		final int depth
	)
	{
		final State state = context.state();
		final long zobrist = state.fullHash();
		final int newMover = state.playerToAgent(state.mover());
		
		boolean valueRetrievedFromMemory = false;
		float contextScore = 0;
		
		final BFSTTData tableData;
		if (transpositionTable != null)
		{
			tableData = transpositionTable.retrieve(zobrist);
			
			if (tableData != null)
			{
				// Already searched for data in TT, use results
				switch(tableData.valueType)
				{
				case TranspositionTableBFS.EXACT_VALUE:
					contextScore = tableData.value;
					valueRetrievedFromMemory = true;
					break;
				case TranspositionTableBFS.INVALID_VALUE:
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
				contextScore = (float) RankUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT;
			}
			else
			{
				float scoreMean = 0f;
				float heuristicScore = 0;
				
				heuristicScore = heuristicValueFunction().computeValue(
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
					final Context contextCopy = copyContext(context);//check that it is not done again
					
					nbPlayoutsDone += 1;
					
					if (learnedSelectionPolicy != null)
						learnedSelectionPolicy.runPlayout(MCTS_Slave, contextCopy);
					else
						context.game().playout(contextCopy, null, 1.0, null, 0, 200, ThreadLocalRandom.current());
					
					scoreMean += RankUtils.agentUtilities(contextCopy)[maximisingPlayer] * normalisingCoeficient / nbPlayoutsPerEvaluation;
				}
				contextScore = heuristicScore * heuristicScoreWeight + scoreMean * (1f - heuristicScoreWeight);
				
				if (debugDisplay)
					if (ThreadLocalRandom.current().nextFloat()<0.1)
						System.out.printf("heuristic score is %.5g while avg score is %.5g -> final value is %.5g\n",heuristicScore,scoreMean,contextScore);
			}

			// Every time a state is evaluated, we store the value in the transposition table (worth?)
			if (transpositionTable != null)
				transpositionTable.store(null, zobrist, contextScore, depth, TranspositionTableBFS.EXACT_VALUE, null);
			
			nbStatesEvaluated += 1;
		};

		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfnodeHashes(nodeHashes)+","+Float.toString(contextScore)+","+((newMover==maximisingPlayer)? 1:2)+"),\n");
		
		minHeuristicEval = Math.min(minHeuristicEval, contextScore);
		maxHeuristicEval = Math.max(maxHeuristicEval, contextScore);
		
		return contextScore;
	}

	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game,  playerID);
		
		boolean featuresAvailable = false;
		if (game.metadata().ai().features() != null)
		{
			final Features featuresMetadata = game.metadata().ai().features();
			if (featuresMetadata.featureSets().length == 1 && featuresMetadata.featureSets()[0].role() == RoleType.Shared)
				featuresAvailable = true;
		}
		else if (game.metadata().ai().trainedFeatureTrees() != null)
		{
			featuresAvailable = true;
		}
		
		if (featuresAvailable)
		{
			setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(epsilon));
			MCTS_Slave = MCTS.createUCT();
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
