package main.math;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;

//-----------------------------------------------------------------------------

/**
 * Polygon with floating point precision, can be concave.
 * 
 * @author cambolbro
 */
public final class Polygon
{
	private final List<Point2D> points = new ArrayList<Point2D>();

	//-------------------------------------------------------------------------

	public Polygon()
	{
	}

	public Polygon(final List<Point2D> pts, final int numRotations)
	{
		for (final Point2D pt : pts)
			points.add(new Point2D.Double(pt.getX(), pt.getY()));
		
		if (numRotations != 0)
			addRotations(numRotations);
			
	}

	public Polygon(final Point2D[] pts)
	{
		for (final Point2D pt : pts)
			points.add(new Point2D.Double(pt.getX(), pt.getY()));
	}

	public Polygon(final Float[][] pts, final int numRotations)
	{
		for (final Float[] pair : pts)
		{
			if (pair.length < 2)
			{
				System.out.println("** Polygon: Two points expected.");
				points.clear();
				break;
			}
			points.add(new Point2D.Double(pair[0].floatValue(), pair[1].floatValue()));
		}
						
		if (numRotations != 0)
			addRotations(numRotations);
	}

	public Polygon(final int numSides)
	{
		final double r = numSides / (2 * Math.PI);
		for (int n = 0; n < numSides; n++)
		{
			final double theta = Math.PI / 2 + (double)n / numSides * 2 * Math.PI;
			
			final double x = r * Math.cos(theta);
			final double y = r * Math.sin(theta);
			
			points.add(new Point2D.Double(x, y));
		}
	}

	//-------------------------------------------------------------------------

	public List<Point2D> points()
	{
		return Collections.unmodifiableList(points);
	}

	//-------------------------------------------------------------------------

	public int size()
	{
		return points.size();
	}
	
	public boolean isEmpty()
	{
		return points.isEmpty();
	}
	
	public void clear()
	{
		points.clear();
	}
	
	public void add(final Point2D pt)
	{
		points.add(pt);
	}
	
	//-------------------------------------------------------------------------

	public void setFrom(final Polygon other)
	{
		clear();
		
		for (final Point2D pt : other.points())
			add(new Point2D.Double(pt.getX(), pt.getY()));
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Add the existing points repeated by the specified number of rotations.
	 * @param numRotations
	 */
	void addRotations(final int numRotations)
	{
		if (numRotations < 0)
			Collections.reverse(points);
		
		final int P = points.size();
		final double rotnAngle = 2.0 * Math.PI / numRotations;
		
		double angle = rotnAngle;
		for (int r = 1; r < Math.abs(numRotations); r++)
		{
			final double sinAngle = Math.sin(angle);
			final double cosAngle = Math.cos(angle);

			for (int p = 0; p < P; p++)
			{
				final double x = points.get(p).getX();
				final double y = points.get(p).getY();
						 
				final double xx = x * cosAngle - y * sinAngle; 
				final double yy = x * sinAngle + y * sinAngle;
				
				points.add(new Point2D.Double(xx, yy));
			}
			angle += rotnAngle;
		}
	}
	
	//-------------------------------------------------------------------------

	public double length()
	{
		double length = 0;
		
		for (int n = 0; n < points.size(); n++)
			length += MathRoutines.distance(points.get(n), points.get((n + 1) % points.size()));
		
		return length;
	}	

	public Point2D midpoint()
	{
		if (points.isEmpty())
			return new Point2D.Double();
		
		double avgX = 0;
		double avgY = 0;
		
		for (int n = 0; n < points.size(); n++)
		{
			avgX += points.get(n).getX();
			avgY += points.get(n).getY();
		}
		
		avgX /= points.size();
		avgY /= points.size();
		
		return new Point2D.Double(avgX, avgY);
	}	
	
	/**
	 * From: https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
	 * @return Signed area of polygon.
	 */
	public double area()
	{
		double area = 0;
		
		for (int n = 0; n < points.size(); n++)
		{
			final Point2D ptN = points.get(n);
			final Point2D ptO = points.get((n + 1) % points.size());
			
			area += ptN.getX() * ptO.getY() - ptO.getX() * ptN.getY();
		}
		
		return area / 2.0;
	}

	//-------------------------------------------------------------------------
	
	public boolean isClockwise()
	{
		double sum = 0;
		
		for (int n = 0, m = points.size() - 1; n < points.size(); m = n++)
		{
			final Point2D ptM = points.get(m);
			final Point2D ptN = points.get(n);
			
			sum += (ptN.getX() - ptM.getX()) * (ptN.getY() + ptM.getY());
		}
		
		return sum < 0;
	}
	
	public boolean clockwise()
	{
		// Use negative area, since Y goes down in screen coordinates
		return area() < 0;
	}

	//-------------------------------------------------------------------------

	public boolean contains(final double x, final double y)
	{
		return contains(new Point2D.Double(x, y));
	}

	/**
	 * From https://forum.processing.org/one/topic/how-do-i-find-if-a-point-is-inside-a-complex-polygon.html.
	 * @return Whether pt is inside complex polygon.
	 */
	public boolean contains(final Point2D pt)
	{	
		final int numPoints = points.size();
		int j = numPoints - 1;
		boolean odd = false;
		
		final double x = pt.getX();
		final double y = pt.getY();
		
		for (int i = 0; i < numPoints; i++) 
		{
			final double ix = points.get(i).getX();
			final double iy = points.get(i).getY();
			final double jx = points.get(j).getX();
			final double jy = points.get(j).getY();
			
			if ((iy < y && jy >= y || jy < y && iy >= y) && (ix <= x || jx <= x)) 
			{
				odd ^= (ix + (y - iy) / (jy - iy) * (jx - ix) < x); 
			}
			j = i; 
		}
		return odd;
	}

//	/**
//	 * @return Whether pt is inside convex counterclockwise polygon poly.
//	 */
//	public boolean containsConvex(final Point2D pt)
//	{	
//		final int numPoints = points.size();
//		for (int i = 0; i < numPoints; i++)
//		{
//			final Point2D a = points.get(i);
//			final Point2D b = points.get((i + 1) % numPoints);
//			
//			if (MathRoutines.whichSide(pt, a, b) < 0)
//				return false;
//		}
//	 	return true;
//	}
	
	//-------------------------------------------------------------------------

	/**
	 * Generate polygon from the description of sides.
	 * 
	 * Can't really distinguish Cell from Vertex versions here (-ve turns make
	 * ambiguous cases) so treat both the same.
	 */
	public void fromSides(final TIntArrayList sides, final int[][] steps)
	{
		int step = steps.length - 1;
		int row = 0;
		int col = 0;
	
		clear();
		
		points.add(new Point2D.Double(col, row));
		
		for (int n = 0; n < sides.size(); n++)
		{
			final int nextStep = sides.get(n);
			
			step = (step + (nextStep < 0 ? -1 : 1) + steps.length) % steps.length;
			
			row += nextStep * steps[step][0];
			col += nextStep * steps[step][1];
			
			points.add(new Point2D.Double(col, row));
		}
	}

	public void fromSides(final TIntArrayList sides, final double[][] steps)
	{
		int step = steps.length - 1;
		double x = 0;
		double y = 0;
	
		clear();
		
		points.add(new Point2D.Double(x, y));
		
		for (int n = 0; n < sides.size(); n++)
		{
			final int nextStep = sides.get(n);
			
			step = (step + (nextStep < 0 ? -1 : 1) + steps.length) % steps.length;
			
			x += nextStep * steps[step][0];
			y += nextStep * steps[step][1];
			
			points.add(new Point2D.Double(x, y));
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Inflates the polygon outwards by the specified amount.
	 * Useful for board shapes that need to be inflated so don't intersect
	 * cell position exactly. 
	 */
	public void inflate(final double amount)
	{
		final List<Point2D> adjustments = new ArrayList<Point2D>();
		
		for (int n = 0; n < points.size(); n++)
		{
			final Point2D ptA = points.get(n);
			final Point2D ptB = points.get((n + 1) % points.size());
			final Point2D ptC = points.get((n + 2) % points.size());
			
			final boolean clockwise = MathRoutines.clockwise(ptA, ptB, ptC);
			
			Vector vecIn  = null;
			Vector vecOut = null;
			
			if (clockwise)
			{
				vecIn  = new Vector(ptA, ptB);
				vecOut = new Vector(ptC, ptB);
			}
			else
			{
				vecIn  = new Vector(ptB, ptA);
				vecOut = new Vector(ptB, ptC);				
			}
				
			vecIn.normalise();
			vecOut.normalise();
			
			vecIn.scale( amount, amount);
			vecOut.scale(amount, amount);
			
			final double xx = (vecIn.x() + vecOut.x()) * 0.5;
			final double yy = (vecIn.y() + vecOut.y()) * 0.5;
			
			adjustments.add(new Point2D.Double(xx, yy));
		}
		
		for (int n = 0; n < points.size(); n++)
		{
			final Point2D pt = points.get(n);
			final Point2D adjustment = adjustments.get((n - 1 + points.size()) % points.size());
			
			final double xx = pt.getX() + adjustment.getX();
			final double yy = pt.getY() + adjustment.getY();
			
			points.remove(n);
			points.add(n, new Point2D.Double(xx, yy));
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Bounding box of points.
	 */
	public Rectangle2D bounds()
	{
		if (points.isEmpty())
			return new Rectangle2D.Double();
		
		double x0 =  1000000;
		double y0 =  1000000;
		double x1 = -1000000;
		double y1 = -1000000;
		
		for (final Point2D pt : points)
		{
			final double x = pt.getX();
			final double y = pt.getY();
			
			if (x < x0)	x0 = x;
			if (x > x1)	x1 = x;
			if (y < y0)	y0 = y;
			if (y > y1)	y1 = y;
		}
				
		return new Rectangle2D.Double(x0, y0, x1-x0, y1-y0);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{			
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Polygon:");
		for (final Point2D pt : points)
			sb.append(" (" + pt.getX() + "," + pt.getY() + ")");

		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
}
