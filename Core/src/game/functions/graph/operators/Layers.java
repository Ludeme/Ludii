package game.functions.graph.operators;

import java.util.BitSet;

import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Face;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.Vector;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Makes multiple layers of the specified graph for 3D games.
 * 
 * @author cambolbro
 * 
 * @remarks The layers are stacked upon each one 1 unit apart. Layers will be
 *          shown in isometric view from the side in a future version.
 */
public final class Layers extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	private final int numLayers;
	private final GraphFunction graphFn;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param layers Number of layers.
	 * @param graph  The graph to layer.
	 * 
	 * @example (layers 3 (square 3)))
	 */
	public Layers
	(
		final DimFunction   layers,
		final GraphFunction graph
	) 
	{
		this.numLayers = layers.eval();
		this.graphFn = graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		// Create N copies of graph and merge them into first entry
		final Graph[] graphs = new Graph[numLayers];
		for (int layer = 0; layer < numLayers; layer++)
		{
			graphs[layer] = graphFn.eval(context, siteType);
			
			//graphs[layer].scale(1, 0.5, 1);        // flatten this layer
			//graphs[layer].skew(0.5);               // skew for isometric effect
			graphs[layer].translate(0, 0, layer);  // move this layer N units in z direction
		
			if (layer == 0)
				continue;
			
			// Merge with graph 0
			final int numVerts = graphs[layer].vertices().size();
			final int vertsStartAt = graphs[0].vertices().size();
			//final int edgesStartAt = graphs[0].edges().size();
			//final int facesStartAt = graphs[0].faces().size();

			// Create duplicate vertices in base graph
			for (final Vertex vertex : graphs[layer].vertices())
				graphs[0].addVertex(vertex.pt());
			
			// Link up any pivots in new vertices
			for (final Vertex vertex : graphs[layer].vertices())
				if (vertex.pivot() != null)
				{
					// Link up pivot in new graph
					final Vertex newVertex = graphs[0].vertices().get(vertsStartAt + vertex.id());
					final int newPivotId = vertsStartAt + vertex.pivot().id();
					newVertex.setPivot(graphs[0].vertices().get(newPivotId));
				}

			// Create duplicate edges in base graph
			for (final Edge edge : graphs[layer].edges())
			{
				final int vidA = vertsStartAt + edge.vertexA().id();
				final int vidB = vertsStartAt + edge.vertexB().id();
				
				final Edge newEdge = graphs[0].addEdge(vidA, vidB);
				
				if (edge.tangentA() != null)
					newEdge.setTangentA(new Vector(edge.tangentA()));
				
				if (edge.tangentB() != null)
					newEdge.setTangentB(new Vector(edge.tangentB()));
			}
			
			// Create new vertices between layers
			for (int v = 0; v < numVerts; v++)
			{
				final Vertex vertexA = graphs[0].vertices().get(vertsStartAt - numVerts + v);
				final Vertex vertexB = graphs[0].vertices().get(vertsStartAt + v);
				graphs[0].addEdge(vertexA, vertexB);
			}
			
			// Create new faces
			for (final Face face : graphs[layer].faces())
			{
				final int[] vids = new int[face.vertices().size()];
				for (int n = 0; n < face.vertices().size(); n++)
					vids[n] = vertsStartAt + face.vertices().get(n).id();
				graphs[0].findOrAddFace(vids);
			}
		}

		graphs[0].reorder();

		//System.out.println(graph);

		return graphs[0];
	}

	//-------------------------------------------------------------------------

//	/**
//	 * Merge vertex vv into vertex v.
//	 */
//	void mergeVertices(final List<Vertex> vertices, final List<Edge> edges)
//	{
//		for (int v = 0; v < vertices.size(); v++)
//		{
//			final Vertex base = vertices.get(v);
//			
//			for (int vv = vertices.size()-1; vv > v; vv--)
//			{
//				final Vertex other = vertices.get(vv);
//				if (base.coincident(other, tolerance))
//					mergeVertices(vertices, edges, v, vv);
//			}
//		}
//	}
//	
//	/**
//	 * Merge vertex vv into vertex v.
//	 */
//	@SuppressWarnings("static-method")
//	void mergeVertices
//	(
//		final List<Vertex> vertices, final List<Edge> edges, 
//		final int vid, final int coincidentId
//	)
//	{
//		final Vertex survivor = vertices.get(vid);
//		
//		// Redirect all references to coincident vertex as pivot
//		for (final Vertex vertex : vertices)
//			if (vertex.pivot() != null && vertex.pivot().id() == coincidentId)
//				vertex.setPivot(survivor);  // redirect to surviving vertex
//	
//		// Redirect edge references to coincident to vertex
//		for (final Edge edge : edges)
//		{
//			if (edge.vertexA().id() == coincidentId)
//				edge.setVertexA(survivor);
//			if (edge.vertexB().id() == coincidentId)
//				edge.setVertexB(survivor);
//		}
//		
//		// Renumber vertices above coincidentId and remove coincident vertex
//		for (int n = coincidentId + 1; n < vertices.size(); n++)
//		{
//			final Vertex vertexN = vertices.get(n);
//			vertexN.setId(vertexN.id() - 1);
//		}	
//		
//		vertices.remove(coincidentId);
//	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return graphFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		
		flags |= graphFn.gameFlags(game);

		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
//		type = SiteType.use(type, game);

		graphFn.preprocess(game);

		if (isStatic())
			precomputedGraph = eval(new Context(game, null),
					(game.board().defaultSite() == SiteType.Vertex ? SiteType.Vertex : SiteType.Cell));
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(graphFn.concepts(game));
		return concepts;
	}

}
