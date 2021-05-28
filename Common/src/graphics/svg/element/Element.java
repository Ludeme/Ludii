package graphics.svg.element;

import java.awt.Color;
import java.awt.Graphics2D;

//-----------------------------------------------------------------------------

/**
 * SVG element type.
 * @author cambolbro
 */
public interface Element
{
	/**
	 * @return Label for this element.
	 */
	public String label();
	
	/**
	 * @return Drawing style for this element.
	 */
	public Style style();
		
	/**
	 * @param other
	 * @return Comparison with other element (order in file).
	 */
	public int compare(final Element other);
	
	/**
	 * @return New element of own type.
	 */
	public Element newInstance();
	
	/**
	 * @return New element of own type.
	 */
	public Element newOne();
	
	/**
	 * Load this element's data from an SVG expression.
	 * @return Whether expression is in the right format and data was loaded.
	 */
	public boolean load(final String expr);
	
	/**
	 * Render this element to a Graphics2D canvas.
	 */
	public abstract void render
	(
		final Graphics2D g2d, 
		final double x0, final double y0, 
		final Color footprintColour, final Color fillColour, final Color strokeColour
	);

}
