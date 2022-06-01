package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.SiteType;
import graphics.svg.SVGtoImage;
import other.context.Context;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class XiangqiDesign extends BoardDesign
{
	public XiangqiDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
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
		drawSymbols(g2d, context);
		drawXiangqiSymbols(g2d);
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
					final Point vaWorld = boardStyle.screenPosn(vA.centroid());
					final Point vbWorld = boardStyle.screenPosn(vB.centroid());
					
					path.moveTo(vaWorld.x, vaWorld.y);
					path.lineTo(vbWorld.x, vbWorld.y);
				}
			}
		}

		if (context.board().topology().vertices().size() == 90)
		{
			Point screenPosn = boardStyle.screenPosn(topology().vertices().get(3).centroid());
			path.moveTo(screenPosn.x, screenPosn.y);
			screenPosn = boardStyle.screenPosn(topology().vertices().get(23).centroid());
			path.lineTo(screenPosn.x, screenPosn.y);
	
			screenPosn = boardStyle.screenPosn(topology().vertices().get(5).centroid());
			path.moveTo(screenPosn.x, screenPosn.y);
			screenPosn = boardStyle.screenPosn(topology().vertices().get(21).centroid());
			path.lineTo(screenPosn.x, screenPosn.y);
	
			screenPosn = boardStyle.screenPosn(topology().vertices().get(86).centroid());
			path.moveTo(screenPosn.x, screenPosn.y);
			screenPosn = boardStyle.screenPosn(topology().vertices().get(66).centroid());
			path.lineTo(screenPosn.x, screenPosn.y);
	
			screenPosn = boardStyle.screenPosn(topology().vertices().get(84).centroid());
			path.moveTo(screenPosn.x, screenPosn.y);
			screenPosn = boardStyle.screenPosn(topology().vertices().get(68).centroid());
			path.lineTo(screenPosn.x, screenPosn.y);
		}

		g2d.draw(path);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws all Xianqgi symbols on the board.
	 */
	public void drawXiangqiSymbols(final Graphics2D g2d)
	{
		final int imgSz = boardPlacement.cellRadiusPixels() * 2;
		
		// Load the decoration for special cells
		final int boardVertexWidth = topology().columns(SiteType.Vertex).size();
		final ArrayList<Integer> symbolLocations = new ArrayList<>();
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 2 + 1));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 2 + 7));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 3 + 2));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 3 + 4));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 3 + 6));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 6 + 2));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 6 + 4));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 6 + 6));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 7 + 1));
		symbolLocations.add(Integer.valueOf(boardVertexWidth * 7 + 7));

		final Color edgeColour = Color.black;
		final Color fillColour = colorSymbol();

		for (final Vertex v : boardStyle.topology().vertices())
		{
			final Point drawPosn = boardStyle.screenPosn(v.centroid());
			
			if (v.index() == boardVertexWidth * 3 || v.index() == boardVertexWidth * 6)
				SVGtoImage.loadFromFilePath
				(
					g2d, "/svg/xiangqi/symbol_left.svg", new Rectangle(
					(int)(drawPosn.x - imgSz*0.125), (int)(drawPosn.y - imgSz*0.375), (int)(imgSz*0.75), (int)(imgSz*0.75)), 
					edgeColour, fillColour, 0
				);

			if (v.index() == boardVertexWidth * 3 + 8 || v.index() == boardVertexWidth * 6 + 8)
				SVGtoImage.loadFromFilePath
				(
					g2d, "/svg/xiangqi/symbol_right.svg", new Rectangle( 
					(int)(drawPosn.x - imgSz*0.6), (int)(drawPosn.y - imgSz*0.375), (int)(imgSz*0.75), (int)(imgSz*0.75)), 
					edgeColour, fillColour, 0
				);
			
			if (symbolLocations.contains(Integer.valueOf(v.index())))
				SVGtoImage.loadFromFilePath
				(
					g2d, "/svg/xiangqi/symbol.svg", new Rectangle(  
					(int)(drawPosn.x - imgSz*0.375), (int)(drawPosn.y - imgSz*0.375), (int)(imgSz*0.75), (int)(imgSz*0.75)),
					edgeColour, fillColour, 0
				);
		}
	}

	//-------------------------------------------------------------------------
	
}
