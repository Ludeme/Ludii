package view.container.aspects.designs.board.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.RelationType;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.EdgeInfoGUI;
import metadata.graphics.util.EdgeType;
import metadata.graphics.util.LineStyle;
import other.context.Context;
import other.topology.Edge;
import util.StrokeUtil;
import view.container.aspects.designs.board.puzzle.PuzzleDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;
import view.container.styles.board.graph.GraphStyle;

public class GraphDesign extends PuzzleDesign
{
	protected boolean drawOrthogonalEdges;
	protected boolean drawDiagonalEdges;
	protected boolean drawOffEdges = false;
	protected boolean drawOrthogonalConnections = false;
	protected boolean drawDiagonalConnections = false;
	protected boolean drawOffConnections = false;
	
	//-------------------------------------------------------------------------
	
	public GraphDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement, final boolean drawOrthogonals, final boolean drawDiagonals) 
	{
		super(boardStyle, boardPlacement);
		drawOrthogonalEdges = drawOrthogonals;
		drawDiagonalEdges   = drawDiagonals;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		// Set vertex radius
		double vr = 0.3 * cellRadiusPixels();
		if (vr < 4)
			vr = 4;
		if (vr > 8)
			vr = 8;

		((GraphStyle)boardStyle).setBaseVertexRadius(vr * context.game().metadata().graphics().boardThickness(BoardGraphicsType.InnerVertices));
		((GraphStyle)boardStyle).setBaseLineWidth(0.666 * vr);

		final float swThin  = (float)((GraphStyle)boardStyle).baseLineWidth();  //Math.max(1, (int)(cellRadiusPixels() * 0.075));
		final float swThick = swThin;
		
		straightLines = context.game().metadata().graphics().straightRingLines();
		
		final Color decorationColour = new Color(200, 200, 200);
		
		setStrokesAndColours
		(
			bridge, 
			context,
			((GraphStyle)boardStyle).baseGraphColour(),
			((GraphStyle)boardStyle).baseGraphColour(),
			null,
			null,
			null,
			null,
			null,
			null,
			decorationColour,
			swThin,
			swThick
		);
		
		drawGround(g2d, context, true);
		
		//detectHints(context);
		
		// Sunken design
		if (!context.game().metadata().graphics().noSunken())
		{
			final double offY = -1.5;
			
			//final Color sunkenColour = Color.BLACK;
			final Color sunkenColour = new Color(100, 100, 100);
			
			// only draw sunken on parts of graph that are visible.
			if (colorEdgesInner == null || colorEdgesInner.getAlpha() != 0)
			{
				drawEdge(g2d, context, sunkenColour, strokeThin, EdgeType.Inner, RelationType.Orthogonal, false, drawOrthogonalEdges, offY);
			}
			if (colorEdgesOuter == null || colorEdgesOuter.getAlpha() != 0)
			{
				drawEdge(g2d, context, sunkenColour, strokeThick(), EdgeType.Outer, RelationType.Orthogonal, false, drawOrthogonalEdges, offY);
			}
			if (colorEdgesInner == null || colorEdgesInner.getAlpha() != 0)
			{
				drawEdge(g2d, context, sunkenColour, StrokeUtil.getDottedStroke(strokeThin.getLineWidth()), EdgeType.All, RelationType.Diagonal, false, drawDiagonalEdges, offY);
				drawEdge(g2d, context, sunkenColour, strokeThin, EdgeType.Inner, RelationType.Orthogonal, true, drawOrthogonalConnections, offY);
				drawEdge(g2d, context, sunkenColour, StrokeUtil.getDottedStroke(strokeThin.getLineWidth()), EdgeType.All, RelationType.Diagonal, true, drawDiagonalConnections, offY);
				drawEdge(g2d, context, sunkenColour, StrokeUtil.getDashedStroke(strokeThin.getLineWidth()), EdgeType.All, RelationType.OffDiagonal, true, drawOffConnections, offY);
			}
			drawVertices(bridge, g2d, context, sunkenColour, ((GraphStyle)boardStyle).baseVertexRadius(), offY);
		}
				
		drawEdge(g2d, context, colorEdgesInner, strokeThin, EdgeType.Inner, RelationType.Orthogonal, false, drawOrthogonalEdges, 0);
		drawEdge(g2d, context, colorEdgesOuter, strokeThick(), EdgeType.Outer, RelationType.Orthogonal, false, drawOrthogonalEdges, 0);
		drawEdge(g2d, context, colorEdgesInner, StrokeUtil.getDottedStroke(strokeThin.getLineWidth()), EdgeType.All, RelationType.Diagonal, false, drawDiagonalEdges, 0);
		drawEdge(g2d, context, colorEdgesInner, strokeThin, EdgeType.Inner, RelationType.Orthogonal, true, drawOrthogonalConnections, 0);
		drawEdge(g2d, context, colorEdgesInner, StrokeUtil.getDottedStroke(strokeThin.getLineWidth()), EdgeType.All, RelationType.Diagonal, true, drawDiagonalConnections, 0);
		drawEdge(g2d, context, colorEdgesInner, StrokeUtil.getDashedStroke(strokeThin.getLineWidth()), EdgeType.All, RelationType.OffDiagonal, true, drawOffConnections, 0);
		
		// Draw arrow heads.
		if (context.metadata().graphics().showEdgeDirections())
			for (final Edge edge : topology().edges())
				drawArrowHeads(g2d, strokeThin, edge);
		
		drawVertices(bridge, g2d, context, ((GraphStyle)boardStyle).baseVertexRadius());
		
		drawSymbols(g2d, context);
		
		if (context.game().isDeductionPuzzle() && context.game().metadata().graphics().showRegionOwner())
			drawRegions(g2d, context, colorSymbol(), strokeThick, hintRegions);
		
		drawGround(g2d, context, false);
		
		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws a specified edge of the board/graph.
	 */
	protected void drawEdge
	(
		final Graphics2D g2d,
		final Context context,
		final Color defaultLineColour, 
		final Stroke defaultLineStroke,
		final EdgeType edgeType, 
		final RelationType relationType,
		final boolean connection,
		final boolean alwaysDraw,
		final double offsetY
	)
	{
		// Check Metadata to see if any specific style or colour has been defined for this Edge/Relation type.
		Color lineColour  = defaultLineColour;
		Stroke lineStroke = defaultLineStroke;
		final EdgeInfoGUI edgeInfoGUI = context.game().metadata().graphics().drawEdge(edgeType, relationType, connection);
		
		if (edgeInfoGUI != null)
		{
			if (edgeInfoGUI.getStyle().equals(LineStyle.Hidden))
				return;
			
			if (edgeInfoGUI.getColour() != null)
				lineColour = edgeInfoGUI.getColour();
			
			if (edgeInfoGUI.getStyle() != null)
				lineStroke = StrokeUtil.getStrokeFromStyle(edgeInfoGUI.getStyle(), strokeThin, strokeThick());
		}
		
		if (alwaysDraw || edgeInfoGUI != null)
		{
			if (relationType.supersetOf(RelationType.Orthogonal))
			{
				if (connection)
				{
					drawOrthogonalConnections(g2d, context, lineColour, lineStroke, offsetY);
				}
				else
				{
					if (edgeType.supersetOf(EdgeType.Inner))
						drawInnerCellEdges(g2d, context, lineColour, lineStroke, offsetY);
					
					if (edgeType.supersetOf(EdgeType.Outer))
						drawOuterCellEdges(g2d, context, lineColour, lineStroke, offsetY);
				}
			}
			if (relationType.supersetOf(RelationType.Diagonal))
			{
				if (connection)
					drawDiagonalConnections(g2d, context, lineColour, lineStroke, offsetY);
				else
					drawDiagonalEdges(g2d, context, lineColour, lineStroke, offsetY);
			}
			if (relationType.supersetOf(RelationType.OffDiagonal))
			{
				if (connection)
					drawOffDiagonalConnections(g2d, context, lineColour, lineStroke, offsetY);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws arrow heads on the edge if it is directional.
	 */
	protected void drawArrowHeads(final Graphics2D g2d, final BasicStroke stroke, final Edge edge)
	{
		final Point drawPosnA = screenPosn(edge.vA().centroid());
		final Point drawPosnB = screenPosn(edge.vB().centroid());
		
		if (Edge.toB())
			drawArrowHead(g2d, new Line2D.Double(drawPosnA, drawPosnB), stroke);
		if (Edge.toA())
			drawArrowHead(g2d, new Line2D.Double(drawPosnB, drawPosnA), stroke);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws an Arrow head on the line passed in.
	 */
	protected static void drawArrowHead(final Graphics2D g2d, final Line2D line, final BasicStroke stroke) 
	{  
		final int strokeWidth = (int)stroke.getLineWidth()*4;
		final Polygon arrowHead = new Polygon();  
		arrowHead.addPoint(0, 0);
		arrowHead.addPoint((int)(-strokeWidth/1.5), -strokeWidth);
		arrowHead.addPoint((int)(strokeWidth/1.5),-strokeWidth);
		final AffineTransform tx = new AffineTransform();
	    tx.setToIdentity();
	    final double angle = Math.atan2(line.getY2()-line.getY1(), line.getX2()-line.getX1());
	    tx.translate(line.getX2(), line.getY2());
	    tx.rotate(angle - Math.PI/2d);  
	    final Graphics2D g = (Graphics2D) g2d.create();
	    g.setTransform(tx);   
	    g.fill(arrowHead);
	    g.dispose();
	}

}
