package graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * Image processing routines.
 * @author cambolbro
 */
public class ImageProcessing
{
	//-------------------------------------------------------------------------

//	/**
//	 * Flood fills image, replacing all pixels with the replacement colour, unless target colour or already visited.
//	 * Uses a depth first search approach. (can lead to stack memory overflow for large images)
//	 */
//	public static void floodFillDepth
//	(
//		final BufferedImage img, final int x, final int y, final int minWidth, final int minHeight, final int maxWidth, final int maxHeight,
//		final int[] rgbaTarget, final int[] rgbaReplacement
//	)
//	{
//		if (x < minWidth || y < minHeight || x >= maxWidth || y >= maxHeight)
//			return;
//
//		final WritableRaster raster  = img.getRaster();
//   		final int[] rgba = { 0, 0, 0, 255 };
//
//   		raster.getPixel(x, y, rgba);
//
//   		if (rgba[0] == rgbaReplacement[0] && rgba[1] == rgbaReplacement[1] && rgba[2] == rgbaReplacement[2] && rgba[3] == rgbaReplacement[3])
//    	  	return;  // already visited
//
//   		if (rgba[0] == rgbaTarget[0] && rgba[1] == rgbaTarget[1] && rgba[2] == rgbaTarget[2] && rgba[3] == rgbaTarget[3])
//   			return;  // is target colour
//
//    	raster.setPixel(x, y, rgbaReplacement);
//
//    	final int[][] off = { {-1,0}, {0,1}, {1,0}, {0,-1} };
//		for (int a = 0; a < 4; a++)
//		{
//			floodFillDepth(img, x+off[a][0], y+off[a][1], minWidth, minHeight, maxWidth, maxHeight, rgbaTarget, rgbaReplacement);
//		}
//	}


//	/**
//	 * Flood fills image, replacing all pixels with the replacement colour, unless target colour or already visited.
//	 * Uses a breadth first search approach.
//	 */
//	public static ArrayList<Point> floodFillBreadth
//	(
//		final BufferedImage img, final ArrayList<Point> pointsToCheck, final int x, final int y, final int width, final int height,
//		final int[] rgbaTarget, final int[] rgbaReplacement
//	)
//	{
//		final WritableRaster raster  = img.getRaster();
//		final int[] rgba = { 0, 0, 0, 255 };
//		raster.getPixel(x, y, rgba);
//		raster.setPixel(x, y, rgbaReplacement);
//
//		pointsToCheck.remove(0);
//
//		final int[][] off = { {-1,0}, {0,1}, {1,0}, {0,-1} };
//		for (int a = 0; a < 4; a++)
//		{
//			if (x+off[a][0] < 0 || y+off[a][1] < 0 || x+off[a][0] >= width || y+off[a][1] >= height)
//				continue;
//			raster.getPixel(x+off[a][0], y+off[a][1], rgba);
//			if (rgba[0] == rgbaReplacement[0] && rgba[1] == rgbaReplacement[1] && rgba[2] == rgbaReplacement[2] && rgba[3] == rgbaReplacement[3])
//	    	  	continue;  // already visited
//			if (rgba[0] == rgbaTarget[0] && rgba[1] == rgbaTarget[1] && rgba[2] == rgbaTarget[2] && rgba[3] == rgbaTarget[3])
//	   			continue;  // is target colour
//			boolean inPointsToCheck = false;
//			final Point pointToAdd = new Point(x+off[a][0],y+off[a][1]);
//
//			for (int i =0; i < pointsToCheck.size(); i++)
//			{
//				if ((pointToAdd.x == pointsToCheck.get(i).x) && (pointToAdd.y == pointsToCheck.get(i).y))
//					inPointsToCheck = true;
//			}
//			if (!inPointsToCheck)
//				pointsToCheck.add(new Point(x+off[a][0],y+off[a][1]));
//		}
//   		return pointsToCheck;
//	}

	//-------------------------------------------------------------------------

//	/**
//	 * Contracts an image by one pixel, i.e. shaves off out pixel layer.
//	 * @param img
//	 * @param width
//	 * @param height
//	 */
//	public static void contractImage(final BufferedImage img, final int width, final int height)
//	{
//		int x, y;
//		final int[][] off = { {-1,0}, {0,1}, {1,0}, {0,-1} };
//
//		final WritableRaster raster  = img.getRaster();
//   		final int[] rgba = { 0, 0, 0, 255 };
//   		final int[] rgbaOff = { 0, 0, 0, 0 };
//
//		//	Pass 1: Find border
//		final boolean[][] border = new boolean[width][height];
//
//   		for (y = 0; y < height; y++)
//   			for (x = 0; x < width; x++)
//   			{
//   				raster.getPixel(x, y, rgba);
//   		  		if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 0)
//   		  		{
//   		  			for (int a = 0; a < 4; a++)
//   		  			{
//	   		  			final int xx = x + off[a][0];
//	   		  			final int yy = y + off[a][1];
//	   		  			if (xx >= 0 && yy >= 0 && xx < width && yy < height)
//	   		  			{
//	   		  				raster.getPixel(xx, yy, rgba);
//	   		  				if (rgba[0] == 0 && rgba[1] == 0 && rgba[2] == 0 && rgba[3] == 0)
//	   		  					border[x][y] = true;
//	   		  			}
//   		  			}
//   		  		}
//   			}
//
//   		//	Pass 2: Remove border pixels
//   		for (y = 0; y < height; y++)
//   			for (x = 0; x < width; x++)
//   				if (border[x][y])
//   					raster.setPixel(x, y, rgbaOff);
//	}

	//-------------------------------------------------------------------------

//	/**
//	 * Converts all non-transparent pixels to the specified mask colour.
//	 * @param img
//	 * @param width
//	 * @param height
//	 * @param rgbaMask Mask colour.
//	 */
//	public static void makeMask(final BufferedImage img, final int width, final int height, final int[] rgbaMask)
//	{
//   		final WritableRaster raster  = img.getRaster();
//   		final int[] rgba     = { 0, 0, 0, 255 };
//
//   		for (int y = 0; y < height; y++)
//   			for (int x = 0; x < width; x++)
//   			{
//   				raster.getPixel(x, y, rgba);
//   		  		if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 0)
//   		  			raster.setPixel(x, y, rgbaMask);
//   			}
//	}

	//------------------------------------------------------------------------

//	public static BufferedImage resize(final BufferedImage img, final int newW, final int newH)
//	{
//	    final Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
//	    final BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
//
//	    final Graphics2D g2d = dimg.createGraphics();
//	    g2d.drawImage(tmp, 0, 0, null);
//	    g2d.dispose();
//
//	    return dimg;
//	}

	//------------------------------------------------------------------------

	public static void ballImage
	(
		final Graphics2D g2d,
		final int x0,
		final int y0,
		final int r,
		final Color baseColour
	)
	{
    	//	Create general ball
        final float[] dist = { 0f, 0.25f, 0.4f, 1f};
        final Color[] colors = {Color.WHITE, baseColour, baseColour, Color.BLACK};

        RadialGradientPaint rgp = new RadialGradientPaint(new Point(x0 + r*2/3, y0 + r*2/3), r*2, dist, colors);
        g2d.setPaint(rgp);
        g2d.fill(new Ellipse2D.Double(x0, y0, r * 2, r * 2));

        // add inner shadow
        final float[] dist2 = { 0f, 0.35f, 1f};
        final Color[] colors2 = {new Color(0,0,0,0), new Color(0,0,0,0), Color.BLACK};

        rgp = new RadialGradientPaint(new Point(x0 + r, y0 + r), r*2, dist2, colors2);
        g2d.setPaint(rgp);
        g2d.fill(new Ellipse2D.Double(x0, y0, r * 2, r * 2));
	}
	
	//------------------------------------------------------------------------

	public static void markerImage
	(
		final Graphics2D g2d,
		final int x0,
		final int y0,
		final int r,
		final Color baseColour
	)
	{
		RadialGradientPaint rgp;
		
    	//	Fill flat disc
        g2d.setPaint(baseColour);
        g2d.fill(new Ellipse2D.Double(x0, y0, r * 2, r * 2));

        // Darken exterior
        final float[] dists = { 0f, 0.9f, 1f};
        
        final int rr = baseColour.getRed() / 2;
        final int gg = baseColour.getGreen() / 2;
        final int bb = baseColour.getBlue() / 2;
        
        final Color[] colors = {new Color(0,0,0,0), new Color(0,0,0,0), new Color(rr, gg, bb, 127)};
        //final Color[] colors = {new Color(0,0,0,0), new Color(0,0,0,0), new Color(0, 0, 0, 80)};

        rgp = new RadialGradientPaint(new Point(x0 + r, y0 + r), r, dists, colors);
        g2d.setPaint(rgp);
        g2d.fill(new Ellipse2D.Double(x0, y0, r * 2, r * 2));
        
        // Darken bottom right even more
        //final float[] dist3 = { 0f, 0.9f, 1f};
        //final Color[] colors3 = {new Color(0,0,0,0), new Color(0,0,0,0), new Color(0, 0, 0, 63)};

        rgp = new RadialGradientPaint(new Point(x0 + r-r/16, y0 + r-r/16), r, dists, colors);
        g2d.setPaint(rgp);
        g2d.fill(new Ellipse2D.Double(x0, y0, r * 2, r * 2));
        
        // Add highlight
        //final float[] distsH = { 0.8f, 0.85f, 0.9f};
        final float[] distsH = { 0.85f, 0.9f, 0.95f};
        final Color[] colorsH = { new Color(255,255,255,0), new Color(255,255,255,150), new Color(255,255,255,0) };

        rgp = new RadialGradientPaint(new Point(x0 + r, y0 + r), r, distsH, colorsH);
        g2d.setPaint(rgp);
        g2d.fill(new Ellipse2D.Double(x0, y0, r * 1.666, r * 1.666));
	}

	//------------------------------------------------------------------------

	/**
	 * Create ring image with empty centre.
	 */
	public static void ringImage
	(
		final Graphics2D g2d,
		final int x0,
		final int y0,
		final int imageSize,
		final Color baseColour
	)
	{        
        final int r = (int)(0.425 * imageSize);  // - 1;
        
        final int off = (imageSize - 2 * r) / 2;
         
        final float swO = 0.15f * imageSize;
        final float swI = 0.075f * imageSize;
        
        final Shape circle = new Ellipse2D.Double(x0 + off, y0 + off, r * 2 - 1, r * 2 - 1);
        
        g2d.setStroke(new BasicStroke(swO, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.black);             
        g2d.draw(circle);
	
        g2d.setStroke(new BasicStroke(swI, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(baseColour);             
        g2d.draw(circle);
	}
	//------------------------------------------------------------------------

	/**
	 * Create chocolate piece image.
	 */
	public static void chocolateImage
	(
		final Graphics2D g2d,
		final int imageSize,
		final int numSides,
		final Color baseColour
	)
	{        
		if (numSides != 4)
			System.out.println("** Only four sided chocolate pieces supported.");
		
		final int offO = (int)(0.125 * imageSize);
		final int offI = (int)(0.2 * imageSize);
		
		final Point[][] pts = new Point[4][2];

		g2d.setColor(baseColour);             
		g2d.fillRect(0, 0, imageSize, imageSize);

		pts[0][0] = new Point(offO, imageSize - 1 - offO);
		pts[0][1] = new Point(offI, imageSize - 1 - offI);
		
		pts[1][0] = new Point(offO, offO);
		pts[1][1] = new Point(offI, offI);
		
		pts[2][0] = new Point(imageSize - 1 - offO, offO);
		pts[2][1] = new Point(imageSize - 1 - offI, offI);

		pts[3][0] = new Point(imageSize - 1 - offO, imageSize - 1 - offO);
		pts[3][1] = new Point(imageSize - 1 - offI, imageSize - 1 - offI);
		
		g2d.setColor(baseColour);             
		g2d.fillRect(0, 0, imageSize, imageSize);

		GeneralPath path = new GeneralPath();
		path.moveTo(pts[0][0].x,  pts[0][0].y);
		path.lineTo(pts[1][0].x,  pts[1][0].y);
		path.lineTo(pts[2][0].x,  pts[2][0].y);
		path.lineTo(pts[2][1].x,  pts[2][1].y);
		path.lineTo(pts[1][1].x,  pts[1][1].y);
		path.lineTo(pts[0][1].x,  pts[0][1].y);
		path.closePath();
		g2d.setColor(new Color(255, 230, 200, 100));
		g2d.fill(path);

		path = new GeneralPath();
		path.moveTo(pts[0][0].x,  pts[0][0].y);
		path.lineTo(pts[3][0].x,  pts[3][0].y);
		path.lineTo(pts[2][0].x,  pts[2][0].y);
		path.lineTo(pts[2][1].x,  pts[2][1].y);
		path.lineTo(pts[3][1].x,  pts[3][1].y);
		path.lineTo(pts[0][1].x,  pts[0][1].y);
		path.closePath();
		g2d.setColor(new Color(50, 40, 20, 100));
		g2d.fill(path);
	}

	//------------------------------------------------------------------------

//	public static BufferedImage pillImage
//	(
//		final double pieceScale,
//		final int dim,
//		final int r,
//		final Color baseColour
//	)
//	{
//		final int diameter = r;  // eh?
//	  	final int sz_master = 2*diameter;  // allow margin so shading can be blurred in from outside
//    	final int off = diameter/2;
//    	final Color clr_off   = new Color(0f, 0f, 0f, 0f);
//    	final Color clr_off_w = new Color(255, 255, 255, 1); //1f, 1f, 1f, 0f);
//		final Color clr_on    = new Color(1f, 1f, 1f, 1f);
//
//    	//	Create super-sized image
//    	final BufferedImage img_master = new BufferedImage(sz_master, sz_master, BufferedImage.TYPE_INT_ARGB);
//    	final Graphics2D g2d_master = (Graphics2D)img_master.getGraphics();
//    	g2d_master.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//    	g2d_master.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//   		//	Fill dark shadow circle
//   		int r0=0, g0=0, b0=0, r1=0, g1=0, b1=0;
//		int  r_hi=0,  g_hi=0,  b_hi=0;
//   		final int blur_amount_hi  = 2;
//
//     	r0 = Math.max(0, baseColour.getRed() - 75);
//   		g0 = Math.max(0, baseColour.getGreen() - 75);
//   		b0 = Math.max(0, baseColour.getBlue() - 75);
//   		r1 = baseColour.getRed();
//   		g1 = baseColour.getGreen();
//   		b1 = baseColour.getBlue();
//
//   		r_hi = Math.min(255, baseColour.getRed() + 255);
//   		g_hi = Math.min(255, baseColour.getGreen() + 250);
//   		b_hi = Math.min(255, baseColour.getBlue() + 240);
//
////   		r_ref = Math.min(255, baseColour.getRed() + 100);
////   		g_ref = Math.min(255, baseColour.getGreen() + 100);
////   		b_ref = Math.min(255, baseColour.getBlue() + 100);
//
//   		final Color clr_hi  = new Color(r_hi, g_hi, b_hi);
//
//   		//	Draw shaded disc
//   		final WritableRaster raster = img_master.getRaster();
//   		final int[] rgba = { 0, 0, 0, 255 };
// 		final int dr = r1 - r0;
//  		final int dg = g1 - g0;
//  		final int db = b1 - b0;
//   		final double radius = diameter / 2.0;
//  		final int cx = sz_master / 2 - 1;
//   		final int cy = sz_master / 2 - 1;
//		for (int x = 0; x < sz_master; x++)
// 	 		for (int y = 0; y < sz_master; y++)
// 	 		{
// 	 			final double dx = x - cx;
// 	 			final double dy = y - cy;
// 	 			final double dist = Math.sqrt(dx * dx + dy * dy) / radius;
//  				double t = 1 - dist;
//
//  				if (dist < .8)
//  					t = 1;
//  				else
//  					t = 1 - (dist - .8) * 5;
//
//  				t = Math.pow(t, .5); //33);
// 				rgba[0] = r0 + (int)(dr * t + 0.5);
// 				rgba[1] = g0 + (int)(dg * t + 0.5);
// 				rgba[2] = b0 + (int)(db * t + 0.5);
// 				raster.setPixel(x, y, rgba);
// 	 		}
//
//		//	Add highlight
//		BufferedImage img_hi = new BufferedImage(sz_master, sz_master, BufferedImage.TYPE_INT_ARGB);
//     	final Graphics2D g2d_hi = (Graphics2D)img_hi.getGraphics();
//    	g2d_hi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//    	g2d_hi.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2d_hi.setPaint(clr_off_w);
//  		g2d_hi.fillRect(0, 0, sz_master, sz_master);
//  		g2d_hi.setPaint(clr_hi);
//  		final float rHi   = 0.41f * diameter;  // radius of top left highlight
//   		final float rHole = 0.575f * diameter;  // radius of hole that clips top left highlight
//   		final float offHole = 0.14f * diameter;
//   		final int x = cx;
//   		final int y = cy; //- 3 * spacing;
//   		final Shape hi = new Ellipse2D.Float(x-rHi, y-rHi, 2*rHi, 2*rHi);
//	   	final Area areaHi = new Area(hi);
//	   	final Shape hole = new Ellipse2D.Float(x-rHole+offHole, y-rHole+offHole, 2*rHole, 2*rHole);
//	   	final Area areaHole = new Area(hole);
//   	   	areaHi.subtract(areaHole);
//        g2d_hi.fill(areaHi);
//        img_hi = Filters.gaussianBlurFilter(blur_amount_hi, true).filter(img_hi, null);
//   		img_hi = Filters.gaussianBlurFilter(blur_amount_hi, false).filter(img_hi, null);
//		g2d_master.drawImage(img_hi, 0, 0, null);
//
//    	//	Clip the master
// 		final BufferedImage img_mask = new BufferedImage(sz_master, sz_master, BufferedImage.TYPE_INT_ARGB);
//     	final Graphics2D g2d_mask = (Graphics2D)img_mask.getGraphics();
//    	g2d_mask.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//    	g2d_mask.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2d_mask.setPaint(clr_off);
//   		g2d_mask.fillRect(0, 0, sz_master, sz_master);
//    	g2d_mask.setPaint(clr_on);
//   		g2d_mask.fillOval(off, off, diameter, diameter);
// 		g2d_master.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
//        g2d_master.drawImage(img_mask, 0, 0, null);
//
////        //	Copy the master to the final result.
////        //	Shrink by one pixel in each direction to stop antialiased pixels on outer edges being clipped.
////        Graphics2D g2d_final = (Graphics2D)this.getGraphics();
////    	g2d_final.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
////    	g2d_final.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
////    	//g2d_final.drawImage(img_master, 0,0, diameter,diameter, off,off, off+diameter,off+diameter, null);
////    	g2d_final.drawImage(img_master, 0,0, diameter,diameter, off-1,off-1, off+diameter+1,off+diameter+1, null);
//
//    	//	Copy the master to the final result.
//        //	Shrink by one pixel in each direction to stop antialiased pixels on outer edges being clipped.
//		final BufferedImage imgFinal = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
//        final Graphics2D g2dFinal = (Graphics2D)imgFinal.getGraphics();
//    	g2dFinal.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//    	g2dFinal.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//    	//g2d_final.drawImage(img_master, 0,0, diameter,diameter, off,off, off+diameter,off+diameter, null);
//    	g2dFinal.drawImage
//    	(
//    			img_master,
//    			(int) (0 + dim * (1-pieceScale)/2),
//    			(int) (0 + dim * (1-pieceScale)/2),
//    			(int) (diameter + dim * (1-pieceScale)/2),
//    			(int) (diameter + dim * (1-pieceScale)/2),
//    			off-1,
//    			off-1,
//    			off+diameter+1,
//    			off+diameter+1,
//    			null
//    	);
//
//    	return imgFinal;
//    }

	//------------------------------------------------------------------------

}
