package view.container.aspects.designs.board;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.SiteType;
import metadata.graphics.util.MetadataImageInfo;
import other.context.Context;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class GoDesign extends BoardDesign
{
	public GoDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();
		final float swRatio = 0.002f;
		final float swThin  = Math.max(0.5f, swRatio * boardStyle.placement().width);
		final float swThick = swThin;

		final Color colourInner = new Color(160, 140, 100);
		final Color colourOuter = new Color(  0,   0,   0);
		final Color colourFill  = new Color(255, 230, 150);
		final Color colourDot   = new Color(130, 120,  90);  //colourInner;
		
		setStrokesAndColours
		(
			bridge,
			context,
			colourInner,
			colourOuter,
			colourFill,
			null,
			null,
			null,
			null,
			null,
			colourDot,
			swThin,
			swThick
		);

		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);
		
		final ArrayList<Integer> symbolLocations = new ArrayList<Integer>();
		final int boardCellsWidth = topology().columns(context.board().defaultSite()).size();
		final int boardCellsHeight = topology().rows(context.board().defaultSite()).size();
		if (boardCellsWidth > 13)
		{
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 3 + 3));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 3 + boardCellsWidth/2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 3 + boardCellsWidth - 4));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-1)/2 + 3));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-1)/2 + boardCellsWidth/2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-1)/2 + boardCellsWidth - 4));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-4) + 3));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-4) + boardCellsWidth/2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-4) + boardCellsWidth - 4));
		}
		else if (boardCellsWidth > 9)
		{
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 3 + 3));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 3 + boardCellsWidth - 4));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-1)/2 + boardCellsWidth/2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-4) + 3));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-4) + boardCellsWidth - 4));
		}
		else
		{
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 2 + 2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * 2 + boardCellsWidth - 3));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-1)/2 + boardCellsWidth/2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-3) + 2));
			symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-3) + boardCellsWidth - 3));
		}

		for (final int i : symbolLocations)
		{
			symbols.add(new MetadataImageInfo(i, SiteType.Vertex, "dot", (float)0.3));
		}
		
		drawSymbols(g2d, context);

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
}
