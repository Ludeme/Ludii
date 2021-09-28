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
	
	/** Scale of drawn image along x-axis. */
	private float scaleX = 1.f;
	
	/** Scale of drawn image along y-axis. */
	private float scaleY = 1.f;
	
	/** Rotation of drawn image. */
	private int rotation = 0;
	
	/** Offset right for drawn image. */
	private float offsetX = 0;
	
	/** Offset down for drawn image. */
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
	 */
	public ValueDisplayInfo
		(
			final ValueLocationType locationType, 
			final boolean offsetImage, 
			final boolean valueOutline, 
			final float scaleX,
			final float scaleY,
			final int rotation,
			final float offsetX,
			final float offsetY
		)
	{
		this.locationType = locationType;
		this.offsetImage = offsetImage;
		this.valueOutline = valueOutline;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
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
	public float scaleX()
	{
		return scaleX;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Scale of drawn image along y-axis.
	 */
	public float scaleY()
	{
		return scaleY;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Rotation of drawn image.
	 */
	public int rotation() 
	{
		return rotation;
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
