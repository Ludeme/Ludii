package game.functions.ints.state;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the index of the next player.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme is used to apply some specific condition or rule to the next player.
 */
public final class Next extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Our singleton instance */
	private static final Next INSTANCE = new Next();

	//-------------------------------------------------------------------------

	/**
	 * @example (next)
	 */
	private Next()
	{
		// Private constructor; singleton!
	}
	
	/**
	 * @return Singleton instance
	 */
	public static Next instance()
	{
		return INSTANCE;
	}
	
	/**
	 * @return Returns our singleton instance as a ludeme
	 * 
	 * @example (next)
	 */
	public static Next construct()
	{
		return INSTANCE;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final int eval(final Context context)
	{
		return context.state().next();
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
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
		// nothing to do
	}
}
