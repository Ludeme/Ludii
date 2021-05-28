package game.functions.graph.operators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Face;
import game.util.graph.Graph;
import game.util.graph.Poly;
import game.util.graph.Vertex;
import main.math.Polygon;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Removes elements from a graph.
 * 
 * @author cambolbro
 * 
 * @remarks The elements to be removed can be vertices, edges or faces.
 *          Elements whose vertices can't be found will be ignored.
 *          Be careful when removing by index, as the graph is modified and 
 *          renumbered with each removal. It is recommended to specify indices 
 *          in decreasing order and to avoid removing vertices, edges and/or 
 *          faces by index on the same call (instead, you can chain multiple
 *          removals by index together, one for each element type). 
 */
public final class Remove extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final GraphFunction graphFn;

	private final Polygon polygon;
	
	private final Float[][][] facePositions;
	private final Float[][][] edgePositions;
	private final Float[][]   vertexPositions;

	private final DimFunction[]   faceIndices;
	private final DimFunction[][] edgeIndices;
	private final DimFunction[]   vertexIndices;
	
	private final boolean trimEdges;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * For removing some graph elements.
	 * 
	 * @param graph     The graph to remove elements from.
	 * @param cells     Locations of vertices of faces to remove.
	 * @param Cells     Indices of faces to remove.
	 * @param edges     Locations of end points of edges to remove.
	 * @param Edges     Indices of end points of edges to remove.
	 * @param vertices  Locations of vertices to remove.
	 * @param Vertices  Indices of vertices to remove.
	 * @param trimEdges Whether to trim edges orphaned by removing faces [True].
	 * 
	 * @example (remove (square 4) vertices:{ { 0.0 3.0 } { 0.5 2 } })
	 * @example (remove (square 4) cells:{ 0 1 2 } edges:{ {0 1} {1 2} } vertices:{
	 *          1 4 })
	 */
	public Remove
	(
						final GraphFunction graph,
		@Opt @Or  @Name final Float[][][] 	cells,
		@Opt @Or  @Name final DimFunction[]     Cells,
		@Opt @Or2 @Name final Float[][][]   edges,
		@Opt @Or2 @Name final DimFunction[][]   Edges,
		@Opt @Or  @Name final Float[][]     vertices,
		@Opt @Or  @Name final DimFunction[]     Vertices,
		@Opt      @Name final Boolean		trimEdges
	)
	{
		int numNonNullF = 0;
		if (cells != null)
			numNonNullF++;
		if (Cells != null)
			numNonNullF++;
		if (numNonNullF > 1)
			throw new IllegalArgumentException("Only one 'cell' parameter can be non-null.");
		
		int numNonNullE = 0;
		if (edges != null)
			numNonNullE++;
		if (Edges != null)
			numNonNullE++;
		if (numNonNullE > 1)
			throw new IllegalArgumentException("Only one 'edge' parameter can be non-null.");

		int numNonNullV = 0;
		if (vertices != null)
			numNonNullV++;
		if (Vertices != null)
			numNonNullV++;
		if (numNonNullV > 1)
			throw new IllegalArgumentException("Only one 'vertex' parameter can be non-null.");

		this.graphFn = graph;
		
		this.polygon = null;
			
		this.facePositions   = cells;
		this.edgePositions   = edges;
		this.vertexPositions = vertices;
			
		this.faceIndices   = Cells;
		this.edgeIndices   = Edges;
		this.vertexIndices = Vertices;
		
		this.trimEdges = (trimEdges == null) ? true : trimEdges.booleanValue();
	}

	/**
	 * For removing some elements according to a polygon.
	 * 
	 * @param graphFn   Graph to clip.
	 * @param poly      Float points defining hole region.
	 * @param trimEdges Whether to trim edges orphaned by removing faces [True].
	 * 
	 * @example (remove (square 8) (poly { { 2.5 2.5 } { 2.5 5.5 } { 4.5 5.5 } { 4.5
	 *          4.5 } {5.5 4.5 } { 5.5 2.5 } }))
	 */
	public Remove
	(
				   final GraphFunction graphFn,
				   final Poly     	   poly,
		@Opt @Name final Boolean	   trimEdges
	) 
	{
		this.graphFn = graphFn;
		
		this.polygon = poly.polygon();
		
		this.facePositions   = null;
		this.edgePositions   = null;
		this.vertexPositions = null;
			
		this.faceIndices     = null;
		this.edgeIndices     = null;
		this.vertexIndices   = null;
						
		this.trimEdges = (trimEdges == null) ? true : trimEdges.booleanValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph graph = graphFn.eval(context, siteType);
				
		// Note: Retain the tiling and shape of the original elements.
		
		if (polygon != null)
		{
			for (int vid = graph.vertices().size() - 1; vid >= 0; vid--)
			{
				final Vertex vertex = graph.vertices().get(vid);
				if (polygon.contains(vertex.pt2D()))
					graph.removeVertex(vertex);
			}	
				
//			// Check intersecting edges?
//			for (int eid = graph.edges().size() - 1; eid >= 0; eid--)
//			{
//				final Edge edge = graph.edges().get(eid);
//				if (polygon.contains(edge.vertexA().pt2D()) ||polygon.contains(edge.vertexB().pt2D()))
//					graph.remove(edge);
//			}	
			
			// Check intersecting faces?
			// ...
		}
		
		// Remove faces
		if (facePositions != null)
		{
			for (final Float[][] pts : facePositions)
			{
				final int[] vertIds = new int[pts.length];
							
				for (int n = 0; n < pts.length; n++)
				{
					if (pts[n].length < 2)
					{
						System.out.println("** Remove: Two values expected for vertex.");
						continue;
					}

					// Find vertex
					final double x = pts[n][0].floatValue();
					final double y = pts[n][1].floatValue();
					final double z = (pts[n].length > 2) ? pts[n][2].floatValue() : 0;
				
					final Vertex vertex = graph.findVertex(x, y, z);	
					if (vertex == null)
					{
						System.out.println("** Couldn't find face vertex.");
						vertIds[n] = -1;  // make sure an obvious error occurs
					}
					else
					{
						vertIds[n] = vertex.id();
					}
				}
				
				final Face face = graph.findFace(vertIds);
				if (face != null)
					graph.remove(face, trimEdges);
				else
					System.out.println("** Face not found from vertices.");
			}
		}
		else if (faceIndices != null)
		{
			final List<Integer> list = new ArrayList<Integer>();
			for (final DimFunction id : faceIndices)
				list.add(Integer.valueOf(id.eval()));
			Collections.sort(list);
			Collections.reverse(list);
			
			for (final Integer id : list)
				graph.removeFace(id.intValue(), trimEdges);
		}
		
		// Remove edges
		if (edgePositions != null)
		{
			for (final Float[][] pts : edgePositions)
			{
				final double ax = pts[0][0].floatValue();
				final double ay = pts[0][1].floatValue();
				final double az = (pts[0].length > 2) ? pts[0][2].floatValue() : 0;
				
				final double bx = pts[1][0].floatValue();
				final double by = pts[1][1].floatValue();
				final double bz = (pts[1].length > 2) ? pts[1][2].floatValue() : 0;
				
				final Vertex vertexA = graph.findVertex(ax, ay, az);
				final Vertex vertexB = graph.findVertex(bx, by, bz);
			
				if (vertexA != null && vertexB != null)
				{
					final Edge edge = graph.findEdge(vertexA,  vertexB);
					if (edge != null)
						graph.remove(edge, false);
					else
						System.out.println("** Edge vertices not found.");
				}
				else
				{
					System.out.println("** Edge vertices not found.");
				}
			}
		}
		else if (this.edgeIndices != null)
		{
			for (final DimFunction[] vids : edgeIndices)
			{
				if (vids.length == 2)
					graph.removeEdge(vids[0].eval(), vids[1].eval());
			}
		}
		
		// Remove vertices
		if (vertexPositions != null)
		{
			for (final Float[] pt : vertexPositions)
			{
				final double x = pt[0].floatValue();
				final double y = pt[1].floatValue();
				final double z = (pt.length > 2) ? pt[2].floatValue() : 0;
				
				final Vertex vertex = graph.findVertex(x, y, z);
				if (vertex != null)
					graph.removeVertex(vertex);
			}
		}
		else if (vertexIndices != null)
		{
			final List<Integer> list = new ArrayList<Integer>();
			for (final DimFunction id : vertexIndices)
				list.add(Integer.valueOf(id.eval()));
			Collections.sort(list);
			Collections.reverse(list);
			
			for (final Integer id : list)
				graph.removeVertex(id.intValue());
		}

		// **
		// ** Do not create faces! That would just restore any deleted faces.
		// **
		//if (siteType == SiteType.Cell)
		//	graph.createFaces();
		
		// **
		// ** Don't reorder, in case ordering matters for other parts of the board.
		// ** The Graph decrements indices to cater for gaps left by removed elements.
		// **
		//graph.reorder();
				
		graph.resetShape();
		
		//System.out.println(graph);
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		final boolean isStatic = true;
		return isStatic;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long flags = 0;
		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		if (isStatic())
			precomputedGraph = eval(new Context(game, null),
					(game.board().defaultSite() == SiteType.Vertex ? SiteType.Vertex : SiteType.Cell));
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		// Commented because if some modifications are done to the graph we can not
		// conclude about the tiling
		// concepts.or(graphFn.concepts(game));
		return concepts;
	}
}
