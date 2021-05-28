package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG line join property.
 * @author cambolbro
 */
public class StrokeLineJoin extends Style
{
	// Format:  stroke-linejoin="round"
	
	private final String lineJoin = "miter";

	//-------------------------------------------------------------------------
	
	public StrokeLineJoin()
	{
		super("stroke-linejoin");
	}
	
	//-------------------------------------------------------------------------

	public String lineJoin()
	{
		return lineJoin;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new StrokeLineJoin();
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
