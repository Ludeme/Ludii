package view.container.aspects.components.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import game.types.board.SiteType;
import metadata.graphics.util.BoardGraphicsType;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.TopologyElement;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.designs.board.puzzle.PuzzleDesign;
import view.container.styles.board.graph.PenAndPaperStyle;

/**
 * Pen and Paper components properties.
 * 
 * @author Matthew.Stephenson
 */
public class PenAndPaperComponents extends PuzzleComponents
{
	private final PenAndPaperStyle graphStyle;
	private final BoardDesign boardDesign;
	
	//-------------------------------------------------------------------------
	
	public PenAndPaperComponents
	(
		final Bridge bridge, final PenAndPaperStyle containerStyle, final PuzzleDesign boardDesign
	)
	{
		super(bridge, containerStyle, boardDesign);
		graphStyle       = containerStyle;
		this.boardDesign = boardDesign;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void drawComponents(final Graphics2D g2d, final Context context)
	{	
		final List<Vertex> vertices = graphStyle.topology().vertices();
		final BasicStroke strokeThick = boardDesign.strokeThick();
		final ContainerState cs = context.state().containerStates()[0];

		if (context.metadata().graphics().replaceComponentsWithFilledCells())
		{
			fillCellsBasedOnOwner(g2d, context);
		}
		else
		{
			super.drawComponents(g2d, context, (ArrayList<? extends TopologyElement>) graphStyle.topology().cells());
			super.drawComponents(g2d, context, (ArrayList<? extends TopologyElement>) graphStyle.topology().vertices());
		}
		
		// Pass 1: Draw thick black edges
		g2d.setColor(Color.BLACK);
		final BasicStroke slightlyThickerStroke = new BasicStroke(strokeThick.getLineWidth() + 4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		g2d.setStroke(slightlyThickerStroke);

		for (final Vertex va : vertices)
		{
			final Point vaPosn = graphStyle.screenPosn(va.centroid());
			for (final Vertex vb : va.orthogonal())
			{
				for (int e = 0; e < context.topology().edges().size(); e++)
				{
					final Edge edge = context.topology().edges().get(e);
					if ((edge.vA() == va && edge.vB() == vb) || (edge.vA() == vb && edge.vB() == va))
					{
						if (cs.whatEdge(e) != 0)
						{
							final Point vbPosn = graphStyle.screenPosn(vb.centroid());
							final java.awt.Shape line = new Line2D.Double(vaPosn.x, vaPosn.y, vbPosn.x, vbPosn.y);
							g2d.draw(line);
						}
					}
				}
			}
		}

		// Pass 2: Redraw thinner edges in player colour
		final BasicStroke roundedThinStroke = new BasicStroke(strokeThick.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		g2d.setStroke(roundedThinStroke);

		for (final Vertex va : vertices)
		{
			final Point vaPosn = graphStyle.screenPosn(va.centroid());

			for (final Vertex vb : va.orthogonal())
			{
				for (int e = 0; e < context.topology().edges().size(); e++)
				{
					final Edge edge = context.topology().edges().get(e);
					if ((edge.vA() == va && edge.vB() == vb) || (edge.vA() == vb && edge.vB() == va))
					{
						if (cs.whatEdge(e) != 0)
						{
							final Point vbPosn = graphStyle.screenPosn(vb.centroid());
							final java.awt.Shape line = new Line2D.Double(vaPosn.x, vaPosn.y, vbPosn.x, vbPosn.y);
							g2d.setColor(bridge.settingsColour().playerColour(context, cs.whoEdge(e)));
							g2d.draw(line);
						}
					}
				}
			}
		}
		
		// If a puzzle, and edge is set to zero, draw a cross.
		final int dim = puzzleStyle.topology().rows(context.board().defaultSite()).size();
		final int bigFontSize = (int) (0.75 * puzzleStyle.placement().getHeight() / dim + 0.5);
		final Font bigFont = new Font("Arial", Font.BOLD,  bigFontSize);	
		g2d.setFont(bigFont);
		for (final Edge e : graphStyle.topology().edges())
		{
			if (cs.isResolved(e.index(), SiteType.Edge) && cs.what(e.index(), SiteType.Edge) == 0)
			{
				final Point drawPosn = graphStyle.screenPosn(e.centroid());
				final Rectangle bounds = g2d.getFontMetrics().getStringBounds("X", g2d).getBounds();
				g2d.drawString("X", drawPosn.x - bounds.width/2, drawPosn.y + bounds.height/3);
			}
		}

		// Draw vertices
		final double rO = graphStyle.baseVertexRadius();
		for (final Vertex vertex : vertices)
		{
			if (cs.what(vertex.index(), SiteType.Vertex) != 0)
			{
				g2d.setColor(bridge.settingsColour().playerColour(context, cs.who(vertex.index(), SiteType.Vertex)));
			}
			else
			{
				if (context.game().metadata().graphics().boardColour(BoardGraphicsType.InnerVertices) == null)
					g2d.setColor(graphStyle.baseGraphColour());
				else
					g2d.setColor(context.game().metadata().graphics().boardColour(BoardGraphicsType.InnerVertices));
			}
			
			final Point circlePosn = graphStyle.screenPosn(vertex.centroid());
			final java.awt.Shape ellipseO = new Ellipse2D.Double(circlePosn.x-rO, circlePosn.y-rO, 2*rO, 2*rO);
			g2d.fill(ellipseO);
		}
	}
	
	//-------------------------------------------------------------------------
	
}
