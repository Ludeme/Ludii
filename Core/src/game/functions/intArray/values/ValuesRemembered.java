package game.functions.intArray.values;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.types.state.GameType;
import main.collections.FastTIntArrayList;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns an array of the sizes of all the groups.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ValuesRemembered extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	private final String name;
	
	/**
	 * The values remembered in the state.
	 * 
	 * @param name The name of the remembering values.
	 */
	public ValuesRemembered
	(
		@Opt final String name
	)
	{
		this.name = name;
	}

	// -------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		if (name == null)
			return context.state().rememberingValues().toArray();
		else
		{
			final FastTIntArrayList rememberingValues = context.state().mapRememberingValues().get(name);
			if (rememberingValues == null)
				return new int[0];

			return rememberingValues.toArray();
		}
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = GameType.RememberingValues;
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Variable.id(), true);
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
		// Nothing to do.
	}
}
