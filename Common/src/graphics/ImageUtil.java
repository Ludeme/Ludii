package graphics;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import graphics.svg.SVGLoader;

/**
 * Functions for assisting with images.
 * 
 * @author Matthew.Stephenson
 */
public class ImageUtil 
{

	//-------------------------------------------------------------------------
	
	/**
	 * Determines the full file path of a specified image name.
	 */
	public static String getImageFullPath(final String imageName) 
	{
		final String imageNameLower = imageName.toLowerCase();
		final String[] svgNames = SVGLoader.listSVGs();
		
		// Pass 1: Look for exact match
		for (final String svgName : svgNames)
		{
			final String sReplaced = svgName.replaceAll(Pattern.quote("\\"), "/");
			final String[] subs = sReplaced.split("/");
			
			if (subs[subs.length-1].toLowerCase().equals(imageNameLower + ".svg"))
			{
				String fullPath = svgName.replaceAll(Pattern.quote("\\"), "/");
				fullPath = fullPath.substring(fullPath.indexOf("/svg/"));
				return fullPath;
			}
		}
		
		// Pass 2: Look for exact match outside of the Jar, at root location.
		final File svgImage = new File(".");
		final String fileName = imageName.toLowerCase();
		for(final File file : svgImage.listFiles())
	    {
	        if(file.getName().toLowerCase().equals(fileName) || file.getName().toLowerCase().equals(fileName + ".svg"))
	        {
	        	try 
				{
					return file.getCanonicalPath();
				} 
				catch (final IOException e) 
				{
					e.printStackTrace();
				}
	        }
	    }
		
		// Handle predefined image types that do not have an SVG
		if (Arrays.asList(ImageConstants.customImageKeywords).contains(imageNameLower))
			return imageNameLower;  // ball is not an SVG, it's a predefined image type
			
		// Pass 3: Look for best substring match
		String longestName = null;
		String longestNamePath = null;
		for (final String svgName : svgNames)
		{
			final String sReplaced = svgName.replaceAll(Pattern.quote("\\"), "/");
			final String[] subs = sReplaced.split("/");
			final String shortName = subs[subs.length-1].split("\\.")[0].toLowerCase();
						
			if (imageNameLower.contains(shortName))
			{
				String fullPath = svgName.replaceAll(Pattern.quote("\\"), "/");
				fullPath = fullPath.substring(fullPath.indexOf("/svg/"));
				if (longestName == null || shortName.length() > longestName.length())
				{
					longestName = new String(shortName);  // store this closest match so far
					longestNamePath = fullPath;
				}
			}
		}
		
		return longestNamePath;
	}
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * Draws and image at a specified position, size, colour and rotation.
//	 */
//	public static void drawImageAtPosn(final Graphics2D g2d, final String img, final Rectangle2D rect, final Color edgeColour, final Color fillColour, final boolean centerImage, final int rotation) 
//	{		
//		// Need to shift x or y position if the bounds are different.
////		int yPush = 0;
////		int xPush = 0;
////		final Rectangle2D bounds = SVGtoImage.getBounds(img, (int) Math.max(rect.getWidth(), rect.getHeight()));
////		if (bounds.getWidth() > bounds.getHeight())
////			yPush = (int) ((bounds.getWidth() - bounds.getHeight()));
////		if (bounds.getHeight() > bounds.getWidth())
////			xPush = (int) ((bounds.getHeight() - bounds.getWidth()));
////		
////		final int x = (int) (rect.getX() - rect.getWidth() / 2) + xPush/2;
////		final int y = (int) (rect.getY() - rect.getHeight() / 2) + yPush/2;
//		
////		final Rectangle2D adjustedRectangle = rect;
////		if (centerImage) 
////		{
////			adjustedRectangle.setRect(rect.getX() + rect.getWidth()/4, rect.getY() + rect.getHeight()/4, rect.getWidth(), rect.getHeight());
////		}
//
//		SVGtoImage.loadFromString(g2d, img, (int)rect.getWidth(), (int)rect.getHeight(), (int)rect.getX(), (int)rect.getY(), edgeColour, fillColour, false, rotation);
//	}
	
	//-------------------------------------------------------------------------
	
}
