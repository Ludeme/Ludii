package metrics;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import other.RankUtils;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.TopologyElement;
import other.trial.Trial;
import search.minimax.AlphaBetaSearch;

/**
 * Helpful functions for metric analysis.
 * 
 * @author Matthew.Stephenson
 */
public class Utils 
{

	//-------------------------------------------------------------------------

	/**
	 * @param game
	 * @param rngState
	 * @return A new context for a given game and RNG.
	 */
	public static Context setupNewContext(final Game game, final RandomProviderState rngState)
	{
		final Context context = new Context(game, new Trial(game));
		context.rng().restoreState(rngState);
		context.reset();
		context.state().initialise(context.currentInstanceContext().game());
		game.start(context);
		context.trial().setStatus(null);
		return context;
	}
	
	public static Context setupTrialContext(final Game game, final RandomProviderState rngState, final Trial trial)
	{
		final Context context = setupNewContext(game, rngState);
		for (final Move m : trial.generateRealMovesList())
			game.apply(context, m);
		return context;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * The number of pieces on the board
	 */
	public static int numPieces(final Context context)
	{
		int numPieces = 0;
		final ContainerState cs = context.containerState(0);
		
		for (int i = 0; i < context.game().board().topology().getAllGraphElements().size(); i++)
		{
			final TopologyElement element = context.game().board().topology().getAllGraphElements().get(i);
			if (context.game().isStacking())
				numPieces += cs.sizeStack(element.index(), element.elementType());
			else
				numPieces += cs.count(element.index(), element.elementType());
		}
		
		return numPieces;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A list of all board sites which have a piece on them.
	 */
	public static ArrayList<TopologyElement> boardAllSitesCovered(final Context context)
	{
		final ArrayList<TopologyElement> boardSitesCovered = new ArrayList<>();
		final ContainerState cs = context.containerState(0);
		
		for (final TopologyElement topologyElement : context.game().board().topology().getAllGraphElements())
			if (cs.what(topologyElement.index(), topologyElement.elementType()) != 0)
				boardSitesCovered.add(topologyElement);
		
		return boardSitesCovered;
	}
	
	/**
	 * A list of all used board sites which have a piece on them.
	 */
	public static ArrayList<TopologyElement> boardUsedSitesCovered(final Context context)
	{
		final ArrayList<TopologyElement> boardSitesCovered = new ArrayList<>();
		final ContainerState cs = context.containerState(0);
		
		for (final TopologyElement topologyElement : context.game().board().topology().getAllUsedGraphElements(context.game()))
			if (cs.what(topologyElement.index(), topologyElement.elementType()) != 0)
				boardSitesCovered.add(topologyElement);
		
		return boardSitesCovered;
	}
	
	/**
	 * A list of all default board sites which have a piece on them.
	 */
	public static ArrayList<TopologyElement> boardDefaultSitesCovered(final Context context)
	{
		final ArrayList<TopologyElement> boardSitesCovered = new ArrayList<>();
		final ContainerState cs = context.containerState(0);
		
		for (final TopologyElement topologyElement : context.game().board().topology().getGraphElements(context.game().board().defaultSite()))
			if (cs.what(topologyElement.index(), topologyElement.elementType()) != 0)
				boardSitesCovered.add(topologyElement);
		
		return boardSitesCovered;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns an evaluation between -1 and 1 for the current (context) state of the mover.
	 */
	public static double evaluateState(final Evaluation evaluation, final Context context, final int mover)
	{
		final Context instanceContext = context.currentInstanceContext();
		
		//TODO need to handle simul games properly
		if (instanceContext.game().isSimultaneousMoveGame())
			return 0.0;
		
		final AlphaBetaSearch agent = new AlphaBetaSearch(false);
		agent.initAI(instanceContext.game(), mover);
		
		final long rngHashcode = Arrays.hashCode(((RandomProviderDefaultState) instanceContext.rng().saveState()).getState());
		final long stateAndMoverHash = instanceContext.state().fullHash() ^ mover ^ rngHashcode;
		
		if (instanceContext.trial().over() || !instanceContext.active(mover))
		{
			// Terminal node (at least for mover)
			return RankUtils.agentUtilities(instanceContext)[mover];
		}
		else if (evaluation.stateEvaluationCacheContains(Long.valueOf(stateAndMoverHash)))
		{
			return evaluation.getStateEvaluationCacheValue(Long.valueOf(stateAndMoverHash));
		}
		else
		{
			// Heuristic evaluation
			float heuristicScore = agent.heuristicValueFunction().computeValue(instanceContext, mover, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
			
			for (final int opp : agent.opponents(mover))
			{
				if (instanceContext.active(opp))
					heuristicScore -= agent.heuristicValueFunction().computeValue(instanceContext, opp, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
				else if (instanceContext.winners().contains(opp))
					heuristicScore -= AlphaBetaSearch.PARANOID_OPP_WIN_SCORE;
			}
			
			// TODO, this seems to only work for two player games.
			// Invert scores if players swapped
			if (instanceContext.state().playerToAgent(mover) != mover)
				heuristicScore = -heuristicScore;

			// Normalise to between -1 and 1
			final double heuristicScoreTanh = Math.tanh(heuristicScore);
			
			// Convert score to between range 0 and 1, rather than -1 and 1
			// heuristicScoreTanh = (heuristicScore + 1.f) / 2.f;
			
			evaluation.putStateEvaluationCacheValue(stateAndMoverHash, heuristicScoreTanh);
			
			return heuristicScoreTanh;
		}
	}
	
	/**
	 * Returns an evaluation of a given move from the current (context) state.
	 */
	public static Double evaluateMove(final Evaluation evaluation, final Context context, final Move move)
	{
		if (context.game().hasSubgames())
			return null;
		
		final long rngHashcode = Arrays.hashCode(((RandomProviderDefaultState) context.rng().saveState()).getState());
		final long stateAndMoveHash = context.state().fullHash() ^ move.toTrialFormat(context).hashCode() ^ rngHashcode;
		
		if (evaluation.stateAfterMoveEvaluationCacheContains(stateAndMoveHash))
			return evaluation.getStateAfterMoveEvaluationCache(stateAndMoveHash);
		
		final TempContext copyContext = new TempContext(context);
		copyContext.game().apply(copyContext, move);
		final double stateEvaluationAfterMove = evaluateState(evaluation, copyContext, move.mover());
		evaluation.putStateAfterMoveEvaluationCache(stateAndMoveHash, stateEvaluationAfterMove);

		return stateEvaluationAfterMove;
	}
	
	/**
	 * Returns an evaluation between 0 and 1 for the current (context) state of each player.
	 */
	public static ArrayList<Double> allPlayerStateEvaluations(final Evaluation evaluation, final Context context)
	{
		if (context.game().hasSubgames())
			return null;
		
		final ArrayList<Double> allPlayerStateEvalations = new ArrayList<>();
		allPlayerStateEvalations.add(Double.valueOf(-1.0));
		for (int i = 1; i <= context.game().players().count(); i++)
			allPlayerStateEvalations.add(Double.valueOf(evaluateState(evaluation, context, i)));
		return allPlayerStateEvalations;
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Get the highest ranked players based on the final player rankings. 
	 */
	public static ArrayList<Integer> highestRankedPlayers(final Trial trial, final Context context)
	{
		if (context.game().hasSubgames())
			return null;
		
		final ArrayList<Integer> highestRankedPlayers = new ArrayList<>();
		final double highestRanking = Arrays.stream(trial.ranking()).max().getAsDouble();
		for (int i = 1; i <= context.game().players().count(); i++)
			if (trial.ranking()[i] == highestRanking)
				highestRankedPlayers.add(i);
		
		return highestRankedPlayers;
	}
	
	//-------------------------------------------------------------------------
	
//	public static double UCTEvaluateState(final Context context, final int mover)
//	{
//		if (!context.active(mover))
//			return RankUtils.rankToUtil(context.trial().ranking()[mover], context.game().players().count());
//		
//		final MCTS agent = MCTS.createUCT();
//		agent.initAI(context.game(), mover);
//		agent.setAutoPlaySeconds(-1);
//		agent.selectAction(context.game(), context, 0.1, -1, -1);		
//		return agent.estimateValue();
//	}
	
	//-------------------------------------------------------------------------
	
//	public static double ABEvaluateState(final Context context, final int mover)
//	{
//		final AlphaBetaSearch agent = new AlphaBetaSearch(false);
//		agent.initAI(context.game(), mover);
//		return agent.alphaBeta(context, 0, -1, -1, mover, -1);
//	}
	
	//-------------------------------------------------------------------------
	
//	/*
//	 * Returns the heuristic estimation of the current state of a context object, for a given player Id.
//	 * Same code used in AlphaBetaSearch.
//	 */
//	public static double HeuristicEvaluateState(final Context context, final int mover)
//	{		
//		final AlphaBetaSearch agent = new AlphaBetaSearch(false);
//		agent.initAI(context.game(), mover);
//		
//		if (context.trial().over() || !context.active(mover))
//		{
//			// Terminal node (at least for mover)
//			return RankUtils.agentUtilities(context)[mover] * AlphaBetaSearch.BETA_INIT;
//		}
//		else
//		{
//			// Heuristic evaluation
//			float heuristicScore = agent.heuristicValueFunction().computeValue(context, mover, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
//			
//			for (final int opp : agent.opponents(mover))
//			{
//				if (context.active(opp))
//					heuristicScore -= agent.heuristicValueFunction().computeValue(context, opp, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
//				else if (context.winners().contains(opp))
//					heuristicScore -= AlphaBetaSearch.PARANOID_OPP_WIN_SCORE;
//			}
//			
//			// Invert scores if players swapped
//			if (context.state().playerToAgent(mover) != mover)
//				heuristicScore = -heuristicScore;
//
//			return Math.tanh(heuristicScore);
//		}
//	}
	
	//-------------------------------------------------------------------------
	
}
