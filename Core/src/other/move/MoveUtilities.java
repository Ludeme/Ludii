package other.move;

import annotations.Hide;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import main.collections.FastArrayList;
import other.context.Context;

/**
 * Helper functions for dealing with the complexity of moves
 * 
 * @author mrraow and Dennis Soemers
 */
@Hide
public final class MoveUtilities
{
	/** Utility class - do not construct */
	private MoveUtilities()
	{
	}

	/**
	 * This code hides all the complexity of chaining a series of rules into a
	 * single call. It is safe to call even if next or current action is empty. It
	 * will avoid adding unnecessary ActionCompound wrappers. Result will be cross
	 * product of currentAction and generated list from next, but we will add
	 * currentAction alone if the generated list is empty.
	 * 
	 * @param context
	 * @param ourActions    parent list which will be returned
	 * @param nextRule      may be null if there are no further actions in the chain
	 * @param currentAction after which we are calling 'next' - may be null if we
	 *                      are delegating
	 * @param prepend       new action goes before passed in action if true
	 */
	public static void chainRuleCrossProduct
	(
		final Context context, final Moves ourActions, final Moves nextRule,
		final Move currentAction, final boolean prepend
	)
	{
		// 0. Sanity check and cleaner code
		if (nextRule == null)
		{
			if (currentAction != null)
				ourActions.moves().add(currentAction);
			return;
		}

		// 1. Get the list
		final Moves generated = nextRule.eval(context);
		
		// 2. Generate the cross product
		if (currentAction == null)
		{
			ourActions.moves().addAll(generated.moves());
		} 
		else //if (generated.moves().size() == 0 && currentAction != null)
		{
			assert (generated.moves().isEmpty());
			ourActions.moves().add(currentAction);
		}
	}

	/**
	 * This code hides all the complexity of chaining a series of rules into a
	 * single call. It is safe to call even if next or current action is empty. It
	 * will avoid adding unnecessary ActionCompound wrappers. Result will be cross
	 * product of currentAction and generated list from next, but we will add
	 * currentAction alone if the generated list is empty.
	 * 
	 * @param context
	 * @param nextRule      may be null if there are no further actions in the chain
	 * @param currentAction after which we are calling 'next' - may be null if we
	 *                      are delegating
	 * @param prepend       new action goes before passed in action if true
	 * @param decision      True if this is a decision.
	 * @return single compound action
	 */
	public static Move chainRuleWithAction
	(
		final Context context, final Moves nextRule, 
		final Move currentAction, final boolean prepend, final boolean decision
	)
	{
		// 0. Sanity check and cleaner code
		if (nextRule == null)
			return currentAction;

		// 1. Get the list
		final Moves generated = nextRule.eval(context);

		if (generated.moves().isEmpty())
			return currentAction;

		if (generated.moves().size() > 1)
		{
			if (!decision)
				for (final Move m : generated.moves()) 
				{
					m.setFromNonDecision(m.actions().get(0).from());
					m.setToNonDecision(m.actions().get(0).to());
					m.setDecision(false);
				}

			return prepend ? new Move(generated.moves(), currentAction)
					: new Move(currentAction, generated.moves());
		}

		final Move m = generated.moves().get(0);

		if (!decision)
			m.setDecision(false);

		// 2. Generate the cross product
		if (currentAction == null)
			return m;

		return prepend ? new Move(m, currentAction)
				: new Move(currentAction, generated.moves().get(0));
	} 
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets relevant data from the given generating Moves ludeme for each of the moves
	 * in the given list of generated moves.
	 * @param moves
	 * @param generatingLudeme
	 */
	public static void setGeneratedMovesData(final FastArrayList<Move> moves, final Moves generatingLudeme)
	{
		final Then cons = generatingLudeme.then();
		if (cons != null)
		{
			for (int i = 0; i < moves.size(); ++i)
			{
				final Move m = moves.get(i);
				m.then().add(cons.moves());
				m.setMovesLudeme(generatingLudeme);
			}
		}
		else
		{
			for (int i = 0; i < moves.size(); ++i)
			{
				final Move m = moves.get(i);
				m.setMovesLudeme(generatingLudeme);
			}
		}
	}
	
	/**
	 * Sets relevant data from the given generating Moves ludeme for each of the moves
	 * in the given list of generated moves.
	 * @param moves
	 * @param generatingLudeme
	 * @param mover Also set the mover of the generated moves to given mover
	 */
	public static void setGeneratedMovesData(final FastArrayList<Move> moves, final Moves generatingLudeme, final int mover)
	{
		final Then cons = generatingLudeme.then();
		if (cons != null)
		{
			for (int i = 0; i < moves.size(); ++i)
			{
				final Move m = moves.get(i);
				m.then().add(cons.moves());
				m.setMovesLudeme(generatingLudeme);
				m.setMover(mover);
			}
		}
		else
		{
			for (int i = 0; i < moves.size(); ++i)
			{
				final Move m = moves.get(i);
				m.setMovesLudeme(generatingLudeme);
				m.setMover(mover);
			}
		}
	}
	
	//-------------------------------------------------------------------------
}
