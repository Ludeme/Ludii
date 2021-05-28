package util;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.other.Regions;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.location.Location;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Functions that assist with container graphics.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class ContainerUtil 
{
	/**
	 * Get the container site that a specified location is on.
	 */
	public static int getContainerSite(final Context context, final int site, final SiteType graphElementType)
	{
		if (site == Constants.UNDEFINED)
			return Constants.UNDEFINED;
		
		if (graphElementType == SiteType.Cell)
		{
			final int contianerId = getContainerId(context, site, graphElementType);
			final int containerSite = site - context.sitesFrom()[contianerId];
			return containerSite;
		}
		
		return site;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get the container index that a specified location is on.
	 */
	public static int getContainerId(final Context context, final int site, final SiteType graphElementType)
	{
		if (site == Constants.UNDEFINED)
			return Constants.UNDEFINED;
		
		// vertices and edges are only on board!
		if (graphElementType != SiteType.Cell)
			return context.board().index();

		return context.containerId()[site];
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Normalise the graph to fill the world space.
	 */
	public static void normaliseGraphElements(final Topology graph) 
	{
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
				
		for (int i = 0; i < graph.vertices().size(); i++)
		{
			final Point2D centroid = graph.vertices().get(i).centroid();
			
			final double cx = centroid.getX();
			final double cy = centroid.getY();
			
			if (cx < minX)
				minX = cx;
			if (cy < minY)
				minY = cy;
			
			if (cx > maxX)
				maxX = cx;
			if (cy > maxY)
				maxY = cy;
		}
		
		// Choose smallest range to normalise on
		double min = minX;
		double max = maxX;
		
		if (maxX - minX < maxY - minY)
		{
			min = minY;
			max = maxY;
		}
		
		// Normalise elements
		normaliseGraphElements((ArrayList<? extends TopologyElement>)graph.vertices(), min, max);
		normaliseGraphElements((ArrayList<? extends TopologyElement>)graph.edges(), min, max);
		normaliseGraphElements((ArrayList<? extends TopologyElement>)graph.cells(), min, max);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Normalise graph elements to the provided range.
	 */
	private static void normaliseGraphElements
	(
		final ArrayList<? extends TopologyElement> graphElements, final double min, final double max
	) 
	{
		for (int i = 0; i < graphElements.size(); i++)
		{
			final double oldX = graphElements.get(i).centroid().getX();
			final double oldY = graphElements.get(i).centroid().getY();
			final double newX = (oldX - min) / (max - min);
			final double newY = (oldY - min) / (max - min);
			graphElements.get(i).setCentroid(newX, newY, 0);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Center the graph within the world space.
	 */
	public static void centerGraphElements(final Topology graph)
	{
		double minX = 9999999;
		double minY = 99999999;
		double maxX = -99999999;
		double maxY = -99999999;
		for (int i = 0; i < graph.vertices().size(); i++)
		{
			if (graph.vertices().get(i).centroid().getX() < minX)
				minX = graph.vertices().get(i).centroid().getX();
			if (graph.vertices().get(i).centroid().getY() < minY)
				minY = graph.vertices().get(i).centroid().getY();
			if (graph.vertices().get(i).centroid().getX() > maxX)
				maxX = graph.vertices().get(i).centroid().getX();
			if (graph.vertices().get(i).centroid().getY() > maxY)
				maxY = graph.vertices().get(i).centroid().getY();
		}
		
		centerGraphElementsBetween((ArrayList<? extends TopologyElement>) graph.vertices(), minX, maxX, minY, maxY);
		centerGraphElementsBetween((ArrayList<? extends TopologyElement>) graph.edges(), minX, maxX, minY, maxY);
		centerGraphElementsBetween((ArrayList<? extends TopologyElement>) graph.cells(), minX, maxX, minY, maxY);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Centers the provided list of graph elements between the minimum and maximum values along both dimensions.
	 */
	private static void centerGraphElementsBetween(final ArrayList<? extends TopologyElement> graphElements, final double minX, final double maxX, final double minY, final double maxY) 
	{
		final double currentMidX = (maxX + minX) / 2;
		final double currentMidY = (maxY + minY) / 2;
		final double differenceX = currentMidX - 0.5;
		final double differenceY = currentMidY - 0.5;

		for (int i = 0; i < graphElements.size(); i++)
		{
			final double oldX = graphElements.get(i).centroid().getX();
			final double oldY = graphElements.get(i).centroid().getY();
			final double newX = oldX - differenceX;
			final double newY = oldY - differenceY;
			graphElements.get(i).setCentroid(newX, newY, 0);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Cell indices of a container that a component is covering.
	 */
	public static ArrayList<Integer> cellsCoveredByPiece(final Context context, final Container container, final Component component, final int site, final int localState)
	{
		final ArrayList<Integer> cellsCoveredByPiece = new ArrayList<>();
		if (component.isLargePiece())
		{
			final TIntArrayList largePieceSites = component.locs(context,site,localState,container.topology());
			for (int i = 0; i < largePieceSites.size(); i++)
			{
				cellsCoveredByPiece.add(Integer.valueOf(container.topology().cells().get(largePieceSites.get(i)).index()));
			}
		}
		else
		{
			cellsCoveredByPiece.add(Integer.valueOf(site));
		}
		return cellsCoveredByPiece;
	}
	
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return A region from equipment that this edge belongs to.
	 */
	public static Regions getRegionOfEdge(final Context context, final Edge e)
	{
		for (final Regions region : context.game().equipment().regions())
			for (final int site : region.eval(context))
				for (final Edge edge : context.board().topology().cells().get(site).edges())
					if (edge.index() == e.index())
						return region;

		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Edges of cellIndex and are on the perimeter of surroundedRegions.
	 */
	public static List<Edge> getOuterRegionEdges(final List<Location> region, final Topology topology) 
	{
		final ArrayList<Edge> regionLines = new ArrayList<>();
		final ArrayList<Edge> outsideRegionLines = new ArrayList<>();
		
		for (final Location loctation : region)
			for (final Edge e : topology.getGraphElement(SiteType.Cell, loctation.site()).regionEdges())
				regionLines.add(e);
		
		for (final Edge edge1 : regionLines)
		{
			int numContains = 0;
			for (final Edge edge2 : regionLines)
			{
				if
				(
					Math.abs(edge1.vA().centroid().getX() - edge2.vA().centroid().getX()) < 0.0001
					&&
					Math.abs(edge1.vB().centroid().getX() - edge2.vB().centroid().getX()) < 0.0001
					&&
					Math.abs(edge1.vA().centroid().getY() - edge2.vA().centroid().getY()) < 0.0001
					&&
					Math.abs(edge1.vB().centroid().getY() - edge2.vB().centroid().getY()) < 0.0001
				)
				{
					numContains++;
				}
				else if
				(
					Math.abs(edge1.vA().centroid().getX() - edge2.vB().centroid().getX()) < 0.0001
					&&
					Math.abs(edge1.vB().centroid().getX() - edge2.vA().centroid().getX()) < 0.0001
					&&
					Math.abs(edge1.vA().centroid().getY() - edge2.vB().centroid().getY()) < 0.0001
					&&
					Math.abs(edge1.vB().centroid().getY() - edge2.vA().centroid().getY()) < 0.0001
				)
				{
					numContains++;
				}
			}
			if (numContains == 1)
			{
				outsideRegionLines.add(edge1);
			}
		}
		
		return outsideRegionLines;
	}
	
	//-------------------------------------------------------------------------
	
}
