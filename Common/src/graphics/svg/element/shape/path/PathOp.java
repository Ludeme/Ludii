package graphics.svg.element.shape.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * An SVG path operation.
 * @author cambolbro
 */
public abstract class PathOp
{
	/** Single char label. Can change upper/lower case when loaded. */
	protected char label;
	
	//-------------------------------------------------------------------------

	/**
	 * Note: Label will be passed in as upper case, but visible case 
	 *       will depend on whether it's absolute or relative.
	 */
	public PathOp(final char label)
	{
		this.label = label;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Op is absolute if its label is uppercase.
	 */
	public boolean absolute()
	{
		return label == Character.toUpperCase(label);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Bounds for this path op, or null if none.
	 */
	public Rectangle2D bounds()
	{
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	public char label()
	{
		return label;  //absolute ? Character.toUpperCase(label) : Character.toLowerCase(label);
	}

	public void setLabel(final char ch)
	{
		label = ch;
	}
	
	public boolean matchesLabel(final char ch)
	{
		return Character.toUpperCase(ch) == Character.toUpperCase(label);
	}

	//-------------------------------------------------------------------------

	public boolean isMoveTo()
	{
		return Character.toLowerCase(label) == 'm';
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Expected number of number arguments.
	 */
	public abstract int expectedNumValues();
	
	//-------------------------------------------------------------------------

	/**
	 * @return New element of own type.
	 */
	public abstract PathOp newInstance();
	
	/**
	 * Load this shape's data from an SVG expression.
	 * @return Whether expression is in the right format and data was loaded.
	 */
	public abstract boolean load(final String expr);

	//-------------------------------------------------------------------------

	/**
	 * Load this shape's data from a list of Doubles.
	 */
	public abstract void setValues(final List<Double> values, final Point2D[] current);
	
	/**
	 * Load this shape's data from a list of Doubles.
	 */
	public abstract void getPoints(final List<Point2D> pts);

	//-------------------------------------------------------------------------

	/**
	 * Apply this operation to the given path.
	 */
	public abstract void apply(final GeneralPath path, final double x0, final double y0);
	
	//-------------------------------------------------------------------------

}
