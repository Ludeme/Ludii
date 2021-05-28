package graphics.svg.element.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.SVGParser;
import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG rectangle shape.
 * @author cambolbro
 */
public class Rect extends Shape
{
	// Formats:
	// <rect x="155" y="5" width="75" height="100"/>
    // <rect x="250" y="5" width="75" height="100" rx="30" ry="20" />

	private double x = 0;
	private double y = 0;
	private double width  = 0;
	private double height = 0;
	private double rx = 0;
	private double ry = 0;

	//-------------------------------------------------------------------------
	
	public Rect()
	{
		super("rect");
	}
	
	//-------------------------------------------------------------------------

	public double x()
	{
		return x;
	}

	public double y()
	{
		return y;
	}

	public double width()
	{
		return width;
	}
	
	public double height()
	{
		return height;
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
		return new Rect();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void setBounds()
	{
		bounds.setRect(x, y, width, height);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;
		
		if (!super.load(expr))
			return false;
		
		if (expr.contains(" x="))
		{
			final Double result = SVGParser.extractDouble(expr, " x=");
			if (result == null)
				return false;
			x = result.doubleValue();
		}

		if (expr.contains(" y="))
		{
			final Double result = SVGParser.extractDouble(expr, " y=");
			if (result == null)
				return false;
			y = result.doubleValue();
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
		
		if (expr.contains(" width="))
		{
			final Double result = SVGParser.extractDouble(expr, " width=");
			if (result == null)
				return false;
			width = result.doubleValue();
		}

		if (expr.contains(" height="))
		{
			final Double result = SVGParser.extractDouble(expr, " height=");
			if (result == null)
				return false;
			height = result.doubleValue();
		}
		
		return okay;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(label() + ": fill=" + style.fill() + ", stroke=" + style.stroke() + ", strokeWidth=" + style.strokeWidth());
		sb.append(" : x=" + x + ", y=" + y + ", rx=" + rx + ", ry=" + ry + 
				               ", width=" + width + ", height=" + height);
		//str += style.toString();
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
		return new Rect();
	}

	//-------------------------------------------------------------------------

}
