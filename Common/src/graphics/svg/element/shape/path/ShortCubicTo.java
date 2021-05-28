package graphics.svg.element.shape.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import graphics.svg.SVGParser;

//-----------------------------------------------------------------------------

/**
 * SVG path shorthand cubic curve to operation.
 * @author cambolbro
 */
public class ShortCubicTo extends PathOp
{
	// Format: 
	//   S 200 200 300 100
    //   s 200 200 300 100
	
	private double x1 = 0;
	private double y1 = 0;
	private double x2 = 0;
	private double y2 = 0;
	private double x = 0;
	private double y = 0;

	//-------------------------------------------------------------------------
	
	public ShortCubicTo()
	{
		super('S');
	}
	
	//-------------------------------------------------------------------------

	public double x2()
	{
		return x2;
	}

	public double y2()
	{
		return y2;
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
		return new ShortCubicTo();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Rectangle2D.Double bounds()
	{
		final double x0 = Math.min(x1, Math.min(x2, x));
		final double y0 = Math.min(y1, Math.min(y2, y));
		final double width  = Math.max(x1, Math.max(x2, x)) - x0;
		final double height = Math.max(y1, Math.max(y2, y)) - y0;

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

		final Double resultX2 = SVGParser.extractDoubleAt(expr, c);
		if (resultX2 == null)
		{
			System.out.println("* Failed to read X2 from " + expr + ".");
			return false;
		}
		x2 = resultX2.doubleValue();
			
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
		y2 = resultY2.doubleValue();

		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
			c++;

		final Double resultX3 = SVGParser.extractDoubleAt(expr, c);
		if (resultX3 == null)
		{
			System.out.println("* Failed to read X3 from " + expr + ".");
			return false;
		}
		x = resultX3.doubleValue();
			
		while (c < expr.length() && SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		while (c < expr.length() && !SVGParser.isNumeric(expr.charAt(c)))
			c++;
			
		final Double resultY3 = SVGParser.extractDoubleAt(expr, c);
		if (resultY3 == null)
		{
			System.out.println("* Failed to read Y3 from " + expr + ".");
			return false;
		}
		y = resultY3.doubleValue();
			
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
		
		x2 = values.get(0).doubleValue();
		y2 = values.get(1).doubleValue();
		x  = values.get(2).doubleValue();
		y  = values.get(3).doubleValue();
		
		// Calculate x1 and y1:
		// 		(newx1, newy1) = (curx - (oldx2 - curx), cury - (oldy2 - cury))
        //                	   = (2*curx - oldx2, 2*cury - oldy2)

		final double currentX = current[0].getX();
		final double currentY = current[0].getY();
		
		final double oldX = (current[1] == null) ? currentX : current[1].getX(); 
		final double oldY = (current[1] == null) ? currentY : current[1].getY(); 
		
		x1 = 2 * currentX - oldX;
		y1 = 2 * currentY - oldY;

		current[0] = new Point2D.Double(x, y);
		current[1] = new Point2D.Double(x1, y1);
	}

	//-------------------------------------------------------------------------

	@Override
	public void getPoints(final List<Point2D> pts)
	{
		pts.add(new Point2D.Double(x1, y1));
		pts.add(new Point2D.Double(x2, y2));
		pts.add(new Point2D.Double(x, y));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label + ": (x1)=" + x1 + ", (y1)=" + y1 + ", x2=" + x2 + ", y2=" + y2 + ", x=" + x + ", y=" + y);
				
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void apply(final GeneralPath path, final double x0, final double y0)
	{
//		final Point2D pt = path.getCurrentPoint();
//		
//		// Calculate x1 and y1:
//		// 		(newx1, newy1) = (curx - (oldx2 - curx), cury - (oldy2 - cury))
//        //                	   = (2*curx - oldx2, 2*cury - oldy2)
//
//		final double oldX = (prevCP == null) ? pt.getX() : prevCP.getX(); 
//		final double oldY = (prevCP == null) ? pt.getY() : prevCP.getY(); 
//		
//		final double x1 = 2 * pt.getX() - oldX;
//		final double y1 = 2 * pt.getY() - oldY;
//		
//		if (absolute())
//		{
//			path.curveTo(x0+x1, y0+y1, x0+x2, y0+y2, x0+x, y0+y);
//		}
//		else
//		{
//			path.curveTo(pt.getX()+x1, pt.getY()+y1, pt.getX()+x2, pt.getY()+y2, pt.getX()+x, pt.getY()+y);
//		}
//		return new Point2D.Double(x2, y2);
		
		if (absolute())
		{
			path.curveTo(x0+x1, y0+y1, x0+x2, y0+y2, x0+x, y0+y);
		}
		else
		{
			final Point2D pt = path.getCurrentPoint();
			path.curveTo(pt.getX()+x1, pt.getY()+y1, pt.getX()+x2, pt.getY()+y2, pt.getX()+x, pt.getY()+y);
		}
//		return new Point2D.Double(x2, y2);
	}
	
	//-------------------------------------------------------------------------

}
