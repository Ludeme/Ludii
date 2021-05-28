package game.functions.graph.generators.basis.mesh;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.GraphElement;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import main.math.Polygon;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a graph from a triangular mesh.
 * 
 * @author cambolbro
 */
@Hide
public class CustomOnMesh extends Basis
{
	private static final long serialVersionUID = 1L;

	private final Integer numVertices;
	private final Polygon polygon = new Polygon();
	private final List<Point2D> points;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Mesh.
	 * 
	 * @param numVertices
	 * @param polygon
	 * @param points
	 */
	public CustomOnMesh
	(
		final DimFunction   numVertices,
		final Polygon		polygon,
		final List<Point2D> points
	)
	{
		this.basis = BasisType.Mesh;
		this.shape  = ShapeType.Custom;

		this.numVertices = Integer.valueOf(numVertices.eval());
		
		this.points = points;
		
		this.polygon.setFrom(polygon);
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final Graph graph = new Graph();
		
		if (numVertices == null)
		{
			// Just use the points directly as vertices
			for (final Point2D pt : points)
				insertVertex(graph, pt);
		}
		else
		{
			// Fill the shape with N randomly generated vertices	
			final Random rng = new Random();
			
			polygon.inflate(0.1);
			final Rectangle2D bounds = polygon.bounds();
			
			for (int n = 0; n < numVertices.intValue(); n++)
			{
				int iterations = 0;
				Point2D pt;
				do
				{
					if (++iterations > 1000)
						throw new RuntimeException("Couldn't place point in mesh shape.");
					
					final double x = bounds.getMinX() + rng.nextDouble() * bounds.getWidth();
					final double y = bounds.getMinY() + rng.nextDouble() * bounds.getHeight();
					
					pt = new Point2D.Double(x, y);
				} while (!polygon.contains(pt));
				
				insertVertex(graph, pt);
			}		
		}
		
		graph.makeFaces(true);
		
		graph.setBasisAndShape(basis, shape);
		//graph.reorder();

		return graph;
	}

	//-------------------------------------------------------------------------

	static void insertVertex(final Graph graph, final Point2D pt)
	{
		final List<? extends GraphElement> vertices = graph.elements(SiteType.Vertex);
		
		if (vertices.isEmpty())
		{
			// Add first vertex
			graph.addVertex(pt);
			return;
		}
		else if (vertices.size() == 1)
		{
			// Add second vertex
			graph.addVertex(pt);
			graph.findOrAddEdge(0, 1);
			return;
		}
		else if (vertices.size() == 2)
		{
			// Add third vertex
			graph.addVertex(pt);
			graph.findOrAddEdge(0, 2);
			graph.findOrAddEdge(1, 2);
			return;
		}
		
		// Add vertex in inscribed triangle, if any
        for (int i = 0; i < vertices.size(); i++)
		{
			final Vertex vertexI = (Vertex)vertices.get(i);
            
			for (int j = i + 1; j < vertices.size(); j++) 
            {
            	if (i == j)
            		continue;
            
            	final Vertex vertexJ = (Vertex)vertices.get(j);
            	
                for (int k = j + 1; k < vertices.size(); k++) 
                {
                	if (i == k || j == k) 
                		continue;
                
                	final Vertex vertexK = (Vertex)vertices.get(k);
            
                	if (MathRoutines.pointInTriangle(pt, vertexI.pt2D(), vertexJ.pt2D(), vertexK.pt2D()))
                	{
                		// Point lies within the triangle formed by I, J, K
                		insertVertex(graph, pt, vertexI, vertexJ, vertexK);
       					return;
                	}
                }
            }
		}
        
        // Point must be outside current mesh, add to two nearest vertices
        
        // **
        // ** TODO: This could allow edge crossings.
        // **       Do not include vertices that would cause an edge crossing.
        // **
        
        double bestDistance     = 1000000;
        double nextBestDistance = 1000000;
        
        GraphElement bestVertex     = null;
        GraphElement nextBestVertex = null;
        
        for (final GraphElement vertex : vertices)
        {
        	final double dist = MathRoutines.distance(pt,  vertex.pt2D());
        	
//        	if (dist < bestDistance || dist < nextBestDistance)
//        	{
//        		if (would cause edge crossing)
//        			continue;
//        	}
        	
        	if (dist < bestDistance)
        	{
        		nextBestDistance = bestDistance;
        		nextBestVertex   = bestVertex;
 
        		bestDistance = dist;
        		bestVertex   = vertex;
        	}
        	else if (dist < nextBestDistance)
        	{
        		nextBestDistance = dist;
        		nextBestVertex = vertex;
        	}
        }
        
        final Vertex vertex = graph.addVertex(pt);
       	graph.findOrAddEdge(vertex.id(), bestVertex.id());
       	graph.findOrAddEdge(vertex.id(), nextBestVertex.id());
 	}
	
	//-------------------------------------------------------------------------
	
	static void insertVertex
	(
		final Graph graph, final Point2D pt, 
		final Vertex vertexI, final Vertex vertexJ, final Vertex vertexK
	)
	{
		final Vertex vertex = graph.addVertex(pt);
	
		// if near to existing edge, delete edge but relink affected vertex
		
		graph.findOrAddEdge(vertex.id(), vertexI.id());
		graph.findOrAddEdge(vertex.id(), vertexJ.id());
		graph.findOrAddEdge(vertex.id(), vertexK.id());
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public long gameFlags(Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(Game game)
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

}
