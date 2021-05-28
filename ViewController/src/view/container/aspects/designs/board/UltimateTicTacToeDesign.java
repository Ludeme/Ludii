package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class UltimateTicTacToeDesign extends BoardDesign
{
	public UltimateTicTacToeDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{		
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swThin  = (float)Math.max(1, 0.005 * boardStyle.placement().width + 0.5);
		final float swThick = 2 * swThin;

		setStrokesAndColours
		(
			bridge,
			context,
			new Color(50, 150, 255),
			null,
			new Color(180, 230, 255),
			new Color(0, 175, 0),
			new Color(230, 50, 20),
			new Color(0, 100, 200),
			null,
			null,
			null,
			swThin,
			swThick
		);
		drawBoard(g2d);
		
		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the board design.
	 */
	protected void drawBoard(final Graphics2D g2d)
	{	
		final int dots = (int) (0.9 * boardStyle.container().topology().cells().size());
		final int dim  = (int)(Math.sqrt(dots));
		
		final Point ptMid = screenPosn(topology().cells().get(dots / 2).centroid());
		
		final Point pt0 = screenPosn(topology().cells().get(0).centroid());
		final Point pt1 = screenPosn(topology().cells().get(1).centroid());
		final int unit = Math.abs(pt1.x - pt0.x);
		
		// Draw faint thin lines of subgames
		g2d.setColor(new Color(200, 220, 255));
		g2d.setStroke(strokeThin);
		
		final int x0 = ptMid.x - 5 * unit + unit / 2; 
		final int y0 = ptMid.y - 5 * unit + unit / 2; 

		int ax, ay, bx, by;
		final double off = 0.15; 
		
		for (int n = 1; n < dim; n++)
		{
			ax = x0 + unit * n;
			ay = y0 + (unit * 0);
			bx = ax;
			by = y0 + (int)(unit * (3 - off));
			g2d.drawLine(ax, ay, bx, by);
			
			ax = x0 + unit * n;
			ay = y0 + (int)(unit * (3 + off));
			bx = ax;
			by = y0 + (int)(unit * (6 - off));
			
			g2d.drawLine(ax, ay, bx, by);
			ax = x0 + unit * n;
			ay = y0 + (int)(unit * (6 + off));
			bx = ax;
			by = y0 + (unit * dim);
			g2d.drawLine(ax, ay, bx, by);

			ax = x0 + (unit * 0);
			ay = y0 + unit * n;
			bx = x0 + (int)(unit * (3 - off));
			by = ay;
			g2d.drawLine(ax, ay, bx, by);

			ax = x0 + (int)(unit * (3 + off));
			ay = y0 + unit * n;
			bx = x0 + (int)(unit * (6 - off));
			by = ay;
			g2d.drawLine(ax, ay, bx, by);

			ax = x0 + (int)(unit * (6 + off));
			ay = y0 + unit * n;
			bx = x0 + (unit * dim);
			by = ay;
			g2d.drawLine(ax, ay, bx, by);
		}

//		// Block out thick white supergame lines to separate subgames a bit  
//		g2d.setColor(Color.white);
//		g2d.setStroke(new BasicStroke(strokeThick.getLineWidth() * 3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
//		//g2d.setStroke(strokeThick);
//
//		for (int n = 3; n < dim; n+=3)
//		{
//			ax = x0 + n * unit;
//			ay = y0 + 0 * unit;
//			bx = x0 + n * unit;
//			by = y0 + dim * unit;
//			g2d.drawLine(ax, ay, bx, by);
//
//			ax = x0 + 0 * unit;
//			ay = y0 + n * unit;
//			bx = x0 + dim * unit;
//			by = y0 + n * unit;
//			g2d.drawLine(ax, ay, bx, by);
//		}

		// Draw thick lines for supergame
		g2d.setColor(new Color(20, 100, 200));
		g2d.setStroke(strokeThick());

		for (int n = 3; n < dim; n+=3)
		{
			ax = x0 + n * unit;
			ay = y0 + 0 * unit;
			bx = x0 + n * unit;
			by = y0 + dim * unit;
			g2d.drawLine(ax, ay, bx, by);

			ax = x0 + 0 * unit;
			ay = y0 + n * unit;
			bx = x0 + dim * unit;
			by = y0 + n * unit;
			g2d.drawLine(ax, ay, bx, by);
		}
	}
	
	//-------------------------------------------------------------------------
	
}
