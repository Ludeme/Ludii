package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import bridge.Bridge;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Radial;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.topology.Vertex;
import view.container.ContainerStyle;

/**
 * Functions for drawing various graphical aspects which are only intended for developers.
 * 
 * @author Matthew.Stephenson
 */
public class DeveloperGUI 
{
	/**
	 * Draw the pre-generated sites of the container.
	 */
	public static void drawPregeneration(final Bridge bridge, final Graphics2D g2d, final Context context, final ContainerStyle containerStyle) 
	{
		try
		{
			final int cellRadiusPixels = containerStyle.cellRadiusPixels();
			
			if (bridge.settingsVC().lastClickedSite() != null)
			{
				final Topology graph = context.board().topology();
				
				if (bridge.settingsVC().lastClickedSite().siteType() == SiteType.Cell)
				{
					if (bridge.settingsVC().drawNeighboursCells())
					{
						drawNeighbours(g2d, bridge.settingsVC().lastClickedSite().site(), true, containerStyle);
					}
					if (bridge.settingsVC().drawRadialsCells())
					{
						drawRadials(bridge, g2d, context, bridge.settingsVC().lastClickedSite().site(), containerStyle, SiteType.Cell);
					}
					if (bridge.settingsVC().drawDistanceCells())
					{
						drawDistance(bridge, g2d, context, bridge.settingsVC().lastClickedSite().site(),
								bridge.settingsVC().lastClickedSite().siteType(), containerStyle);
					}
				}
				
				if (bridge.settingsVC().lastClickedSite().siteType() == SiteType.Vertex)
				{
					if (bridge.settingsVC().drawNeighboursVertices())
					{
						drawNeighbours(g2d, bridge.settingsVC().lastClickedSite().site(), false, containerStyle);
					}
					if (bridge.settingsVC().drawRadialsVertices())
					{
						drawRadials(bridge, g2d, context, bridge.settingsVC().lastClickedSite().site(), containerStyle, SiteType.Vertex);
					}

					if (bridge.settingsVC().drawDistanceVertices())
					{
						drawDistance(bridge, g2d, context, bridge.settingsVC().lastClickedSite().site(),
								bridge.settingsVC().lastClickedSite().siteType(), containerStyle);
					}
				}

				if (bridge.settingsVC().lastClickedSite().siteType() == SiteType.Edge)
				{
					if (bridge.settingsVC().drawDistanceEdges())
					{
						drawDistance(bridge, g2d, context, bridge.settingsVC().lastClickedSite().site(),
								bridge.settingsVC().lastClickedSite().siteType(), containerStyle);
					}
				}
				
				if (bridge.settingsVC().drawVerticesOfEdges() && bridge.settingsVC().lastClickedSite().siteType() == SiteType.Edge)
				{
					for (final Vertex v : graph.edges().get(bridge.settingsVC().lastClickedSite().site()).vertices())
					{
						g2d.setColor(new Color(0,255,255,125));
						final Point drawPosn = containerStyle.screenPosn(v.centroid());
						g2d.fillOval(drawPosn.x - cellRadiusPixels / 2, drawPosn.y - cellRadiusPixels / 2, cellRadiusPixels, cellRadiusPixels);
					}
				}
				
				if (bridge.settingsVC().drawVerticesOfFaces() && bridge.settingsVC().lastClickedSite().siteType() == SiteType.Cell)
				{
					for (final Vertex v : graph.cells().get(bridge.settingsVC().lastClickedSite().site()).vertices())
					{
						g2d.setColor(new Color(0,255,255,125));
						final Point drawPosn = containerStyle.screenPosn(v.centroid());
						g2d.fillOval(drawPosn.x - cellRadiusPixels / 2, drawPosn.y - cellRadiusPixels / 2, cellRadiusPixels, cellRadiusPixels);
					}
				}
				
				if (bridge.settingsVC().drawEdgesOfFaces() && bridge.settingsVC().lastClickedSite().siteType() == SiteType.Cell)
				{
					for (final Edge e : graph.cells().get(bridge.settingsVC().lastClickedSite().site()).edges())
					{
						g2d.setColor(new Color(0,255,255,125));
						final Point drawPosn = containerStyle.screenPosn(e.centroid());
						g2d.fillOval(drawPosn.x - cellRadiusPixels / 2, drawPosn.y - cellRadiusPixels / 2, cellRadiusPixels, cellRadiusPixels);
					}
				}
				
				if (bridge.settingsVC().drawEdgesOfVertices() && bridge.settingsVC().lastClickedSite().siteType() == SiteType.Vertex)
				{
					for (final Edge e : graph.vertices().get(bridge.settingsVC().lastClickedSite().site()).edges())
					{
						g2d.setColor(new Color(0,255,255,125));
						final Point drawPosn = containerStyle.screenPosn(e.centroid());
						g2d.fillOval(drawPosn.x - cellRadiusPixels / 2, drawPosn.y - cellRadiusPixels / 2, cellRadiusPixels, cellRadiusPixels);
					}
				}
				
				if (bridge.settingsVC().drawFacesOfEdges() && bridge.settingsVC().lastClickedSite().siteType() == SiteType.Edge)
				{
					for (final Cell c : graph.edges().get(bridge.settingsVC().lastClickedSite().site()).cells())
					{
						g2d.setColor(new Color(0,255,255,125));
						final Point drawPosn = containerStyle.screenPosn(c.centroid());
						g2d.fillOval(drawPosn.x - cellRadiusPixels / 2, drawPosn.y - cellRadiusPixels / 2, cellRadiusPixels, cellRadiusPixels);
					}
				}
				
				if (bridge.settingsVC().drawFacesOfVertices() && bridge.settingsVC().lastClickedSite().siteType() == SiteType.Vertex)
				{
					for (final Cell c : graph.vertices().get(bridge.settingsVC().lastClickedSite().site()).cells())
					{
						g2d.setColor(new Color(0,255,255,125));
						final Point drawPosn = containerStyle.screenPosn(c.centroid());
						g2d.fillOval(drawPosn.x - cellRadiusPixels / 2, drawPosn.y - cellRadiusPixels / 2, cellRadiusPixels, cellRadiusPixels);
					}
				}
			}
		}
		catch(final Exception E)
		{
			// something went wrong, probably changed an option or the game.
			return;
		}
		
		drawPregenerationRegions(bridge, g2d, context, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	/**
     * Draw the pre-generated regions of the container.
     */
    private static void drawPregenerationRegions(final Bridge bridge, final Graphics2D g2d, final Context context, final ContainerStyle containerStyle)
	{
		final Topology graph = context.board().topology();
		g2d.setStroke(new BasicStroke((2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
    	
		// Cells
    	final List<TopologyElement> allGraphElementsToDraw = new ArrayList<>();
    	if (bridge.settingsVC().drawCornerCells())
    	{
			for (final TopologyElement v : graph.corners(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(255,0,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerConcaveCells())
		{
			for (final TopologyElement v : graph.cornersConcave(SiteType.Cell))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerConvexCells())
		{
			for (final TopologyElement v : graph.cornersConvex(SiteType.Cell))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawOuterCells())
    	{
			for (final TopologyElement v : graph.outer(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,255,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawMajorCells())
		{
			for (final TopologyElement v : graph.major(SiteType.Cell))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawMinorCells())
		{
			for (final TopologyElement v : graph.minor(SiteType.Cell))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawPerimeterCells())
		{
			for (final TopologyElement v : graph.perimeter(SiteType.Cell))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawInnerCells())
    	{
    		for (final TopologyElement v : graph.inner(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(127,0,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawTopCells())
    	{
			for (final TopologyElement v : graph.top(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,127,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawBottomCells())
    	{
			for (final TopologyElement v : graph.bottom(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,0,127,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawLeftCells())
    	{
			for (final TopologyElement v : graph.left(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(255,255,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawRightCells())
    	{
			for (final TopologyElement v : graph.right(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(255,0,255,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawCenterCells())
    	{
			for (final TopologyElement v : graph.centre(SiteType.Cell))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,127,127,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
		if (bridge.settingsVC().drawPhasesCells())
		{
			g2d.setColor(new Color(255, 0, 0, 125));
			drawPhase(bridge, g2d, context, SiteType.Cell, containerStyle);
		}

    	allGraphElementsToDraw.clear();
		for (final Entry<DirectionFacing, List<TopologyElement>> entry : graph.sides(SiteType.Cell).entrySet())
    	{
    		final String DirectionName = entry.getKey().uniqueName().toString();
    		if (bridge.settingsVC().drawSideCells().containsKey(DirectionName) && bridge.settingsVC().drawSideCells().get(DirectionName).booleanValue())
        	{
    			try
    			{
					for (final TopologyElement c : entry.getValue())
	        		{
	        			allGraphElementsToDraw.add(c);
	        		}
    			}
    			catch (final Exception e)
    			{
    				// carry on
    			}
        	}
    	}
    	g2d.setColor(new Color(255,50,50,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	// Vertices
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawCornerVertices())
    	{
			for (final TopologyElement v : graph.corners(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(255,0,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerConcaveVertices())
		{
			for (final TopologyElement v : graph.cornersConcave(SiteType.Vertex))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerConvexVertices())
		{
			for (final TopologyElement v : graph.cornersConvex(SiteType.Vertex))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawMajorVertices())
		{
			for (final TopologyElement v : graph.major(SiteType.Vertex))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawMinorVertices())
		{
			for (final TopologyElement v : graph.minor(SiteType.Vertex))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawPerimeterVertices())
		{
			for (final TopologyElement v : graph.perimeter(SiteType.Vertex))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawOuterVertices())
    	{
			for (final TopologyElement v : graph.outer(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,255,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawInnerVertices())
    	{
			for (final TopologyElement v : graph.inner(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(127,0,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawTopVertices())
    	{
			for (final TopologyElement v : graph.top(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,127,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawBottomVertices())
    	{
			for (final TopologyElement v : graph.bottom(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(0,0,127,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawLeftVertices())
    	{
			for (final TopologyElement v : graph.left(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(255,255,0,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawRightVertices())
    	{
			for (final TopologyElement v : graph.right(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	g2d.setColor(new Color(255,0,255,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	if (bridge.settingsVC().drawCenterVertices())
    	{
			for (final TopologyElement v : graph.centre(SiteType.Vertex))
    		{
    			allGraphElementsToDraw.add(v);
    		}
    	}
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
		if (bridge.settingsVC().drawPhasesVertices())
		{
			g2d.setColor(new Color(0, 255, 0, 125));
			drawPhase(bridge, g2d, context, SiteType.Vertex, containerStyle);
		}

    	allGraphElementsToDraw.clear();
		for (final Entry<DirectionFacing, List<TopologyElement>> entry : graph.sides(SiteType.Vertex).entrySet())
    	{
    		final String DirectionName = entry.getKey().uniqueName().toString();
			if (bridge.settingsVC().drawSideVertices().containsKey(DirectionName)
					&& bridge.settingsVC().drawSideVertices().get(DirectionName).booleanValue())
        	{
    			try
    			{
					for (final TopologyElement v : entry.getValue())
	        		{
	        			allGraphElementsToDraw.add(v);
	        		}
    			}
    			catch (final Exception e)
    			{
    				// carry on
    			}
        	}
    	}
    	g2d.setColor(new Color(255,50,50,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	for (int i = 0; i < bridge.settingsVC().drawColumnsCells().size(); i++)
    	{
    		if (bridge.settingsVC().drawColumnsCells().get(i).booleanValue())
        	{
    			try
    			{
					for (final TopologyElement v : graph.columns(SiteType.Cell).get(i))
            		{
    					allGraphElementsToDraw.add(v);
            		}
    			}
    			catch (final Exception e)
    			{
    				// carry on
    			}
        	}
    	}
    	g2d.setColor(new Color(0,255,255,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	for (int i = 0; i < bridge.settingsVC().drawColumnsVertices().size(); i++)
    	{
    		if (bridge.settingsVC().drawColumnsVertices().get(i).booleanValue())
        	{
    			try
    			{
					for (final TopologyElement v : graph.columns(SiteType.Vertex).get(i))
            		{
    					allGraphElementsToDraw.add(v);
            		}
    			}
    			catch (final Exception e)
    			{
    				// carry on
    			}
        	}
    	}
    	g2d.setColor(new Color(0,255,255,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	for (int i = 0; i < bridge.settingsVC().drawRowsCells().size(); i++)
    	{
    		if (bridge.settingsVC().drawRowsCells().get(i).booleanValue())
        	{
    			try
    			{
					for (final TopologyElement v : graph.rows(SiteType.Cell).get(i))
            		{
    					allGraphElementsToDraw.add(v);
            		}
    			}
    			catch (final Exception e)
    			{
    				// carry on
    			}
        	}
    	}
    	g2d.setColor(new Color(0,255,255,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
    	allGraphElementsToDraw.clear();
    	for (int i = 0; i < bridge.settingsVC().drawRowsVertices().size(); i++)
    	{
    		if (bridge.settingsVC().drawRowsVertices().get(i).booleanValue())
        	{
    			try
    			{
					for (final TopologyElement v : graph.rows(SiteType.Vertex).get(i))
            		{
    					allGraphElementsToDraw.add(v);
            		}
    			}
    			catch (final Exception e)
    			{
    				// carry on
    			}
        	}
    	}
    	g2d.setColor(new Color(0,255,255,125));
    	drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
    	
		// Edges
		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerEdges())
		{
			for (final TopologyElement v : graph.corners(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerConcaveEdges())
		{
			for (final TopologyElement v : graph.cornersConcave(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCornerConvexEdges())
		{
			for (final TopologyElement v : graph.cornersConvex(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawMajorEdges())
		{
			for (final TopologyElement v : graph.major(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawMinorEdges())
		{
			for (final TopologyElement v : graph.minor(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawAxialEdges())
		{
			for (final TopologyElement v : graph.axial(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawHorizontalEdges())
		{
			for (final TopologyElement v : graph.horizontal(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawVerticalEdges())
		{
			for (final TopologyElement v : graph.vertical(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawAngledEdges())
		{
			for (final TopologyElement v : graph.angled(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawSlashEdges())
		{
			for (final TopologyElement v : graph.slash(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawSloshEdges())
		{
			for (final TopologyElement v : graph.slosh(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawPerimeterEdges())
		{
			for (final TopologyElement v : graph.perimeter(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawOuterEdges())
		{
			for (final TopologyElement v : graph.outer(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawInnerEdges())
		{
			for (final TopologyElement v : graph.inner(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(127, 0, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawTopEdges())
		{
			for (final TopologyElement v : graph.top(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 127, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawBottomEdges())
		{
			for (final TopologyElement v : graph.bottom(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(0, 0, 127, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawLeftEdges())
		{
			for (final TopologyElement v : graph.left(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 255, 0, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawRightEdges())
		{
			for (final TopologyElement v : graph.right(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 255, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		allGraphElementsToDraw.clear();
		if (bridge.settingsVC().drawCentreEdges())
		{
			for (final TopologyElement v : graph.centre(SiteType.Edge))
			{
				allGraphElementsToDraw.add(v);
			}
		}
		g2d.setColor(new Color(255, 0, 255, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);

		if (bridge.settingsVC().drawPhasesEdges())
		{
			g2d.setColor(new Color(0, 0, 255, 125));
			drawPhase(bridge, g2d, context, SiteType.Edge, containerStyle);
		}

		allGraphElementsToDraw.clear();
		for (final Entry<DirectionFacing, List<TopologyElement>> entry : graph.sides(SiteType.Edge).entrySet())
		{
			final String DirectionName = entry.getKey().uniqueName().toString();
			if (bridge.settingsVC().drawSideEdges().containsKey(DirectionName)
					&& bridge.settingsVC().drawSideEdges().get(DirectionName).booleanValue())
			{
				try
				{
					for (final TopologyElement c : entry.getValue())
					{
						allGraphElementsToDraw.add(c);
					}
				}
				catch (final Exception e)
				{
					// carry on
				}
			}
		}
		g2d.setColor(new Color(255, 50, 50, 125));
		drawGraphElementList(g2d, allGraphElementsToDraw, containerStyle);
	}
    
    //-------------------------------------------------------------------------
    
    /**
     * Draw a circle at each of the specific vertices in the vertexList.
     */
    private static void drawGraphElementList(final Graphics2D g2d, final List<TopologyElement> graphElementList, final ContainerStyle containerStyle)
	{
		for (int i = 0; i < graphElementList.size(); i++)
		{
			final int circleSize = 20;
			final Point drawPosn = containerStyle.screenPosn(graphElementList.get(i).centroid());
			g2d.drawOval(drawPosn.x - circleSize / 2, drawPosn.y - circleSize / 2, circleSize, circleSize);
		}
	}
    
	//-------------------------------------------------------------------------

	/**
	 * Draws container phases.
	 */
	public static void drawPhase(final Bridge bridge, final Graphics2D g2d, final Context context, final SiteType type,
			final ContainerStyle containerStyle)
	{
		try
		{
			g2d.setFont(bridge.settingsVC().displayFont());
			
			final List<List<TopologyElement>> phases = context.topology().phases(type);
			for (int phase = 0; phase < phases.size(); phase++)
			{
				for (final TopologyElement elementToPrint : phases.get(phase))
				{
					final String str = phase + "";
					final Point drawPosn = containerStyle.screenPosn(elementToPrint.centroid());
					g2d.drawString(str, drawPosn.x, drawPosn.y);
				}
			}
		}
		catch (final Exception E)
		{
			// probably invalid vertexIndex
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws distance from a given site.
	 */
	public static void drawDistance(final Bridge bridge, final Graphics2D g2d, final Context context, final int index,
			final SiteType type, final ContainerStyle containerStyle)
	{
		try
		{
			g2d.setFont(bridge.settingsVC().displayFont());
			
			final TopologyElement element = context.board().topology().getGraphElement(type, index);
			final int[] distance = context.board().topology().distancesToOtherSite(type)[element.index()];
			for(int i = 0; i < distance.length;i++)
			{
				final TopologyElement elementToPrint = context.board().topology().getGraphElement(type, i);
				final String str = distance[i] + "";
				final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(str, g2d);
				final Point drawPosn = containerStyle.screenPosn(elementToPrint.centroid());
				g2d.drawString(str, (int) (drawPosn.x - bounds.getWidth()),
						(int) (drawPosn.y + bounds.getHeight()));
			}
		}
		catch (final Exception E)
		{
			// probably invalid vertexIndex
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws radials from a given graph element.
	 */
	public static void drawRadials(final Bridge bridge, final Graphics2D g2d, final Context context, final int indexElem,
			final ContainerStyle containerStyle, final SiteType type)
	{
		try
		{
			final Topology topology = context.board().topology();
			final List<DirectionFacing> directions = topology.supportedDirections(type);

			for (final DirectionFacing direction : directions)
			{
				final AbsoluteDirection absDirection = direction.toAbsolute();
				final List<Radial> radials = topology.trajectories().radials(type, indexElem, absDirection);

				final String directionString = direction.toString();
				
				g2d.setFont(bridge.settingsVC().displayFont());
				
				g2d.setColor(Color.BLACK);

				for (final Radial radial : radials)
				{
					for (int distance = 1; distance < radial.steps().length; distance++)
					{
						final int indexElementRadial = radial.steps()[distance].id();
						final TopologyElement elementRadial = topology.getGraphElement(type, indexElementRadial);
						final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(directionString + distance,
								g2d);
						final Point drawPosn = containerStyle.screenPosn(elementRadial.centroid());
						g2d.drawString(directionString + distance, (int) (drawPosn.x - bounds.getWidth() / 2),
								(int) (drawPosn.y + bounds.getHeight() / 2));
					}
				}
			}
		}
		catch (final Exception E)
		{
			// probably invalid vertexIndex
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws neighbours of a given vertex index.
	 */
	public static void drawNeighbours(final Graphics2D g2d, final int vertexIndex, final boolean drawCells, final ContainerStyle containerStyle)
	{
		try
		{
			// neighbours
			final List<? extends TopologyElement> adjacentNeighbours;
			final List<? extends TopologyElement> orthogonalNeighbours;
			final List<? extends TopologyElement> secondaryNeighbours;
			final List<? extends TopologyElement> diagonalNeighbours;
			
			if (drawCells)
			{
				final Cell cell = containerStyle.drawnCells().get(vertexIndex);
				adjacentNeighbours = cell.adjacent();
				orthogonalNeighbours = cell.orthogonal();
				secondaryNeighbours = cell.off();
				diagonalNeighbours = cell.diagonal();
			}
			else
			{
				final Vertex vertex = containerStyle.drawnVertices().get(vertexIndex);
				adjacentNeighbours = vertex.adjacent();
				orthogonalNeighbours = vertex.orthogonal();
				secondaryNeighbours = vertex.off();
				diagonalNeighbours = vertex.diagonal();
			}
			
			final int circleSize = containerStyle.cellRadiusPixels();
			
			if (adjacentNeighbours != null)
			{
				for (final TopologyElement v : adjacentNeighbours)
				{
					g2d.setColor(new Color(255,0,0,125));
					final Point drawPosn = containerStyle.screenPosn(v.centroid());
					g2d.fillOval(drawPosn.x - circleSize / 2, drawPosn.y - circleSize / 2, circleSize, circleSize);
				}
			}
			
			if (orthogonalNeighbours != null)
			{
				for (final TopologyElement v : orthogonalNeighbours)
				{
					g2d.setColor(new Color(0,255,0,125));
					final Point drawPosn = containerStyle.screenPosn(v.centroid());
					g2d.fillOval(drawPosn.x - circleSize / 2, drawPosn.y - circleSize / 2, circleSize, circleSize);
				}
			}
			
			if (secondaryNeighbours != null)
			{
				for (final TopologyElement v : secondaryNeighbours)
				{
					g2d.setColor(new Color(0,0,255,125));
					final Point drawPosn = containerStyle.screenPosn(v.centroid());
					g2d.fillOval(drawPosn.x - circleSize / 2, drawPosn.y - circleSize / 2, circleSize, circleSize);
				}
			}
			
			if (diagonalNeighbours != null)
			{
				for (final TopologyElement v : diagonalNeighbours)
				{
					g2d.setColor(new Color(0,255,255,125));
					final Point drawPosn = containerStyle.screenPosn(v.centroid());
					g2d.fillOval(drawPosn.x - circleSize / 2, drawPosn.y - circleSize / 2, circleSize, circleSize);
				}
			}
		}
		catch (final Exception E)
		{
			// probably invalid vertexIndex
		}
	}
	
	//-------------------------------------------------------------------------
}
