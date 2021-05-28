package graphics.svg.element.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.SVGParser;
import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG ellipse shape.
 * @author cambolbro
 */
public class Ellipse extends Shape
{
	// Format: <ellipse cx="75" cy="125" rx="50" ry="25" />

	private double cx = 0;
	private double cy = 0;
	private double rx = 0;
	private double ry = 0;

	//-------------------------------------------------------------------------
	
	public Ellipse()
	{
		super("ellipse");
	}
	
	//-------------------------------------------------------------------------

	public double cx()
	{
		return cx;
	}

	public double cy()
	{
		return cy;
	}

	public double rx()
	{
		return rx;
	}

	public double ry()
	{
		return ry;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Element newInstance()
	{
		return new Ellipse();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void setBounds()
	{
		final double x = cx - rx;
		final double y = cy - ry;
		final double width  = 2 * rx;
		final double height = 2 * ry;
		
		bounds.setRect(x, y, width, height);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;
		
		if (!super.load(expr))
			return false;
		
		if (expr.contains(" cx="))
		{
			final Double result = SVGParser.extractDouble(expr, " cx=");
			if (result == null)
				return false;
			cx = result.doubleValue();
		}

		if (expr.contains(" cy="))
		{
			final Double result = SVGParser.extractDouble(expr, " cy=");
			if (result == null)
				return false;
			cy = result.doubleValue();
		}
		
		if (expr.contains(" rx="))
		{
			final Double result = SVGParser.extractDouble(expr, " rx=");
			if (result == null)
				return false;
			rx = result.doubleValue();
		}

		if (expr.contains(" ry="))
		{
			final Double result = SVGParser.extractDouble(expr, " ry=");
			if (result == null)
				return false;
			ry = result.doubleValue();
		}
		
		return okay;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label() + ": fill=" + style.fill() + ", stroke=" + style.stroke() + ", strokeWidth=" + style.strokeWidth());
		sb.append(" : cx=" + cx + ", cy=" + cy + ", rx=" + rx + ", ry=" + ry);
		
		return sb.toString();

	}
	
	//-------------------------------------------------------------------------

	@Override
	public void render
	(
		final Graphics2D g2d, final double x0, final double y0, 
		final Color footprintColour, final Color fillColour, final Color strokeColour
	)
	{
		// ...
	}

	@Override
	public Element newOne()
	{
		return new Ellipse();
	}

	//-------------------------------------------------------------------------

}
