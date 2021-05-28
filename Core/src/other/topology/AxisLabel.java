package other.topology;

import java.awt.geom.Point2D;
import java.io.Serializable;

//-----------------------------------------------------------------------------

/**
 * Board cell.
 * 
 * @author cambolbro
 */
public final class AxisLabel implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Label. */
	private String label = "?";

	/** Position. */
	private final Point2D.Double posn = new Point2D.Double(0, 0);

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param label
	 * @param x
	 * @param y
	 */
	public AxisLabel(final String label, final double x, final double y)
	{
		this.label = label;
		posn.setLocation(x, y);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Cell label.
	 */
	public String label()
	{
		return label;
	}

	/**
	 * @return Centroid.
	 */
	public Point2D.Double posn()
	{
		return posn;
	}

	//-------------------------------------------------------------------------

}
