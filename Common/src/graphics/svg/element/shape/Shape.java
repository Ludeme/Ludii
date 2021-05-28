package graphics.svg.element.shape;

import java.awt.geom.Rectangle2D;

import graphics.svg.element.BaseElement;

//-----------------------------------------------------------------------------

/**
 * Base class for SVG shapes. Shapes are scoped by <angled brackets>.
 * @author cambolbro
 */
public abstract class Shape extends BaseElement
{
	
	//-------------------------------------------------------------------------

	public Shape(final String label)
	{
		super(label);
	}

	//-------------------------------------------------------------------------

	public Rectangle2D.Double bounds()
	{
		return bounds;
	}
		
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		return style.load(expr);		
	}
	
	//-------------------------------------------------------------------------

	@Override
	public double strokeWidth()
	{
		return style.strokeWidth();
	}

	//-------------------------------------------------------------------------

}
