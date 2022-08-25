package search.minimax;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TLongArrayList;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilityAdvanced;
import metadata.ai.heuristics.terms.NullHeuristic;
import other.AI;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import utils.data_structures.transposition_table.TranspositionTableUBFM;

public class UBFMKilothonContender extends UBFM
{
	/** A learned policy to use in the selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;
	
	private boolean firstTurn;
	
	private int totalNumberOfTurns;
	
	private float avgNumberOfTurns;
	
	private int nbTerminalPlayouts;
	
	private int nbSelectActionCalls;
	
	private float timeLeft;
	
	private boolean stochasticGame = false;
	
	private MCTS UCT_Helper; // for stochastic games

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
		nbSelectActionCalls += 1;
		
		if (firstTurn)
		{
			for (int i=0; i<10; i++)
			{
				Context contextCopy = new TempContext(context);
				
				final List<NaiveActionBasedSelection> agents = new ArrayList<NaiveActionBasedSelection>();
				agents.add(null);
				
				final int nbPlayers = game.players().count();
				for (int j=1; j<=nbPlayers; j++)
				{
					NaiveActionBasedSelection playoutAI = new NaiveActionBasedSelection();
					
					playoutAI.initAI(game, j);
					
					agents.add(playoutAI);
				}
				
				game.playout(contextCopy, new ArrayList<AI>(agents), 0.01, null, 0, 120, ThreadLocalRandom.current()); 

				for (int j=1; j<=nbPlayers; j++)
				{
					totalNumberOfTurns += agents.get(j).getSelectActionNbCalls()*5;
//					System.out.println("nb of turns for agent "+Integer.toString(j)+" in this playout is "+Integer.toString(agents.get(j).getSelectActionNbCalls()));
					nbTerminalPlayouts += 5;
				}
				
				avgNumberOfTurns = ((float) totalNumberOfTurns)/nbTerminalPlayouts;
			}			
			
			firstTurn = false;
		}
		
		float timeForDecision = timeLeft / Math.max(1f,1.5f*(1.5f*avgNumberOfTurns-nbSelectActionCalls));

//		System.out.println("Time allowed for this decision : "+Float.toString(timeForDecision));
		
		final long startTime = System.currentTimeMillis();
		
		Move selectedMove;
		if (!stochasticGame)
			selectedMove = super.selectAction(game, context, timeForDecision, maxIterations, maxDepth);
		else
		{
			selectedMove = UCT_Helper.selectAction(game, context, timeForDecision, maxIterations, maxDepth);
//			System.out.println("UCT dealing with this decision");
		}	
			
		timeLeft -= Math.max(0, (System.currentTimeMillis()-startTime)/1000f );
		
//		System.out.println("Time left : "+Float.toString(timeLeft));
		
		return selectedMove;
	}
	
	/** 
	 * Method to evaluate a state, with heuristics if the state is not terminal.
	 * 
	 * @param context
	 * @param maximisingPlayer
	 * @param nodeHashes
	 * @param depth
	 * @return
	 */
	@Override
	protected float getContextValue
	(
		final Context context,
		final int maximisingPlayer,
		final TLongArrayList nodeHashes,
		final int depth // just used  to fill the depth field in the TT which is not important
	)
	{
		if (context.trial().over() || !context.active(maximisingPlayer))
		{
//			System.out.println("state met at depth "+Integer.toString(depth));
			
			// We want the latest information to have a more import weight
			totalNumberOfTurns += (depth+nbSelectActionCalls)*nbSelectActionCalls;
			nbTerminalPlayouts += nbSelectActionCalls;
			
			avgNumberOfTurns = ((float) totalNumberOfTurns)/nbTerminalPlayouts;
		}
		
		return super.getContextValue(context, maximisingPlayer, nodeHashes, depth);
	}

	/**
	 * Sets the learned policy to use in Selection phase
	 * @param policy The policy.
	 */
	public void setLearnedSelectionPolicy(final SoftmaxPolicy policy)
	{
		learnedSelectionPolicy = policy;
	}

	@Override
	public boolean supportsGame(final Game game)
	{
		// We assume this AI can try to do better than random on stochasticGames
		if (game.hiddenInformation())
			return false;
		
		if (game.hasSubgames())		// Cant properly init most heuristics
			return false;
		
		if (!(game.isAlternatingMoveGame()))
			return false;
		
		return true;
	}
	

	/**
	 * Initialising the AI (almost the same as with AlphaBeta)
	 */
	@Override
	public void initAI(final Game game, final int playerID)
	{
		// Fix the parameters:		
		setSelectionEpsilon(0.2f);
		setTTReset(false);
		setIfFullPlayouts(false);
		savingSearchTreeDescription = false;
		debugDisplay = false;
		
		//Initialise new variables:
		firstTurn = true;
		totalNumberOfTurns = 0;
		avgNumberOfTurns = 0f;
		nbTerminalPlayouts = 0;
		nbSelectActionCalls = 0;
		
		timeLeft = 60f;
		
		if (heuristicsFromMetadata)
		{
			if (debugDisplay) System.out.println("Reading heuristics from game metadata...");
			
			// Read heuristics from game metadata
			final metadata.ai.Ai aiMetadata = game.metadata().ai();
			if (aiMetadata != null && aiMetadata.heuristics() != null)
			{
				heuristicValueFunction = Heuristics.copy(aiMetadata.heuristics());
			}
			else
			{
				// construct default heuristic
				heuristicValueFunction = new Heuristics(new HeuristicTerm[]{
						new Material(null, Float.valueOf(1.f), null, null),
						new MobilityAdvanced(null, Float.valueOf(0.05f))
				});
			}
		}

		if ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null))
		{
			setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0.3f));
			learnedSelectionPolicy.initAI(game, playerID);
		}
		
		if (heuristicValueFunction() != null)
		{
			try
				{ heuristicValueFunction().init(game); }
			catch (Exception e)
			{
				heuristicValueFunction = new Heuristics( new NullHeuristic());
				heuristicValueFunction().init(game);
			}
		}
		
		// reset these things used for visualisation purposes
		estimatedRootScore = 0.f;
		maxHeuristicEval = 0f;
		minHeuristicEval = 0f;
		analysisReport = null;
		
		currentRootMoves = null;
		rootValueEstimates = null;
		
		if (game.isStochasticGame())
		{
			stochasticGame = true;
			System.out.println("game is stochastic...");
			UCT_Helper = MCTS.createUCT();
			UCT_Helper.initAI(game, playerID);
		}
		
		// and these things for ExIt
		lastSearchedRootContext = null; //always null, so useless?
		lastReturnedMove = null;
		
		numPlayersInGame = game.players().count();
		
		transpositionTable = new TranspositionTableUBFM(numBitsPrimaryCodeForTT);
	}
}
