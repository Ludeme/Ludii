package game.functions.dim.math;

import annotations.Alias;
import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * Computes the first parameter to the power of the second parameter.
 * 
 * @author Eric.Piette
 */
@Alias(alias = "^")
public final class Pow extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value. */
	private final DimFunction a;

	/** The power. */
	private final DimFunction b;

	//-------------------------------------------------------------------------

	/**
	 * @param a The value.
	 * @param b The power.
	 * @example (^ 2 2)
	 */
	public Pow
	(
		final DimFunction a, 
		final DimFunction b
	)
	{
		this.a = a;
		this.b = b;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval()
	{
		return (int) (Math.pow(a.eval(), b.eval()));
	}
}
