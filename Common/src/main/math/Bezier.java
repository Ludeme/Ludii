package main.math;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Cubic Bezier segment.
 * 
 * @author cambolbro
 */
public final class Bezier
{
	private final Point2D[] cps = new Point2D[4];

	//-------------------------------------------------------------------------

	public Bezier()
	{
	}

	public Bezier(final List<Point2D> pts)
	{
		cps[0] = pts.get(0);
		cps[1] = pts.get(1);
		cps[2] = pts.get(2);
		cps[3] = pts.get(3);
	}

	public Bezier(final Point2D[] pts)
	{
		cps[0] = pts[0];
		cps[1] = pts[1];
		cps[2] = pts[2];
		cps[3] = pts[3];
	}

	public Bezier(final Float[][] pts)
	{
		cps[0] = new Point2D.Double(pts[0][0].floatValue(), pts[0][1].floatValue());
		cps[1] = new Point2D.Double(pts[1][0].floatValue(), pts[1][1].floatValue());
		cps[2] = new Point2D.Double(pts[2][0].floatValue(), pts[2][1].floatValue());
		cps[3] = new Point2D.Double(pts[3][0].floatValue(), pts[3][1].floatValue());
	}

	public Bezier(final Point2D ptA, final Point2D ptAperp, final Point2D ptB, final Point2D ptBperp)
	{
		cps[0] = ptA;
		cps[3] = ptB;
		
		final Vector vecA = new Vector(ptA, ptAperp);
		final Vector vecB = new Vector(ptB, ptBperp);
		
		vecA.normalise();
		vecB.normalise();
		
		final double distAB = MathRoutines.distance(ptA, ptB);
		final double off = 0.333 * distAB;
		
		final Point2D ptA1 = new Point2D.Double
								 (
									ptA.getX() - off * vecA.y(), 
									ptA.getY() + off * vecA.x()
								 );
		final Point2D ptA2 = new Point2D.Double
								 (
									ptA.getX() + off * vecA.y(), 
									ptA.getY() - off * vecA.x()
								 );
		
		final Point2D ptB1 = new Point2D.Double
								 (
									ptB.getX() - off * vecB.y(), 
									ptB.getY() + off * vecB.x()
								 );
		final Point2D ptB2 = new Point2D.Double
								 (
									ptB.getX() + off * vecB.y(), 
									ptB.getY() - off * vecB.x()
								 );
		
		if (MathRoutines.distance(ptA1, ptB) < MathRoutines.distance(ptA2, ptB))
			cps[1] = ptA1;
		else
			cps[1] = ptA2;
		
		if (MathRoutines.distance(ptB1, ptA) < MathRoutines.distance(ptB2, ptA))
			cps[2] = ptB1;
		else
			cps[2] = ptB2;
	}

	public Bezier(final Point ptA, final Point ptAperp, final Point ptB, final Point ptBperp)
	{
		this
		(
			new Point2D.Double(ptA.x, ptA.y), 
			new Point2D.Double(ptAperp.x, ptAperp.y), 
			new Point2D.Double(ptB.x, ptB.y), 
			new Point2D.Double(ptBperp.x, ptBperp.y)
		);
	}

	//-------------------------------------------------------------------------

	public Point2D[] cps()
	{
		return cps;
	}
		
	//-------------------------------------------------------------------------

	public double length()
	{
		return 	MathRoutines.distance(cps[0], cps[1]) 
				+
				MathRoutines.distance(cps[1], cps[2]) 
				+
				MathRoutines.distance(cps[2], cps[3]); 
	}
	
	public Point2D midpoint()
	{
		//return sample(0.5);
		
		final Point2D ab = new Point2D.Double((cps[0].getX() + cps[1].getX()) / 2, (cps[0].getY() + cps[1].getY()) / 2); 
		final Point2D bc = new Point2D.Double((cps[1].getX() + cps[2].getX()) / 2, (cps[1].getY() + cps[2].getY()) / 2); 
		final Point2D cd = new Point2D.Double((cps[2].getX() + cps[3].getX()) / 2, (cps[2].getY() + cps[3].getY()) / 2); 
		
		final Point2D abbc = new Point2D.Double((ab.getX() + bc.getX()) / 2, (ab.getY() + bc.getY()) / 2); 
		final Point2D bccd = new Point2D.Double((bc.getX() + cd.getX()) / 2, (bc.getY() + cd.getY()) / 2); 
		
		return new Point2D.Double((abbc.getX() + bccd.getX()) / 2, (abbc.getY() + bccd.getY()) / 2);	
	}
	
	// De Casteljau algorithm
	public Point2D sample(final double t)
	{
		final Point2D ab = MathRoutines.lerp(t, cps[0], cps[1]); 
		final Point2D bc = MathRoutines.lerp(t, cps[1], cps[2]); 
		final Point2D cd = MathRoutines.lerp(t, cps[2], cps[3]); 
		
		final Point2D abbc = MathRoutines.lerp(t, ab, bc); 
		final Point2D bccd = MathRoutines.lerp(t, bc, cd); 
		
		return MathRoutines.lerp(t, abbc, bccd);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Bounding box of points.
	 */
	public Rectangle2D bounds()
	{
		double x0 =  1000000;
		double y0 =  1000000;
		double x1 = -1000000;
		double y1 = -1000000;
		
		for (final Point2D pt : cps)
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
		
		sb.append("Bezier:");
		for (final Point2D pt : cps)
			sb.append(" (" + pt.getX() + "," + pt.getY() + ")");

		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
}
