package other.move;

import annotations.Hide;
import game.rules.play.moves.Moves;
import other.context.Context;

/**
 * Helper functions for dealing with the complexity of moves
 * 
 * @author mrraow
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
	public static void chainRuleCrossProduct(final Context context, final Moves ourActions, final Moves nextRule,
			final Move currentAction, final boolean prepend)
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
}
