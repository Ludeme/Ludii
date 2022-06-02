package game.functions.graph.generators.basis.tiling.tiling33344;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Poly;
import main.math.MathRoutines;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board on a semi-regular 3.3.3.4.4 tiling.
 * 
 * @author cambolbro
 * 
 * Tiling 3.3.3.4.4 is composed of rows of triangles and squares.
 */
@Hide
public class Tiling33344 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		{    0,    0 },
		{    0, unit },
		{ unit, unit },
		{ unit,    0 },
	};

	//-------------------------------------------------------------------------

	/**
	 * @param dimA Number of rows (in squares).
	 * @param dimB Number of columns (in squares).
	 * 
	 * @example (tiling33344 3 4)
	 */
	public Tiling33344
	(
		   	 final DimFunction dimA,
		@Opt final DimFunction dimB
	)
	{
		this.basis = BasisType.T33344;
		this.shape = ShapeType.Rhombus;
			
		this.dim = new int[] { dimA.eval(), (dimB == null ? dimA.eval() : dimB.eval()) };
	}

	/**
	 * @param poly  Points defining the board shape.
	 * @param sides Side lengths around board in clockwise order.
	 * 
	 * @example (tiling33344 (poly { {1 2} {1 6} {3 6} } ))
	 * @example (tiling33344 { 4 3 -1 2 3 })
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
		@Or final Poly 		poly,
		@Or final DimFunction[] sides
	)
	{
		int numNonNull = 0;
		if (poly != null)
			numNonNull++;
		if (sides != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Exactly one array parameter must be non-null.");
		
		if (poly != null)
			return new CustomOn33344(poly.polygon());
		else
			return new CustomOn33344(sides);
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = dim[0];
		final int cols = dim[1];
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				final Point2D ptRef = Tiling33344.xy(row, col);
				
				for (int n = 0; n < Tiling33344.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling33344.ref[n][1];
					final double y = ptRef.getY() + Tiling33344.ref[n][0];
					
					// See if vertex already created
					int vid;
					for (vid = 0; vid < vertexList.size(); vid++)
					{
						final double[] ptV = vertexList.get(vid);
						final double dist = MathRoutines.distance(ptV[0], ptV[1], x, y);
						if (dist < 0.1)
							break;
					}
					
					if (vid >= vertexList.size())
						vertexList.add(new double[]{ x, y });
				}
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		graph.reorder();
		
		//System.out.println(graph);
		
		return graph;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D.
	 */
	public static Point2D xy(final int row, final int col)
	{
		final double dx = unit;
		final double dy = unit * (1 + Math.sqrt(3) / 2);

		return new Point2D.Double((col + 0.5 * row) * dx, row *dy);
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
		concepts.set(Concept.SemiRegularTiling.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
