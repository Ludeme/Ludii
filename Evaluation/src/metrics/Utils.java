package metrics;

import java.util.ArrayList;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import other.RankUtils;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.TopologyElement;
import other.trial.Trial;
import search.mcts.MCTS;
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
			TopologyElement element = context.game().board().topology().getAllGraphElements().get(i);
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
		
		for (TopologyElement topologyElement : context.game().board().topology().getAllGraphElements())
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
		
		for (TopologyElement topologyElement : context.game().board().topology().getAllUsedGraphElements(context.game()))
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
		
		for (TopologyElement topologyElement : context.game().board().topology().getGraphElements(context.game().board().defaultSite()))
			if (cs.what(topologyElement.index(), topologyElement.elementType()) != 0)
				boardSitesCovered.add(topologyElement);
		
		return boardSitesCovered;
	}
	
	//-------------------------------------------------------------------------
	
//	public static double UCTEvaluateState(final Context context, final int mover)
//	{
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
	
	/*
	 * Returns the heuristic estimation of the current state of a context object, for a given player Id.
	 * 
	 * Note. Make sure to assign this value to the player at context.state.playerToAgent 
	 * playerScores[context.state.playerToAgent(mover)] = HeuristicEvaluateState(context, mover);
	 */
	public static double HeuristicEvaluateState(final Context context, final int mover)
	{		
		if (context.trial().over() || !context.active(mover))
			return (float) RankUtils.agentUtilities(context)[mover] * AlphaBetaSearch.BETA_INIT;
		
		final AlphaBetaSearch agent = new AlphaBetaSearch(false);
		agent.initAI(context.game(), mover);
		final float heuristicScore = agent.heuristicValueFunction().computeValue(context, mover, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
		return heuristicScore;
	}
	
	public static double HeuristicEvaluateMove(final Context context, final int mover, final Move move)
	{
		TempContext copyContext = new TempContext(context);
		copyContext.game().apply(copyContext, move);
		return HeuristicEvaluateState(copyContext, mover);
	}
	
	//-------------------------------------------------------------------------
	
}
