package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG fill opacity property.
 * @author cambolbro
 */
public class FillOpacity extends Style
{
	// Format:  fill-opacity=".5"
	
	private final double opacity = 1;

	//-------------------------------------------------------------------------
	
	public FillOpacity()
	{
		super("fill-opacity");
	}
	
	//-------------------------------------------------------------------------

	public double opacity()
	{
		return opacity;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new FillOpacity();
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
