package game.functions.graph.generators.shape;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.concept.Concept;
import other.context.Context;

/**
 * Defines a regular polygon.
 * 
 * @author cambolbro
 * 
 * If the Star shape is specified, then edges are created to form points at 
 * each vertex, rather than joining the vertices around the perimeter.
 */
public class Regular extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param star     Whether to make a star shape.
	 * @param numSides Number of sides.
	 * 
	 * @example (regular 3)
	 */
	public Regular
	(
		@Opt final ShapeStarType star,
		final DimFunction numSides
	)
	{
		this.basis = BasisType.NoBasis;
		this.shape = (star != null) ? ShapeType.Star : ShapeType.Polygon;

		this.dim = new int[] { numSides.eval() };	
	}

	/**
	 * @param basis The basis.
	 * @param shape The shape.
	 * @param dimA  The dimension A.
	 * @param dimB  The dimension B.
	 */
	@Hide
	public Regular
	(	
			 final BasisType basis,
			 final ShapeType shape,
			 final DimFunction   dimA,
		@Opt final DimFunction   dimB
	)
	{
		this.basis = basis;
		this.shape  = shape;
		
		if (dimB == null)
			this.dim = new int[]{ dimA.eval() };	
		else
			this.dim = new int[]{ dimA.eval(), dimB.eval() };	
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int numSides = dim[0];
		final double r = numSides / (2 * Math.PI);
		
		final Graph graph = new Graph();
			
		final double offset = (numSides == 4) ? Math.PI / 4 : Math.PI / 2;
			
		for (int n = 0; n < numSides; n++)
		{
			final double theta = offset + (double)n / numSides * 2 * Math.PI;
			
			final double x = r * Math.cos(theta);
			final double y = r * Math.sin(theta);
			
			graph.addVertex(x, y);
		}
		
		if (shape == ShapeType.Star)
		{
			// Simple star
			for (int n = 0; n < numSides; n++)
				graph.addEdge(n, (n + (numSides - 1) / 2) % numSides);
		}
		else
		{
			// Simple polygon
			for (int n = 0; n < numSides; n++)
				graph.addEdge(n, (n + 1) % numSides);
		}
		
		if (siteType == SiteType.Cell)
			graph.makeFaces(true);
			
		graph.reorder();
		
//		System.out.println(graph);
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

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
