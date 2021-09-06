package game.functions.dim.math;

import annotations.Alias;
import game.Game;
import game.functions.dim.BaseDimFunction;
import game.functions.dim.DimFunction;

/**
 * Adds many values.
 * 
 * @author Eric.Piette
 */
@Alias(alias = "+")
public final class Add extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first value. */
	private final DimFunction a;

	/** The second value. */
	private final DimFunction b;

	/** The list of values. */
	protected final DimFunction[] list;

	//-------------------------------------------------------------------------

	/**
	 * To add two values.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * @example (+ 5 2)
	 */
	public Add
	(
		final DimFunction a, 
		final DimFunction b
	)
	{
		this.a = a;
		this.b = b;
		list = null;
	}

	/**
	 * To add all the values of a list.
	 * 
	 * @param list The list of the values.
	 * @example (+ {10 2 5})
	 */
	public Add
	(
		final DimFunction[] list
	)
	{
		a = null;
		b = null;
		this.list = list;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval()
	{
		if (list == null)
		{
			return a.eval() + b.eval();
		}

		int sum = 0;
		for (final DimFunction elem : list)
			sum += elem.eval();

		return sum;
	}

	@Override
	public String toEnglish(final Game game) 
	{
		return a.toEnglish(game) + " + " + b.toEnglish(game);
	}
}
