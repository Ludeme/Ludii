package game.functions.ints.state;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.play.PrevType;
import other.context.Context;

/**
 * Returns the index of the previous player.
 * 
 * @author Eric.Piette
 * @remarks To apply some specific conditions/rules to the previous player.
 */
public final class Prev extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The type of the previous state. */
	final PrevType type;
	
	/**
	 * @param type The type of the previous state [Mover].
	 * @example (prev)
	 */
	public Prev
	(
		@Opt final PrevType type
	)
	{
		this.type = (type == null) ? PrevType.Mover: type;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final int eval(final Context context)
	{
		if (type.equals(PrevType.Mover))
			return context.state().prev();
		else
			return context.trial().lastTurnMover(context.state().mover());
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the previous " + type.name().toLowerCase();
	}
	
	//-------------------------------------------------------------------------
}
