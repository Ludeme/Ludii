package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;

import game.types.board.SiteType;
import main.Constants;
import main.math.MathRoutines;
import other.topology.Cell;
import other.topology.Topology;

/**
 * Code for determining cell colours for "sunken" effect.
 *  
 * @author cambolbro
 */
public class ShadedCells 
{
	public static void drawShadedCell
	(
		final Graphics2D g2d, final Cell cell, final GeneralPath path, final Color[][] colours, final boolean checkeredBoard, final Topology topology
	)
	{	
		final int phase = !checkeredBoard ? 0 : topology.phaseByElementIndex(SiteType.Cell, cell.index());
								
		final Point2D centre = pathCentre(path);
		//System.out.println("Centre is " + centre);
		
		final double[] coords = new double[6];
		
//		if (colours[phase][1].getAlpha() == 0)
//			return;  // user does not want this cell shown
		
		// Fill full cell dark
		g2d.setColor(colours[phase][0]);
		g2d.fill(path);

		// Fill light half of cell
		final BitSet highlights = determineHighlightedSides(path);
		//System.out.println(highlights);
		
		final GeneralPath pathLight = new GeneralPath();
		double currX = 0;
		double currY = 0;
		int side = 0;
		
		for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) 
		{
	        switch (pi.currentSegment(coords))
	        {
	        case PathIterator.SEG_CLOSE: 
	        	pathLight.closePath();
	        	break;
	        case PathIterator.SEG_CUBICTO:
	        	if (highlights.get(side))
	        	{
	        		pathLight.moveTo(centre.getX(), centre.getY());
	        		pathLight.lineTo(currX, currY);
	        		pathLight.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
	        		pathLight.closePath();
	        		//pathLight.moveTo(centre.getX(), centre.getY());
	        	}
	        	currX = coords[4];
	        	currY = coords[5];
	        	break;
	        case PathIterator.SEG_LINETO:
	        	if (highlights.get(side))
	        	{
	        		pathLight.moveTo(centre.getX(), centre.getY());
	        		pathLight.lineTo(currX, currY);
	        		pathLight.lineTo(coords[0], coords[1]);
	        		pathLight.closePath();
	        	}
	        	currX = coords[0];
	        	currY = coords[1];
	        	break;
	        case PathIterator.SEG_MOVETO:
	        	//pathLight.moveTo(coords[0], coords[1]);
	        	currX = coords[0];
	        	currY = coords[1];
	        	break;
	        case PathIterator.SEG_QUADTO:
	        	if (highlights.get(side))
	        	{
	        		pathLight.moveTo(centre.getX(), centre.getY());
	        		pathLight.lineTo(currX, currY);
	        		pathLight.quadTo(coords[0], coords[1], coords[2], coords[3]);
	        		pathLight.closePath();
	        	}
	        	currX = coords[2];
	        	currY = coords[3];
	        	break;
	        }
	        side++;
		}

		g2d.setColor(colours[phase][2]);
		g2d.fill(pathLight);
				
		// Fill shrunken cell offset in base colour
		final GeneralPath pathInner = new GeneralPath();
		final double amount = 1;
		
		for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) 
		{
	        switch (pi.currentSegment(coords))
	        {
	        case PathIterator.SEG_CLOSE: 
	        	pathInner.closePath();
	        	break;
	        case PathIterator.SEG_CUBICTO:
	        	{
	        		final double distA = MathRoutines.distance(centre.getX(), centre.getY(), coords[0], coords[1]);
	        		final double distB = MathRoutines.distance(centre.getX(), centre.getY(), coords[2], coords[3]);
	        		final double distC = MathRoutines.distance(centre.getX(), centre.getY(), coords[4], coords[5]);
	        		final double offA = (distA - amount) / distA;
	        		final double offB = (distB - amount) / distB;
	        		final double offC = (distC - amount) / distC;
	        		coords[0] = centre.getX() + offA * (coords[0] - centre.getX());
		        	coords[1] = centre.getY() + offA * (coords[1] - centre.getY());
		        	coords[2] = centre.getX() + offB * (coords[2] - centre.getX());
		        	coords[3] = centre.getY() + offB * (coords[3] - centre.getY());
		        	coords[4] = centre.getX() + offC * (coords[4] - centre.getX());
		        	coords[5] = centre.getY() + offC * (coords[5] - centre.getY());
		        	pathInner.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
	        	}
	        	break;
	        case PathIterator.SEG_LINETO:
	        	{
	        		final double dist = MathRoutines.distance(centre.getX(), centre.getY(), coords[0], coords[1]);
	        		final double off = (dist - amount) / dist;
	        		coords[0] = centre.getX() + off * (coords[0] - centre.getX());
	        		coords[1] = centre.getY() + off * (coords[1] - centre.getY());
	        		pathInner.lineTo(coords[0], coords[1]);
	        	}
	        	break;
	        case PathIterator.SEG_MOVETO:
	        	{
	        		final double dist = MathRoutines.distance(centre.getX(), centre.getY(), coords[0], coords[1]);
	              	final double off = (dist - amount) / dist;
	              	coords[0] = centre.getX() + off * (coords[0] - centre.getX());
	        		coords[1] = centre.getY() + off * (coords[1] - centre.getY());
	        		pathInner.moveTo(coords[0], coords[1]);
	        	}
	        	break;
	        case PathIterator.SEG_QUADTO:
	        	{
	        		final double distA = MathRoutines.distance(centre.getX(), centre.getY(), coords[0], coords[1]);
	        		final double distB = MathRoutines.distance(centre.getX(), centre.getY(), coords[2], coords[3]);
	        		final double offA = (distA - amount) / distA;
	        		final double offB = (distB - amount) / distB;
	        		coords[0] = centre.getX() + offA * (coords[0] - centre.getX());
	        	   	coords[1] = centre.getY() + offA * (coords[1] - centre.getY());
	        	   	coords[3] = centre.getX() + offB * (coords[3] - centre.getX());
	        	   	coords[4] = centre.getY() + offB * (coords[4] - centre.getY());
	        	   	pathInner.quadTo(coords[0], coords[1], coords[2], coords[3]);
	        	}
	        	break;
	        } 
		}
			
		g2d.setColor(colours[phase][1]);
		g2d.fill(pathInner);
	}
	
	/**
	 * @return Bit set indicating which sides of the path should be highlighted.
	 */
	private static BitSet determineHighlightedSides(final GeneralPath path)
	{
		final Point2D centre = pathCentre(path);
		//System.out.println("Centre is " + centre);
		
		final double[] coords = new double[6];
		
		// Determine shading of cells
		final BitSet highlights = new BitSet();
		
		int side = 0;
		double currX = 0;
		double currY = 0;
		for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) 
		{
	        switch (pi.currentSegment(coords))
	        {
	        case PathIterator.SEG_CLOSE: 
	        {
	           	break;
	        }
	        case PathIterator.SEG_CUBICTO:
	        {
	        	final double mx = (currX + coords[4]) / 2.0;
	        	final double my = (currY + coords[5]) / 2.0;
	        	
	        	final Point2D vec = MathRoutines.normalisedVector(centre.getX(), centre.getY(), mx, my);
	        	final double theta = Math.atan2(vec.getY(), vec.getX());
	        	
	        	if (theta > 0.1 * Math.PI || theta < -0.9 * Math.PI)
	        		highlights.set(side);
	        	
	        	currX = coords[4];
	        	currY = coords[5];
	        	break;
	        }
	        case PathIterator.SEG_LINETO:
	        {
	        	final double mx = (currX + coords[0]) / 2.0;
	           	final double my = (currY + coords[1]) / 2.0;

	           	final Point2D vec = MathRoutines.normalisedVector(centre.getX(), centre.getY(), mx, my);
	        	final double theta = Math.atan2(vec.getY(), vec.getX());
	        	
	        	//System.out.println("Side " + side + " has vec " + vec);
	        	
	        	if (theta > 0.9 * Math.PI || theta < -0.1 * Math.PI)
	        		highlights.set(side);	        	

	           	currX = coords[0];
	        	currY = coords[1];
	        	break;
	        }
	        case PathIterator.SEG_MOVETO:
	        {
	        	currX = coords[0];
	        	currY = coords[1];
	        	break;
	        }
	        case PathIterator.SEG_QUADTO:
	        {
	        	final double mx = (currX + coords[2]) / 2.0;
	           	final double my = (currY + coords[3]) / 2.0;
	        	
	           	final Point2D vec = MathRoutines.normalisedVector(centre.getX(), centre.getY(), mx, my);
	        	final double theta = Math.atan2(vec.getY(), vec.getX());
	        	
	           	if (theta > 0.1 * Math.PI || theta < -0.9 * Math.PI)
	        		highlights.set(side);	        	
	           	
	           	currX = coords[2];
	        	currY = coords[3];
	        	break;
	        }
	        } 
	        side++;
		}
		
		return highlights;
	}
	
	/**
	 * @return Approximately central point within path (if convex). 
	 */
	private static Point2D pathCentre(final GeneralPath path)
	{
		final Rectangle2D bounds = path.getBounds2D();
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}
	
//	private static String pathDetails(final GeneralPath path) 
//	{
//		final StringBuffer sb = new StringBuffer();
//		final double[] coords = new double[6];
//		for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) 
//		{
//	        switch (pi.currentSegment(coords))
//	        {
//	        case PathIterator.SEG_CLOSE: 
//	        	sb.append("Z\n");
//	        	break;
//	        case PathIterator.SEG_CUBICTO:
//	        	sb.append("C " + coords[0] + " " + coords[1] + " " + coords[2] + " " + coords[3] + " " + coords[4] + " " + coords[5] + "\n");
//	        	break;
//	        case PathIterator.SEG_LINETO:
//	        	sb.append("L " + coords[0] + " " + coords[1] + "\n");
//	        	break;
//	        case PathIterator.SEG_MOVETO:
//	        	sb.append("M " + coords[0] + " " + coords[1] + "\n");
//	        	break;
//	        case PathIterator.SEG_QUADTO:
//	        	sb.append("Q " + coords[0] + " " + coords[1] + " " + coords[2] + " " + coords[3] + "\n");
//	        	break;
//	        } 
//		}
//		return sb.toString();
//	}
	
	//-------------------------------------------------------------------------

	public static void setCellColourByPhase
	(
		final Graphics2D g2d, final int index, final Topology topology, 
		final Color colorFillPhase0, final Color colorFillPhase1, final Color colorFillPhase2, 
		final Color colorFillPhase3, final Color colorFillPhase4, final Color colorFillPhase5
	) 
	{
		switch (topology.elementPhase(SiteType.Cell, index))
		{
		case 0: g2d.setColor(colorFillPhase0); break;
		case 1: g2d.setColor(colorFillPhase1); break;
		case 2: g2d.setColor(colorFillPhase2); break;
		case 3: g2d.setColor(colorFillPhase3); break;
		case 4: g2d.setColor(colorFillPhase4); break;
		case 5: g2d.setColor(colorFillPhase5); break;
		default: System.out.println("** Error: Bad phase for cell " + index + ".");
		}
	}

	public static Color[][] shadedPhaseColours
	(
		final Color colorFillPhase0, final Color colorFillPhase1, final Color colorFillPhase2, 
		final Color colorFillPhase3, final Color colorFillPhase4, final Color colorFillPhase5
	) 
	{
		// [c][0] = low, [c][1] = fill, [c][2] = high
		final Color[][] colours = new Color[Constants.MAX_CELL_COLOURS][3];
		
		colours[0][1] = colorFillPhase0;
		colours[1][1] = colorFillPhase1;
		colours[2][1] = colorFillPhase2;
		colours[3][1] = colorFillPhase3;
		colours[4][1] = colorFillPhase4;
		colours[5][1] = colorFillPhase5;
		
		// Check that all phases have a base colour
		for (int c = 0; c < Constants.MAX_CELL_COLOURS; c++)
		{
			if (colours[c][1] != null)
				continue;  // everything's fine
			
			if (c == 0)
			{
				// Set default light colour for phase 0
				colours[0][1] = new Color(250, 221, 144, colours[0][1].getAlpha());
				continue;
			}
			
			// Successively darken from previous phase
			final int r = colours[c - 1][1].getRed();
			final int g = colours[c - 1][1].getGreen();
			final int b = colours[c - 1][1].getBlue();
			final int a = colours[c - 1][1].getAlpha();
			
			// Darken slightly
			final double darken = 0.8;
			colours[c][1] = new Color((int)(darken * r), (int)(darken * g), (int)(darken * b), a);
		}
			
		for (int c = 0; c < Constants.MAX_CELL_COLOURS; c++)
		{
			final int r = colours[c][1].getRed();
			final int g = colours[c][1].getGreen();
			final int b = colours[c][1].getBlue();
			final int a = colours[c][1].getAlpha();
			
			// Calculate lowlight colour from base colour
			final double darken = 0.75;
			colours[c][0] = new Color((int)(darken * r), (int)(darken * g), (int)(darken * b), a);
			
			// Calculate highlight colour from base colour
			colours[c][2] = new Color
								(
//									Math.min(255, 127 + r),
//									Math.min(255, 127 + g),
//									Math.min(255, 127 + b)
									Math.min(255, 32 + (int)(Math.sqrt(r / 255.0) * 255.0)),
									Math.min(255, 32 + (int)(Math.sqrt(g / 255.0) * 255.0)),
									Math.min(255, 32 + (int)(Math.sqrt(b / 255.0) * 255.0)),
									a
								);
		}
		
		return colours;
	}
	
}
