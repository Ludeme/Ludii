package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG line cap property.
 * @author cambolbro
 */
public class StrokeLineCap extends Style
{
	// Format:  stroke-linecap="round"
	
	private final String lineCap = "butt";

	//-------------------------------------------------------------------------
	
	public StrokeLineCap()
	{
		super("stroke-linecap");
	}
	
	//-------------------------------------------------------------------------

	public String lineCap()
	{
		return lineCap;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new StrokeLineCap();
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
