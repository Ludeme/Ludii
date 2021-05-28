package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG dash offset property.
 * @author cambolbro
 */
public class StrokeDashOffset extends Style
{
	// Format:  stroke-dashoffset="1"
	
	private final double offset = 1;

	//-------------------------------------------------------------------------
	
	public StrokeDashOffset()
	{
		super("stroke-dashoffset");
	}
	
	//-------------------------------------------------------------------------

	public double offset()
	{
		return offset;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new StrokeDashOffset();
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
	public void render(Graphics2D g2d, double x0, double y0, Color footprintColour, Color fillColour,
			Color strokeColour)
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
