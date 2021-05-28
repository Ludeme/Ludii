package game.functions.dim;

import annotations.Hide;

/**
 * Constant dim value.
 * 
 * @author Eric.Piette and cambolbro
 */
@Hide
public final class DimConstant extends BaseDimFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Constant value. */
	private final int a;

	//-------------------------------------------------------------------------

	/**
	 * @param a The integer value.
	 */
	public DimConstant(final int a)
	{
		this.a = a;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval()
	{
		return a;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "" + a;
		return str;
	}
}
