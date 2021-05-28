package util;
import java.awt.Point;

import game.equipment.component.Component;
import game.types.board.SiteType;

/**
 * Object for storing all relevant information about a component image.
 * 
 * @author Matthew.Stephenson
 */
public class ImageInfo {
	
	/** World position of image. */
	private final Point drawPosn;
	
	/** Image transparency. */
	private final double transparency;
	
	/** Image rotation */
	private final int rotation;
	
	/** The site (index) of the image. */
	private final int site;
	
	/** The level of the image. */
	private final int level;
	
	/** The GraphElementType that the image is placed on. */
	private final SiteType graphElementType;
	
	/** The component associated with this image. */
	private final Component component;
	
	/** The local state of the component associated with this image. */
	private final int localState;
	
	/** The value of the component associated with this image. */
	private final int value;
	
	/** The index of the container where the image is drawn. */
	private final int containerIndex;
	
	/** The size of the image (in pixels). */
	private final int imageSize;
	
	/** the count for the image (including the minus one if dragged)*/ 
	private final int count;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor for specifying all information about image.
	 * @param drawPosn
	 * @param site
	 * @param level
	 * @param graphElementType
	 * @param component
	 * @param localState
	 * @param value
	 * @param transparency
	 * @param rotation
	 * @param containerIndex
	 * @param imageSize
	 * @param count
	 */
	public ImageInfo(final Point drawPosn, final int site, final int level, final SiteType graphElementType, final Component component, final int localState, final int value, final double transparency, final int rotation, final int containerIndex, final int imageSize, final int count)
	{
		this.drawPosn = drawPosn;
		this.transparency = transparency;
		this.rotation = rotation;
		this.site = site;
		this.level = level;
		this.graphElementType = graphElementType;
		this.component = component;
		this.localState = localState;
		this.containerIndex = containerIndex;
		this.imageSize = imageSize;
		this.count = count;
		this.value = value;
	}
	
	/**
	 * Constructor for specifying minimum information about image.
	 * @param drawPosn
	 * @param site
	 * @param level
	 * @param graphElementType
	 */
	public ImageInfo(final Point drawPosn, final int site, final int level, final SiteType graphElementType)
	{
		this.drawPosn = drawPosn;
		transparency = 0;
		rotation = 0;
		this.site = site;
		this.level = level;
		this.graphElementType = graphElementType;
		component = null;
		localState = 0;
		containerIndex = 0;
		imageSize = 0;
		count = 0;
		value = 0;
	}
	
	//-------------------------------------------------------------------------
	
	public Point drawPosn()
	{
		return drawPosn;
	}
	
	public double transparency()
	{
		return transparency;
	}
	
	public int rotation()
	{
		return rotation;
	}
	
	public int site()
	{
		return site;
	}
	
	public int level()
	{
		return level;
	}
	
	public SiteType graphElementType()
	{
		return graphElementType;
	}
	
	public Component component()
	{
		return component;
	}
	
	public int localState()
	{
		return localState;
	}
	
	public int containerIndex()
	{
		return containerIndex;
	}
	
	public int imageSize()
	{
		return imageSize;
	}
	
	public int count()
	{
		return count;
	}

	public int value() 
	{
		return value;
	}
	
	//-------------------------------------------------------------------------
	
}
