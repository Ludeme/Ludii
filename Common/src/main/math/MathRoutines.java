package main.math;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TFloatArrayList;

//-----------------------------------------------------------------------------

/**
 * Useful math routines.
 * @author cambolbro and Dennis Soemers
 */
public final class MathRoutines
{
	public static final double EPSILON = 0.0000001;
	
	public static final double MAX_RANGE = 1000000;
	
	//-------------------------------------------------------------------------
	// Numerical routines

//	/**
//	 * @return Value normalised to range -1..1 (can overshoot).
//	 */
//	public static double normalise(final int value) 
//	{ 
//	    double norm = 0;	
//	    if (value > 0) 
//	        norm =  Math.log10( value) / Math.log10(MAX_RANGE);
//	    else if (value < 0) 
//	        norm = -Math.log10(-value) / Math.log10(MAX_RANGE);
//	    return norm;
//	}

	/**
	 * @return Large value normalised to range -1..1 (clamped to range).
	 */
	public static double normaliseLarge(final double value) 
	{ 
	    double norm = 0;
	
	    if (value > 0) 
	        norm = Math.min(1,  Math.log10( value + 1) / Math.log10(MAX_RANGE));
	    else if (value < 0) 
	        norm = Math.min(1, -Math.log10(-value + 1) / Math.log10(MAX_RANGE));

	    return norm;
	}

	/**
	 * @return Small value normalised to range -1..1.
	 */
	public static double normaliseSmall(final double value) 
	{ 
		return Math.tanh(value);
	}
	
	/**
	 * @param a
	 * @param b
	 * @param epsilon
	 * @return True if and only if the absolute difference between a and b is less than epsilon
	 */
	public static boolean approxEquals(final double a, final double b, final double epsilon)
	{
		return (Math.abs(a - b) < epsilon);
	}
	
	/**
	 * @param x
	 * @return Base-2 logarithm of x: log_2 (x)
	 */
	public static double log2(final double x)
	{
		return Math.log(x) / Math.log(2);
	}

	//-------------------------------------------------------------------------
	// Geometry routines
	
	/**
	 * @return Linear interpolant between two values.
	 */
	public static double lerp(final double t, final double a, final double b)
	{
		return a + t * (b - a);
	}
	
	/**
	 * @return Linear interpolant between two points.
	 */
	public static Point2D lerp(final double t, final Point2D a, final Point2D b)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();

		return new Point2D.Double(a.getX() + t * dx, a.getY() + t * dy);
	}
	
	/**
	 * @return Linear interpolant between two points.
	 */
	public static Point3D lerp(final double t, final Point3D a, final Point3D b)
	{
		final double dx = b.x() - a.x();
		final double dy = b.y() - a.y();
		final double dz = b.z() - a.z();

		return new Point3D(a.x() + t * dx, a.y() + t * dy, a.z() + t * dz);
	}
	
	/**
	 * @return Distance between two points.
	 */
	public static double distance(final Point2D a, final Point2D b)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();

		return Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * @return Distance between two 3D points.
	 */
	public static double distance(final Point3D a, final Point3D b)
	{
		final double dx = b.x() - a.x();
		final double dy = b.y() - a.y();
		final double dz = b.z() - a.z();

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * @return Distance between two points.
	 */
	public static double distanceSquared(final Point2D a, final Point2D b)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();

		return dx * dx + dy * dy;
	}

	/**
	 * @return Distance between two points.
	 */
	public static double distance(final Point a, final Point b)
	{
		final double dx = b.x - a.x;
		final double dy = b.y - a.y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * @return Distance between two points.
	 */
	public static double distance(final double ax, final double ay, final double bx, final double by)
	{
		final double dx = bx - ax;
		final double dy = by - ay;

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * @return Distance between two points.
	 */
	public static double distance
	(
		final double ax, final double ay, final double az, 
		final double bx, final double by, final double bz
	)
	{
		final double dx = bx - ax;
		final double dy = by - ay;
		final double dz = bz - az;

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * @param theta
	 * @param pt
	 * @return Given point, rotated counterclockwise by angle theta about the (0, 0) origin
	 */
	public static Point2D rotate(final double theta, final Point2D pt)
	{
		return new Point2D.Double
		(
			pt.getX() * Math.cos(theta) - pt.getY() * Math.sin(theta), 
			pt.getY() * Math.cos(theta) + pt.getX() * Math.sin(theta)
		);
	}
	
	/**
	 * @param theta
	 * @param pt
	 * @param pivot
	 * @return Given point, rotated counterclockwise by angle theta about the given pivot
	 */
	public static Point2D rotate(final double theta, final Point2D pt, final Point2D pivot)
	{
		return new Point2D.Double
		(
			(pt.getX() - pivot.getX()) * Math.cos(theta) - (pt.getY() - pivot.getY()) * Math.sin(theta) + pivot.getX(), 
			(pt.getY() - pivot.getY()) * Math.cos(theta) + (pt.getX() - pivot.getX()) * Math.sin(theta) + pivot.getY()
		);
	}
	
	//-------------------------------------------------------------------------
		
	/**
	 * @return Whether a and b are coincident.
	 */
	public static boolean coincident(final Point2D a, final Point2D b)
	{
		return distance(a, b) < EPSILON;
	}

	//-------------------------------------------------------------------------
		
	/**
	 * @return Angle between two points, in radians (-PI .. PI).
	 */
	public static double angle(final Point2D a, final Point2D b)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();

		return Math.atan2(dy, dx);
	}

	/**
	 * @return Angle between two points, in radians (0 .. 2*PI).
	 */
	public static double positiveAngle(final double theta)
	{
		double angle = theta;
		
		while (angle < 0)
			angle += 2 * Math.PI;
		
		while (angle > 2 * Math.PI)
			angle -= 2 * Math.PI;
	
		return angle;
	}

	/**
	 * @return Angle between two points, in radians (0 .. 2*PI).
	 */
	public static double positiveAngle(final Point2D a, final Point2D b)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();

		return positiveAngle(Math.atan2(dy, dx));
	}

	/**
	 * @return Angle formed by points a, b and c.
	 */
	public static double angleDifference(final Point2D a, final Point2D b, final Point2D c)
	{
//		double difference = Math.atan2(c.getY() - b.getY(), c.getX() - b.getX()) 
//							- 
//							Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());

//		// From: https://en.wikipedia.org/wiki/Atan2#Angle_sum_and_difference_identity
//		final double x1 = a.getX() - b.getX();
//		final double y1 = a.getY() - b.getY();
//		final double x2 = c.getX() - b.getX();
//		final double y2 = c.getY() - b.getY();
//		
//		double difference = Math.atan2(y1 * x2 - y2 * x1, x1 * x2 - y1 * y2);

//		final Vector vecAB = new Vector(b.getX() - a.getX(), b.getY() - a.getY());
//		final Vector vecBC = new Vector(c.getX() - b.getX(), c.getY() - b.getY());
//		
//		vecAB.normalise();
//		vecBC.normalise();
//		
//		double difference = Math.acos(vecAB.dotProduct(vecBC));
		
//		System.out.println("difference=" + difference + "difference2=" + difference2);
		
		// Partially based on: https://stackoverflow.com/a/3487062/6735980
		//
		// Define two vectors:
		// v = (b - a)
		// u = (c - b)
		//
		// This rotation matrix makes v overlap with the x-axis:
		//
		// [  v.x   v.y ]
		// [ -v.y   v.x ]
		//
		// Applying this rotation matrix to u gives the following vector u':
		//
		// u' = [u'.x] = [ u.x * v.x + u.y * v.y  ]
		//      [u'.y]   [ u.x * -v.y + u.y * v.x ]
		//
		// Now we just need the angle between u' and the x-axis:
		//
		// Difference 	= atan2(u'.y, u'.x)
		//				= atan2(u.x * -v.y + u.y * v.x, u.x * v.x + u.y * v.y)
		
		final double vx = b.getX() - a.getX();
		final double vy = b.getY() - a.getY();
		final double ux = c.getX() - b.getX();
		final double uy = c.getY() - b.getY();
		
		final double difference = Math.atan2(ux * -vy + uy * vx, ux * vx + uy * vy);
		
//		if (difference > Math.PI)
//			difference -= 2 * Math.PI;
//		else if (difference < -Math.PI)
//			difference += 2 * Math.PI;
		
		return difference;
	}
	
	/**
	 * Computes angle formed by points a, b, and c, using an optimised implementation
	 * that is only valid if the x coordinate of the vector (c - b) is positive.
	 * Informally, this restriction means that c must at least somewhat lie in
	 * the same direction a --> b.
	 * 
	 * If the restriction is violated, we'll only know that the true angle difference
	 * will have a big absolute value: > 0.5pi or < - 0.5pi. We will mark this by
	 * returning a value of positive infinity.
	 * 
	 * @return Angle formed by points a, b and c, or infinity if the x coordinate
	 * 	of the vector (c - b) is not positive.
	 */
	public static double angleDifferencePosX(final Point2D a, final Point2D b, final Point2D c)
	{
		final double vx = b.getX() - a.getX();
		final double vy = b.getY() - a.getY();
		final double ux = c.getX() - b.getX();
		final double uy = c.getY() - b.getY();
		
		final double x = ux * vx + uy * vy;
		
		if (x <= 0.0)
			return Double.POSITIVE_INFINITY;
		
		return Math.atan((ux * -vy + uy * vx) / x);
	}
	
	/**
	 * Computes the absolute value of the tangent of the angle formed by the three
	 * points a, b, and c, using an optimised implementation that is only valid if
	 * the x coordinate of the vector (c - b) is positive. Informally, this
	 * restriction means that c must at least somewhat lie in the same direction
	 * a --> b.
	 * 
	 * If the restriction is violated, we return infinity.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return Absolute value of the tangent of angle formed by points a, b and c, 
	 * 	or infinity if the x coordinate of the vector (c - b) is not positive.
	 */
	public static double absTanAngleDifferencePosX(final Point2D a, final Point2D b, final Point2D c)
	{
		final double vx = b.getX() - a.getX();
		final double vy = b.getY() - a.getY();
		final double ux = c.getX() - b.getX();
		final double uy = c.getY() - b.getY();
		
		final double x = ux * vx + uy * vy;
		
		if (x <= 0.0)
			return Double.POSITIVE_INFINITY;
		
		return Math.abs((ux * -vy + uy * vx) / x);
	}
	
	/**
	 * Computes the absolute value of the tangent of the angle formed by the three
	 * points a, b, and c in 3D space, using an optimized implementation that is only 
	 * valid if the dot product of vectors (b - a) and (c - b) is positive. 
	 * 
	 * If the restriction is violated, we return infinity.
	 * 
	 * @param a First point
	 * @param b Second point
	 * @param c Third point
	 * @return Absolute value of the tangent of angle formed by points a, b, and c,
	 *         or infinity if the dot product of (b - a) and (c - b) is not positive.
	 */
	public static double absTanAngleDifference3D(final Point3D a, final Point3D b, final Point3D c) 
	{
	    // Vectors from a->b and b->c
	    final double vx = b.x() - a.x();
	    final double vy = b.y() - a.y();
	    final double vz = b.z() - a.z();
	    final double ux = c.x() - b.x();
	    final double uy = c.y() - b.y();
	    final double uz = c.z() - b.z();
	    
	    // Dot product (u · v)
	    final double dotProduct = ux * vx + uy * vy + uz * vz;
	    
	    // Restriction: if dot product is not positive, return infinity
	    if (dotProduct <= 0.0)
	        return Double.POSITIVE_INFINITY;
	    
	    // Cross product (u × v)
	    final double crossX = vy * uz - vz * uy;
	    final double crossY = vz * ux - vx * uz;
	    final double crossZ = vx * uy - vy * ux;
	    
	    // Magnitude of cross product (|u × v|)
	    final double crossMagnitude = Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);
	    
	    // Return the absolute tangent: |u × v| / (u · v)
	    return Math.abs(crossMagnitude / dotProduct);
	}


	//-------------------------------------------------------------------------
	
	/**
	 * @param pts
	 * @return Whether the polygon is clockwise.
	 */
	public static boolean isClockwise(final List<Point2D> pts)
	{
		double sum = 0;
		for (int n = 0, m = pts.size() - 1; n < pts.size(); m = n++)
		{
			final Point2D ptM = pts.get(m);
			final Point2D ptN = pts.get(n);
			sum += (ptN.getX() - ptM.getX()) * (ptN.getY() + ptM.getY());
		}
		return sum < 0;
	}
	
	public static Point2D.Double normalisedVector(final double x0, final double y0, final double x1, final double y1)
	{
		final double dx = x1 - x0;
		final double dy = y1 - y0;
		
		double len = Math.sqrt(dx * dx + dy * dy);
		if (len == 0)
			len = 1;
		
		return new Point2D.Double(dx / len, dy / len);
	}
	
	//-------------------------------------------------------------------------
	// Probability routines
	
	/**
	 * Computes union of probabilities (using inclusion-exclusion principle)
	 * for all the probabilities stored in given list.
	 * 
	 * @param probs
	 * @return Union of probabilities
	 */
	public static float unionOfProbabilities(final TFloatArrayList probs)
	{
		float union = 0.f;
		
		for (int i = 0; i < probs.size(); ++i)
		{
			float baseEval = probs.getQuick(i);
			
			for (int j = 0; j < i; ++j)
				baseEval *= (1.f - probs.getQuick(j));
			
			union += baseEval;
		}
		
		return union;
	}

	//-------------------------------------------------------------------------
	// Colour routines
	
	/**
	 * @param colour
	 * @param adjust
	 * @return Colour with shade adjusted by specified amount.
	 */
	public static Color shade(final Color colour, final double adjust)
	{
		final int r = Math.max(0, Math.min(255, (int)(colour.getRed()   * adjust + 0.5)));
		final int g = Math.max(0, Math.min(255, (int)(colour.getGreen() * adjust + 0.5)));
		final int b = Math.max(0, Math.min(255, (int)(colour.getBlue()  * adjust + 0.5)));
		return new Color(r, g, b);
	}
	
	//-------------------------------------------------------------------------
	// Auxiliary routines
	
	/**
	 * @param val
	 * @param min
	 * @param max
	 * @return The given value, but clipped between min and max (both inclusive)
	 */
	public static int clip(final int val, final int min, final int max)
	{
		if (val <= min)
			return min;
		if (val >= max)
			return max;
		
		return val;
	}
	
	/**
	 * @param val
	 * @param min
	 * @param max
	 * @return The given value, but clipped between min and max (both inclusive)
	 */
	public static double clip(final double val, final double min, final double max)
	{
		if (val <= min)
			return min;
		if (val >= max)
			return max;
		
		return val;
	}

	//-------------------------------------------------------------------------

//    /**
//     * @return Degrees as radians.
//     */
//    public static double degToRad(final double d)	
//    { 
//    	return d * Math.PI / 180.0; 
//    }
//	
//    /**
//     * @return Radians to degrees.
//     */
//    public static double radToDeg(final double r)	
//    { 
//    	return r * 180.0 / Math.PI; 
//    }

	//-------------------------------------------------------------------------
	
	/**
	 * @return Average position of two points.
	 */
	public static Point2D.Double average(final Point2D a, final Point2D b)
	{
		final double xx = b.getX() + a.getX();
		final double yy = b.getY() + a.getY();

		return new Point2D.Double(xx * 0.5, yy * 0.5);
	}
	
	//-------------------------------------------------------------------------
	// Line routines

	/**
	 * From Morrison "Graphics Gems III" (1991) p10.
	 * @return Distance from pt to infinite line through a and b.
	 */
	public static double distanceToLine(final Point2D pt, final Point2D a, final Point2D b)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();
	
		if (Math.abs(dx) + Math.abs(dy) < EPSILON)
			return distance(pt, a);  // endpoints a and b are coincident
		
		final double a2 = (pt.getY() - a.getY()) * dx - (pt.getX() - a.getX()) * dy;	
		return Math.sqrt(a2 * a2 / (dx * dx + dy * dy));
	}
	
	/**
	 * From Bowyer & Woodwark "A Programmer's Geometry" p47.
	 * @return Distance from pt to line segment ab (squared).
	 */
	public static double distanceToLineSegment(final Point2D pt, final Point2D a, final Point2D b)
	{
		if (Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) < EPSILON)
			return distance(pt, a);  // endpoints a and b are coincident
		
		return Math.sqrt(distanceToLineSegmentSquared(pt, a, b));
	}
	
	/**
	 * From Bowyer & Woodwark "A Programmer's Geometry" p47.
	 * @return Distance from pt to line segment ab (squared).
	 */
	public static double distanceToLineSegmentSquared
	(
		final Point2D pt, final Point2D a, final Point2D b
	)
	{
		final double xkj = a.getX() - pt.getX();
		final double ykj = a.getY() - pt.getY();
		final double xlk = b.getX() - a.getX();
		final double ylk = b.getY() - a.getY();
	
		final double denom = xlk * xlk + ylk * ylk;
	
		if (Math.abs(denom) < EPSILON)
		{
			//	Coincident ends
			return (xkj * xkj + ykj * ykj);
		}
	
		final double t = -(xkj * xlk + ykj * ylk) / denom;
	
		if (t <= 0.0)
		{
			//	Beyond A
			return (xkj * xkj + ykj * ykj);
		}
		else if (t >= 1.0)
		{
			//	Beyond B
			final double xlj = b.getX() - pt.getX();
			final double ylj = b.getY() - pt.getY();
			return (xlj * xlj + ylj * ylj);
		}
		else
		{
			final double xfac = xkj + t * xlk;
			final double yfac = ykj + t * ylk;
			return (xfac * xfac + yfac * yfac);
		}
	}

	/**
	 * From Bowyer & Woodwark "A Programmer's Geometry" p47.
	 * @return Distance from pt to line segment ab (squared).
	 */
	public static double distanceToLineSegment(final Point3D pt, final Point3D a, final Point3D b)
	{
		if (Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z()) < EPSILON)
			return distance(pt, a);  // endpoints a and b are coincident
		
		return Math.sqrt(distanceToLineSegmentSquared(pt, a, b));
	}
	
	/**
	 * From Bowyer & Woodwark "A Programmer's Geometry" p47.
	 * @return Distance from pt to line segment ab (squared).
	 */
	public static double distanceToLineSegmentSquared
	(
		final Point3D pt, final Point3D a, final Point3D b
	)
	{
		final double xkj = a.x() - pt.x();
		final double ykj = a.y() - pt.y();
		final double zkj = a.z() - pt.z();
		
		final double xlk = b.x() - a.x();
		final double ylk = b.y() - a.y();
		final double zlk = b.z() - a.z();
	
		final double denom = xlk * xlk + ylk * ylk + zlk * zlk;
	
		if (Math.abs(denom) < EPSILON)
		{
			//	Coincident ends
			return (xkj * xkj + ykj * ykj + zkj * zkj);
		}
	
		final double t = -(xkj * xlk + ykj * ylk + zkj * zlk) / denom;
	
		if (t <= 0.0)
		{
			//	Beyond A
			return (xkj * xkj + ykj * ykj + zkj * zkj);
		}
		else if (t >= 1.0)
		{
			//	Beyond B
			final double xlj = b.x() - pt.x();
			final double ylj = b.y() - pt.y();
			final double zlj = b.z() - pt.z();
			return (xlj * xlj + ylj * ylj + zlj * zlj);
		}
		else
		{
			final double xfac = xkj + t * xlk;
			final double yfac = ykj + t * ylk;
			final double zfac = zkj + t * zlk;
			return (xfac * xfac + yfac * yfac + zfac * zfac);
		}
	}

	/**
	 * @return Intersection point of two infinite lines, else null if parallel.
	 */
	public static Point2D.Double intersectionPoint
	(
		final Point2D a, final Point2D b, final Point2D c, final Point2D d
	)
	{
		final double dd = 	(a.getX() - b.getX()) * (c.getY() - d.getY()) 
							- 
							(a.getY() - b.getY()) * (c.getX() - d.getX());
	
	    if (Math.abs(dd) < EPSILON)  //dd == 0) 
	    {
	    	System.out.println("** MathRoutines.intersectionPoint(): Parallel lines.");
			return null;  // parallel lines
	    }
	
	    final double xi = (
	    				(c.getX() - d.getX()) * (a.getX()*b.getY() - a.getY()*b.getX()) 
	    				- 
	    				(a.getX() - b.getX()) * (c.getX()*d.getY() - c.getY()*d.getX())
	    			) / dd;
		final double yi = (
						(c.getY() - d.getY()) * (a.getX()*b.getY() - a.getY()*b.getX()) 
						- 
						(a.getY() - b.getY()) * (c.getX()*d.getY() - c.getY()*d.getX())
					) / dd;
		
		return new Point2D.Double(xi, yi);
	}

//	/**
//	 * From Klassen "Graphics Gems IV" (1994) p273, after Bowyer & Woodwark (1983).
//	 * @return Intersection point of two lines, else null if parallel.
//	 */
//	public static Point2D.Double intersectionPoint
//	(
//		final Point2D.Double a0, final Point2D.Double a1,
//		final Point2D.Double b0, final Point2D.Double b1
//	)
//	{	
//		final double xlk = a1.x - a0.x;
//		final double ylk = a1.y - a0.y;		
//		final double xnm = b1.x - b0.x;		
//		final double ynm = b1.y - b0.y;		
//		final double xmk = b0.x - a0.x;		
//		final double ymk = b0.y - a0.y;		
//			
//		final double det = xnm * ylk - ynm * xlk;
//		
//		if (Math.abs(det) < Geometry.EPSILON)
//		{
//			System.out.println("Geometry.intersection(): Parallel lines.");
//			return null;  // parallel lines
//		}
//		
//		final double detinv = 1.0 / det;
//		final double s = (xnm * ymk - ynm * xmk) * detinv;
//		//final double t = (xlk * ymk - ylk * xmk) * detinv;
//
//		return new Point2D.Double(a0.x + s*(a1.x-a0.x), a0.y + s*(a1.y-a0.y));
//	}

	/**
	 * @return Projection point of pt onto infinite line through a and b.
	 */
	public static Point2D.Double projectionPoint
	(
		final Point2D pt, final Point2D a, final Point2D b
	)
	{
		final double dx = b.getX() - a.getX();
		final double dy = b.getY() - a.getY();

		final Vector v = new Vector(dx, dy);
		v.normalise();
		v.perpendicular();

		final Point2D.Double pt2 = new Point2D.Double(pt.getX() + v.x(), pt.getY() + v.y());

		return intersectionPoint(a, b, pt, pt2);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * From: Klassen p273 in Graphics Gems IV (1994), after Bowyer & Woodwark (1983).
	 * @return Whether line segments A and B intersect.
	 */
	public static boolean lineSegmentsIntersect
	(
		final double a0x, final double a0y, final double a1x, final double a1y, 	
		final double b0x, final double b0y, final double b1x, final double b1y 	
	)
	{	
		final double xlk = a1x - a0x;
		final double ylk = a1y - a0y;		
		final double xnm = b1x - b0x;		
		final double ynm = b1y - b0y;		
		final double xmk = b0x - a0x;		
		final double ymk = b0y - a0y;		
			
		final double det = xnm * ylk - ynm * xlk;
		
		if (Math.abs(det) < EPSILON)
			return false;  // parallel lines
		
		final double detinv = 1.0 / det;
		
		final double s = (xnm * ymk - ynm * xmk) * detinv;  // pt on A [0..1]
		final double t = (xlk * ymk - ylk * xmk) * detinv;  // pt on B [0..1]
	
		return s >= -EPSILON && s <= 1 + EPSILON && t >= -EPSILON && t <= 1 + EPSILON;
	}
	
	/**
	 * @return Whether line segments A and B cross internally, i.e. not at the vertices.
	 */
	public static boolean isCrossing
	(
		final double a0x, final double a0y, final double a1x, final double a1y, 	
		final double b0x, final double b0y, final double b1x, final double b1y 	
	)
	{	
		final double MARGIN = 0.01;
		
		final double xlk = a1x - a0x;
		final double ylk = a1y - a0y;		
		final double xnm = b1x - b0x;		
		final double ynm = b1y - b0y;		
		final double xmk = b0x - a0x;		
		final double ymk = b0y - a0y;		
			
		final double det = xnm * ylk - ynm * xlk;
		
		if (Math.abs(det) < EPSILON)
			return false;  // parallel lines
		
		final double detinv = 1.0 / det;
		
		final double s = (xnm * ymk - ynm * xmk) * detinv;  // pt on A [0..1]
		final double t = (xlk * ymk - ylk * xmk) * detinv;  // pt on B [0..1]
	
		return s > MARGIN && s < 1 - MARGIN && t > MARGIN && t < 1 - MARGIN;
	}
	
	/**
	 * @return Whether line segments A and B cross internally, i.e. not at the vertices.
	 */
	public static Point2D crossingPoint
	(
		final double a0x, final double a0y, final double a1x, final double a1y, 	
		final double b0x, final double b0y, final double b1x, final double b1y 	
	)
	{	
		final double MARGIN = 0.01;
		
		final double xlk = a1x - a0x;
		final double ylk = a1y - a0y;		
		final double xnm = b1x - b0x;		
		final double ynm = b1y - b0y;		
		final double xmk = b0x - a0x;		
		final double ymk = b0y - a0y;		
			
		final double det = xnm * ylk - ynm * xlk;
		
		if (Math.abs(det) < EPSILON)
			return null;  // parallel lines
		
		final double detinv = 1.0 / det;
		
		final double s = (xnm * ymk - ynm * xmk) * detinv;  // pt on A [0..1]
		final double t = (xlk * ymk - ylk * xmk) * detinv;  // pt on B [0..1]
	
		if (s > MARGIN && s < 1 - MARGIN && t > MARGIN && t < 1 - MARGIN)
			return new Point2D.Double(a0x + s * (a1x - a0x), a0y + s * (a1y - a0y));
		
		return null;  // no crossing
	}
	
	/**
	 * @return Whether point P touches line segment A, apart from at its vertices.
	 */
	public static Point3D touchingPoint
	(
		final Point3D pt, final Point3D a, final Point3D b	
	)
	{	
		final double MARGIN = 0.001;
	
		final double distToA  = distance(pt, a);
		final double distToB  = distance(pt, b);
		final double distToAB = distanceToLineSegment(pt, a, b);
		
		if (distToA < MARGIN || distToB < MARGIN || distToAB > MARGIN)
			return null;  // not touching
			
		final double distAB = distance(a, b);

		return lerp(distToA/distAB, a, b);
	}

	//-------------------------------------------------------------------------
	// Polygon routines
		
	/**
	 * From Bowyer & Woodwark, p63.
	 * @return Whether three points are in clockwise order.
	 */
	public static boolean clockwise
	(
		double x0, double y0, double x1, double y1, double x2, double y2
	)
	{
		final double result = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
		return (result < EPSILON);
	}
	
	public static boolean clockwise
	(
		final Point2D a, final Point2D b, final Point2D c
	)
	{
		return clockwise(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY());
	}
	
	public static boolean clockwise(final List<Point2D> poly)
	{
		// Use negative area, since Y goes down in screen coordinates
		return polygonArea(poly) < 0;
	}

//	BOOL
//	GEO_Geometry::PtInCorner
//	(
//		CPoint3 const& P, CPoint3 const& A, 
//		CPoint3 const& B, CPoint3 const& C
//	)
//	{
//		return 
//		(
//			WhichSide(P, B, A) == 1 
//			&& 
//			WhichSide(P, B, C) == -1
//		);
//	}
			
	/**
	 * @return Which side of line AB point (x,y) lies on.
	 */
	public static int whichSide
	(
		final double  x, final double  y,
		final double ax, final double ay, 
		final double bx, final double by 
	)
	{
		final double result = (bx - ax) * (y - ay) - (by - ay) * (x - ax);
		if (result < -EPSILON)
			return -1;
		if (result > EPSILON)
			return 1;
		return 0;
	}
			
	public static int whichSide
	(
		final Point2D pt, final Point2D a, final Point2D b
	)
	{
		return whichSide(pt.getX(), pt.getY(), a.getX(), a.getY(), b.getX(), b.getY());
	}

	public static boolean pointInTriangle
	(
		final Point2D pt, final Point2D a, final Point2D b, final Point2D c
	)
	{
		return 	whichSide(pt, a, b) >= 0 
				&& 
				whichSide(pt, b, c) >= 0 
				&& 
				whichSide(pt, c, a) >= 0;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Whether pt is inside convex counterclockwise polygon poly.
	 */
	public static boolean pointInConvexPolygon(final Point2D pt, final Point2D[] poly)
	{	
		final int sz = poly.length;
		for (int i = 0; i < sz; i++)
		{
			final Point2D a = poly[i];
			final Point2D b = poly[(i + 1) % sz] ;
			
			final double side = (pt.getX() - a.getX()) * (b.getY() - a.getY()) - (pt.getY() - a.getY()) * (b.getX() - a.getX());
			if (side < -EPSILON)
				return false;
		}
	 	return true;
	}

	/**
	 * From https://forum.processing.org/one/topic/how-do-i-find-if-a-point-is-inside-a-complex-polygon.html.
	 * @return Whether pt is inside complex polygon.
	 */
	public static boolean pointInPolygon(final Point2D pt, final Point2D[] poly)
	{	
		final int sz = poly.length;
		int j = sz - 1;
		boolean odd = false;
		
		for (int i = 0; i < sz; i++) 
		{
			if 
			(
				(
					poly[i].getY() < pt.getY() && poly[j].getY() >= pt.getY() 
					|| 
					poly[j].getY() < pt.getY() && poly[i].getY() >= pt.getY()
				) 
				&& 
				(poly[i].getX() <= pt.getX() || poly[j].getX() <= pt.getX())
			) 
			{
				odd ^=  (
							poly[i].getX() + (pt.getY() - poly[i].getY())
							/
							(poly[j].getY() - poly[i].getY()) * (poly[j].getX() - poly[i].getX()) 
							< 
							pt.getX()
						); 
			}
			j=i; 
		}
		return odd;
	}

	/**
	 * From https://forum.processing.org/one/topic/how-do-i-find-if-a-point-is-inside-a-complex-polygon.html.
	 * @return Whether pt is inside complex polygon.
	 */
	public static boolean pointInPolygon(final Point2D pt, final List<Point2D> poly)
	{	
		final int sz = poly.size();
		int j = sz - 1;
		boolean odd = false;
		
		final double x = pt.getX();
		final double y = pt.getY();
		
		for (int i = 0; i < sz; i++) 
		{
			final double ix = poly.get(i).getX();
			final double iy = poly.get(i).getY();
			final double jx = poly.get(j).getX();
			final double jy = poly.get(j).getY();
			
			if ((iy < y && jy >= y || jy < y && iy >= y) && (ix <= x || jx <= x)) 
			{
				odd ^= (ix + (y - iy) / (jy - iy) * (jx - ix) < x); 
			}
			j = i; 
		}
		return odd;
	}

	//-------------------------------------------------------------------------

	/**
	 * From: https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
	 * @return Signed area of polygon.
	 */
	public static double polygonArea(final List<Point2D> poly)
	{
		double area = 0;
		
		for (int n = 0; n < poly.size(); n++)
		{
			final Point2D ptN = poly.get(n);
			final Point2D ptO = poly.get((n + 1) % poly.size());
			area += ptN.getX() * ptO.getY() - ptO.getX() * ptN.getY();
		}
		return area / 2.0;
	}

	//-------------------------------------------------------------------------

	/**
	 * Inflates the polygon outwards by the specified amount.
	 * Useful for board shapes that need to be inflated so don't intersect
	 * cell position exactly. 
	 */
	public static void inflatePolygon(final List<Point2D> polygon, final double amount)
	{
		final List<Point2D> adjustments = new ArrayList<Point2D>();
		
		for (int n = 0; n < polygon.size(); n++)
		{
			final Point2D ptA = polygon.get(n);
			final Point2D ptB = polygon.get((n + 1) % polygon.size());
			final Point2D ptC = polygon.get((n + 2) % polygon.size());
			
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
		
		for (int n = 0; n < polygon.size(); n++)
		{
			final Point2D pt = polygon.get(n);
			final Point2D adjustment = adjustments.get((n - 1 + polygon.size()) % polygon.size());
			
			final double xx = pt.getX() + adjustment.getX();
			final double yy = pt.getY() + adjustment.getY();
			
			polygon.remove(n);
			polygon.add(n, new Point2D.Double(xx, yy));
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Bounding box of points.
	 */
	public static Rectangle2D bounds(final List<Point2D> points)
	{
		double x0 =  1000000;
		double y0 =  1000000;
		double x1 = -1000000;
		double y1 = -1000000;
		
		for (final Point2D pt : points)
		{
			final double x = pt.getX();
			final double y = pt.getY();
			
			if (x < x0)
				x0 = x;
			
			if (x > x1)
				x1 = x;
			
			if (y < y0)
				y0 = y;
			
			if (y > y1)
				y1 = y;
		}
		
		if (x0 == 1000000 || y0 == 1000000)
		{
			x0 = 0;
			y0 = 0;
			x1 = 0;
			y1 = 0;
		}
		
		return new Rectangle2D.Double(x0, y0, x1-x0, y1-y0);
	}

	//-------------------------------------------------------------------------
	
}
