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

public class ShogiDesign extends BoardDesign
{
	public ShogiDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{		
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 4 / 1000.0f;
		final float swThin = Math.max(1, (int) ((swRatio * 100) / topology().vertices().size() * boardStyle.placement().width + 0.5));
		final float swThick = swThin;

		setStrokesAndColours
		(
			bridge,
			context,
			new Color(100, 75, 50),
			new Color(100, 75, 50),
			new Color(255, 230, 130),
			null,
			null,
			null,
			null,
			null,
			new Color(0, 0, 0),
			swThin,
			swThick
		);

		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		
		// Load the decoration for special cells
		final int boardCellsWidth = topology().columns(context.board().defaultSite()).size() + 1;
		final int boardCellsHeight = topology().rows(context.board().defaultSite()).size() + 1;

		int dotInwardsValueVertical, dotInwardsValueHorizontal;
		dotInwardsValueVertical = dotInwardsValueHorizontal = boardCellsWidth/3;
		
		// Taikyoku Shogi
		if (topology().cells().size() == 1296)
		{
			dotInwardsValueVertical = 6;
			dotInwardsValueHorizontal = 7;
		}
		
		final ArrayList<Integer> symbolLocations = new ArrayList<>();
		symbolLocations.add(Integer.valueOf(boardCellsWidth * dotInwardsValueVertical + dotInwardsValueHorizontal));
		symbolLocations.add(Integer.valueOf(boardCellsWidth * dotInwardsValueVertical + boardCellsWidth - dotInwardsValueHorizontal-1));
		symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-dotInwardsValueVertical-1) + dotInwardsValueHorizontal));
		symbolLocations.add(Integer.valueOf(boardCellsWidth * (boardCellsHeight-dotInwardsValueVertical-1) + boardCellsWidth - dotInwardsValueHorizontal-1));

		if (topology().numEdges() == 4)
			for (final int i : symbolLocations)
				symbols.add(new MetadataImageInfo(i,SiteType.Vertex,"dot",(float)0.2));
		
		drawSymbols(g2d);

		drawOuterCellEdges(bridge, g2d, context);

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
}
