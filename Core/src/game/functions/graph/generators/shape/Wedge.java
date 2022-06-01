package game.functions.graph.generators.shape;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import main.math.MathRoutines;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a triangular wedge shaped graph, with one vertex at the top and 
 * three vertices along the bottom.
 * 
 * @author cambolbro
 * 
 * @remarks Wedges can be used to add triangular arms to Alquerque boards.
 */
public class Wedge extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param rows    Number of rows.
	 * @param columns Number of columns.
	 * 
	 * @example (wedge 3)
	 */
	public Wedge
	(
		     final DimFunction rows,
		@Opt final DimFunction columns
	)
	{
		//super(BasisType.NoBasis, ShapeType.Wedge, rows, columns);
		this.basis = BasisType.NoBasis;
		this.shape = ShapeType.Wedge;

		if (columns == null)
			this.dim = new int[]{ rows.eval() };	
		else
			this.dim = new int[]{ rows.eval(), columns.eval() };	
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = dim[0];
		final int columns = (dim.length == 1) ? 3 : dim[1];
		
		final Graph graph = new Graph();
		
		// Create vertices
		final int mid = rows - 1;
		for (int r = 0; r < rows; r++)
		{
			if (r == 0)
			{
				// Apex
				graph.addVertex(mid, mid);
			}
			else
			{
				// Non-apex row
				final int left  = mid - r;
				final int right = mid + r;
				for (int c = 0; c < columns; c++)
				{
					final double t = c / (double)(columns - 1);
					final double x = MathRoutines.lerp(t, left, right);
					final double y = mid - r;
					graph.addVertex(x, y);
				}
			}
		}

		// Create edges
		for (int r = 0; r < rows; r++)
		{
			if (r == 0)
			{
				// Apex
				for (int c = 0; c < columns; c++)
					graph.addEdge(0, c + 1);
			}
			else
			{
				// Non-apex row
				final int from = r * columns - columns + 1;

				// Edges across
				for (int c = 0; c < columns - 1; c++)
					graph.addEdge(from + c, from + c + 1);
				
				if (r < rows - 1)
				{
					// Edges down
					for (int c = 0; c < columns; c++)
						graph.addEdge(from + c, from + columns + c);
				}
			}
		}
		
		//graph.reorder();
		
		graph.setBasisAndShape(basis, shape);

		//System.out.println(graph);
		
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
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}
	
	//-------------------------------------------------------------------------
	
}
