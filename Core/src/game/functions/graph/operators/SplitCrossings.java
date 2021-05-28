package game.functions.graph.operators;

import java.awt.geom.Point2D;
import java.util.BitSet;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import main.math.Point3D;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Splits edge crossings within a graph to create a new vertex at each crossing point.
 * 
 * @author cambolbro
 */
public final class SplitCrossings extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

    private final GraphFunction graphFn;

	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param graph The graph to split edge crossings.
	 * 
	 * @example (splitCrossings (merge (rectangle 2 5) (square 5)))
	 */
	public SplitCrossings
	(
	     final GraphFunction graph
	) 
	{
		this.graphFn = graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph graph = graphFn.eval(context, siteType);

//		System.out.println("\n" + graph);
		
//		boolean didSplit;
//		do
//		{
//			didSplit = false;
//			for (int ea = 0; ea < graph.edges().size() && !didSplit; ea++)
//			{	
//				final Edge edgeA = graph.edges().get(ea);
//				final Point3D ptAA = edgeA.vertexA().pt();
//				final Point3D ptAB = edgeA.vertexB().pt();
//				
//				Vertex pivot = null;
//				if (edgeA.vertexA().pivot() != null)
//					pivot = edgeA.vertexA().pivot();
//				if (edgeA.vertexB().pivot() != null)
//					pivot = edgeA.vertexB().pivot();
//				
//				for (int eb = ea + 1; eb < graph.edges().size() && !didSplit; eb++)
//				{
//					final Edge edgeB = graph.edges().get(eb);
//					final Point3D ptBA = edgeB.vertexA().pt();
//					final Point3D ptBB = edgeB.vertexB().pt();
//					
//					if (pivot == null && edgeB.vertexA().pivot() != null)
//						pivot = edgeB.vertexA().pivot();
//					if (pivot == null && edgeB.vertexB().pivot() != null)
//						pivot = edgeB.vertexB().pivot();
//
//					final Point2D ptX = MathRoutines.crossingPoint
//										(
//											ptAA.x(), ptAA.y(), ptAB.x(), ptAB.y(), 
//											ptBA.x(), ptBA.y(), ptBB.x(), ptBB.y()
//										);
//					if (ptX != null)
//					{
//						// Edges cross, split them
//						final Vertex vertex = graph.addVertex(ptX.getX(), ptX.getY());
//						final int vid = vertex.id();  //graph.vertices().size() - 1;
//						
//						if (pivot != null)
//							vertex.setPivot(pivot);
//						
////						System.out.println("Splitting edges " + edgeA.id() + " and " + edgeB.id() + "...");
//						
//						graph.removeEdge(eb);  //, false);  // remove eb first as has higher index
//						graph.removeEdge(ea);  //, false);
//
//						graph.findOrAddEdge(edgeA.vertexA().id(), vid);
//						graph.findOrAddEdge(vid, edgeA.vertexB().id());
//						
//						graph.findOrAddEdge(edgeB.vertexA().id(), vid);
//						graph.findOrAddEdge(vid, edgeB.vertexB().id());						
//						
////						System.out.println("\n" + graph);
//
//						didSplit = true;
//					}
//				}
//			}
//		} while (didSplit);
		splitAtCrossingPoints(graph);
		
		// Now check for cases where vertices lie on an edge that needs to be split.
		// This can happen when multiple edges cross at the same point in the middle,
		// e.g. in the Game of Solomon.
		splitAtTouchingPoints(graph);
		
		graph.makeFaces(true);

		graph.resetBasis();
		
//		System.out.println("\n" + graph);

		return graph;
	}

	//-------------------------------------------------------------------------

	static void splitAtCrossingPoints(final Graph graph)
	{
		boolean didSplit;
		do
		{
			didSplit = false;
			for (int ea = 0; ea < graph.edges().size() && !didSplit; ea++)
			{	
				final Edge edgeA = graph.edges().get(ea);
				final Point3D ptAA = edgeA.vertexA().pt();
				final Point3D ptAB = edgeA.vertexB().pt();
				
				Vertex pivot = null;
				if (edgeA.vertexA().pivot() != null)
					pivot = edgeA.vertexA().pivot();
				if (edgeA.vertexB().pivot() != null)
					pivot = edgeA.vertexB().pivot();
				
				for (int eb = ea + 1; eb < graph.edges().size() && !didSplit; eb++)
				{
					final Edge edgeB = graph.edges().get(eb);
					final Point3D ptBA = edgeB.vertexA().pt();
					final Point3D ptBB = edgeB.vertexB().pt();
					
					if (pivot == null && edgeB.vertexA().pivot() != null)
						pivot = edgeB.vertexA().pivot();
					if (pivot == null && edgeB.vertexB().pivot() != null)
						pivot = edgeB.vertexB().pivot();

					final Point2D ptX = MathRoutines.crossingPoint
										(
											ptAA.x(), ptAA.y(), ptAB.x(), ptAB.y(), 
											ptBA.x(), ptBA.y(), ptBB.x(), ptBB.y()
										);
					if (ptX == null)
						continue;
					
					// Edges cross, split them
					final Vertex vertex = graph.addVertex(ptX.getX(), ptX.getY());
					final int vid = vertex.id();  //graph.vertices().size() - 1;
						
					if (pivot != null)
						vertex.setPivot(pivot);
						
//					System.out.println("Splitting edges " + edgeA.id() + " and " + edgeB.id() + "...");
						
					graph.removeEdge(eb);  // remove eb first as has higher index
					graph.removeEdge(ea);  

					graph.findOrAddEdge(edgeA.vertexA().id(), vid);
					graph.findOrAddEdge(vid, edgeA.vertexB().id());
						
					graph.findOrAddEdge(edgeB.vertexA().id(), vid);
					graph.findOrAddEdge(vid, edgeB.vertexB().id());						
						
//					System.out.println("\n" + graph);

					didSplit = true;
				}
			}
		} while (didSplit);
	}
	
	//-------------------------------------------------------------------------

	static void splitAtTouchingPoints(final Graph graph)
	{
		boolean didSplit;
		do
		{
			didSplit = false;
			for (int ea = 0; ea < graph.edges().size() && !didSplit; ea++)
			{	
				final Edge edgeA = graph.edges().get(ea);
				
				final Point3D ptAA = edgeA.vertexA().pt();
				final Point3D ptAB = edgeA.vertexB().pt();
				
				Vertex pivot = null;
				if (edgeA.vertexA().pivot() != null)
					pivot = edgeA.vertexA().pivot();
				if (edgeA.vertexB().pivot() != null)
					pivot = edgeA.vertexB().pivot();
				
				for (final Vertex vertex : graph.vertices())
				{
					if (edgeA.contains(vertex))
						continue;

					// Check vertex
					final Point3D ptT = MathRoutines.touchingPoint(vertex.pt(), ptAA, ptAB);
					if (ptT == null)
						continue;
					
					// Edge touched by vertex BA, split it
					if (pivot != null)
						vertex.setPivot(pivot);
						
//					System.out.println("T: Splitting edge " + edgeA.id() + " at vertex " + vertex.id() + "...\n");
						
					graph.removeEdge(ea);
					graph.findOrAddEdge(edgeA.vertexA().id(), vertex.id());
					graph.findOrAddEdge(vertex.id(), edgeA.vertexB().id());
						
					didSplit = true;
				}
			}
		} while (didSplit);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return graphFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long flags = graphFn.gameFlags(game);

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
		// Commented because if some modifications are done to the graph we can not
		// conclude about the tiling
		// concepts.or(graphFn.concepts(game));
		return concepts;
	}
}
