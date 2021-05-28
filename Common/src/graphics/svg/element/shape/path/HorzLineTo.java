package graphics.svg.element.shape.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import graphics.svg.SVGParser;

//-----------------------------------------------------------------------------

/**
 * SVG path horizontal line to operation.
 * @author cambolbro
 */
public class HorzLineTo extends PathOp
{
	// Format: 
	//   H 100	
    //   h 100
	
	private double x = 0;
	private double y = 0;

	//-------------------------------------------------------------------------
	
	public HorzLineTo()
	{
		super('H');
	}
	
	//-------------------------------------------------------------------------

	public double x()
	{
		return x;
	}

	//-------------------------------------------------------------------------

	@Override
	public PathOp newInstance()
	{
		return new HorzLineTo();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Rectangle2D.Double bounds()
	{
		return new Rectangle2D.Double(x, y, 0, 0);
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
		
		final Double resultX = SVGParser.extractDoubleAt(expr, c);
		if (resultX == null)
		{
			System.out.println("* Failed to read X from " + expr + ".");
			return false;
		}
		x = resultX.doubleValue();
			
//		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
//			c++;
//			
//		final Double resultY = SVGParser.extractDoubleAt(expr, c);
//		if (resultY == null)
//		{
//			System.out.println("* Failed to read Y from " + expr + ".");
//			return false;
//		}
//		y = resultY.doubleValue();
		
		return true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int expectedNumValues()
	{
		return 1;
	}

	@Override 
	public void setValues(final List<Double> values, final Point2D[] current)
	{
//		if (values.size() != expectedNumValues())
//			return false;
		
		x = values.get(0).doubleValue();
		
		y = current[0].getY();
		
		current[0] = new Point2D.Double(x, y);
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
		
		sb.append(label + ": x=" + x + ", (y)=" + y);
				
		return sb.toString();

	}
	
	//-------------------------------------------------------------------------

	@Override
	public void apply(final GeneralPath path, final double x0, final double y0)
	{
		final Point2D pt = path.getCurrentPoint();
		if (absolute())
		{
			path.moveTo(x0+x, pt.getY());
		}
		else
		{
			path.moveTo(pt.getX()+x, pt.getY());
		}
	}
	
	//-------------------------------------------------------------------------

}
