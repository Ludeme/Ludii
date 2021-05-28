package main.math;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;

/**
 * 2D vector.
 * @author cambolbro
 */
public class Vector
{
	private double x = 0;
	private double y = 0;
	private double z = 0;
	
	//-------------------------------------------------------------------------
	
	public Vector(final double x, final double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(final Point2D pt)
	{
		this.x = pt.getX();
		this.y = pt.getY();
	}

	public Vector(final Point2D ptA, final Point2D ptB)
	{
		this.x = ptB.getX() - ptA.getX();
		this.y = ptB.getY() - ptA.getY();
	}
	
	public Vector(final Point3D ptA, final Point3D ptB)
	{
		this.x = ptB.x() - ptA.x();
		this.y = ptB.y() - ptA.y();
		this.z = ptB.z() - ptA.z();
	}
	
	public Vector(final Vector other)
	{
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	//-------------------------------------------------------------------------

	public double x()
	{
		return x;
	}
	
	public double y()
	{
		return y;
	}

	public double z()
	{
		return z;
	}
	
	public void set(final double xx, final double yy, final double zz)
	{
		x = xx;
		y = yy;
		z = zz;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Magnitude of vector.
	 */
	public double magnitude()
	{
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	/**
	 * Normalise to unit vector.
	 */
	public void normalise()
	{
		final double mag = magnitude();
		if (mag < MathRoutines.EPSILON)
			return;  // too small
		x /= mag;
		y /= mag;
		z /= mag;
	}
	
	public double direction()
	{
		return Math.atan2(y, x);  // in radians, -PI .. PI
	}

	public void reverse()
	{
		x = -x;
		y = -y;
		z = -z;
	}
	
	/**
	 * Make perpendicular (to right?).
	 */
	public void perpendicular()
	{
		final double tmp = x;
		x = -y;
		y = tmp;
	}
	
	public void translate(final double dx, final double dy)
	{
		x += dx;
		y += dy;
	}
	
	/**
	 * Translate.
	 */
	public void translate(final double dx, final double dy, final double dz)
	{
		x += dx;
		y += dy;
		z += dz;
	}
		
	public void scale(final double factor)
	{
		x *= factor;
		y *= factor;
		z *= factor;
	}
		
	public void scale(final double sx, final double sy)
	{
		x *= sx;
		y *= sy;
	}
		
	public void scale(final double sx, final double sy, final double sz)
	{
		x *= sx;
		y *= sy;
		z *= sz;
	}
		
	/**
	 * @param theta Radians.
	 */
	public void rotate(final double theta)
	{
		final double xx = x * Math.cos(theta) - y * Math.sin(theta);
		final double yy = y * Math.cos(theta) + x * Math.sin(theta);

		x = xx;
		y = yy;
	}
	
	/**
	 * @return Dot product of this vector and another.
	 */
	public double dotProduct(final Vector other)
	{
		return x * other.x + y * other.y + z * other.z;
	}
	
	/**
	 * @return Determinant of this vector and another.
	 */
	public double determinant(final Vector other)
	{
		return x * other.y - y * other.x;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final DecimalFormat df = new DecimalFormat("#.###");
		return "<" + df.format(x) + "," + df.format(y) + "," + df.format(z) + ">"; 
	}
	
	//-------------------------------------------------------------------------
	
}
