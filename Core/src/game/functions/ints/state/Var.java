package game.functions.ints.state;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the value stored in the var variable from the context.
 * 
 * @author mrraow
 * @remarks To identify the value stored previously with a key in the context.
 *          If no key specified, the var variable of the context is returned.
 */
public final class Var extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The key of the variable. */
	private final String key;

	//-------------------------------------------------------------------------

	/**
	 * @param key The key String value to check.
	 * @example (var "current")
	 */
	public Var(@Opt final String key)
	{
		this.key = key;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (key == null)
			return context.state().temp();

		return context.state().getValue(key);
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
		return GameType.MapValue;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
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

	//-------------------------------------------------------------------------

	@Override
	public String toString() {
		return "GetVariable [key=" + key + "]";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "state variable" + key;
	}
	
	//-------------------------------------------------------------------------
		
}
