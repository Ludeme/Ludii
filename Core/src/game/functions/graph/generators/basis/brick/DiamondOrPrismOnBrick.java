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
public class DiamondOrPrismOnBrick extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final boolean trim;
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param dimA The dimension A.
	 * @param dimB The dimension B.
	 * @param trim True if this is trimmed.
	 */
	public DiamondOrPrismOnBrick
	(
	   	 final DimFunction dimA,
	   	 final DimFunction dimB,
	   	 final Boolean trim
	)
	{
		this.basis = BasisType.Brick;
		this.shape  = (dimB == null) ? ShapeType.Diamond : ShapeType.Prism;

		if (dimB == null)
			this.dim = new int[] { dimA.eval(), dimA.eval() };
		else
			this.dim = new int[] { dimA.eval(), dimB.eval() };
		
		this.trim = (trim == null) ? false : trim.booleanValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		//final double tolerance = 0.0001;
		
		final int side = dim[0];
		final int mid  = (shape == ShapeType.Diamond) ? 0 : dim[1];
		
		final int rows = 2 * side + 2 * mid;
		final int cols = 2 * side;
		
		// Create the graph on-the-fly
		final Graph graph = new Graph();
		
		for (int r = 0; r < rows; r++)
			for (int c = (r + 1) % 2; c < cols; c += 2)
			{
				if (r + c < side - 1)
					continue;
				
				if (c - r  >= side)
					continue;

				if (r + c >= 3 * side - 1)
					continue;
				
				if (r - c  > side)
					continue;
					
				if (trim && c == 0)
					Brick.addHalfBrick(graph, r, c+1);
				else if (trim && c >= cols - 1)
					Brick.addHalfBrick(graph, r, c);
				else
					Brick.addBrick(graph, r, c);
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
		
		if (shape.equals(ShapeType.Diamond))
			concepts.set(Concept.DiamondShape.id(), true);
		else
			concepts.set(Concept.PrismShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
