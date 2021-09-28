package metadata.graphics.util;

/**
 * Display information when drawing local states or values on a piece.
 * @author Matthew.Stephenson
 */
public class ValueDisplayInfo 
{	
	/** The location to draw the value. */
	private ValueLocationType locationType = ValueLocationType.None;
	
	/** Offset the image by the size of the displayed value. */
	private boolean offsetImage = false;
	
	/** Draw outline around the displayed value. */
	private boolean valueOutline = false;
	
	/** Scale of drawn value. */
	private float scale = 1.f;
	
	/** Offset right for drawn value. */
	private float offsetX = 0;
	
	/** Offset down for drawn value. */
	private float offsetY = 0;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor
	 */
	public ValueDisplayInfo()
	{
		// Default constructor
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param locationType
	 * @param offsetImage
	 * @param valueOutline
	 * @param scale 
	 * @param offsetX 
	 * @param offsetY 
	 */
	public ValueDisplayInfo
		(
			final ValueLocationType locationType, 
			final boolean offsetImage, 
			final boolean valueOutline, 
			final float scale,
			final float offsetX,
			final float offsetY
		)
	{
		this.locationType = locationType;
		this.offsetImage = offsetImage;
		this.valueOutline = valueOutline;
		this.scale = scale;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return locationType
	 */
	public ValueLocationType getLocationType() 
	{
		return locationType;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return offsetImage
	 */
	public boolean isOffsetImage() 
	{
		return offsetImage;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return valueOutline
	 */
	public boolean isValueOutline() 
	{
		return valueOutline;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Scale of drawn image along x-axis.
	 */
	public float scale()
	{
		return scale;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Offset right for drawn image.
	 */
	public float offsetX()
	{
		return offsetX;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Offset down for drawn image.
	 */
	public float offsetY()
	{
		return offsetY;
	}
	
	//-------------------------------------------------------------------------

}
