package app.tutorialVisualisation;

import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.UpdateTabMessages;
import game.rules.end.EndRule;
import game.rules.end.If;
import manager.Referee;
import other.move.Move;
import other.trial.Trial;
import util.ContainerUtil;

public class MoveGeneration
{

	//-------------------------------------------------------------------------
	
	public final static void generateTrials(final PlayerApp app, final Referee ref, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG, final int numberTrials)
	{
		while (generatedTrials.size() < numberTrials)
		{
			System.out.print(".");
			app.restartGame();
			ref.randomPlayout(app.manager());
			generatedTrials.add(new Trial(ref.context().trial()));
			generatedTrialsRNG.add(new RandomProviderDefaultState(app.manager().currGameStartRngState().getState()));
		}
		System.out.println("\nTrials Generated.");
	}
	
	//-------------------------------------------------------------------------
	
	public final static void recordTrialMoves(final PlayerApp app, final Referee ref, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG, final List<MoveCompleteInformation> condensedMoveList, final List<String> rankingStrings, final List<MoveCompleteInformation> endingMoveList, final boolean includeHandMoves)
	{
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
				// Get complete information about the selected move. Needs to be got from the legal moves to include move ludemes
				Move move = trial.getMove(i);
				boolean moveFound = false;
				for (final Move m : ref.context().game().moves(ref.context()).moves())
				{
					if (m.toTrialFormat(ref.context()).equals(move.toTrialFormat(ref.context())))
					{
						final Move newMove = new Move(m.getMoveWithConsequences(ref.context()));
						newMove.setMovesLudeme(m.movesLudeme());
						move = newMove;
						moveFound = true;
						break;
					}
				}
				if (!moveFound)
					System.out.println("ERROR NO MATCHING LEGAL MOVE FOUND");
				
				final int what = ValueUtils.getWhatOfMove(ref.context(), move);
				final List<Move> similarMoves = MoveComparison.similarMoves(ref.context(), move);

				final MoveCompleteInformation newMove = new MoveCompleteInformation(ref.context().game(), trial, trialRNG, move, i, what, similarMoves);
							
				// Record if the move involved hands at all.
				final boolean moveFromBoard = ContainerUtil.getContainerId(ref.context(), move.from(), move.fromType()) == 0;
				final boolean moveToBoard = ContainerUtil.getContainerId(ref.context(), move.to(), move.toType()) == 0;
				final boolean moveInvolvesHands = !moveFromBoard || !moveToBoard;
				
				// State information for updating the context for ending moves.
				final int prev = ref.context().state().prev();
				final int mover = ref.context().state().mover();
				final int next = ref.context().state().next();
				
				// Skip moves without an associated component or which move from the hands (if desired).
				if (what != -1 && (includeHandMoves || !moveInvolvesHands))
				{
					// Determine if the move should be added to the condensed list.
					boolean addMove = true;
					for (int j = 0; j < condensedMoveList.size(); j++)
					{
						final MoveCompleteInformation priorMove = condensedMoveList.get(j);
						if (MoveComparison.movesCanBeMerged(ref.context(), newMove, priorMove))
						{
							// Check if the new move has a larger number of possible moves, if so replace the old move.
							if (newMove.similarMoves.size() > priorMove.similarMoves.size())
								condensedMoveList.set(j, newMove);
							
							addMove = false;
							break;
						}
					}
					if (addMove)
						condensedMoveList.add(newMove);
				
					// Apply the move to update the context for the next move.
					ref.context().game().apply(ref.context(), move);
					
					// Get endingString for move.
					if (ref.context().trial().over())
					{
						// Check if the last move should be stored.
						final String rankingString = UpdateTabMessages.gameOverMessage(ref.context(), trial);
						
						// Store the toEnglish of the end condition.
						for (final EndRule endRule : ref.context().game().endRules().endRules())
						{
							if (endRule instanceof If)
							{
								((If) endRule).endCondition().preprocess(ref.context().game());
								if (((If) endRule).result() != null && ((If) endRule).result().result() != null && ((If) endRule).endCondition().eval(ref.context()))
								{
									newMove.endingString = ((If) endRule).endCondition().toEnglish(ref.context().game());
									break;
								}
							}
						}
						
						// Check if any of our previous ending moves triggered the same condition.
						boolean endingStringFoundBefore = false;
						for (final MoveCompleteInformation endingMoveInformation : endingMoveList)
							if (endingMoveInformation.endingString.equals(newMove.endingString))
								endingStringFoundBefore = true;
						
						// Only store the ending move/result if we haven't encountered this ranking or endingString before.
						if (!rankingStrings.contains(rankingString) || !endingStringFoundBefore)
						{
							// Set these vales in the state to those before the game ended.
							ref.context().state().setPrev(prev);
							ref.context().state().setMover(mover);
							ref.context().state().setNext(next);
	
							rankingStrings.add(rankingString);
							endingMoveList.add(newMove);
						}
					}
				}
				else
				{
					// Apply the move to update the context for the next move.
					ref.context().game().apply(ref.context(), move);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
