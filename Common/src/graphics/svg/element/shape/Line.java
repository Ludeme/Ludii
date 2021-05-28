package graphics.svg.element.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.SVGParser;
import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG line shape.
 * @author cambolbro
 */
public class Line extends Shape
{
	// Format: <line x1="0" y1="150" x2="400" y2="150" stroke-width="2" stroke="blue"/>
	
	private double x1 = 0;
	private double y1 = 0;
	private double x2 = 0;
	private double y2 = 0;

	//-------------------------------------------------------------------------
	
	public Line()
	{
		super("line");
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

	public double x2()
	{
		return x2;
	}

	public double y2()
	{
		return y2;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newInstance()
	{
		return new Line();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void setBounds()
	{
		final double x = Math.min(x1, x2);
		final double y = Math.min(y1, y2);
		final double width  = Math.max(x1, x2) - x;
		final double height = Math.max(y1, y2) - y;
		
		bounds.setRect(x, y, width, height);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;
		
		if (!super.load(expr))
			return false;

		if (expr.contains(" x1="))
		{
			final Double result = SVGParser.extractDouble(expr, " x1=");
			if (result == null)
				return false;
			x1 = result.doubleValue();
		}

		if (expr.contains(" y1="))
		{
			final Double result = SVGParser.extractDouble(expr, " y1=");
			if (result == null)
				return false;
			y1 = result.doubleValue();
		}
		
		if (expr.contains(" x2="))
		{
			final Double result = SVGParser.extractDouble(expr, " x2=");
			if (result == null)
				return false;
			x2 = result.doubleValue();
		}

		if (expr.contains(" y2="))
		{
			final Double result = SVGParser.extractDouble(expr, " y2=");
			if (result == null)
				return false;
			y2 = result.doubleValue();
		}
		
		return okay;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label() + ": fill=" + style.fill() + ", stroke=" + style.stroke() + ", strokeWidth=" + style.strokeWidth());
		sb.append(" : x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2);
		
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
		return new Line();
	}

	//-------------------------------------------------------------------------

}
