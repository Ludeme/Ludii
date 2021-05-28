package view.container.aspects.designs.board.puzzle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import bridge.Bridge;
import game.types.board.SiteType;
import game.util.directions.CompassDirection;
import metadata.graphics.util.PuzzleHintLocationType;
import other.context.Context;
import other.topology.TopologyElement;
import other.topology.Vertex;
import view.container.aspects.designs.board.graph.GraphDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class FutoshikiDesign extends GraphDesign
{
	public FutoshikiDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement, false, false);
		hintLocationType = PuzzleHintLocationType.BetweenVertices;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void drawPuzzleHints(final Graphics2D g2d, final Context context)
	{
		if (hintValues == null)
			detectHints(context);

		for (final TopologyElement graphElement : topology().getAllGraphElements())
		{
			final SiteType type = graphElement.elementType();
			final int site = graphElement.index();

			final Point2D posn = graphElement.centroid();

			final Point drawnPosn = screenPosn(posn);

			for (int i = 0; i < hintValues.size(); i++)
			{
				if (locationValues.get(i).site() == site && locationValues.get(i).siteType() == type)
				{
					int maxHintvalue = 0;
					for (int j = 0; j < hintValues.size(); j++)
					{
						if (hintValues.get(i) != null)
						{
							if (hintValues.get(i).intValue() > maxHintvalue)
							{
								maxHintvalue = hintValues.get(i).intValue();
							}
						}
					}
					
					Font valueFont = new Font("Arial", Font.BOLD, (boardStyle.cellRadiusPixels()));
					g2d.setColor(Color.BLACK);
					g2d.setFont(valueFont);
					Rectangle2D rect = g2d.getFont().getStringBounds("^", g2d.getFontRenderContext());
					
					if (hintDirections.get(i) == CompassDirection.W)
					{
						rect = g2d.getFont().getStringBounds("<", g2d.getFontRenderContext());
						g2d.drawString("<", (int)(drawnPosn.x - rect.getWidth()/2), (int)(drawnPosn.y + rect.getHeight()/3));
					}
					else if (hintDirections.get(i) == CompassDirection.N)
					{
						rect = g2d.getFont().getStringBounds("^", g2d.getFontRenderContext());
						g2d.drawString("^", (int)(drawnPosn.x - rect.getWidth()/2), (int)(drawnPosn.y + rect.getHeight()/2));
					}
					else if (hintDirections.get(i) == CompassDirection.E)
					{
						rect = g2d.getFont().getStringBounds(">", g2d.getFontRenderContext());
						g2d.drawString(">", (int)(drawnPosn.x - rect.getWidth()/2), (int)(drawnPosn.y + rect.getHeight()/3));
					}
					else if (hintDirections.get(i) == CompassDirection.S)
					{
						valueFont = new Font("Arial", Font.BOLD, -(boardStyle.cellRadiusPixels()));
						g2d.setFont(valueFont);
						g2d.drawString("^", (int)(drawnPosn.x + rect.getWidth()/2), (int)(drawnPosn.y - rect.getHeight()/2));
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	protected void drawVertices(final Bridge bridge, final Graphics2D g2d, final Context context, final double radius)
	{
		for (final Vertex vertex : topology().vertices())
		{
			final int squareSize = (int) (boardStyle.cellRadiusPixels() * 1.2);
			g2d.setColor(colorEdgesOuter);
			g2d.setStroke(new BasicStroke(strokeThick.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
			final Point pt = boardStyle.screenPosn(vertex.centroid());
			g2d.drawRect(pt.x-squareSize/2, pt.y-squareSize/2, squareSize, squareSize);
		}
	}
	
	//-------------------------------------------------------------------------

}
