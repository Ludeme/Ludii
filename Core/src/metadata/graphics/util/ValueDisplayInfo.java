package metadata.graphics.util;

/**
 * Display information when drawing local states or values on a piece.
 * @author Matthew.Stephenson
 */
public class ValueDisplayInfo {
	
	/** The location to draw the value. */
	private ValueLocationType locationType = ValueLocationType.None;
	
	/** Offset the image by the size of the displayed value. */
	private boolean offsetImage = false;
	
	/** Draw outline around the displayed value. */
	private boolean valueOutline = false;
	
	/**
	 * Default constructor
	 */
	public ValueDisplayInfo()
	{
		// Default constructor
	}
	
	/**
	 * @param locationType
	 * @param offsetImage
	 * @param valueOutline
	 */
	public ValueDisplayInfo(final ValueLocationType locationType, final boolean offsetImage, final boolean valueOutline)
	{
		this.locationType = locationType;
		this.offsetImage = offsetImage;
		this.valueOutline = valueOutline;
	}

	/**
	 * @return locationType
	 */
	public ValueLocationType getLocationType() 
	{
		return locationType;
	}

	/**
	 * @return offsetImage
	 */
	public boolean isOffsetImage() 
	{
		return offsetImage;
	}

	/**
	 * @return valueOutline
	 */
	public boolean isValueOutline() 
	{
		return valueOutline;
	}

}
