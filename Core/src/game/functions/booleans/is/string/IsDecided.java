package game.functions.booleans.is.string;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;

/**
 * Returns true if that decision was made.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsDecided extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private final String decision;
	
	/** int representation of the decision */
	private int decisionInt;

	//-------------------------------------------------------------------------

	/**
	 * @param decision Decision to be decided.
	 * 
	 * @example (isDecided "End")
	 */
	public IsDecided
	(
		final String decision
	)
	{
		this.decision = decision;
		decisionInt = Constants.UNDEFINED;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.state().isDecided() == decisionInt;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsDecided()";
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
		return GameType.Vote;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		decisionInt = game.registerVoteString(decision);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return decision + " has been made";
	}
	
	//-------------------------------------------------------------------------

}
