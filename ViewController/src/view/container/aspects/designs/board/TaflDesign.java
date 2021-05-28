package view.container.aspects.designs.board;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.SiteType;
import metadata.graphics.util.MetadataImageInfo;
import other.context.Context;
import other.topology.TopologyElement;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class TaflDesign extends BoardDesign
{
	public TaflDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
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
			new Color(220, 170, 70),
			new Color(175, 125, 75),
			new Color(250, 200, 100),
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
		
		final ArrayList<Integer> symbolLocations = new ArrayList<>();

		// Draw the centre
		for (final TopologyElement v : topology().centre(SiteType.Cell))
			symbolLocations.add(Integer.valueOf(v.index()));
		
		for (final int i : symbolLocations)
		{
			// If the number of sides of the cell is divisible by 3, then use a triangle knot.
			if (topology().cells().get(i).vertices().size() % 3 == 0)
				symbols.add(new MetadataImageInfo(i,SiteType.Cell,"knotTriangle",(float)0.8));
			
			// Otherwise use the square knot.
			else
				symbols.add(new MetadataImageInfo(i,SiteType.Cell,"knotSquare",(float)0.9));	
		}
		
		drawSymbols(g2d);
		drawOuterCellEdges(bridge, g2d, context);

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------

}
