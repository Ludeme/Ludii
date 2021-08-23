package game.rules.start;

import java.io.Serializable;

import annotations.Or;
import game.Game;
import game.types.board.SiteType;
import other.BaseLudeme;
import other.action.BaseAction;
import other.action.move.ActionAdd;
import other.context.Context;
import other.move.Move;

/**
 * Defines a starting position.
 * 
 * @author cambolbro

 * @remarks For any game with starting rules, like pieces already placed on the
 *        board.
 */
public class Start extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The starting rules. */
	private final StartRule[] rules;

	//-------------------------------------------------------------------------
	
	/**
	 * @param rules The starting rules.
	 * @param rule  The starting rule.
	 * @example (start { (place "Pawn1" {"F4" "F5" "F6" "F7" "F8" "F9" "G5" "G6"
	 *          "G7" "G8"}) (place "Knight1" {"F3" "G4" "G9" "F10"}) (place "Pawn2"
	 *          {"K4" "K5" "K6" "K7" "K8" "K9" "J5" "J6" "J7" "J8"}) (place
	 *          "Knight2" {"K3" "J4" "J9" "K10"}) })
	 */
	public Start
	(
		@Or final StartRule[] rules,
		@Or final StartRule   rule
	)
	{
		int numNonNull = 0;
		if (rules != null)
			numNonNull++;
		if (rule != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");
		
		if (rules != null)
			this.rules = rules;
		else
		{
			this.rules = new StartRule[1];
			this.rules[0] = rule;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The starting rules.
	 */
	public StartRule[] rules()
	{
		return rules;
	}

	//-------------------------------------------------------------------------

	/**
	 * Evaluate the starting rules.
	 * 
	 * @param context The context.
	 */
	public void eval(final Context context)
	{
		for (final StartRule rule : rules)
			rule.eval(context);
	}

	//-------------------------------------------------------------------------

	/**
	 * Place piece on a site.
	 * 
	 * @param context     The context.
	 * @param locn        The location of the piece.
	 * @param what        The index of the piece.
	 * @param count       The count of the piece.
	 * @param state       The state of the site.
	 * @param rotation    The rotation of the site.
	 * @param value       The piece value of the site.
	 * @param isStack     True if the piece has to be placed in a stack.
	 * @param type        The graph element type of the site.
	 */
	public static void placePieces
	(
		final Context context, final int locn, final int what, final int count, 
			final int state, final int rotation, final int value, final boolean isStack, final SiteType type
	)
	{
		if (isStack)
		{
			final BaseAction actionAtomic = new ActionAdd(type, locn, what, 1, state,
					rotation, value, Boolean.TRUE);
			actionAtomic.apply(context, true);
			context.trial().addMove(new Move(actionAtomic));
			context.trial().addInitPlacement();
		}
		else
		{
			final BaseAction actionAtomic = new ActionAdd(type, locn, what, count, state, rotation, value, null);
			actionAtomic.apply(context, true);
			context.trial().addMove(new Move(actionAtomic));
			context.trial().addInitPlacement();
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return super.toEnglish(game);
	}
}
