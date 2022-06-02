package game.functions.graph.generators.basis.brick;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a square or rectangular brick board.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class SpiralOnBrick extends Basis
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param dimA The dimension A.
	 */
	public SpiralOnBrick
	(
	   	 final DimFunction dimA
	)
	{
		this.basis = BasisType.Brick;
		this.shape  = ShapeType.Spiral;

		this.dim = new int[] { dimA.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		//final double tolerance = 0.0001;
		
		final int rings = dim[0];
		
		// Create the graph on-the-fly
		final Graph graph = new Graph();
		
		for (int ring = 0; ring < rings; ring++)
		{
			if (ring == 0)
			{
				Brick.addHalfBrick(graph, rings-1, rings-1);
			}
			else
			{
				for (int n = 0; n < 2 * ring; n += 2)
				{
					Brick.addVerticalBrick(graph, rings-ring + n, rings-ring-1);
					Brick.addBrick(graph, rings+ring-1, rings-ring+n);			
					Brick.addVerticalBrick(graph, rings-ring-1+n, rings+ring-1);
					Brick.addBrick(graph, rings-ring-1, rings-ring-1+n);			
				}			
			}
		}
		
		graph.setBasisAndShape(basis, shape);
		graph.reorder();
		
		return graph;
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

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.BrickTiling.id(), true);
		concepts.set(Concept.SpiralShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
