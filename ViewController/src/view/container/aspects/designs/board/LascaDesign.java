package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import other.topology.TopologyElement;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class LascaDesign extends BoardDesign
{
	public LascaDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
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
			null,
			null,
			new Color(200, 200, 200),
			null,
			null,
			new Color(255, 255, 255),
			null,
			null,
			null,
			swThin,
			swThick
		);

		final double vertexRadius = boardStyle.cellRadiusPixels() * 0.95;
		
		drawBoardOutline(g2d);
		drawVertices(bridge, g2d, context, vertexRadius);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	@Override
	protected void drawVertices(final Bridge bridge, final Graphics2D g2d, final Context context, final double vertexRadius)
	{
		g2d.setColor(colorFillPhase3);

		for (final Vertex vertex : topology().vertices())
		{
			final Point position = screenPosn(vertex.centroid());
			final java.awt.Shape ellipseO = new Ellipse2D.Double(position.x - vertexRadius, position.y - vertexRadius, 2*vertexRadius, 2*vertexRadius);
			g2d.fill(ellipseO);
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void drawBoardOutline(final SVGGraphics2D g2d)
	{
		g2d.setStroke(strokeThin);

		double minX = 9999;
		double minY = 9999;
		double maxX = -9999;
		double maxY = -9999;
		final GeneralPath path = new GeneralPath();

		for (final TopologyElement cell : topology().vertices())
		{			
			final Point posn = screenPosn(cell.centroid());
			
			final int x = posn.x;
			final int y = posn.y;

			if (minX > x)
				minX = x;
			if (minY > y)
				minY = y;
			if (maxX < x)
				maxX = x;
			if (maxY < y)
				maxY = y;
		}
		g2d.setColor(colorFillPhase0);

		final int OuterBufferDistance = (int) (cellRadiusPixels() * 1.1);
		path.moveTo(minX - OuterBufferDistance, minY - OuterBufferDistance);
		path.lineTo(minX - OuterBufferDistance, maxY + OuterBufferDistance);
		path.lineTo(maxX + OuterBufferDistance, maxY + OuterBufferDistance);
		path.lineTo(maxX + OuterBufferDistance, minY - OuterBufferDistance);
		path.lineTo(minX - OuterBufferDistance, minY - OuterBufferDistance);
		g2d.fill(path);
	}
	
	//-------------------------------------------------------------------------
	
}
