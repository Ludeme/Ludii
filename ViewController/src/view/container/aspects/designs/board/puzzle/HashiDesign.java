package view.container.aspects.designs.board.puzzle;

import java.awt.Color;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class HashiDesign extends PuzzleDesign
{
	public HashiDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 3 / 1000.0f;
		final float swThin  = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = 2 * swThin;

		setStrokesAndColours
		(
			bridge, 
			context,
			new Color(0, 0, 0),
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			new Color(0, 0, 0),
			swThin,
			swThick
		);
		
		//boardStyle.detectHints(context);

		final double cellDistance = boardStyle.cellRadius();
		final double vertexRadius = 0.5 * boardStyle.placement().width * cellDistance;
		drawVertices(bridge, g2d, context, vertexRadius);

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
}
