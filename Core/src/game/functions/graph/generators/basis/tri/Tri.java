package game.functions.graph.generators.basis.tri;

import java.awt.geom.Point2D;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.dim.math.Add;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Poly;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board on a triangular tiling.
 * 
 * @author cambolbro
 */
@SuppressWarnings("javadoc")
public class Tri extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For defining a tri tiling with two dimensions.
	 * 
	 * @param shape Board shape [Triangle].
	 * @param dimA  Board dimension; cells or vertices per side.
	 * @param dimB  Board dimension; cells or vertices per side.
	 * 
	 * @example (tri 8)
	 * @example (tri Hexagon 3)
	 */
	public static GraphFunction construct
	(
		@Opt       final TriShapeType shape,
		   	       final DimFunction      dimA,
		@Opt       final DimFunction      dimB
//		@Opt @Name final Boolean      pyramidal   	 
	)
	{
		final TriShapeType st = (shape == null) ? TriShapeType.Triangle : shape;		
		switch (st)
		{
		case Hexagon:
			return new HexagonOnTri(dimA);
		case Triangle:
			return new TriangleOnTri(dimA);
		case Diamond:
			return new DiamondOnTri(dimA, null);
		case Prism:
			return new DiamondOnTri(dimA, (dimB != null ? dimB : dimA));
		case Square:
			return new RectangleOnTri(dimA, dimA);
		case Rectangle:
			return new RectangleOnTri(dimA, (dimB != null ? dimB : dimA));
		case Star:
			return new StarOnTri(dimA);
		case Limping:
			final DimFunction dimAplus1 = new Add(dimA,new DimConstant(1));
			return new CustomOnTri(new DimFunction[] { dimA, dimAplus1 } );
			//$CASES-OMITTED$
		default:
			throw new IllegalArgumentException("Shape " + st + " not supported for hex tiling.");
		}
	}

	/**
	 * For defining a tri tiling with a polygon or the number of sides.
	 * 
	 * @param poly  Points defining the board shape.
	 * @param sides Side lengths around board in clockwise order.
	 * 
	 * @example (tri (poly { {1 2} {1 6} {3 6} {3 4} } ))
	 * @example (tri { 4 3 -1 2 3 })
	 */
	public static GraphFunction construct
	(
		@Or final Poly      poly,
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
			return new CustomOnTri(poly.polygon());
		else
			return new CustomOnTri(sides);
	}

	//-------------------------------------------------------------------------

	private Tri()
	{
		// Ensure that compiler does not pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Null placeholder to make the grammar recognise Tri
		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D.
	 */
	public static Point2D xy(final int row, final int col)
	{
		final double hx = unit;
		final double hy = Math.sqrt(3) / 2.0;

		return new Point2D.Double(hx * (col - 0.5 * row), hy * row);
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
	
	//-------------------------------------------------------------------------
	
}
