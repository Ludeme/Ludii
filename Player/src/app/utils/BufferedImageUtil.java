package app.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import app.PlayerApp;

/**
 * Functions for helping out with image manipulation and rendering.
 * 
 * @author Matthew Stephenson
 */
public class BufferedImageUtil
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Makes a buffered image translucent.
	 */
	public static BufferedImage makeImageTranslucent(final BufferedImage source, final double alpha)
	{
		if (source == null)
			return null;
		
	    final BufferedImage target = new BufferedImage(source.getWidth(),
	        source.getHeight(), java.awt.Transparency.TRANSLUCENT);
	    // Get the images graphics
	    final Graphics2D g = target.createGraphics();
	    // Set the Graphics composite to Alpha
	    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
	        (float) alpha));
	    // Draw the image into the prepared receiver image
	    g.drawImage(source, null, 0, 0);
	    // let go of all system resources in this Graphics
	    g.dispose();
	    // Return the image
	    return target;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Flips a buffered image vertically.
	 */
	public static BufferedImage createFlippedVertically(final BufferedImage image)
    {
		if (image == null)
			return null;
		
        final AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        return createTransformed(image, at);
    }
	
	//-------------------------------------------------------------------------

	/**
	 * Flips a buffered image horizontally.
	 */
	public static BufferedImage createFlippedHorizontally(final BufferedImage image)
    {
		if (image == null)
			return null;
		
        final AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-image.getWidth(), 0));
        return createTransformed(image, at);
    }
	
	//-------------------------------------------------------------------------

	/**
	 * Transforms a buffered image.
	 */
	public static BufferedImage createTransformed(final BufferedImage image, final AffineTransform at)
    {
		if (image == null)
			return null;
		
        final BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = newImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }
	
	//-------------------------------------------------------------------------

	/**
	 * Rotates a buffered image.
	 */
	public static BufferedImage rotateImageByDegrees(final BufferedImage img, final double angle) 
	{
		if (img == null)
			return null;
		
        final double rads = Math.toRadians(angle);
        final int w = img.getWidth();
        final int h = img.getHeight();

        final BufferedImage rotated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        final AffineTransform at = new AffineTransform();
        at.translate(0, 0);

        final int x = w / 2;
        final int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        
        return rotated;
    }
	
	//-------------------------------------------------------------------------

	/**
	 * Resizes a buffered image.
	 */
	public static BufferedImage resize(final BufferedImage img, final int newW, final int newH)
	{
		if (img == null)
			return null;
		
		int width = newW;
		int height = newH;
		
		if (newW < 1)
			width = 1;
		
		if (newH < 1)
			height = 1;
		
	    final Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	    final BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	    final Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Sets all pixels of an image to a certain colour (masked pieces).
	 * Use componentStyle.createPieceImageColourSVG instead when possible.
	 */
	public static BufferedImage setPixelsToColour(final BufferedImage image, final Color colour)
	{
		if (image == null)
			return null;
		
		for (int y = 0; y < image.getHeight(); ++y)
		{
		    for (int x = 0; x < image.getWidth(); ++x)
		    {
		    	  if(image.getRGB(x,y) != 0x00)
		    	  {
		    		  image.setRGB(x, y, colour.getRGB());
		    	  }
		    }
		}
		
		return image;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Deep copies a buffered image.
	 */
	public static BufferedImage deepCopy(final BufferedImage image)
	{
		 final ColorModel cm = image.getColorModel();
		 final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 final WritableRaster raster = image.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Combines two bufferedImage objects together.
	 */
	public static BufferedImage joinBufferedImages(final BufferedImage img1, final BufferedImage img2) 
	{
		return joinBufferedImages(img1, img2, 0, 0);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Combines two bufferedImage objects together, with specified offsets.
	 */
	public static BufferedImage joinBufferedImages(final BufferedImage img1,final BufferedImage img2, final int offsetX, final int offsetY) 
	{
		final int w = Math.max(img1.getWidth(), img2.getWidth());
		final int h = Math.max(img1.getHeight(), img2.getHeight());
		final BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = combined.createGraphics();
		g.drawImage(img1, 0, 0, null);
		g.drawImage(img2, offsetX, offsetY, null);
		return combined;
    }
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * Returns a bufferedImage of an SVG file.
//	 */
//	public static BufferedImage getImageFromSVGName(final String svgName, final int imageSize)
//	{
//		final String SVGPath = ImageUtil.getImageFullPath(svgName);
//		SVGGraphics2D g2d = new SVGGraphics2D(imageSize, imageSize);
//		g2d = new SVGGraphics2D(imageSize, imageSize);
//		SVGtoImage.loadFromString
//		(
//			g2d, SVGPath, imageSize, imageSize, 0, 0,
//			Color.BLACK, Color.BLACK, true, 0
//		);
//		return SVGUtil.createSVGImage(g2d.getSVGDocument(), imageSize, imageSize);
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines if a specified point overlaps an image in the graphics cache.
	 */
	public static boolean pointOverlapsImage(final Point p, final BufferedImage image, final Point imageDrawPosn)
	{
		try
		{								
			final int imageWidth = image.getWidth();
			final int imageHeight = image.getHeight();

			final int pixelOnImageX = p.x - imageDrawPosn.x;
			final int pixelOnImageY = p.y - imageDrawPosn.y;

			if (pixelOnImageX < 0 || pixelOnImageY < 0 || pixelOnImageY > imageHeight || pixelOnImageX > imageWidth)
			{
				return false;
			}

			final int pixelClicked = image.getRGB(pixelOnImageX, pixelOnImageY);

			if( (pixelClicked>>24) != 0x00 )
			{
				return true;
			}
		}
		catch (final Exception E)
		{
			return false;
		}
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws a String on top of a bufferedImage
	 */
	public static BufferedImage createImageWithText(final PlayerApp app, final BufferedImage image, final String string)
	{
	    final Graphics2D g2d = image.createGraphics();
	    
	    g2d.setFont(app.bridge().settingsVC().displayFont());
	   
	    g2d.setColor(Color.RED);
	    g2d.drawString(string, image.getWidth()/2, image.getHeight()/2);
	    return image; // but return the second
	}
	
	//-------------------------------------------------------------------------
}
