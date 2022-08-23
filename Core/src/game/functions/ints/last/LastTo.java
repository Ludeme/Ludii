package game.functions.ints.last;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import main.Constants;
import other.context.Context;
import other.move.Move;

/**
 * Returns the ``to'' location of the last decision.
 * 
 * @author Eric.Piette
 */
@Hide
public final class LastTo extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** To get the last "to" loc after applied the subsequents. */
	private final BooleanFunction afterSubsequentsFn;

	//-------------------------------------------------------------------------

	/**
	 * @param afterSubsequents Whether to return the ``to'' location after applying
	 *                         subsequents [False].
	 */
	public LastTo
	(
		@Opt @Name final BooleanFunction afterSubsequents
	)
	{
		afterSubsequentsFn = (afterSubsequents == null) ? new BooleanConstant(false) : afterSubsequents;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public int eval(final Context context)
	{
		final Move move = context.trial().lastMove();
		if (afterSubsequentsFn.eval(context))
		{
			if (move == null)
				return Constants.UNDEFINED;
			return move.toAfterSubsequents();
		}
		else
		{
			if (move == null)
				return Constants.UNDEFINED;
			return move.toNonDecision();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return afterSubsequentsFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(afterSubsequentsFn.concepts(game));
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(afterSubsequentsFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(afterSubsequentsFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		afterSubsequentsFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= afterSubsequentsFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= afterSubsequentsFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		final String str = "(LastTo)";
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the to location of the last move";
	}
	
	//-------------------------------------------------------------------------
	
}
