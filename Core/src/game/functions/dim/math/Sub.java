package game.functions.dim.math;

import annotations.Alias;
import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * Returns the subtraction A minus B.
 * 
 * @author Eric Piette
 */
@Alias(alias = "-")
public final class Sub extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value A. */
	private final DimFunction valueA;

	/** Which value B. */
	private final DimFunction valueB;

	//-------------------------------------------------------------------------

	/**
	 * @param valueA The value A.
	 * @param valueB The value B.
	 * @example (- 5 1)
	 */
	public Sub
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
		return valueA.eval() - valueB.eval();
	}
}
