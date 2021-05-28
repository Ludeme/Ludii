package graphics.svg.element;

import java.awt.geom.Rectangle2D;

//-----------------------------------------------------------------------------

/**
 * Base SVG element type.
 * @author cambolbro
 */
public abstract class BaseElement implements Element
{
	private final String label;
	private int filePos;  // start position in SVG file

	protected final Style style = new Style();

	protected Rectangle2D.Double bounds = new Rectangle2D.Double();

	//-------------------------------------------------------------------------
	
	public BaseElement(final String label)
	{
		this.label = new String(label);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String label()
	{
		return label;
	}
	
	@Override
	public int compare(final Element other)
	{
		return filePos - ((BaseElement)other).filePos;
	}

	//-------------------------------------------------------------------------

	public int filePos()
	{
		return filePos;
	}

	public void setFilePos(final int pos)
	{
		filePos = pos;
	}
	
	@Override
	public Style style()
	{
		return style;
	}

	//-------------------------------------------------------------------------

	public Rectangle2D.Double bounds()
	{
		return bounds;
	}

	/**
	 * Set bounds for this shape.
	 */
	public abstract void setBounds();

	//-------------------------------------------------------------------------

	/**
	 * @return Stroke width of element (else 0 is none specified). 
	 */
	public double strokeWidth()
	{
		return 0;  // default implementation
	}
	
	//-------------------------------------------------------------------------

}
