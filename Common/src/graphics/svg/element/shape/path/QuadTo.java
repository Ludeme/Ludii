package graphics.svg.element.shape.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import graphics.svg.SVGParser;

//-----------------------------------------------------------------------------

/**
 * SVG path quadratic curve to operation.
 * @author cambolbro
 */
public class QuadTo extends PathOp
{
	// Format: 
	//   Q 100 100 200 200
    //   q 100 100 200 200
	
	private double x1 = 0;
	private double y1 = 0;
	private double x = 0;
	private double y = 0;

	//-------------------------------------------------------------------------
	
	public QuadTo()
	{
		super('Q');
	}
	
	//-------------------------------------------------------------------------

	public double x1()
	{
		return x1;
	}

	public double y1()
	{
		return y1;
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
		return new QuadTo();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Rectangle2D.Double bounds()
	{
		final double x0 = Math.min(x1, x);
		final double y0 = Math.min(y1, y);
		final double width  = Math.max(x1, x) - x0;
		final double height = Math.max(y1, y) - y0;

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
		
		int c = 1;
		
		final Double resultX1 = SVGParser.extractDoubleAt(expr, c);
		if (resultX1 == null)
		{
			System.out.println("* Failed to read X1 from " + expr + ".");
			return false;
		}
		x1 = resultX1.doubleValue();
			
		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		final Double resultY1 = SVGParser.extractDoubleAt(expr, c);
		if (resultY1 == null)
		{
			System.out.println("* Failed to read Y1 from " + expr + ".");
			return false;
		}
		y1 = resultY1.doubleValue();

		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
			c++;

		final Double resultX2 = SVGParser.extractDoubleAt(expr, c);
		if (resultX2 == null)
		{
			System.out.println("* Failed to read X2 from " + expr + ".");
			return false;
		}
		x = resultX2.doubleValue();
			
		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		final Double resultY2 = SVGParser.extractDoubleAt(expr, c);
		if (resultY2 == null)
		{
			System.out.println("* Failed to read Y2 from " + expr + ".");
			return false;
		}
		y = resultY2.doubleValue();
		
		return true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int expectedNumValues()
	{
		return 4;
	}

	@Override 
	public void setValues(final List<Double> values, final Point2D[] current)
	{
//		if (values.size() != expectedNumValues())
//			return false;
		
		x1 = values.get(0).doubleValue();
		y1 = values.get(1).doubleValue();
		x  = values.get(2).doubleValue();
		y  = values.get(3).doubleValue();
		
		current[0] = new Point2D.Double(x, y);
		current[1] = new Point2D.Double(x1, y1);
	}

	//-------------------------------------------------------------------------

	@Override
	public void getPoints(final List<Point2D> pts)
	{
		pts.add(new Point2D.Double(x1, y1));
		pts.add(new Point2D.Double(x, y));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label + ": x1=" + x1 + ", y1=" + y1 + ", x=" + x + ", y=" + y);
				
		return sb.toString();

	}
	
	//-------------------------------------------------------------------------

	@Override
	public void apply(final GeneralPath path, final double x0, final double y0)
	{
		if (absolute())
		{
			path.quadTo(x0+x1, y0+y1, x0+x, y0+y);
		}
		else
		{
			final Point2D pt = path.getCurrentPoint();
			path.quadTo(pt.getX()+x1, pt.getY()+y1, pt.getX()+x, pt.getY()+y);
		}
//		return new Point2D.Double(x1, y1);
	}
	
	//-------------------------------------------------------------------------

}
