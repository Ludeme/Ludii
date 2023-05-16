package view.container.aspects.designs.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.BitSet;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.other.Regions;
import game.functions.region.RegionFunction;
import game.util.graph.Properties;
import main.math.MathRoutines;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class ConnectiveGoalDesign extends BoardDesign
{
	public ConnectiveGoalDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * fill, draw internal grid lines, draw symbols, draw outer border on top.
	 * @return SVG as string.
	 */
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		// Set all values
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();
		final double boardLineThickness = boardStyle.cellRadiusPixels()/15.0;
		
		checkeredBoard = context.game().metadata().graphics().checkeredBoard();
		straightLines  = context.game().metadata().graphics().straightRingLines();
		
		final float swThin = (float) Math.max(1, boardLineThickness);
		final float swThick = swThin;
		
		setStrokesAndColours
		(
			bridge,
			context,
			new Color(120, 190, 240),
			new Color(120, 190, 240),
			new Color(210, 230, 255),
			new Color(210, 0, 0),
			new Color(0, 230, 0),
			new Color(0, 0, 255),
			null,
			null,
			new Color(0, 0, 0),
			swThin,
			swThick
		);
		
		// Background
		drawGround(g2d, context, true);
		
		// Cells
		fillCells(bridge, g2d, context);
		
		// Edges
		drawOuterCellEdges(bridge, g2d, context);
		
		// Symbols
		drawSymbols(g2d, context);
		
		// Foreground
		drawGround(g2d, context, false);

		return g2d.getSVGDocument();
	}	

	//-------------------------------------------------------------------------
	
	@Override
	protected void drawOuterCellEdges(final Bridge bridge, final Graphics2D g2d, final Context context)
	{
		final Regions[] regionsList = context.game().equipment().regions(); 
		final int numPlayers = context.game().players().count();
		
		final Point2D.Double centre = topology().centrePoint();
		final Point ptCentre = screenPosn(centre);
		
		// Create a path for each player
		final GeneralPath[] paths = new GeneralPath[context.game().players().count()+1];
		for (int pid = 0; pid < paths.length; pid++)
			paths[pid] = new GeneralPath();
		
		// Locate sites shared by more than one player
		final BitSet[] shared = new BitSet[numPlayers+2];
		for (int pid = 0; pid < shared.length; pid++)
			shared[pid] = new BitSet();
		
		for (final Regions regionsO : regionsList)
		{
			final int pid = regionsO.owner();
			final int[] sites = regionsO.eval(context);
		
			for (final int cid : sites)
				shared[pid].set(cid, true);
		}
		
		for (int pidA = 1; pidA < shared.length; pidA++)
			for (int pidB = 2; pidB < shared.length; pidB++)
			{
				if (pidA == pidB)
					continue;
				
				final BitSet bs = (BitSet)shared[pidA].clone();
				bs.and(shared[pidB]);
				shared[0].or(bs);
			}

		for (int pid = 1; pid < shared.length; pid++)
			shared[pid].and(shared[0]);
			
		// Generate the paths
		for (final Regions regionsO : regionsList)
		{
			final int pid = regionsO.owner();
			final int[] sites = regionsO.eval(context);
				
			for (final int site : sites)
			{
				final Cell cell = topology().cells().get(site);
				for (final Edge edge : cell.edges())
				{
					if (edge.properties().get(Properties.OUTER))
					{
						final Point2D va = edge.vA().centroid();
						final Point2D vb = edge.vB().centroid();

						final Point ptA = screenPosn(va);
						final Point ptB = screenPosn(vb);
					
						paths[pid].moveTo(ptA.x, ptA.y);
						paths[pid].lineTo(ptB.x, ptB.y);
					}
				}				
			}
		}
		
		// Draw the border and player colour in thick strokes
		final int thickness = (int)(4 * strokeThick.getLineWidth());
		final BasicStroke borderStroke = new BasicStroke(thickness+2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		final Stroke playerStroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		
		// Draw each region, clipped as necessary
		final Shape oldClip = g2d.getClip();
		
		for (final Regions regionsO : regionsList)
		{			
			if (regionsO.region() == null)
				continue;
			
			final int pid = regionsO.owner();
			final Color playerColour = bridge.settingsColour().playerColour(context, pid);
			final Color borderColour = MathRoutines.shade(playerColour, 0.75);

			for (final RegionFunction region : regionsO.region())
			{
				final int[] sites = region.eval(context).sites();
				
				final GeneralPath path = new GeneralPath();
				
				Point ptSharedA = null;
				Point ptSharedB = null;
				
				for (final int site : sites)
				{
					final Cell cell = topology().cells().get(site);
					if (shared[0].get(site))
					{
						// Shared cell: store its pixel location
						final Point pt = screenPosn(cell.centroid());
						if (ptSharedA == null)
							ptSharedA = pt;
						else
							ptSharedB = pt;
					}
					
					for (final Edge edge : cell.edges())
					{
						if (edge.properties().get(Properties.OUTER))
						{
							final Point2D va = edge.vA().centroid();
							final Point2D vb = edge.vB().centroid();
	
							final Point ptA = screenPosn(va);
							final Point ptB = screenPosn(vb);
						
							path.moveTo(ptA.x, ptA.y);
							path.lineTo(ptB.x, ptB.y);
						}
					}				
				}
				
				g2d.setClip(oldClip);
				if (ptSharedA != null && ptSharedB != null)
				{
					// Clip triangle from centre to lines projected through 
					// shared cells (should be extremities of regions).
					final int ax = ptCentre.x + 2 * (ptSharedA.x - ptCentre.x);
					final int ay = ptCentre.y + 2 * (ptSharedA.y - ptCentre.y);
					final Point ptA = new Point(ax, ay);
					
					final int bx = ptCentre.x + 2 * (ptSharedB.x - ptCentre.x);
					final int by = ptCentre.y + 2 * (ptSharedB.y - ptCentre.y);
					final Point ptB = new Point(bx, by);
					
					final GeneralPath pathClip = new GeneralPath();
					pathClip.moveTo(ptCentre.x, ptCentre.y);
					pathClip.lineTo(ptA.x, ptA.y);
					pathClip.lineTo(ptB.x, ptB.y);
					pathClip.closePath();
					g2d.setClip(pathClip);
				}
				else if (ptSharedA != null)
				{
					// Clip triangle from centre to lines projected through shared cell
					// Only one shared cell!
					final int ax = ptCentre.x + 2 * (ptSharedA.x - ptCentre.x);
					final int ay = ptCentre.y + 2 * (ptSharedA.y - ptCentre.y);
					final Point ptA = new Point(ax, ay);
					
					// Find furthest cell in region
					Point ptBestB = null;
					double maxDist = 0;
					
					for (final int site : sites)
					{
						final Cell cell = topology().cells().get(site);
						final Point pt = screenPosn(cell.centroid());
						
						final double dist = MathRoutines.distance(pt, ptA);
						if (dist  > maxDist)
						{
							ptBestB = new Point(pt.x, pt.y);
							maxDist = dist;
						}
					}
					
					if (ptBestB == null)
					{
						System.out.println("** Failed to find furthest point.");
						return;
					}
								
					// Extend beyond further point, so it isn't clipped 
					final int bestBx = ptA.x + (int)(1.25 * (ptBestB.x - ptA.x));
					final int bestBy = ptA.y + (int)(1.25 * (ptBestB.y - ptA.y));
					ptBestB = new Point(bestBx, bestBy);
					
					final int bx = ptCentre.x + 2 * (ptBestB.x - ptCentre.x);
					final int by = ptCentre.y + 2 * (ptBestB.y - ptCentre.y);
					final Point ptB = new Point(bx, by);

					final GeneralPath pathClip = new GeneralPath();
					pathClip.moveTo(ptCentre.x, ptCentre.y);
					pathClip.lineTo(ptA.x, ptA.y);
					pathClip.lineTo(ptB.x, ptB.y);
					pathClip.closePath();
					g2d.setClip(pathClip);
				}
												
				// Draw extra thick line in dark border colour
				g2d.setColor(borderColour);
				g2d.setStroke(borderStroke);
				g2d.draw(path);  //s[pid]);

				// Draw thick line in player colour
				g2d.setColor(playerColour);
				g2d.setStroke(playerStroke);
				g2d.draw(path);
			}
		}
		g2d.setClip(oldClip);
	}

	//-------------------------------------------------------------------------

}
