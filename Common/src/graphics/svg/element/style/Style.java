package graphics.svg.element.style;

import graphics.svg.element.BaseElement;
import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * Base class for SVG paint elements.
 * @author cambolbro
 */
public abstract class Style extends BaseElement
{

	//-------------------------------------------------------------------------

	public Style(final String label)
	{
		super(label);
	}

	//-------------------------------------------------------------------------

	/**
	 * Load this element's painting properties.
	 * @return Whether expression is in the right format and data was loaded.
	 */
	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;
		
		// ...
		
		return okay;
	}

	//-------------------------------------------------------------------------

	@Override
	public Element newOne()
	{
		return new StrokeDashArray();
	}
	
}
