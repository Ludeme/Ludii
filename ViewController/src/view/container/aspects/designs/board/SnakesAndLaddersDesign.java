package view.container.aspects.designs.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.Game;
import game.equipment.other.Map;
import main.math.MathRoutines;
import other.context.Context;
import other.topology.Cell;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class SnakesAndLaddersDesign extends BoardDesign
{
	public SnakesAndLaddersDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context) 
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 5 / 1000.0f;
		final float swThin = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = Math.max(2, (int) (0.002 * boardStyle.placement().width + 0.5));

		final Color shade0 = new Color(210, 240, 255);
		final Color shade1 = new Color(190, 220, 255); // Global.shade(shade0, 0.9);
		final Color shadeEdge = MathRoutines.shade(shade0, 0.25);

		setStrokesAndColours
		(
			bridge, 
			context, 
			null, 
			null, 
			shade0, // new Color(250, 221, 144),
			shade1, // new Color(200, 150, 75),
			null, 
			null, 
			null,
			null,
			shadeEdge, // new Color(153, 51, 0),
			swThin, 
			swThick
		);

		fillCells(bridge, g2d, context);
		drawSnakesAndLadders(g2d, context.game());
		drawOuterCellEdges(bridge, g2d, context);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the snakes and ladders graphics on the board.
	 */
	private void drawSnakesAndLadders(final Graphics2D g2d, final Game game) 
	{
		// Draw snakes
		for (final Map map : game.equipment().maps())
			for (int n = 0; n < map.map().size(); n++) {
				final int from = map.map().keys()[n];
				final int to = map.map().values()[n];

				if (from > to)
					drawSnake(g2d, from, to);
			}

		// Draw ladders
		for (final Map map : game.equipment().maps())
			for (int n = 0; n < map.map().size(); n++) {
				final int from = map.map().keys()[n];
				final int to = map.map().values()[n];

				if (from < to)
					drawLadder(g2d, from, to);
			}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws the snakes on the board.
	 */
	private void drawLadder(final Graphics2D g2d, final int from, final int to) 
	{
		final List<Cell> cells = topology().cells();
		
		// Amount to clip ladder ends
		final double clip = 0.5 * boardStyle.cellRadius() * boardStyle.placement().width;

		final Cell cellA = cells.get(from);
		final Cell cellB = cells.get(to);

		final Point pixelA = screenPosn(cellA.centroid());
		final Point pixelB = screenPosn(cellB.centroid());

		final double angle = Math.atan2(pixelB.y - pixelA.y, pixelB.x - pixelA.x);

		final Point2D.Double ptA = new Point2D.Double(pixelA.x + clip * Math.cos(angle), pixelA.y + clip * Math.sin(angle));
		final Point2D.Double ptB = new Point2D.Double(pixelB.x + clip * Math.cos(angle + Math.PI), pixelB.y + clip * Math.sin(angle + Math.PI));

		// Spacing between rails
		final double width = 0.3 * boardStyle.cellRadius() * boardStyle.placement().width;

		final double l0x = ptA.x + width * Math.cos(angle + Math.PI / 2);
		final double l0y = ptA.y + width * Math.sin(angle + Math.PI / 2);
		final double l1x = ptB.x + width * Math.cos(angle + Math.PI / 2);
		final double l1y = ptB.y + width * Math.sin(angle + Math.PI / 2);
		final double r0x = ptA.x + width * Math.cos(angle - Math.PI / 2);
		final double r0y = ptA.y + width * Math.sin(angle - Math.PI / 2);
		final double r1x = ptB.x + width * Math.cos(angle - Math.PI / 2);
		final double r1y = ptB.y + width * Math.sin(angle - Math.PI / 2);

		// Draw rungs
		final double length = MathRoutines.distance(ptA, ptB);
		final int numRungs = (int) (0.75 * length / width);

		final BasicStroke stroke = new BasicStroke((float) (0.125 * boardStyle.cellRadius() * boardStyle.placement().width),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		g2d.setStroke(stroke);

		g2d.setColor(new Color(255, 127, 0));

		for (int r = 1; r < numRungs - 1; r++) {
			final double t = r / (double) (numRungs - 1);

			final double rungLx = l0x + t * (l1x - l0x);
			final double rungLy = l0y + t * (l1y - l0y);
			final double rungRx = r0x + t * (r1x - r0x);
			final double rungRy = r0y + t * (r1y - r0y);

			final java.awt.Shape rung = new Line2D.Double(rungLx, rungLy, rungRx, rungRy);
			g2d.draw(rung);
		}

		// Draw rails
		final java.awt.Shape left = new Line2D.Double(l0x, l0y, l1x, l1y);
		final java.awt.Shape right = new Line2D.Double(r0x, r0y, r1x, r1y);

		g2d.draw(left);
		g2d.draw(right);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws the snakes on the board.
	 */
	private void drawSnake(final Graphics2D g2d, final int from, final int to) 
	{
		final List<Cell> cells = topology().cells();
		
		// Amount to clip
		final double u = boardStyle.cellRadius() * boardStyle.placement().width;
		final double clipTail = 0.5 * u;
		final double clipHead = 0.75 * u;

		final Cell cellA = cells.get(from);
		final Cell cellB = cells.get(to);

		final Point pixelA = screenPosn(cellA.centroid());
		final Point pixelB = screenPosn(cellB.centroid());

		final double angle = Math.atan2(pixelB.y - pixelA.y, pixelB.x - pixelA.x);

		final Point2D.Double ptA = new Point2D.Double(pixelA.x + clipTail * Math.cos(angle),
				pixelA.y + clipTail * Math.sin(angle));
		final Point2D.Double ptB = new Point2D.Double(pixelB.x + clipHead * Math.cos(angle + Math.PI),
				pixelB.y + clipHead * Math.sin(angle + Math.PI));

		// Undulations
		final double offI = 0.2 * u;
		final double offO = 0.6 * u;

		final double length = MathRoutines.distance(ptA, ptB);
		final int numBends = 4 + (int) (0.5 * length / u);

		final Point2D.Double[][] cps = new Point2D.Double[numBends + 1][2];
		cps[0][0] = ptA;
		cps[0][1] = ptA;

		cps[numBends - 1][0] = ptB;
		cps[numBends - 1][1] = ptB;

		for (int b = 1; b < numBends - 1; b++) {
			final double t = b / (double) (numBends - 1);

			final double tx = ptA.x + t * (ptB.x - ptA.x);
			final double ty = ptA.y + t * (ptB.y - ptA.y);

			if (b % 2 == 0) {
				cps[b][0] = new Point2D.Double(tx + offI * Math.cos(angle + Math.PI / 2),
						ty + offI * Math.sin(angle + Math.PI / 2));
				cps[b][1] = new Point2D.Double(tx + offO * Math.cos(angle + Math.PI / 2),
						ty + offO * Math.sin(angle + Math.PI / 2));
			} else {
				cps[b][1] = new Point2D.Double(tx + offI * Math.cos(angle - Math.PI / 2),
						ty + offI * Math.sin(angle - Math.PI / 2));
				cps[b][0] = new Point2D.Double(tx + offO * Math.cos(angle - Math.PI / 2),
						ty + offO * Math.sin(angle - Math.PI / 2));
			}
		}

		// Draw snake
		final GeneralPath path = new GeneralPath();
		path.moveTo(ptA.x, ptA.y);

		final double off = 0.6;

		for (int b = 0; b < numBends - 2; b++) {
			final double b0x = cps[b][0].x;
			final double b0y = cps[b][0].y;
			final double b1x = cps[b + 1][0].x;
			final double b1y = cps[b + 1][0].y;
			final double b2x = cps[b + 2][0].x;
			final double b2y = cps[b + 2][0].y;

			final double ax = (b0x + b1x) / 2.0;
			final double ay = (b0y + b1y) / 2.0;
			final double dx = (b1x + b2x) / 2.0;
			final double dy = (b1y + b2y) / 2.0;

			final double bx = ax + off * (b1x - ax);
			final double by = ay + off * (b1y - ay);
			final double cx = dx + off * (b1x - dx);
			final double cy = dy + off * (b1y - dy);

			path.curveTo(bx, by, cx, cy, dx, dy);
		}
		path.lineTo(ptB.x, ptB.y);

		for (int b = numBends - 3; b >= 0; b--) {
			final double b0x = cps[b + 2][1].x;
			final double b0y = cps[b + 2][1].y;
			final double b1x = cps[b + 1][1].x;
			final double b1y = cps[b + 1][1].y;
			final double b2x = cps[b + 0][1].x;
			final double b2y = cps[b + 0][1].y;

			final double ax = (b0x + b1x) / 2.0;
			final double ay = (b0y + b1y) / 2.0;
			final double dx = (b1x + b2x) / 2.0;
			final double dy = (b1y + b2y) / 2.0;

			final double bx = ax + off * (b1x - ax);
			final double by = ay + off * (b1y - ay);
			final double cx = dx + off * (b1x - dx);
			final double cy = dy + off * (b1y - dy);

			path.curveTo(bx, by, cx, cy, dx, dy);
		}
		path.closePath();

		g2d.setColor(new Color(0, 127, 0));
		g2d.fill(path);

		final BasicStroke stroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		g2d.setStroke(stroke);

		g2d.setColor(new Color(0, 0, 0));
		g2d.draw(path);
	}

	//-------------------------------------------------------------------------

	@Override
	protected void fillCells(final Bridge bridge, final Graphics2D g2d, final Context context) 
	{
		final List<Cell> cells = topology().cells();
		
		final int fontSize = (int) (0.85 * boardStyle.cellRadius() * boardStyle.placement().width + 0.5);
		final Font font = new Font("Arial", Font.PLAIN, fontSize);
		g2d.setFont(font);

		g2d.setStroke(strokeThin);
		for (final Cell cell : cells) {
			final GeneralPath path = new GeneralPath();
			for (int v = 0; v < cell.vertices().size(); v++) {
				if (path.getCurrentPoint() == null) {
					final Vertex prev = cell.vertices().get(cell.vertices().size() - 1);
					final Point prevPosn = screenPosn(prev.centroid());
					path.moveTo(prevPosn.x, prevPosn.y);
				}
				final Vertex corner = cell.vertices().get(v);
				final Point cornerPosn = screenPosn(corner.centroid());
				path.lineTo(cornerPosn.x, cornerPosn.y);
			}

			if ((cell.col() + cell.row()) % 2 == 0)
				g2d.setColor(colorFillPhase1);
			else
				g2d.setColor(colorFillPhase0);

			g2d.fill(path);
		}

		// Draw cell coordinates
		g2d.setColor(Color.white);
		for (final Cell cell : cells) 
		{
			final String cellNumber = (cell.row() % 2 == 0) 
										? "" + (cell.index() + 1)
										: "" + (cell.row() * 10 + 10 - cell.col());

			final Rectangle bounds = g2d.getFontMetrics().getStringBounds(cellNumber, g2d).getBounds();

			//final Point2D.Double pt = cell.centroid();
			final Point pt = screenPosn(cell.centroid());
			g2d.drawString
			(
				cellNumber, 
				pt.x - (int)(0.5 * bounds.getWidth()), 
				pt.y + (int)(0.3 * bounds.getHeight())
			);
		}
	}
	
	//-------------------------------------------------------------------------
	
}
