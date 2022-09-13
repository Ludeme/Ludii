package view.container.aspects.designs.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.util.BitSet;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class HoundsAndJackalsDesign extends BoardDesign
{
	final private BitSet specialDots = new BitSet();
	
	//-------------------------------------------------------------------------
	
	public HoundsAndJackalsDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);

		specialDots.set(0);
		specialDots.set(5);
		specialDots.set(7);
		specialDots.set(9);
		specialDots.set(14);
		specialDots.set(19);
		specialDots.set(24);
		specialDots.set(29);
		specialDots.set(34);
		specialDots.set(36); 
		specialDots.set(38);
		specialDots.set(43);
		specialDots.set(48);
		specialDots.set(53);
		specialDots.set(58);
	}

	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 5 / 1000.0f;
		final float swThin  = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = 2 * swThin;

		setStrokesAndColours
		(
			bridge,
			context,
			new Color(200, 200, 200),
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			new Color(140,140,140),
			swThin,
			swThick
		);
		
		drawHoundsAndJackalsBoard(g2d);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the hounds and Jackals board design.
	 */
	void drawHoundsAndJackalsBoard(final Graphics2D g2d)
	{
		// Draw the board
		GeneralPath path = new GeneralPath();
		
		final Point pt0 = screenPosn(topology().vertices().get(0).centroid());
		final Point pt1 = screenPosn(topology().vertices().get(1).centroid());
		final int unit = pt1.y - pt0.y;
		
		final Point ptA = screenPosn(topology().vertices().get(10).centroid());
		final Point ptB = screenPosn(topology().vertices().get(23).centroid());
		final Point ptE = screenPosn(topology().vertices().get(58).centroid());
		final Point ptH = screenPosn(topology().vertices().get(52).centroid());
		final Point ptI = screenPosn(topology().vertices().get(39).centroid());
		
		final int border = (int)(0.9 * unit);

		// CPs up left side
		int ax = ptA.x - border;
		int ay = ptA.y + border;
		
		int bx = ax;
		int by = ptB.y;
		
		int cx = ax;
		int cy = by - 3 * unit;
		
		// CPs along top
		final int ex = ptE.x;
		final int ey = ptE.y - border;
		
		int dx = ex - 1 * unit;
		int dy = ey;
		
		final int fx = ex + 1 * unit;
		final int fy = ey;
		
		// CPs down right side
		final int hx = ptH.x + border;
		final int hy = ptH.y;

		final int gx = hx;
		final int gy = hy - 3 * unit;

		final int ix = hx;
		final int iy = ptI.y + border;
		
		path.moveTo(ax, ay);
		path.lineTo(bx, by);
		path.curveTo(cx, cy, dx, dy, ex, ey);
		path.curveTo(fx, fy, gx, gy, hx, hy);
		path.lineTo(ix, iy);

		path.closePath();

		g2d.setColor(new Color(255, 240, 220));
		g2d.fill(path);

		final BasicStroke strokeB = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		g2d.setStroke(strokeB);

		g2d.setColor(new Color(127, 120, 110));
		g2d.draw(path);
		
		// Draw the dots
		final int rO = (int)(0.15 * unit);
		final int rI = rO / 2;

		final float sw = 0.03f * unit;
		
		final BasicStroke strokeD = new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		g2d.setStroke(strokeD);

		final Color dotColour = new Color(190, 150, 100);
		g2d.setColor(dotColour);
		
		for (int vid = 0; vid < topology().vertices().size(); vid++)
		{
			final Vertex vertex = topology().vertices().get(vid);
		
			final Point pt = screenPosn(vertex.centroid());
			
			final java.awt.Shape arcO = new Arc2D.Double(pt.x-rO, pt.y-rO, 2*rO+1, 2*rO+1, 0, 360, 0);
			g2d.draw(arcO);
			
			if (specialDots.get(vid))
			{
				// Also draw inner dot
				final java.awt.Shape arcI = new Arc2D.Double(pt.x-rI, pt.y-rI, 2*rI+1, 2*rI+1, 0, 360, 0);
				g2d.draw(arcI);
			}
		}

		// Draw short curves
		final BasicStroke strokeC = new BasicStroke(2*sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(strokeC);
		
		final Point pt5  = screenPosn(topology().vertices().get(5).centroid());
		final Point pt7  = screenPosn(topology().vertices().get(7).centroid());
		final Point pt9  = screenPosn(topology().vertices().get(9).centroid());
		final Point pt19 = screenPosn(topology().vertices().get(19).centroid());
		final Point pt34 = screenPosn(topology().vertices().get(34).centroid());
		final Point pt36 = screenPosn(topology().vertices().get(36).centroid());
		final Point pt38 = screenPosn(topology().vertices().get(38).centroid());
		final Point pt48 = screenPosn(topology().vertices().get(48).centroid());

		final int d1 = (int)(0.333 * unit);
		final int d2 = (int)(1.333 * unit);
		
		// Lower left
		ax = pt9.x - d1;
		ay = pt9.y;
		bx = pt9.x - d2;
		by = pt9.y;
		cx = pt7.x - d2;
		cy = pt7.y;
		dx = pt7.x - d1;
		dy = pt7.y;
		
		path = new GeneralPath();
		path.moveTo(ax, ay);
		path.curveTo(bx, by, cx, cy, dx, dy);
		g2d.draw(path);
		
		// Lower right
		ax = pt38.x + d1;
		ay = pt38.y;
		bx = pt38.x + d2;
		by = pt38.y;
		cx = pt36.x + d2;
		cy = pt36.y;
		dx = pt36.x + d1;
		dy = pt36.y;

		path = new GeneralPath();
		path.moveTo(ax, ay);
		path.curveTo(bx, by, cx, cy, dx, dy);
		g2d.draw(path);
		
		// Upper left
		ax = pt5.x - d1;
		ay = pt5.y;
		bx = pt5.x - d2;
		by = pt5.y;
		cx = pt19.x + d2;
		cy = pt19.y;
		dx = pt19.x + d1;
		dy = pt19.y;

		path = new GeneralPath();
		path.moveTo(ax, ay);
		path.curveTo(bx, by, cx, cy, dx, dy);
		g2d.draw(path);
		
		// Upper right
		ax = pt34.x + d1;
		ay = pt34.y;
		bx = pt34.x + d2;
		by = pt34.y;
		cx = pt48.x - d2;
		cy = pt48.y;
		dx = pt48.x - d1;
		dy = pt48.y;

		path = new GeneralPath();
		path.moveTo(ax, ay);
		path.curveTo(bx, by, cx, cy, dx, dy);
		g2d.draw(path);
	}
	
	//-------------------------------------------------------------------------
	
}
