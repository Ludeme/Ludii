package main.math;

import java.io.Serializable;

//-----------------------------------------------------------------------------

/**
 * [row,column,layer] coordinate.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class RCL implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Row. */
	private int row = -1;

	/** Column. */
	private int column = -1;

	/** Layer. */
	private int layer = -1;

	//-------------------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public RCL()
	{
	}

	/**
	 * Constructor 2D.
	 */
	public RCL(final int r, final int c)
	{
		row    = r;
		column = c;
		layer  = 0;
	}

	/**
	 * Constructor 2D.
	 */
	public RCL(final int r, final int c, final int l)
	{
		row    = r;
		column = c;
		layer  = l;
	}

	//-------------------------------------------------------------------------

	public int row()
	{
		return row;
	}

	public void setRow(final int r)
	{
		row = r;
	}

	public int column()
	{
		return column;
	}

	public void setColumn(final int c)
	{
		column = c;
	}

	public int layer()
	{
		return layer;
	}

	public void setLayer(final int l)
	{
		layer = l;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param r Row.
	 * @param c Column.
	 * @param l Layer.
	 */
	public void set(final int r, final int c, final int l)
	{
		row    = r;
		column = c;
		layer  = l;
	}

	/**
		 */
	public void set(final RCL other)
	{
		row    = other.row;
		column = other.column;
		layer  = other.layer;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "row = " + row + ", column = " + column + ", layer = " + layer;
	}

	//-------------------------------------------------------------------------

}
