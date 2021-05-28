package game.functions.intArray.iteraror;

import java.util.BitSet;

import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the team iterator.
 * 
 * @author Eric.Piette
 */
public final class Team extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (team)
	 */
	public Team()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		return context.team();
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
		final long flag = 0;
		return flag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Team.id(), true);
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
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.Player.id(), true);
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		final boolean missingRequirement = false;
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		final boolean willCrash = false;
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
}
