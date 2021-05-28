package graphics.svg.element.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG polygon shape.
 * @author cambolbro
 */
public class Polygon extends Polyline
{

	//-------------------------------------------------------------------------
	
	public Polygon()
	{
		super("polygon");  // load using Polyline.load()
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Element newInstance()
	{
		return new Polygon();
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
	
	//-------------------------------------------------------------------------

}
