package game.functions.dim.math;

import game.Game;
import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * Return the absolute value of a dim.
 * 
 * @author Eric Piette
 */
public final class Abs extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value. */
	protected final DimFunction value;

	//-------------------------------------------------------------------------

	/**
	 * Return the absolute value of a value.
	 * 
	 * @param value The value.
	 * @example (abs (- 8 5))
	 */
	public Abs
	(
		final DimFunction value
	)
	{
		this.value = value;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval()
	{
		return Math.abs(value.eval());
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "The absolute value of " + value.toString();
	}
}
