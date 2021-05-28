package view.container.aspects.designs.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import main.math.MathRoutines;
import other.context.Context;
import other.topology.Cell;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class GooseDesign extends BoardDesign
{
	public GooseDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final Rectangle placement = boardStyle.placement();
		
		//pieceScale = 0.95;
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 2 / 1000.0f;
		final float swThin = Math.max(1, (int) (swRatio * placement.width + 0.5));
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
			new Color(140, 140, 140), 
			swThin, 
			swThick
		);

		final int r = (int)
					  (
						MathRoutines.distance
					    (
					    	topology().cells().get(0).centroid(),
					    	topology().cells().get(1).centroid()
						 )
						 *
						 placement.width * 0.475 + 0.5
					  );

		final Color fillColour   = new Color(240, 240, 240);
		final Color borderColour = new Color(200, 200, 200);
		
		g2d.setStroke(new BasicStroke(swThin, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(borderColour);

		// Draw lines between nbors
		for (int vid = 0; vid < topology().cells().size()-1; vid++)
		{
			final Cell vertexA = topology().cells().get(vid);
			final Cell vertexB = topology().cells().get(vid+1);

			final double ax = vertexA.centroid().getX() * placement.width;
			final double ay = placement.width - vertexA.centroid().getY() * placement.width;
			
			final double bx = vertexB.centroid().getX() * placement.width;
			final double by = placement.width - vertexB.centroid().getY() * placement.width;

			final java.awt.Shape line = new Line2D.Double(ax, ay, bx, by);
			g2d.draw(line);
		}
			
		// Draw cells
		for (int vid = 0; vid < topology().cells().size(); vid++)
		{
			final Cell vertexA = topology().cells().get(vid);
			
			final double ax = vertexA.centroid().getX() * placement.width;
			final double ay = placement.width - vertexA.centroid().getY() * placement.width;

			g2d.setColor(fillColour);
			g2d.fillArc((int)ax-r, (int)ay-r, 2*r+1, 2*r+1, 0, 360);		

			g2d.setColor(borderColour);
			g2d.drawArc((int)ax-r, (int)ay-r, 2*r+1, 2*r+1, 0, 360);		
		}

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
}
