package app.tutorialVisualisation;

import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.PlayerApp;
import app.utils.GameUtil;
import game.rules.end.EndRule;
import game.rules.end.If;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import util.ContainerUtil;

public class MoveGeneration
{

	//-------------------------------------------------------------------------
	
	public final static void generateTrials(final PlayerApp app, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG, final int numberTrials)
	{
		while (generatedTrials.size() < numberTrials)
		{
			System.out.print(".");
			app.restartGame();
			app.manager().ref().randomPlayout(app.manager());
			generatedTrials.add(new Trial(app.manager().ref().context().trial()));
			generatedTrialsRNG.add(new RandomProviderDefaultState(app.manager().currGameStartRngState().getState()));
		}
		
		System.out.println("\nTrials Generated.");
	}
	
	//-------------------------------------------------------------------------
	
	public final static void recordTrialMoves(final PlayerApp app, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG, final List<MoveCompleteInformation> condensedMoveList, final List<String> rankingStrings, final List<MoveCompleteInformation> endingMoveList, final boolean includeHandMoves)
	{
		Context context = app.manager().ref().context();
		
		for (int trialIndex = 0; trialIndex < generatedTrials.size(); trialIndex++)
		{
			System.out.print(".");
			
			// Reset the game for the new trial.
			final Trial trial = generatedTrials.get(trialIndex);
			final RandomProviderDefaultState trialRNG = generatedTrialsRNG.get(trialIndex);
			app.manager().setCurrGameStartRngState(trialRNG);
			GameUtil.resetGame(app, true);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				// Find the corresponding move in the legal moves, to include movesLudeme.
				Move move = trial.getMove(i);
				boolean moveFound = false;
				for (final Move m : context.game().moves(context).moves())
				{
					//System.out.println(m.toTrialFormat(context));
					//System.out.println(move.toTrialFormat(context));
					if (m.toTrialFormat(context).equals(move.toTrialFormat(context)))
					{
						final Move newMove = new Move(m.getMoveWithConsequences(context));
						newMove.setMovesLudeme(m.movesLudeme());
						move = newMove;
						moveFound = true;
						break;
					}
				}
				if (!moveFound)
					System.out.println("ERROR! no matching legal move found.");
				
				// Get complete information about the selected move.
				final int what = ValueUtils.getWhatOfMove(context, move);
				final List<Move> similarMoves = MoveComparison.similarMoves(context, move);
				final MoveCompleteInformation newMove = new MoveCompleteInformation(context.game(), trial, trialRNG, move, i, what, similarMoves);
							
				// Record if the move involved hands at all.
				final boolean moveFromBoard = ContainerUtil.getContainerId(context, move.from(), move.fromType()) == 0;
				final boolean moveToBoard = ContainerUtil.getContainerId(context, move.to(), move.toType()) == 0;
				final boolean moveInvolvesHands = !moveFromBoard || !moveToBoard;
				
				// State information for updating the context for ending moves.
				final int prev = context.state().prev();
				final int mover = context.state().mover();
				final int next = context.state().next();
				
				// Skip moves without an associated component or which move from the hands (if desired).
				if (what != -1 && (includeHandMoves || !moveInvolvesHands))
				{
					// Determine if the move should be added to the condensed list.
					boolean addMove = true;
					for (int j = 0; j < condensedMoveList.size(); j++)
					{
						final MoveCompleteInformation priorMove = condensedMoveList.get(j);
						if (MoveComparison.movesCanBeMerged(newMove, priorMove))
						{
							// Check if the new move has a larger number of possible moves, if so replace the old move.
							if (newMove.similarMoves().size() > priorMove.similarMoves().size())
								condensedMoveList.set(j, newMove);
							
							addMove = false;
							break;
						}
					}
					if (addMove)
						condensedMoveList.add(newMove);
				
					// Apply the move to update the context for the next move.
					context.game().apply(context, move);
					
					// Get endingString for move.
					if (context.trial().over())
					{
						// Check if the last move should be stored.
						// final String rankingString = UpdateTabMessages.gameOverMessage(context, trial);
						final String rankingString = "Game won by Player " + trial.status().winner() + ".\n";
						
						// Set these vales in the state to those before the game ended.
						context.state().setPrev(prev);
						context.state().setMover(mover);
						context.state().setNext(next);
						
						// Store the toEnglish of the end condition.
						for (final EndRule endRule : context.game().endRules().endRules())
						{
							if (endRule instanceof If)
							{
								((If) endRule).endCondition().preprocess(context.game());
								if (((If) endRule).result() != null && ((If) endRule).result().result() != null && ((If) endRule).endCondition().eval(context))
								{
									newMove.setEndingDescription(((If) endRule).endCondition().toEnglish(context.game()));
									break;
								}
							}
						}
						
						// Check if any of our previous ending moves triggered the same condition.
						boolean endingStringFoundBefore = false;
						for (final MoveCompleteInformation endingMoveInformation : endingMoveList)
							if (endingMoveInformation.endingDescription().equals(newMove.endingDescription()))
								endingStringFoundBefore = true;
						
						// Only store the ending move/ranking if we haven't encountered this combination before.
						if (!rankingStrings.contains(rankingString) || !endingStringFoundBefore)
						{
							rankingStrings.add(rankingString);
							endingMoveList.add(newMove);
						}
					}
				}
				else
				{
					// Apply the move to update the context for the next move.
					context.game().apply(context, move);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
