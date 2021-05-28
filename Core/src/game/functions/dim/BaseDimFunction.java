package game.functions.dim;

import annotations.Alias;
import game.Game;
import other.BaseLudeme;

/**
 * Common functionality for DimFunction - override where necessary.
 * 
 * @author Eric.Piette and cambolbro
 */
@Alias(alias = "dim")
public abstract class BaseDimFunction extends BaseLudeme implements DimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}
}
