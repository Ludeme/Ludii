package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG stroke width property.
 * @author cambolbro
 */
public class StrokeWidth extends Style
{
	// Format:  stroke-width="1"
	
	private final double width = 1;

	//-------------------------------------------------------------------------
	
	public StrokeWidth()
	{
		super("stroke-width");
	}
	
	//-------------------------------------------------------------------------

	public double width()
	{
		return width;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new StrokeWidth();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;
		
		// ...
		
		return okay;
	}

	@Override
	public Element newInstance()
	{
		return null;
	}

	@Override
	public void render
	(
		Graphics2D g2d, double x0, double y0, Color footprintColour, 
		Color fillColour, Color strokeColour
	)
	{
		// ...
		
	}

	@Override
	public void setBounds()
	{
		// ...
		
	}
	
	//-------------------------------------------------------------------------

}
