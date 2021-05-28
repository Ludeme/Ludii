package app.utils;

import java.awt.image.BufferedImage;

import util.ImageInfo;

/**
 * Object that links a drawn bufferedImage to other useful information about it.
 * 
 * @author Matthew Stephenson
 */
public class DrawnImageInfo
{
	
	private final BufferedImage pieceImage;
	private final ImageInfo imageInfo;
	
	//-------------------------------------------------------------------------
	
	public DrawnImageInfo(final BufferedImage pieceImage, final ImageInfo imageInfo)
	{
		this.pieceImage = pieceImage;
		this.imageInfo = imageInfo;
	}
	
	//-------------------------------------------------------------------------

	public BufferedImage pieceImage()
	{
		return pieceImage;
	}
	
	public ImageInfo imageInfo()
	{
		return imageInfo;
	}
	
	//-------------------------------------------------------------------------
	
}
