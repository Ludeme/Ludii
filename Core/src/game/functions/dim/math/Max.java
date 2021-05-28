package game.functions.dim.math;

import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * Returns the maximum of two specified values.
 * 
 * @author Eric Piette
 */
public final class Max extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Value A . */
	private final DimFunction valueA;

	/** Value B. */
	private final DimFunction valueB;

	//-------------------------------------------------------------------------

	/**
	 * @param valueA The first value.
	 * @param valueB The second value.
	 * @example (max 9 3)
	 */
	public Max
	(
		final DimFunction valueA, 
		final DimFunction valueB
	)
	{
		this.valueA = valueA;
		this.valueB = valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval()
	{
		return Math.max(valueA.eval(), valueB.eval());
	}
}
