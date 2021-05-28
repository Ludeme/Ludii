package graphics.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import graphics.svg.element.BaseElement;
import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * Contents of an SVG file. 
 * @author cambolbro
 */
public class SVG
{
	//private final XMLHeader = null;
	//private final SVGHeader = null;

	//private double width  = 0;
	//private double height = 0;
	
	private final List<Element> elements = new ArrayList<Element>();

	private Rectangle2D.Double bounds = new Rectangle2D.Double();
	
	//-------------------------------------------------------------------------
	
	public List<Element> elements()
	{
		return Collections.unmodifiableList(elements);
	}
	
	//-------------------------------------------------------------------------

//	public double width()
//	{
//		return width;
//	}
//
//	public double height()
//	{
//		return height;
//	}
//
//	public void setWidth(final double set)
//	{
//		width = set;
//	}
//
//	public void setHeight(final double set)
//	{
//		height = set;
//	}
	
	public Rectangle2D.Double bounds()
	{
		return bounds;
	}
	
	//-------------------------------------------------------------------------

	public void clear()
	{
		elements.clear();
	}
	
	//-------------------------------------------------------------------------

	public void setBounds()
	{
		bounds = null;
		for (Element element : elements)
		{
			((BaseElement)element).setBounds();
			if (bounds == null)
			{
				bounds = new Rectangle2D.Double();
				bounds.setRect(((BaseElement)element).bounds());
			}
			else
			{	
				bounds.add(((BaseElement)element).bounds());
			}
		}
//		System.out.println("Bounds are: " + bounds);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Maximum stroke width specified for any element.
	 */
	public double maxStrokeWidth()
	{
		double maxWidth = 0;
		for (Element element : elements)
		{
			final double sw = ((BaseElement)element).strokeWidth();
			if (sw > maxWidth)
				maxWidth = sw;
		}
		return maxWidth;
	}

	//-------------------------------------------------------------------------

	/**
	 * Render an image from the SVG code just parsed.
	 */
	public BufferedImage render
	(	
		final Color fillColour, final Color borderColour, final int desiredSize
	)
	{			
		final int x0 = (int)(bounds.getX()) - 1;
		final int x1 = (int)(bounds.getX() + bounds.getWidth()) + 1;
		final int sx = x1 - x0;
		
		final int y0 = (int)(bounds.getY()) - 1;
		final int y1 = (int)(bounds.getY() + bounds.getHeight()) + 1;
		final int sy = y1 - y0;
		
		//final double scale = maxDim / (double)Math.max(sx, sy);

		//final int imgSx = (int)(scale * sx + 0.5);
		//final int imgSy = (int)(scale * sy + 0.5);
		
//		final BufferedImage image = new BufferedImage(imgSx, imgSy, BufferedImage.TYPE_INT_ARGB);
		BufferedImage image = new BufferedImage(sx, sy, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = image.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		//g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				
		// Pass 1: Fill footprint with player colour
		for (Element element : elements)
			element.render(g2d, -bounds.getX(), -bounds.getY(), fillColour, null, null);
		
		// Pass 2: Fill paths with border colour
		for (Element element : elements)
			element.render(g2d, -bounds.getX(), -bounds.getY(), null, borderColour, null);
			
		// Pass 3: Stroke edges in border colour (if element's stroke width > 0)
		for (Element element : elements)
			if (element.style().strokeWidth() > 0)
			{
				System.out.println("Stroking element " + element.label());
				element.render(g2d, -bounds.getX(), -bounds.getY(), null, null, borderColour);
			}
		
		// Resize image to requested size
//		int sxDesired = sx;
//		int syDesired = sy;
//		
//		if (sx >= sy)
//		{
//			// Image is wider than tall (or square) 
//			syDesired = (int)(desiredSize * sx / (double)sy + 0.5); 
//			
//		}
//		else
//		{
//			// Image is taller than wide 
//			sxDesired = (int)(desiredSize * sy / (double)sx + 0.5); 
//		}
		
		image = resize(image, desiredSize, desiredSize);  //sxDesired, syDesired);
		
		return image;
	}

	//-------------------------------------------------------------------------

	/**
	 * Resizes a buffered image to the specified width and height values
	 * 
	 * @param img buffered image to be resized
	 * @param newW width in pixels
	 * @param newH height in pixels
	 * @return Resized image.
	 */
	public static BufferedImage resize(final BufferedImage img, final int newW, final int newH) 
	{ 
	    final Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    final BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    final Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}  

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(elements.size() + " elements:\n");
		for (Element element : elements)
			sb.append(element + "\n");
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
}
