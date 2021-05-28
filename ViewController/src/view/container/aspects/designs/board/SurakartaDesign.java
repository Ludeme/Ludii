package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.container.board.Track;
import game.equipment.container.board.Track.Elem;
import game.types.board.SiteType;
import main.math.MathRoutines;
import other.context.Context;
import other.topology.Edge;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

/**
 * Custom board rendering for Surakarta-type boards.
 * 
 * @author cambolbro and matthew.stephenson
 */

public class SurakartaDesign extends BoardDesign 
{
	public SurakartaDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}

	//-------------------------------------------------------------------------
	
	/** The colour of the board loops. */
	private final Color[] loopColours = 
		{
			new Color(  0, 175,   0),
			new Color(230,  50,  20),
			new Color(  0, 100, 200),
			new Color(150, 150,   0),
			new Color(150,   0, 150),
			new Color(  0, 150, 150)
		};
	
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
		switch (topology().graph().basis())
		{
		case Square:  	 drawBoardSquare(g2d); 	   break;
		case Triangular: drawBoardTriangular(g2d); break;
		//$CASES-OMITTED$
		default: System.out.println("** Board type " + topology().graph().basis() + " not supported for Surkarta.");
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws the board design if square basis.
	 */
	protected void drawBoardSquare(final Graphics2D g2d)
	{	
		final int rows = boardStyle.container().topology().rows(SiteType.Vertex).size();
		final int cols = boardStyle.container().topology().columns(SiteType.Vertex).size();

		// Get four corner points
		final Point ptSW = screenPosn(topology().vertices().get(0).centroid());
		final Point ptNW = screenPosn(topology().vertices().get(rows * cols - cols).centroid());
		final Point ptNE = screenPosn(topology().vertices().get(rows * cols - 1).centroid());
		final Point ptSE = screenPosn(topology().vertices().get(cols - 1).centroid());
		
		// Fill the board area
		g2d.setColor(colorFillPhase0);
		
		final GeneralPath border = new GeneralPath();
		border.moveTo(ptSW.x, ptSW.y);
		border.lineTo(ptNW.x, ptNW.y);
		border.lineTo(ptNE.x, ptNE.y);
		border.lineTo(ptSE.x, ptSE.y);
		border.closePath();
		g2d.fill(border);
		
//		for (final Cell cell : topology().cells())
//		{
//			final GeneralPath path = new GeneralPath();
//			for (int n = 0; n < cell.vertices().size(); n++)
//			{
//				final Point2D pt = cell.vertices().get(n).centroid();
//				if (n == 0)
//					path.moveTo(pt.getX(),pt.getY());
//				else
//					path.lineTo(pt.getX(),pt.getY());
//			}
//			g2d.fill(path);
//		}

		// Draw the grid lines
		g2d.setStroke(strokeThin);
		g2d.setColor(colorEdgesInner);
		
//		for (int row = 0; row < rows; row++)
//		{
//			final Point ptA = screenPosn(topology().vertices().get(row * cols).centroid());
//			final Point ptB = screenPosn(topology().vertices().get(row * cols + cols - 1).centroid());
//			g2d.drawLine(ptA.x, ptA.y, ptB.x, ptB.y);	
//		}
//
//		for (int col = 0; col < cols; col++)
//		{
//			final Point ptA = screenPosn(topology().vertices().get(col).centroid());
//			final Point ptB = screenPosn(topology().vertices().get(rows * cols - cols + col).centroid());
//			g2d.drawLine(ptA.x, ptA.y, ptB.x, ptB.y);
//		}
		
		for (final Edge edge : topology().edges())
		{
			final Point ptA = screenPosn(edge.vA().centroid());
			final Point ptB = screenPosn(edge.vB().centroid());
			g2d.drawLine(ptA.x, ptA.y, ptB.x, ptB.y);		
		}
		g2d.draw(border);

		// Draw the tracks
		g2d.setStroke(strokeThick());
		for (int t = 0; t < boardStyle.container().tracks().size(); t += 2)
		{
			g2d.setColor(loopColours[(t / 2) % boardStyle.container().tracks().size() % loopColours.length]);

			final Track track = boardStyle.container().tracks().get(t);
			
			for (int e = 0; e < track.elems().length; e++)
			{
				final Elem elemM = track.elems()[e];
				final Elem elemN = track.elems()[(e + 1) % track.elems().length];
			
				final Point ptM = screenPosn(topology().vertices().get(elemM.site).centroid());
				final Point ptN = screenPosn(topology().vertices().get(elemN.site).centroid());
				
				if (elemM.bump > 0)
				{
					// Previous pair is speed bump: draw loop
					final int rowM = elemM.site / cols;
					final int colM = elemM.site % cols;
					final int rowN = elemN.site / cols;
					final int colN = elemN.site % cols;
									
					if ((rowM == 0 || rowN == 0) && (colM == 0 || colN == 0))
					{
						// SW corner
						final int r = (int)(MathRoutines.distance(ptM, ptSW) + 0.5);
						g2d.drawArc(ptSW.x-r, ptSW.y-r, 2*r, 2*r, 90, 270);
					}
					else if ((rowM == rows - 1 || rowN == rows - 1) && (colM == 0 || colN == 0))
					{
						// NW corner
						final int r = (int)(MathRoutines.distance(ptM, ptNW) + 0.5);
						g2d.drawArc(ptNW.x-r, ptNW.y-r, 2*r, 2*r, 0, 270);
					}
					else if ((rowM == rows - 1 || rowN == rows - 1) && (colM == cols - 1 || colN == cols - 1))
					{
						// NE corner
						final int r = (int)(MathRoutines.distance(ptM, ptNE) + 0.5);
						g2d.drawArc(ptNE.x-r, ptNE.y-r, 2*r, 2*r, 270, 270);
					}
					else if ((rowM == 0 || rowN == 0) && (colM == cols - 1 || colN == cols - 1))
					{
						// SE corner
						final int r = (int)(MathRoutines.distance(ptM, ptSE) + 0.5);
						g2d.drawArc(ptSE.x-r, ptSE.y-r, 2*r, 2*r, 180, 270);
					}
				}
				else
				{
					// Draw line
					g2d.drawLine(ptM.x, ptM.y, ptN.x, ptN.y);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws the board design if triangular basis.
	 */
	protected void drawBoardTriangular(final Graphics2D g2d)
	{	
		final int rows = boardStyle.container().topology().rows(SiteType.Vertex).size();
		//final int cols = boardStyle.container().topology().columns(SiteType.Vertex).size();

		//System.out.println("rows=" + rows + ", cols=" + cols);
		
		// Get four corner points
		final Point ptSW  = screenPosn(topology().vertices().get(0).centroid());
		final Point ptTop = screenPosn(topology().vertices().get(topology().vertices().size() - 1).centroid());
		final Point ptSE  = screenPosn(topology().vertices().get(rows - 1).centroid());
		
		// Fill the board area
		g2d.setColor(colorFillPhase0);
		final GeneralPath border = new GeneralPath();
		border.moveTo( ptSW.x,  ptSW.y);
		border.lineTo(ptTop.x, ptTop.y);
		border.lineTo( ptSE.x,  ptSE.y);
		border.closePath();
		g2d.fill(border);
		
//		for (final Cell cell : topology().cells())
//		{
//			final GeneralPath path = new GeneralPath();
//			for (int n = 0; n < cell.vertices().size(); n++)
//			{
//				final Point2D pt = cell.vertices().get(n).centroid();
//				if (n == 0)
//					path.moveTo(pt.getX(),pt.getY());
//				else
//					path.lineTo(pt.getX(),pt.getY());
//			}
//			g2d.fill(path);
//		}
		
		// Draw the grid lines
		g2d.setStroke(strokeThin);
		g2d.setColor(colorEdgesInner);
		
		for (final Edge edge : topology().edges())
		{
			final Point ptA = screenPosn(edge.vA().centroid());
			final Point ptB = screenPosn(edge.vB().centroid());
			g2d.drawLine(ptA.x, ptA.y, ptB.x, ptB.y);		
		}
		g2d.draw(border);

		// Draw the tracks
		g2d.setStroke(strokeThick());
		for (int t = 0; t < boardStyle.container().tracks().size(); t += 2)
		{
			g2d.setColor(loopColours[(t / 2) % boardStyle.container().tracks().size() % loopColours.length]);

			final Track track = boardStyle.container().tracks().get(t);
			
			for (int e = 0; e < track.elems().length; e++)
			{
				final Elem elemM = track.elems()[e];
				final Elem elemN = track.elems()[(e + 1) % track.elems().length];
			
				final Point ptM = screenPosn(topology().vertices().get(elemM.site).centroid());
				final Point ptN = screenPosn(topology().vertices().get(elemN.site).centroid());
				
				if (elemM.bump > 0)
				{
					final int diff = elemN.site - elemM.site;
					
					if (diff > 0 && diff <= rows / 2)
					{
						// Top corner loop
						final Point ptRef = ptTop;
						final int r = (int)(MathRoutines.distance(ptM, ptRef) * Math.sqrt(3) / 2 + 0.5);
						
						final int ax = ptRef.x + (int)(r * Math.cos(Math.toRadians(210)) - 0.5);
						final int ay = ptRef.y - (int)(r * Math.sin(Math.toRadians(210)) + 0.5);
						
						g2d.drawLine(ptM.x, ptM.y, ax, ay);	
						
						g2d.drawArc(ptRef.x-r, ptRef.y-r, 2 * r, 2 * r, 330, 240);	
						
						final int r2 = (int)(MathRoutines.distance(ptM, ptRef) + 0.5);
						
						final int bx = ptRef.x + (int)(r * Math.cos(Math.toRadians(330)) + 0.5);
						final int by = ptRef.y - (int)(r * Math.sin(Math.toRadians(330)) + 0.5);
						final int cx = ptRef.x + (int)(r2 * Math.cos(Math.toRadians(300)) + 0.5);
						final int cy = ptRef.y - (int)(r2 * Math.sin(Math.toRadians(300)) + 0.5);
						
						g2d.drawLine(bx, by, cx, cy);	
					}
					else if (diff >= rows / 2)
					{
						// Bottom left corner loop
						final Point ptRef = ptSW;
						final int r = (int)(MathRoutines.distance(ptM, ptRef) * Math.sqrt(3) / 2 + 0.5);
						
						final int ax = ptRef.x + (int)(r * Math.cos(Math.toRadians(330)) + 0.5);
						final int ay = ptRef.y - (int)(r * Math.sin(Math.toRadians(330)) - 0.5);
						
						g2d.drawLine(ptM.x, ptM.y, ax, ay);	
						
						g2d.drawArc(ptRef.x-r, ptRef.y-r, 2 * r, 2 * r, 90, 240);	
						
						final int r2 = (int)(MathRoutines.distance(ptM, ptRef) + 0.5);
						
						final int bx = ptRef.x + (int)(r * Math.cos(Math.toRadians(90)) + 0.5);
						final int by = ptRef.y - (int)(r * Math.sin(Math.toRadians(90)) + 0.5);
						final int cx = ptRef.x + (int)(r2 * Math.cos(Math.toRadians(60)) + 0.5);
						final int cy = ptRef.y - (int)(r2 * Math.sin(Math.toRadians(60)) + 0.5);
						
						g2d.drawLine(bx, by, cx, cy);	
					}
					else if (diff < -rows / 2)
					{
						// Bottom right corner loop
						final Point ptRef = ptSE;
						final int r = (int)(MathRoutines.distance(ptM, ptRef) * Math.sqrt(3) / 2 + 0.5);
						
						final int ax = ptRef.x + (int)(r * Math.cos(Math.toRadians(90)) + 0.5);
						final int ay = ptRef.y - (int)(r * Math.sin(Math.toRadians(90)) + 0.5);
						
						g2d.drawLine(ptM.x, ptM.y, ax, ay);	
						
						g2d.drawArc(ptRef.x-r, ptRef.y-r, 2 * r, 2 * r, 210, 240);	
						
						final int r2 = (int)(MathRoutines.distance(ptM, ptRef) + 0.5);
						
						final int bx = ptRef.x + (int)(r * Math.cos(Math.toRadians(210)) + 0.5);
						final int by = ptRef.y - (int)(r * Math.sin(Math.toRadians(210)) + 0.5);
						final int cx = ptRef.x + (int)(r2 * Math.cos(Math.toRadians(180)) + 0.5);
						final int cy = ptRef.y - (int)(r2 * Math.sin(Math.toRadians(180)) + 0.5);
						
						g2d.drawLine(bx, by, cx, cy);	
					}
				}
				else
				{
					// Draw line
					g2d.drawLine(ptM.x, ptM.y, ptN.x, ptN.y);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
