package other.model;

import java.util.List;
import java.util.Random;

import game.Game;
import game.match.Subgame;
import game.rules.play.moves.Moves;
import other.AI;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;

/**
 * Model for multi-game Matches
 *
 * @author Dennis Soemers
 */
public class MatchModel extends Model
{
	
	//-------------------------------------------------------------------------
	
	/** Model to use within the current instance */
	protected transient Model currentInstanceModel = null;

	//-------------------------------------------------------------------------

	@Override
	public Move applyHumanMove(final Context context, final Move move, final int player)
	{
		return currentInstanceModel.applyHumanMove(context, move, player);
	}

	@Override
	public Model copy()
	{
		return new MatchModel();
	}

	@Override
	public boolean expectsHumanInput()
	{
		return currentInstanceModel != null && currentInstanceModel.expectsHumanInput();
	}

	@Override
	public List<AI> getLastStepAIs()
	{
		return currentInstanceModel.getLastStepAIs();
	}

	@Override
	public List<Move> getLastStepMoves()
	{
		return currentInstanceModel.getLastStepMoves();
	}

	@Override
	public synchronized void interruptAIs()
	{
		if (currentInstanceModel != null)
			currentInstanceModel.interruptAIs();
	}

	@Override
	public boolean isReady()
	{
		return currentInstanceModel == null || currentInstanceModel.isReady();
	}

	@Override
	public boolean isRunning()
	{
		return currentInstanceModel != null && currentInstanceModel.isRunning();
	}

	@Override
	public synchronized void randomStep
	(
		final Context context, 
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback
	)
	{
		currentInstanceModel.randomStep(context, inPreAgentMoveCallback, inPostAgentMoveCallback);
	}
	
	/**
	 * Resets this MatchModel's current instance Model back to null
	 */
	public void resetCurrentInstanceModel()
	{
		currentInstanceModel = null;
	}
	
	@Override
	public boolean verifyMoveLegal(final Context context, final Move move)
	{
		return context.subcontext().model().verifyMoveLegal(context, move);
	}

	//-------------------------------------------------------------------------

	@Override
	public synchronized void startNewStep
	(
		final Context context,
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final boolean block,
		final boolean forceThreaded,
		final boolean forceNotThreaded,
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback
	)
	{
		startNewStep(context,ais,maxSeconds,maxIterations,maxSearchDepth,minSeconds,block,forceThreaded,forceNotThreaded,inPreAgentMoveCallback,inPostAgentMoveCallback,false,null);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public synchronized void startNewStep
	(
		final Context context,
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final boolean block,
		final boolean forceThreaded,
		final boolean forceNotThreaded,
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback,
		final boolean checkMoveValid,
		final MoveMessageCallback moveMessageCallback
	)
	{
		currentInstanceModel = context.subcontext().model();

		currentInstanceModel.startNewStep
		(
			context, 
			ais, 
			maxSeconds, 
			maxIterations, 
			maxSearchDepth, 
			minSeconds, 
			block, 
			forceThreaded, 
			forceNotThreaded, 
			inPreAgentMoveCallback, 
			inPostAgentMoveCallback,
			checkMoveValid,
			moveMessageCallback
		);
	}

	@Override
	public void unpauseAgents
	(
		final Context context,
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback,
		final boolean checkMoveValid,
		final MoveMessageCallback moveMessageCallback
	)
	{
		currentInstanceModel.unpauseAgents
		(
			context, 
			ais, 
			maxSeconds, 
			maxIterations, 
			maxSearchDepth, 
			minSeconds, 
			inPreAgentMoveCallback, 
			inPostAgentMoveCallback,
			checkMoveValid,
			moveMessageCallback
		);
	}

	//-------------------------------------------------------------------------

	@Override
	public List<AI> getLiveAIs()
	{
		return currentInstanceModel.getLiveAIs();
	}

	//-------------------------------------------------------------------------

	@Override
	public Trial playout
	(
		final Context context, final List<AI> ais, final double thinkingTime,
		final PlayoutMoveSelector playoutMoveSelector, final int maxNumBiasedActions, 
		final int maxNumPlayoutActions, final Random random
	)
	{
		final Game match = context.game();
		final Trial matchTrial = context.trial();
		
		int numActionsApplied = 0;
		
		while
		(
			!matchTrial.over()
			&&
			(maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied)
		)
		{
			final Subgame instance = match.instances()[context.currentSubgameIdx()];
			final Game instanceGame = instance.getGame();
			
			final Context subcontext = context.subcontext();
			final Trial subtrial = subcontext.trial();
			final int numStartMoves = subtrial.numMoves();
			
			// May have to tell subtrial to store auxiliary data
			if (context.trial().auxilTrialData() != null)
			{
				if (context.trial().auxilTrialData().legalMovesHistory() != null)
					subtrial.storeLegalMovesHistory();
				
				if (context.trial().auxilTrialData().legalMovesHistorySizes() != null)
					subtrial.storeLegalMovesHistorySizes();
			}
			
			final Trial instanceEndTrial = instanceGame.playout
					(
						subcontext, ais, thinkingTime, playoutMoveSelector, 
						maxNumBiasedActions, maxNumPlayoutActions - numActionsApplied, 
						random
					);
			
			// Will likely have to append some extra moves to the match-wide trial
			final List<Move> subtrialMoves = subtrial.generateCompleteMovesList();
			final int numMovesAfterPlayout = subtrialMoves.size();
			final int numMovesToAppend = numMovesAfterPlayout - numStartMoves;
			
			for (int i = 0; i < numMovesToAppend; ++i)
			{
				context.trial().addMove(subtrialMoves.get(subtrialMoves.size() - numMovesToAppend + i));
			}
			
			// If the instance we over, we have to advance here in this Match
			if (subcontext.trial().over())
			{
				final Moves legalMatchMoves = context.game().moves(context);
				assert (legalMatchMoves.moves().size() == 1);
				assert (legalMatchMoves.moves().get(0).containsNextInstance());
				context.game().apply(context, legalMatchMoves.moves().get(0));
			}
			
			// May have to update Match-wide auxiliary trial data
			if (context.trial().auxilTrialData() != null)
			{
				context.trial().auxilTrialData().updateFromSubtrial(subtrial);
				
				// Need to add 1 here, for the special state where we have to play a NextInstance move
				if (context.trial().auxilTrialData().legalMovesHistorySizes() != null)
					context.trial().auxilTrialData().legalMovesHistorySizes().add(1);
			}
			
			numActionsApplied += (instanceEndTrial.numMoves() - numStartMoves);
		}
		
		return matchTrial;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean callsGameMoves()
	{
		return true;
	}

	//-------------------------------------------------------------------------

}
