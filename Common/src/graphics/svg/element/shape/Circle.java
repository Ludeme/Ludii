package graphics.svg.element.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.SVGParser;
import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG circle shape.
 * @author cambolbro
 */
public class Circle extends Shape
{
	// Format: <circle cx="50" cy="50" r="25" />	

	private double cx = 0;
	private double cy = 0;
	private double r = 0;

	//-------------------------------------------------------------------------
	
	public Circle()
	{
		super("circle");
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

	public double r()
	{
		return r;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Element newInstance()
	{
		return new Circle();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void setBounds()
	{
		final double x = cx - r;
		final double y = cy - r;
		final double width  = 2 * r;
		final double height = 2 * r;
		
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

		if (expr.contains(" r="))
		{
			final Double result = SVGParser.extractDouble(expr, " r=");
			if (result == null)
				return false;
			r = result.doubleValue();
		}
		
		return okay;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label() + ": fill=" + style.fill() + ", stroke=" + style.stroke() + ", strokeWidth=" + style.strokeWidth());
		sb.append(" : cx=" + cx + ", cy=" + cy + ", r=" + r);
		
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
		return new Circle();
	}

	//-------------------------------------------------------------------------

}
