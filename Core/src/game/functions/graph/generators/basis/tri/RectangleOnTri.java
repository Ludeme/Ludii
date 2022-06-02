package game.functions.graph.generators.basis.tri;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
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
 * Defines a triangle shaped board with a hexagonal tiling.
 * 
 * @author cambolbro
 */
@Hide
public class RectangleOnTri extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Tri.
	 * 
	 * @param dimA The dimension A.
	 * @param dimB The dimension B.
	 */
	public RectangleOnTri
	(
	   	 	  final DimFunction dimA,
	   	 @Opt final DimFunction dimB
	)
	{
		final int rows = dimA.eval();
		final int cols = (dimB != null) ? dimB.eval() : rows;
		
		this.basis = BasisType.Triangular;
		this.shape  = (rows == cols) ? ShapeType.Square : ShapeType.Rectangle;
		
		this.dim = new int[]{ rows, cols };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = dim[0] + (siteType == SiteType.Cell ? 1 : 0);
		final int cols = dim[1] + (siteType == SiteType.Cell ? 1 : 0);
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols + rows; c++)
			{
				if (c < (r + 1) / 2 || c >= cols + r / 2)
					continue;
				final Point2D pt = Tri.xy(r, c);
				vertexList.add(new double[]{ pt.getX(), pt.getY() });
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
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
		// ...
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.TriangleTiling.id(), true);
		if (shape.equals(ShapeType.Square))
			concepts.set(Concept.SquareShape.id(), true);
		else
			concepts.set(Concept.RectangleShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
