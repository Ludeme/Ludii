package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG fill colour property.
 * @author cambolbro
 */
public class Fill extends Style
{
	// Format:  fill="rgb(255,0,0)"
	
	private final Color colour = new Color(0, 0, 0);

	//-------------------------------------------------------------------------
	
	public Fill()
	{
		super("fill");
	}
	
	//-------------------------------------------------------------------------

	public Color colour()
	{
		return colour;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new Fill();
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
