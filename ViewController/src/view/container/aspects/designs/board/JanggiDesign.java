package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class JanggiDesign extends BoardDesign
{
	public JanggiDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
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
		final float swThick = swThin;

		setStrokesAndColours
		(
			bridge,
			context,
			new Color(100, 75, 50),
			new Color(100, 75, 50),
			new Color(255, 165, 0),
			null,
			null,
			null,
			null,
			null,
			new Color(0, 0, 0),
			swThin,
			swThick
		);

		drawBoardOutline(g2d);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	@Override
	protected void drawInnerCellEdges(final Graphics2D g2d, final Context context)
	{
		// Draw cell edges (inner)
		g2d.setStroke(strokeThin);
		g2d.setColor(colorEdgesInner);
		final GeneralPath path = new GeneralPath();
		for (final Vertex vA : topology().vertices())
		{
			for (final Vertex vB : vA.orthogonal())
			{
				final Point2D va = vA.centroid();
				final Point2D vb = vB.centroid();

				// only draw inner edges if not overlapping the river
				if ((va.getY() < 0.5 || vb.getY() > 0.5) && (va.getY() > 0.5 || vb.getY() < 0.5))
				{
					final Point vaWorld = screenPosn(vA.centroid());
					final Point vbWorld = screenPosn(vB.centroid());
					
					path.moveTo(vaWorld.x, vaWorld.y);
					path.lineTo(vbWorld.x, vbWorld.y);
				}
			}
		}

		Point screenPosn = screenPosn(topology().vertices().get(3).centroid());
		path.moveTo(screenPosn.x, screenPosn.y);
		screenPosn = screenPosn(topology().vertices().get(23).centroid());
		path.lineTo(screenPosn.x, screenPosn.y);

		screenPosn = screenPosn(topology().vertices().get(5).centroid());
		path.moveTo(screenPosn.x, screenPosn.y);
		screenPosn = screenPosn(topology().vertices().get(21).centroid());
		path.lineTo(screenPosn.x, screenPosn.y);

		screenPosn = screenPosn(topology().vertices().get(86).centroid());
		path.moveTo(screenPosn.x, screenPosn.y);
		screenPosn = screenPosn(topology().vertices().get(66).centroid());
		path.lineTo(screenPosn.x, screenPosn.y);

		screenPosn = screenPosn(topology().vertices().get(84).centroid());
		path.moveTo(screenPosn.x, screenPosn.y);
		screenPosn = screenPosn(topology().vertices().get(68).centroid());
		path.lineTo(screenPosn.x, screenPosn.y);

		g2d.draw(path);
	}

	//-------------------------------------------------------------------------
	
}
