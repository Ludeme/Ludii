package main.math;

/**
 * 2D point.
 * @author Dennis Soemers and cambolbro
 */
public class Point2D
{
	
	//-------------------------------------------------------------------------
	
	/** X coordinate */
	private double x;
	
	/** Y coordinate */
	private double y;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param x
	 * @param y
	 */
	public Point2D(final double x, final double y)
	{
		this.x = x;
		this.y = y;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return X coordinate
	 */
	public double x()
	{
		return x;
	}
	
	/**
	 * @return Y coordinate
	 */
	public double y()
	{
		return y;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Set coordinates
	 * @param newX
	 * @param newY
	 */
	public void set(final double newX, final double newY)
	{
		x = newX;
		y = newY;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other
	 * @return Euclidean distance to other point
	 */
	public double distance(final Point2D other)
	{
		final double dx = other.x - x;
		final double dy = other.y - y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	//-------------------------------------------------------------------------

	/**
	 * Translate the point
	 * @param dx
	 * @param dy
	 */
	public void translate(final double dx, final double dy)
	{
		x += dx;
		y += dy;
	}
		
	/**
	 * Scale the point by one scalar
	 * @param s
	 */
	public void scale(final double s)
	{
		x *= s;
		y *= s;
	}
	
	/**
	 * Scale the two coordinates by two scalars
	 * @param sx
	 * @param sy
	 */
	public void scale(final double sx, final double sy)
	{
		x *= sx;
		y *= sy;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) 
	{
		if (this == obj)
			return true;
		
		if (!(obj instanceof Point2D))
			return false;
		
		final Point2D other = (Point2D) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param other
	 * @param tolerance
	 * @return Are we approximately equal (given tolerance level)?
	 */
	public boolean equalsApprox(final Point2D other, final double tolerance)
	{
		return (Math.abs(x - other.x) <= tolerance && Math.abs(y - other.y) <= tolerance);
	}
	
	//-------------------------------------------------------------------------
	
}
