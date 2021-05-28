package graphics.svg.element.shape.path;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import graphics.svg.SVGParser;
import graphics.svg.element.Element;
import graphics.svg.element.shape.Shape;
import main.math.MathRoutines;

//-----------------------------------------------------------------------------

/**
 * SVG path shape.
 * @author cambolbro
 */
public class Path extends Shape
{
	// Format: 
	//    <path d="M 100 100 L 300 50 zM 100 100 l 300 50 l 300 250 Z"
    //          fill="red" stroke="blue" stroke-width="3" />
	
	// For a good description of path ops, see: https://www.w3.org/TR/SVG/paths.html
	
	private final List<PathOp> ops = new ArrayList<PathOp>();
	
	private final double pathLength = 0;  // optional author-specified estimate of path length

	//-------------------------------------------------------------------------
	
	public Path()
	{
		super("path");
	}
	
	//-------------------------------------------------------------------------

	public List<PathOp> ops()
	{
		return Collections.unmodifiableList(ops);
	}
	
	public double pathLength()
	{
		return pathLength;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newInstance()
	{
		return new Path();
	}
		
	//-------------------------------------------------------------------------

	@Override
	public void setBounds()
	{
		double x0 =  10000;
		double y0 =  10000;
		double x1 = -10000;
		double y1 = -10000;
		
		for (final PathOp op : ops)
		{
			final Rectangle2D.Double bound = (Rectangle2D.Double)op.bounds();
			
			if (bound == null)
				continue;  // op has no bounds
			
			if (bound.x < x0)
				x0 = bound.x;
			if (bound.y < y0)
				y0 = bound.y;
			
			final double x = bound.x + bound.width; 
			final double y = bound.y + bound.height; 
			
			if (x > x1)
				x1 = x;
			if (y > y1)
				y1 = y;
		}
			
		bounds.setRect(x0, y0, x1-x0, y1-y0);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;

		if (!super.load(expr))
			return false;

//		System.out.println("Loaded style: " + style);
			
		ops.clear();

		// Load path ops from expression
		if (expr.contains(" d=\""))
		{
			// Contains path data
			final int pos = expr.indexOf(" d=\"");
			String str = SVGParser.extractStringAt(expr, pos+3);
			if (str == null)
			{
				System.out.println("* Failed to extract string from: " + expr.substring(pos+3));
				return false;
			}

//			int c = 0;
//			while (c < str.length())
//			{
//				final char ch = Character.toLowerCase(str.charAt(c));
//				if (ch >= 'a' && ch <= 'z')
//				{
//					// Is (probably) a path op label
//					final PathOp op = PathOpFactory.get().generate(ch);
//					if (op == null)
//					{
//						System.out.println("* Couldn't find path op with label: " + op);
//						return false;
//					}
//					
//					String strOp = str.substring(c);
//					strOp = strOp.replaceAll("-", " -");
//					
//					op.load(strOp);
//					ops.add(op);
//				}
//				c++;
//			}

			// **
			// ** BEWARE OF THIS!
			// **
			// ** From: https://www.w3.org/TR/SVG/paths.html
			// **
			// ** A command letter may be eliminated if an identical command letter would otherwise precede it; 
			// ** for instance, the following contains an unnecessary second "L" command:
			// **
			// **     M 100 200 L 200 100 L -100 -200
			// **
			// ** It may be expressed more compactly as:
			// **
			// **     M 100 200 L 200 100 -100 -200
			// **
			
			str = str.replaceAll("-", " -");
			
			PathOp prevOp = null;
			//Point2D.Double prevCP = null;
			
			// current[0] = is the path's current point following the last operation
			// current[1] = is the last operation last control point (else null)
			final Point2D.Double[] current = new Point2D.Double[2];
			
			while (!str.isEmpty())
			{
				str = str.trim();
				
				PathOp op = prevOp;

				final char ch = str.charAt(0);
				if (Character.toLowerCase(ch) >= 'a' && Character.toLowerCase(ch) <= 'z')
				{
					op = PathOpFactory.get().generate(ch);
					if (op == null)
					{	
						System.out.println("* Couldn't find path op for leading char: " + str);
						return false;
					}
					str = str.substring(1).trim();
				}
				else if (!SVGParser.isNumeric(ch))
				{
					System.out.println("* Non-numeric leading char: " + str);
					return false;
				}
	
				final List<Double> values = new ArrayList<Double>(); 
				str = extractValues(str, op.expectedNumValues(), values);
				
				if (str == null)
				{
					// Done
					//System.out.println("* Error extracting values.");
					//return false;
					return true;
				}

				op.setValues(values, current);
				//if (!op.setValues(values))
				//{
				//	System.out.println("* Couldn't set values for op: " + values + " (" + op.expectedNumValues() + " expected).");
				//	return false;
				//}
				ops.add(op);
				
				prevOp = op;  // remember in case next command is duplicated and omitted 
			}
		}
		
		return okay;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Extracts the specified number of values and removes them from the string.
	 * @param strIn
	 * @param numExpected
	 * @param values
	 * @return String with values removed.
	 */
	public static String extractValues(final String strIn, final int numExpected, final List<Double> values)
	{
		values.clear();
		
		String str = new String(strIn);
		
		//System.out.println("Extracting " + numExpected + " values from: " + strIn);
		
		while (values.size() < numExpected)
		{
			str = str.trim();
		
			if (str.isEmpty())
				return null;
			
			if (!SVGParser.isNumeric(str.charAt(0)))
				return null;
			
			if (str.charAt(0) == '0' && str.charAt(1) != '.')
			{
				// Single digit number for 0
				values.add(Double.valueOf(0.0));
				str = str.substring(1);
			}
			else
			{
				String sub = "";
				int c;
				for (c = 0; c < str.length() && SVGParser.isNumeric(str.charAt(c)); c++)
					sub += str.charAt(c);
			
//				System.out.println("-- sub: " + sub);
		
				final Double result;
				try
				{
					result = Double.parseDouble(sub);
				}
				catch (final Exception e)
				{
					System.out.println("* Error extracting Double from: " + sub);
					e.printStackTrace();
					return null;
				}

//				System.out.println("-- result: " + result);

				values.add(result);
				str = str.substring(c);
			}
		}
		
		return str;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label() + ": fill=" + style.fill() + ", stroke=" + style.stroke() + ", strokeWidth=" + style.strokeWidth());
		for (final PathOp op : ops)
			sb.append("\n   " + op + (op.absolute() ? " *" : ""));
			
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	public static boolean isMoveTo(final PathOp op)
	{
		return Character.toLowerCase(op.label()) == 'm';
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void render
	(
		final Graphics2D g2d, final double x0, final double y0, 
		final Color footprintColour, final Color fillColour, final Color strokeColour
	)
	{
		if (footprintColour != null)
		{
			// Fill this path's total footprint, including holes
			g2d.setPaint(footprintColour);
			
			int c = 0;
			while (c < ops.size())
			{
				// Move to start of next run
				while (c < ops.size() && !ops.get(c).isMoveTo())
					c++;
				if (c >= ops.size())
					break;
				
				// Check whether next run is clockwise
				final List<Point2D> run = nextRunFrom(c);
				if (MathRoutines.isClockwise(run))
				{
					// Close this sub-path and fill it
					final GeneralPath subPath = new GeneralPath(Path2D.WIND_NON_ZERO);
					do
					{
						ops.get(c).apply(subPath,  x0, y0);
						c++;
						
					} while (c < ops.size() && !ops.get(c).isMoveTo());
					subPath.closePath();
					g2d.fill(subPath);
				}
				else
				{
					// Skip this counterclockwise run
					c++;
					while (c < ops.size() && !ops.get(c).isMoveTo())
						c++;
				}
				
				if (c >= ops.size())
					break;
			}
		}
		
		if (fillColour != null)
		{
			// Fill this path as god intended
			final GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD);
			
			for (final PathOp op : ops)
				op.apply(path, x0, y0);
			
			g2d.setPaint(fillColour);
			g2d.fill(path);
		}

		if (strokeColour != null && style.strokeWidth() > 0)
		{
			// Stroke this path as god intended
			final GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD);
			
			for (final PathOp op : ops)
				op.apply(path, x0, y0);
			
			final BasicStroke stroke = new BasicStroke((float)style.strokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
			
			g2d.setPaint(strokeColour);
			g2d.setStroke(stroke);
						
			g2d.draw(path);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Run of points from the specified PathOp to the next MoveTo.
	 */
	List<Point2D> nextRunFrom(final int from)
	{
		final List<Point2D> pts = new ArrayList<Point2D>();
		
		int c = from;
		do
		{
			ops.get(c).getPoints(pts);
			c++;
		} while (c < ops.size() && !ops.get(c).isMoveTo());
		
		return pts;
	}

	@Override
	public Element newOne()
	{
		return new Path();
	}
		
	//-------------------------------------------------------------------------
	
}
