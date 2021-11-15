package game.rules.meta;

import java.util.BitSet;

import game.Game;
import game.types.play.PassEndType;
import game.types.state.GameType;
import other.context.Context;

/**
 * To apply a certain end result to all players if all players pass their turns.
 * 
 * @author Eric.Piette
 */
public class PassEnd extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The passEnd type.
	 */
	final PassEndType type;

	/**
	 * @param type The type of passEnd.
	 * 
	 * @example (passEnd NoEnd)
	 */
	public PassEnd(final PassEndType type)
	{
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		return type == PassEndType.NoEnd ? GameType.NotAllPass : 0l;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public int hashCode()
	{
		final int result = 1;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof PassEnd))
			return false;

		return true;
	}
}