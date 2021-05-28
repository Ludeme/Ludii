package graphics.svg.element.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG dash array property.
 * @author cambolbro
 */
public class StrokeDashArray extends Style
{
	// Format:  stroke-dasharray= ?
	
	private final List<Integer> dash = new ArrayList<Integer>();

	//-------------------------------------------------------------------------
	
	public StrokeDashArray()
	{
		super("stroke-dasharray");
	}
	
	//-------------------------------------------------------------------------

	public List<Integer> dash()
	{
		return Collections.unmodifiableList(dash);
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new StrokeDashArray();
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
