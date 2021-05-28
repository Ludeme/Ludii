package game.functions.dim.math;

import annotations.Alias;
import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * To divide a value by another.
 * 
 * @author Eric.Piette and cambolbro
 * @remarks The result will be an integer and round down the result.
 */
@Alias(alias = "/")
public final class Div extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to divide. */
	private final DimFunction a;

	/** To divide by b. */
	private final DimFunction b;

	//-------------------------------------------------------------------------

	/**
	 * To divide a value by another.
	 * 
	 * @param a The value to divide.
	 * @param b To divide by b.
	 * @example (/ 4 2)
	 */
	public Div
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
		final int evalB = b.eval();
		if (evalB == 0)
			throw new IllegalArgumentException("Division by zero.");

		return a.eval() / evalB;
	}
}
