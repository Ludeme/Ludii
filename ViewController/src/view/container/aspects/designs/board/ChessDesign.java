package view.container.aspects.designs.board;

import java.awt.Color;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class ChessDesign extends BoardDesign
{
	public ChessDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		checkeredBoard = true;
		
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 5 / 1000.0f;
		final float swThin  = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = 1 * swThin;

		setStrokesAndColours
		(
			bridge, 
			context,
			new Color(0,0,0),
			new Color(150, 75, 0),    // border
			new Color(200, 150, 75),   // dark cells
			new Color(250, 221, 144),  // light cells
			new Color(223, 178, 110),   // middle cells
			new Color(255, 240, 200),   // other cells
			null,
			null,
			new Color(0,0,0),
			swThin,
			swThick
		);
		
		drawGround(g2d, context, true);

		fillCells(bridge, g2d, context);
		
		drawSymbols(g2d);
		
		drawGround(g2d, context, false);

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
}
