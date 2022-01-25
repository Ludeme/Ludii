package app.manualGeneration;

import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.UpdateTabMessages;
import game.Game;
import game.rules.end.End;
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
	
	public final static void recordTrialMoves(final PlayerApp app, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG, final List<MoveCompleteInformation> condensedMoveList, final List<String> rankingStrings, final List<MoveCompleteInformation> endingMoveList, final boolean includeHandMoves, final boolean includeNoWhatMoves)
	{
		final Context context = app.manager().ref().context();
		
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
				Move matchingLegalMove = null;
				Move move = trial.getMove(i);
				int matchesFound = 0;
				
				// Context used to copy the state reaching the terminal state before the game is over.
				Context prevContext = null;
				for (final Move m : context.game().moves(context).moves())
				{
					if (m.toTrialFormat(context).equals(move.toTrialFormat(context)))
					{
						final Move newMove = new Move(m.getMoveWithConsequences(context));
						newMove.setMovesLudeme(m.movesLudeme());
						move = newMove;
						matchingLegalMove = m;
						matchesFound++;
						break;
					}
				}
				if (matchesFound != 1)
				{
					System.out.println("ERROR! exactly one match should be found, we found " + matchesFound);
					System.exit(0);
				}
				
				// Get complete information about the selected move.
				final int what = ManualGenerationUtils.getWhatOfMove(context, move);
				final List<Move> similarMoves = MoveComparison.similarMoves(context, move);
				final MoveCompleteInformation newMove = new MoveCompleteInformation(context.game(), trial, trialRNG, move, i, ManualGenerationUtils.getComponentNameFromIndex(context, what), similarMoves);
							
				// Record if the move involved hands at all.
				final boolean moveFromBoard = ContainerUtil.getContainerId(context, move.from(), move.fromType()) == 0;
				final boolean moveToBoard = ContainerUtil.getContainerId(context, move.to(), move.toType()) == 0;
				final boolean moveInvolvesHands = !moveFromBoard || !moveToBoard;
				
				// Skip moves without an associated component or which move from the hands (if desired).
				if ((what != -1 || includeNoWhatMoves) && (includeHandMoves || !moveInvolvesHands))
				{
					// Determine if the move should be added to the condensed list.
					boolean addMove = true;
					for (int j = 0; j < condensedMoveList.size(); j++)
					{
						final MoveCompleteInformation priorMove = condensedMoveList.get(j);
						if (MoveComparison.movesCanBeMerged(context.game(), newMove, priorMove))
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
					
					// We keep the context before the ending state for To English of the terminal condition.
					if(i == trial.numMoves()-1)
						prevContext = new Context(context);
					
					// Apply the move to update the context for the next move.
					context.game().apply(context, matchingLegalMove);
					
					// Get endingString for move.
					if (context.trial().over())
					{
						// Check if the last move should be stored.
						final String rankingString = UpdateTabMessages.gameOverMessage(context, trial);
						//final String rankingString = "Game won by Player " + trial.status().winner() + ".\n";
						
						// We update the context without to modify the data of prevContext.
						context.trial().lastMove().apply(prevContext, true);
						
						// Store the toEnglish of the end condition.
						for(final End end : getEnd(context.game()))
						{
							if(end != null && end.endRules() != null)
							{
								for (final EndRule endRule : end.endRules())
								{
									if (endRule instanceof If)
									{
										final If ifEndRule = (If) endRule;
										ifEndRule.endCondition().preprocess(context.game());
										if (ifEndRule.result() != null && ifEndRule.result().result() != null && ifEndRule.endCondition().eval(prevContext))
										{
											newMove.setEndingDescription(((If) endRule).endCondition().toEnglish(context.game()));
											break;
										}
									}
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
					context.game().apply(context, matchingLegalMove);
				}
			}
		}
		
		System.out.println("\nMoves Recorded.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return A list of End rule objects for the given game.
	 */
	private static End[] getEnd(final Game game)
	{
		if (game.rules().phases().length == 1)
		{
			return new End[] {game.endRules()};
		}
		else
		{
			final End[] phaseEnd = new End[game.rules().phases().length];
			for (int i = 0; i < game.rules().phases().length; i++)
				phaseEnd[i] = game.rules().phases()[i].end();
			
			return phaseEnd;
		}
	}
	
	//-------------------------------------------------------------------------
	
}
