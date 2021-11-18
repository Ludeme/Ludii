package graphics.qr_codes;

/* 
 * Fast QR Code generator demo
 * 
 * Run this command-line program with no arguments. The program creates/overwrites a bunch of
 * PNG and SVG files in the current working directory to demonstrate the creation of QR Codes.
 * 
 * Copyright (c) Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/fast-qr-code-generator-library
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

/**
 * cambolbro: Added Ludii logo insertion into final image.
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Objects;

//-----------------------------------------------------------------------------

public final class ToImage 
{
	public static BufferedImage toLudiiCodeImage
	(
		final QrCode qr, final int scale, final int border
	) 
	{
		final BufferedImage img = toImage(qr, scale, border, 0xFFFFFF, 0x000000);
		insertLudiiLogo(img, scale, false);
		return img;
	}
	
	public static BufferedImage toImage(QrCode qr, int scale, int border) 
	{
		return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
	}
	
	/**
	 * Returns a raster image depicting the specified QR Code, with
	 * the specified module scale, border modules, and module colors.
	 * <p>For example, scale=10 and border=4 means to pad the QR Code with 4 light border
	 * modules on all four sides, and use 10&#xD7;10 pixels to represent each module.
	 * @param qr the QR Code to render (not {@code null})
	 * @param scale the side length (measured in pixels, must be positive) of each module
	 * @param border the number of border modules to add, which must be non-negative
	 * @param lightColor the color to use for light modules, in 0xRRGGBB format
	 * @param darkColor the color to use for dark modules, in 0xRRGGBB format
	 * @return a new image representing the QR Code, with padding and scaling
	 * @throws NullPointerException if the QR Code is {@code null}
	 * @throws IllegalArgumentException if the scale or border is out of range, or if
	 * {scale, border, size} cause the image dimensions to exceed Integer.MAX_VALUE
	 */
	public static BufferedImage toImage
	(
		QrCode qr, int scale, int border, int lightColor, int darkColor
	) 
	{
		Objects.requireNonNull(qr);
		if (scale <= 0 || border < 0)
			throw new IllegalArgumentException("Value out of range");
		if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
			throw new IllegalArgumentException("Scale or border too large");
		
		//System.out.println("border=" + border + ", qr.size=" + qr.size + ", scale=" + scale);
		
		final int width  = (qr.size + border * 2) * scale;
		final int height = (qr.size + border * 2) * scale;
		
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) 
			{
				boolean color = qr.getModule(x / scale - border, y / scale - border);
				img.setRGB(x, y, color ? darkColor : lightColor);
			}
		return img;
	}
	
	/**
	 * Insert the Ludii logo in the centre of the image.
	 * Destructively modifies the code, so use MEDIUM error correction level or better!
	 * @param img
	 * @param scale
	 * @param invert
	 */
	public static void insertLudiiLogo
	(
		final BufferedImage img, final int scale, final boolean invert
	)
	{
		// . . . . . . . . .
		// . o o o . o o o .
		// . o . o . o . o .
		// . o o o o o o o .
		// . . . o . o . . .
		// . o o o o o o o .
		// . o . o . o . o .
		// . o o o . o o o . 
		// . . . . . . . . .
		
		final int cx = img.getWidth()  / 2;
		final int cy = img.getHeight() / 2;
		
		final int r = scale;
		
		final int d2  = 2 * scale;
		final int d92 = 9 * scale / 2;
		
	 	Graphics2D g2d = (Graphics2D)img.getGraphics();
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    	// Fill square background area
    	if (invert)
    		g2d.setColor(Color.black);
    	else
    		g2d.setColor(Color.white);
    	
    	g2d.fillRect(cx-d92, cy-d92, 2*d92, 2*d92);
    	
    	// Draw logo in opposite colour
    	if (invert)
    		g2d.setColor(Color.white);
    	else
    		g2d.setColor(Color.black);

     	final GeneralPath path = new GeneralPath();
    	path.append(new Arc2D.Double(cx-d2-r, cy+d2-r, 2*r, 2*r,  90, 270, Arc2D.OPEN), true);
    	path.append(new Arc2D.Double(cx-d2-r, cy-d2-r, 2*r, 2*r,   0, 270, Arc2D.OPEN), true);
    	path.append(new Arc2D.Double(cx+d2-r, cy-d2-r, 2*r, 2*r, 270, 270, Arc2D.OPEN), true);
    	path.append(new Arc2D.Double(cx+d2-r, cy+d2-r, 2*r, 2*r, 180, 270, Arc2D.OPEN), true);
        path.closePath();

        final float sw = (float)(1.25 * scale);
        g2d.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    	g2d.draw(path);
	}	
	
//	// Helper function to reduce code duplication.
//	private static void writePng(BufferedImage img, String filepath) throws IOException 
//	{
//		ImageIO.write(img, "png", new File(filepath));
//	}
	
	/**
	 * Returns a string of SVG code for an image depicting the specified QR Code, with the specified
	 * number of border modules. The string always uses Unix newlines (\n), regardless of the platform.
	 * @param qr the QR Code to render (not {@code null})
	 * @param border the number of border modules to add, which must be non-negative
	 * @param lightColor the color to use for light modules, in any format supported by CSS, not {@code null}
	 * @param darkColor the color to use for dark modules, in any format supported by CSS, not {@code null}
	 * @return a string representing the QR Code as an SVG XML document
	 * @throws NullPointerException if any object is {@code null}
	 * @throws IllegalArgumentException if the border is negative
	 */
	public static String toSvgString
	(
		QrCode qr, int border, String lightColor, String darkColor
	) 
	{
		Objects.requireNonNull(qr);
		Objects.requireNonNull(lightColor);
		Objects.requireNonNull(darkColor);
		if (border < 0)
			throw new IllegalArgumentException("Border must be non-negative");
		long brd = border;
		StringBuilder sb = new StringBuilder()
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
			.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n")
			.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 %1$d %1$d\" stroke=\"none\">\n",
				qr.size + brd * 2))
			.append("\t<rect width=\"100%\" height=\"100%\" fill=\"" + lightColor + "\"/>\n")
			.append("\t<path d=\"");
		for (int y = 0; y < qr.size; y++)
			for (int x = 0; x < qr.size; x++)
				if (qr.getModule(x, y)) 
				{
					if (x != 0 || y != 0)
						sb.append(" ");
					sb.append(String.format("M%d,%dh1v1h-1z", x + brd, y + brd));
				}
		return sb
			.append("\" fill=\"" + darkColor + "\"/>\n")
			.append("</svg>\n")
			.toString();
	}
}
