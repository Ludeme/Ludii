package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.Board.BackgammonPlacement;
import view.container.styles.board.BackgammonStyle;

public class BackgammonDesign extends BoardDesign
{
	private final BackgammonStyle backgammonStyle;
	private final BackgammonPlacement backgammonPlacement;
	
	//-------------------------------------------------------------------------
	
	public BackgammonDesign(final BackgammonStyle boardStyle, final BackgammonPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
		backgammonStyle = boardStyle;
		backgammonPlacement = boardPlacement;
	}
	
	//-------------------------------------------------------------------------

	private final Color[] boardColours =
	{
		new Color(225, 182, 130),  // light strip
		new Color(116,  58,  41),  // dark strip
		new Color(140,  75,  45),  // frame
		new Color(185, 130,  85),  // base
	};
	
	//-------------------------------------------------------------------------
	
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		boardPlacement.customiseGraphElementLocations(context);

		// Board image
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();
		setStrokesAndColours
		(
			bridge, 
			context,
			new Color(120, 190, 240),
			new Color(125, 75, 0),
			new Color(210, 230, 255),
			null,
			null,
			null,
			null,
			null,
			new Color(0, 0, 0),
			Math.max(1, (int) (0.0025 * boardStyle.placement().width + 0.5)),
			(int) (2.0 * Math.max(1, (int) (0.0025 * boardStyle.placement().width + 0.5)))
		);
		drawBackgammonBoard(g2d);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the Backgammon board design.
	 */
	void drawBackgammonBoard(final Graphics2D g2d)
	{
		//  N = homeSize
		//
		//	A------------------------------------------+
		//	| C-----------------+  E-----------------+ |
		//	| |2N+1  .  .  .  3N|3N|3N+2 .  .  . 4N+1| |
		//	| |                 |+1|                 | |
		//	| |                 |  |                 | |
		//	| |                 |  |                 | |
		//	| |                 |  |                 | |
		//	| |                 |  |                 | |
		//	| |                 |  |                 | |
		//	| |                 |  |                 | |
		//	| |                 |  |                 | |
		//	| |0  .  .  .  . N-1|N |N+1  .  .  .  2N | |
		//	| +-----------------D  +-----------------F |
		//	+------------------------------------------B

		final Point pt0 = screenPosn(topology().vertices().get(0).centroid());
		final Point pt1 = screenPosn(topology().vertices().get(1).centroid());
		final int off = pt1.x - pt0.x;
		final int unit = off;
		
		final Point ptD = screenPosn(topology().vertices().get(backgammonPlacement.homeSize()-1).centroid());
		final Point ptF = screenPosn(topology().vertices().get(2 * backgammonPlacement.homeSize()).centroid());
		final Point ptC = screenPosn(topology().vertices().get(2 * backgammonPlacement.homeSize() + 1).centroid());
		final Point ptE = screenPosn(topology().vertices().get(3 * backgammonPlacement.homeSize() + 2).centroid());
		
		final int pr = (int)(off * 0.5);
		final int border = (int)(off * 0.5);

		final int cx = ptC.x - pr;
		final int cy = ptC.y - pr;

		final int ex = ptE.x - pr;
		final int ey = ptE.y - pr;

		final int dx = ptD.x + pr;
		final int dy = ptD.y + pr;

		final int fx = ptF.x + pr;
		final int fy = ptF.y + pr;

		final int ax = cx - border;
		final int ay = cy - border;

		final int bx = fx + border;
		final int by = fy + border;

		g2d.setColor(boardColours[2]);
		g2d.fillRect(ax, ay, Math.abs(bx-ax), Math.abs(by-ay));

		g2d.setColor(boardColours[3]);
		g2d.fillRect(cx, cy, Math.abs(dx-cx), Math.abs(dy-cy));
		g2d.fillRect(ex, ey, Math.abs(fx-ex), Math.abs(fy-ey));

		// Draw the triangular strips
		final GeneralPath pathD = new GeneralPath();
		final GeneralPath pathL = new GeneralPath();

		final int halfSize = topology().vertices().size() / 2;
		int counter = 0;  // counter for checking triangle parity
		
		for (int n = 0; n < halfSize; n++)
		{
			if (n == backgammonPlacement.homeSize() || n == 3 * backgammonPlacement.homeSize() + 1)
				continue;
		
			counter++;
			
			final int tx0 = cx + n % halfSize * unit;
			final int ty0 = cy;
			final int ty1 = ty0 + (int)(4.5 * unit + 0.5);

			final int bx0 = cx + n % halfSize * unit;
			final int by0 = dy;
			final int by1 = by0 - (int)(4.5 * unit + 0.5);

			if (counter % 2 == 0)
			{
				pathD.moveTo(tx0, ty0);
				pathD.lineTo(tx0 + unit, ty0);
				pathD.lineTo(tx0 + 0.5 * unit, ty1);
				pathD.closePath();
	
				pathL.moveTo(bx0, by0);
				pathL.lineTo(bx0 + unit, by0);
				pathL.lineTo(bx0 + 0.5 * unit, by1);
				pathL.closePath();
			}
			else
			{
				pathL.moveTo(tx0, ty0);
				pathL.lineTo(tx0 + unit, ty0);
				pathL.lineTo(tx0 + 0.5 * unit, ty1);
				pathL.closePath();
	
				pathD.moveTo(bx0, by0);
				pathD.lineTo(bx0 + unit, by0);
				pathD.lineTo(bx0 + 0.5 * unit, by1);
				pathD.closePath();				
			}
		}

		g2d.setColor(boardColours[0]);
		g2d.fill(pathL);

		g2d.setColor(boardColours[1]);
		g2d.fill(pathD);
	}

	public BackgammonStyle getBackgammonStyle()
	{
		return backgammonStyle;
	}
	
	//-------------------------------------------------------------------------
	
}
