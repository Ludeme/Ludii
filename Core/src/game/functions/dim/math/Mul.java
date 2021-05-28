package game.functions.dim.math;

import annotations.Alias;
import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * Returns to multiple of values.
 * 
 * @author Eric.Piette
 */
@Alias(alias = "*")
public final class Mul extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Value a. */
	private final DimFunction a;

	/** Value b. */
	private final DimFunction b;

	/** List of values. */
	private final DimFunction[] list;

	//-------------------------------------------------------------------------

	/**
	 * For a product of two values.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * 
	 * @example (* 6 2)
	 */
	public Mul
	(
		final DimFunction a, 
		final DimFunction b
	)
	{
		this.a = a;
		this.b = b;
		this.list = null;
	}

	/**
	 * For a product of many values.
	 * 
	 * @param list The list of values.
	 * @example (* {3 2 5})
	 */
	public Mul(final DimFunction[] list)
	{
		this.list = list;
		this.a = null;
		this.b = null;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval()
	{
		if (list == null)
		{
			return a.eval() * b.eval();
		}
		else
		{
			int mul = 1;
			for (final DimFunction elem : list)
				mul *= elem.eval();

			return mul;
		}
	}
}
