package graphics.svg.element.shape.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * SVG path cubic curve to operation.
 * @author cambolbro
 */
public class Arc extends PathOp
{
	// Format: 
	//   A 100 100 0 0,1 100 100
    //   a 100 100 0 0,1 100 100
	
	private double rx = 0;
	private double ry = 0;
	private double xAxis = 0;
	private int largeArc = 0;
	private int sweep = 0;
	private double x = 0;
	private double y = 0;

	//-------------------------------------------------------------------------
	
	public Arc()
	{
		super('A');
	}
	
	//-------------------------------------------------------------------------

	public double rx()
	{
		return rx;
	}

	public double ry()
	{
		return ry;
	}

	public double xAxis()
	{
		return xAxis;
	}

	public int largeArc()
	{
		return largeArc;
	}

	public int sweep()
	{
		return sweep;
	}

	public double x()
	{
		return x;
	}

	public double y()
	{
		return y;
	}

	//-------------------------------------------------------------------------

	@Override
	public PathOp newInstance()
	{
		return new Arc();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Rectangle2D.Double bounds()
	{
		final double x0 = x - rx;
		final double y0 = y - ry;
		final double width  = 2 * rx;
		final double height = 2 * ry;

		return new Rectangle2D.Double(x0, y0, width, height);
	}
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
//		System.out.println("+ Loading " + label + ": " + expr + "...");
		
		// Is absolute if label is upper case
		label = expr.charAt(0); 
//		absolute = (label == Character.toUpperCase(label));

//		int c = 1;
//		
//		final Double resultX1 = SVGParser.extractDoubleAt(expr, c);
//		if (resultX1 == null)
//		{
//			System.out.println("* Failed to read X1 from " + expr + ".");
//			return false;
//		}
//		x1 = resultX1.doubleValue();
//			
//		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		final Double resultY1 = SVGParser.extractDoubleAt(expr, c);
//		if (resultY1 == null)
//		{
//			System.out.println("* Failed to read Y1 from " + expr + ".");
//			return false;
//		}
//		y1 = resultY1.doubleValue();
//
//		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//
//		final Double resultX2 = SVGParser.extractDoubleAt(expr, c);
//		if (resultX2 == null)
//		{
//			System.out.println("* Failed to read X2 from " + expr + ".");
//			return false;
//		}
//		x2 = resultX2.doubleValue();
//			
//		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		final Double resultY2 = SVGParser.extractDoubleAt(expr, c);
//		if (resultY2 == null)
//		{
//			System.out.println("* Failed to read Y2 from " + expr + ".");
//			return false;
//		}
//		y2 = resultY2.doubleValue();
//
//		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//
//		final Double resultX3 = SVGParser.extractDoubleAt(expr, c);
//		if (resultX3 == null)
//		{
//			System.out.println("* Failed to read X3 from " + expr + ".");
//			return false;
//		}
//		x3 = resultX3.doubleValue();
//			
//		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//		
//		final Double resultY3 = SVGParser.extractDoubleAt(expr, c);
//		if (resultY3 == null)
//		{
//			System.out.println("* Failed to read Y3 from " + expr + ".");
//			return false;
//		}
//		y3 = resultY3.doubleValue();
		
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public int expectedNumValues()
	{
		return 7;
	}

	@Override 
	public void setValues(final List<Double> values, final Point2D[] current)
	{
//		if (values.size() != expectedNumValues())
//			return false;
		
		rx       =      values.get(0).doubleValue();
		ry       =      values.get(1).doubleValue();
		xAxis    =      values.get(2).doubleValue();
		largeArc = (int)values.get(3).doubleValue();  // 0 or 1
		sweep    = (int)values.get(4).doubleValue();  // 0 or 1
		x        =      values.get(5).doubleValue();
		y        =      values.get(6).doubleValue();
		
		current[0] = new Point2D.Double(x,  y);
		current[1] = null;
	}

	//-------------------------------------------------------------------------

	@Override
	public void getPoints(final List<Point2D> pts)
	{
		pts.add(new Point2D.Double(x, y));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label + ": rx=" + rx + ", ry=" + ry + ", xAxis=" + xAxis + ", largeArc=" + largeArc + ", sweep=" + sweep + " +, x=" + x + ", y=" + y );
				
		return sb.toString();

	}
	
	//-------------------------------------------------------------------------

	@Override
	public void apply(final GeneralPath path, final double x0, final double y0)
	{
//		path.curveTo(x1, y1, x2, y2, x3, y3);
	}
	
	//-------------------------------------------------------------------------

}
