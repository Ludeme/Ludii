package game.functions.graph.generators.basis.square;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a diamond shaped board on a square tiling.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class DiamondOnSquare extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final DiagonalsType diagonals;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Rectangle.
	 * 
	 * @param dim       The dimension.
	 * @param diagonals The diagonal type.
	 */
	public DiamondOnSquare
	(
		final DimFunction dim,
		final DiagonalsType diagonals
	)
	{
		this.basis = BasisType.Square;
		this.shape = ShapeType.Diamond;

		this.dim       = new int[] { dim.eval() };
		this.diagonals = diagonals;	

	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Always use dim as dim is the same whether playing on vertices or cells
		final int d = dim[0];
		
		final int rows = 2 * d;
		final int cols = 2 * d;
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				if (r + c < d-1 || c - r > d || r - c > d || r + c >= 3 * d)
					continue;
				
				vertexList.add(new double[] { c, r });
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);

		Square.handleDiagonals(graph, 0, rows, 0, cols, diagonals);  //, 0.0001);

		graph.makeFaces(false);
		
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
		if (diagonals == null)
			concepts.set(Concept.SquareTiling.id(), true);
		else if (diagonals.equals(DiagonalsType.Alternating))
			concepts.set(Concept.AlquerqueTiling.id(), true);
		concepts.set(Concept.DiamondShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
