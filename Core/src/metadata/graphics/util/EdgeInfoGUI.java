package metadata.graphics.util;

import java.awt.Color;

/**
 * Relevant GUI information about an edge
 * 
 * @author Matthew.Stephenson
 */
public class EdgeInfoGUI 
{
	private LineStyle style;
	private Color colour;
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @param style
	 * @param colour
	 */
	public EdgeInfoGUI
	(
		final LineStyle style,
		final Color colour
	)
	{
		this.setStyle(style);
		this.setColour(colour);
	}

	/**
	 * @return The style of the edge.
	 */
	public LineStyle getStyle() 
	{
		return style;
	}

	/**
	 * Set the style of the edge.
	 * 
	 * @param style
	 */
	public void setStyle(final LineStyle style) 
	{
		this.style = style;
	}

	/**
	 * @return The colour of the edge.
	 */
	public Color getColour() 
	{
		return colour;
	}

	/**
	 * Set the colour of the edge.
	 * 
	 * @param colour
	 */
	public void setColour(final Color colour) 
	{
		this.colour = colour;
	}
}
