package graphics.svg;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * An SVG operation.
 * @author cambolbro
 */
public class SVGPathOp
{
	public static enum PathOpType
	{
		ArcTo,
		MoveTo,
		LineTo,
		HLineTo,
		VLineTo,
		CurveTo,
		QuadraticTo,
		ShortCurveTo,
		ShortQuadraticTo,
		ClosePath,
	}

	private final PathOpType type;
	
	private final boolean absolute;
	
	private final List<Point2D.Double> pts = new ArrayList<Point2D.Double>();
	private double xAxisRotation = 0;
	private double largeArcSweep = 0;
	private int sweepFlag = 0;
	
	//-------------------------------------------------------------------------
	
	public SVGPathOp(final PathOpType type, final boolean absolute, final String[] subs)
	{
		this.type = type;
		this.absolute = absolute;
		
//		System.out.print("Parsed:");
//		for (String sub : subs)
//			System.out.print(" " + sub);
//		System.out.println();
	
		parseNumbers(subs);
	}
	
	//-------------------------------------------------------------------------
	
	public PathOpType type()
	{
		return type;
	}
	
	public boolean absolute()
	{
		return absolute;
	}
	
	public List<Point2D.Double> pts()
	{
		return Collections.unmodifiableList(pts);
	}
	
	public double xAxisRotation()
	{
		return xAxisRotation;
	}
	
	public double largeArcSweep()
	{
		return largeArcSweep;
	}
	
	public int sweepFlag()
	{
		return sweepFlag;
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @return SVG character code (case sensitive).
//	 */
//	public abstract char code();
//	
//	//-------------------------------------------------------------------------
//
//	/**
//	 * @return SVG character code (lower case).
//	 */
//	public abstract int process(final String[] subs, final int s, final List<SVGop> ops);
	
	//-------------------------------------------------------------------------

	boolean parseNumbers(final String[] subs)
	{
		if (subs == null)
			return true;  // nothing to do
		
		if (subs.length % 2 != 0 && subs.length != 7)
		{
			System.out.println("** Odd number of substrings.");
			return false;
		}
		
		for (int s = 0; s < subs.length; s += 2)
		{
			//if (subs[s] == null)
			//	continue;
			//System.out.println("subs[" + s + "] is: " + subs[s]);
			
			double da = -1;
			double db = -1;
			
			// Get first (x) value
			String str = subs[s].trim();
			try
			{
				da = Double.parseDouble(str);
			}
			catch (final Exception e)
			{
				System.out.println("** '" + str + "' is not a double (x, " + s + ").");
				e.printStackTrace();
				return false;
			}
			
			if (s < subs.length - 1)
			{
				// Get second (y) value
				String str1 = subs[s+1].trim();
				try
				{
					db = Double.parseDouble(str1);
				}
				catch (final Exception e)
				{
					System.out.println("** '" + str1 + "' is not a double (y, " + s + ").");
					e.printStackTrace();
					return false;
				}
			}
			pts.add(new Point2D.Double(da, db));
		}
	
		if (subs.length == 7)
		{
			// Is an ArcTo
			xAxisRotation = pts.get(2).x;
			largeArcSweep = pts.get(2).y;
			sweepFlag = (int)pts.get(3).x;
			pts.remove(2);
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
		
}
