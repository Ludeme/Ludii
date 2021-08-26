package game.functions.booleans.is.string;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;

/**
 * Returns true if that proposition is proposed.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsProposed extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private final String proposition;
	
	/** int representation of the proposition */
	private int propositionInt;

	//-------------------------------------------------------------------------

	/**
	 * @param proposition Proposition being proposed.
	 * 
	 * @example (isProposed "End")
	 */
	public IsProposed(final String proposition)
	{
		this.proposition = proposition;
		propositionInt = Constants.UNDEFINED;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.state().propositions().contains(propositionInt);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsProposed()";
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
		propositionInt = game.registerVoteString(proposition);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "The proposed is " +proposition;
	}
}
