package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import game.types.board.SiteType;
import game.util.graph.Properties;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.topology.Vertex;
import view.container.BaseContainerStyle;

/**
 * Functions relating to Graphs.
 * 
 * @author Matthew.Stephenson
 */
public class GraphUtil 
{
	/**
	 * Returns a list of orthogonal cell connections within a given topology.
	 */
	public static List<Edge> orthogonalCellConnections(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Cell va : topology.cells())
		{
			final Point2D drawPosnA = va.centroid();
			for (final Cell vb : va.orthogonal())
			{
				if (vb.index() > va.index())
				{
					final Point2D drawPosnB = vb.centroid();
					connections.add(new Edge(new Vertex(-1, drawPosnA.getX(), drawPosnA.getY(), 0),
							new Vertex(-1, drawPosnB.getX(), drawPosnB.getY(), 0)));
				}
			}
		}
		return connections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of diagonal cell connections within a given topology.
	 */
	public static List<Edge> diagonalCellConnections(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Cell va : topology.cells())
		{
			final Point2D drawPosnA = va.centroid();
			for (final Cell vb : va.diagonal())
			{
				if (vb.index() > va.index())
				{
					final Point2D drawPosnB = vb.centroid();
					connections.add(new Edge(new Vertex(-1, drawPosnA.getX(), drawPosnA.getY(), 0),
							new Vertex(-1, drawPosnB.getX(), drawPosnB.getY(), 0)));
				}
			}
		}
		return connections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of off-diagonal cell connections within a given topology.
	 */
	public static List<Edge> offCellConnections(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Cell va : topology.cells())
		{
			final Point2D drawPosnA = va.centroid();
			for (final Cell vb : va.off())
			{
				if (vb.index() > va.index())
				{
					final Point2D drawPosnB = vb.centroid();
					connections.add(new Edge(new Vertex(-1, drawPosnA.getX(), drawPosnA.getY(), 0),
							new Vertex(-1, drawPosnB.getX(), drawPosnB.getY(), 0)));
				}
			}
		}
		return connections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of orthogonal edges within a given topology.
	 */
	public static List<Edge> orthogonalEdgeRelations(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Vertex va : topology.vertices())
		{
			final Point2D drawPosnA = va.centroid();
			for (final Vertex vb : va.orthogonal())
			{
				if (vb.index() > va.index())
				{
					final Point2D drawPosnB = vb.centroid();
					connections.add(new Edge(new Vertex(-1, drawPosnA.getX(), drawPosnA.getY(), 0),
							new Vertex(-1, drawPosnB.getX(), drawPosnB.getY(), 0)));
				}
			}
		}
		return connections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of diagonal edges within a given topology.
	 */
	public static List<Edge> diagonalEdgeRelations(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Vertex va : topology.vertices())
		{
			final Point2D drawPosnA = va.centroid();
			for (final Vertex vb : va.diagonal())
			{
				if (vb.index() > va.index())
				{
					final Point2D drawPosnB = vb.centroid();
					connections.add(new Edge(new Vertex(va.index(), drawPosnA.getX(), drawPosnA.getY(), 0),
							new Vertex(vb.index(), drawPosnB.getX(), drawPosnB.getY(), 0)));
				}
			}
		}
		return connections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of inner edges within a given topology.
	 */
	public static List<Edge> innerEdgeRelations(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Edge edge : topology.edges())
			if (!edge.properties().get(Properties.OUTER))
				connections.add(edge);
		
		return connections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of outer edges within a given topology.
	 */
	public static List<Edge> outerEdgeRelations(final Topology topology)
	{
		final List<Edge> connections = new ArrayList<>();
		for (final Edge edge : topology.edges())
			if (edge.properties().get(Properties.OUTER))
				connections.add(edge);
		
		return connections;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Creates an SVG graph image for a given container style.
	 */
	public static String createSVGGraphImage(final BaseContainerStyle boardStyle)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		g2d.setBackground(new Color(0, 0, 0, 0));

		g2d.setColor(new Color(120, 120, 120));

		final Stroke solid = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		final Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{10}, 0);
		final Stroke dotted = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{3}, 0);
		
		g2d.setStroke(solid);
		for (final Edge e : boardStyle.drawnEdges())
		{
			final Point drawPosnA = boardStyle.screenPosn(e.vA().centroid());
			final Point drawPosnB = boardStyle.screenPosn(e.vB().centroid());
			final java.awt.Shape line = new Line2D.Double(drawPosnA.x, drawPosnA.y, drawPosnB.x, drawPosnB.y);
			g2d.draw(line);
		}
		
		g2d.setStroke(dashed);
		for (final Vertex vA : boardStyle.drawnVertices())
		{
			for (final Vertex vB : vA.diagonal())
			{
				if (vA.index() > vB.index())
				{
					final Point drawPosnA = boardStyle.screenPosn(vA.centroid());
					final Point drawPosnB = boardStyle.screenPosn(vB.centroid());
					final java.awt.Shape line = new Line2D.Double(drawPosnA.x, drawPosnA.y, drawPosnB.x, drawPosnB.y);
					g2d.draw(line);
				}
			}
		}
		
		g2d.setStroke(dotted);
		for (final Vertex vA : boardStyle.drawnVertices())
		{
			for (final Vertex vB : vA.off())
			{
				if (vA.index() > vB.index())
				{		
					final Point drawPosnA = boardStyle.screenPosn(vA.centroid());
					final Point drawPosnB = boardStyle.screenPosn(vB.centroid());
					final java.awt.Shape line = new Line2D.Double(drawPosnA.x, drawPosnA.y, drawPosnB.x, drawPosnB.y);
					g2d.draw(line);
				}
			}
		}

		g2d.setStroke(solid);
		for (final Vertex va : boardStyle.drawnVertices())
		{
			// Draw vertices
			final int r = 4;
			final Point drawPosn = boardStyle.screenPosn(va.centroid());
			g2d.fillArc(drawPosn.x - r, drawPosn.y - r, 2 * r + 1, 2 * r + 1, 0, 360);
		}
		
		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Creates an SVG connections image for a given container style.
	 */
	public static String createSVGConnectionsImage(final BaseContainerStyle boardStyle)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		g2d.setBackground(new Color(0, 0, 0, 0));

		g2d.setColor(new Color(127, 127, 255));

		final Stroke solid = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		final Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{10}, 0);
		final Stroke dotted = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{3}, 0);
		
		g2d.setStroke(solid);
		for (final Cell vA : boardStyle.drawnCells())
		{
			if (vA.centroid().getX() < 0 || vA.centroid().getY() < 0)
				continue;
			
			for (final Cell vB : vA.orthogonal())
			{
				final Cell vBDrawn = boardStyle.drawnCells().get(vB.index());
				
				if (vBDrawn.centroid().getX() < 0 || vBDrawn.centroid().getY() < 0)
					continue;
				
				if (vA.index() > vBDrawn.index())
				{
					final Point drawPosnA = boardStyle.screenPosn(vA.centroid());
					final Point drawPosnB = boardStyle.screenPosn(vBDrawn.centroid());
					final java.awt.Shape line = new Line2D.Double(drawPosnA.x, drawPosnA.y, drawPosnB.x, drawPosnB.y);
					g2d.draw(line);
				}
			}
		}
		
		g2d.setStroke(dashed);
		for (final Cell vA : boardStyle.drawnCells())
		{
			if (vA.centroid().getX() < 0 || vA.centroid().getY() < 0)
				continue;
			
			for (final Cell vB : vA.diagonal())
			{
				final Cell vBDrawn = boardStyle.drawnCells().get(vB.index());
				
				if (vBDrawn.centroid().getX() < 0 || vBDrawn.centroid().getY() < 0)
					continue;
				
				if (vA.index() > vBDrawn.index())
				{
					final Point drawPosnA = boardStyle.screenPosn(vA.centroid());
					final Point drawPosnB = boardStyle.screenPosn(vBDrawn.centroid());
					final java.awt.Shape line = new Line2D.Double(drawPosnA.x, drawPosnA.y, drawPosnB.x, drawPosnB.y);
					g2d.draw(line);
				}
			}
		}
		
		g2d.setStroke(dotted);
		for (final Cell vA : boardStyle.drawnCells())
		{
			if (vA.centroid().getX() < 0 || vA.centroid().getY() < 0)
				continue;
			
			for (final Cell vB : vA.off())
			{
				final Cell vBDrawn = boardStyle.drawnCells().get(vB.index());
				
				if (vBDrawn.centroid().getX() < 0 || vBDrawn.centroid().getY() < 0)
					continue;
				
				if (vA.index() > vBDrawn.index())
				{		
					final Point drawPosnA = boardStyle.screenPosn(vA.centroid());
					final Point drawPosnB = boardStyle.screenPosn(vBDrawn.centroid());
					final java.awt.Shape line = new Line2D.Double(drawPosnA.x, drawPosnA.y, drawPosnB.x, drawPosnB.y);
					g2d.draw(line);
				}
			}
		}

		g2d.setStroke(solid);
		for (final Cell vA : boardStyle.drawnCells())
		{
			if (vA.centroid().getX() < 0 || vA.centroid().getY() < 0)
				continue;
			
			// Draw vertices
			final int r = 4;
			final Point drawPosn = boardStyle.screenPosn(vA.centroid());
			g2d.fillArc(drawPosn.x - r, drawPosn.y - r, 2 * r + 1, 2 * r + 1, 0, 360);
		}

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Reorders the graph elements of a give topology to be top-down, based on their Y-position and layer value (z-position).
	 */
	public static List<TopologyElement> reorderGraphElementsTopDown(final List<TopologyElement> allGraphElements, final Context context)
	{
		// Reorder the elements based on their Y position.
		if (context.game().isStacking())
		{
			Collections.sort(allGraphElements, new Comparator<TopologyElement>() 
			{
			    @Override
			    public int compare(final TopologyElement o1, final TopologyElement o2) 
			    {
			    	final Double obj1 = Double.valueOf(o1.centroid().getY());
			        final Double obj2 = Double.valueOf(o2.centroid().getY());
			        return obj1.compareTo(obj2);
			    }
			});
			Collections.reverse(allGraphElements);
		}
		
		// Reorder the elements based on their layer value.
		else if (context.board().topology().layers(SiteType.Vertex).size() > 1)
		{
			Collections.sort(allGraphElements, new Comparator<TopologyElement>() 
			{
			    @Override
			    public int compare(final TopologyElement o1, final TopologyElement o2) 
			    {
			    	final Double obj1 = Double.valueOf(o1.layer());
			        final Double obj2 = Double.valueOf(o2.layer());
			        return obj1.compareTo(obj2);
			    }
			});
		}
		return allGraphElements;
	}
	
	
	//-------------------------------------------------------------------------
	
	/**
	 * Calculates the radius of a given cell.
	 */
	public static double calculateCellRadius(final Cell cell) 
	{
		double acc = 0;
		if (cell.edges().size() > 0)
		{
			for (final Edge edge : cell.edges())
			{
				final Point2D midpoint = edge.centroid();
				final double dx = midpoint.getX() - cell.centroid().getX();
				final double dy = midpoint.getY() - cell.centroid().getY();
				final double dist = Math.sqrt(dx * dx + dy * dy);
				acc += dist;
			}
			acc /= cell.edges().size();
		}
		return acc;
	}
	
	//-------------------------------------------------------------------------
	
}
