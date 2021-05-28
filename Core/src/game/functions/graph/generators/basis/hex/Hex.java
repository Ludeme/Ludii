package game.functions.graph.generators.basis.hex;

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
 * Defines a board on a hexagonal tiling.
 * 
 * @author cambolbro
 */
public class Hex extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The unit on x.
	 */
	public static final double ux = Math.sqrt(3) / 2;

	/**
	 * The unit on y.
	 */
	public static final double uy = unit;

	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		{  0.0 * ux,  1.0 * uy },
		{  1.0 * ux,  0.5 * uy },
		{  1.0 * ux, -0.5 * uy },
		{  0.0 * ux, -1.0 * uy },
		{ -1.0 * ux, -0.5 * uy },
		{ -1.0 * ux,  0.5 * uy },
	};

	//-------------------------------------------------------------------------

	/**
	 * For defining a hex tiling with two dimensions.
	 * 
	 * @param shape Board shape [Hexagon].
	 * @param dimA  Primary board dimension; cells or vertices per side.
	 * @param dimB  Secondary Board dimension; cells or vertices per side.
	 * 
	 * @example (hex 5)
	 * @example (hex Diamond 11)
	 * @example (hex Rectangle 4 6)
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
		@Opt       final HexShapeType shape,
		   	       final DimFunction  dimA,
		@Opt       final DimFunction  dimB
//		@Opt @Name final Boolean      pyramidal   	 
	)
	{
		final HexShapeType st = (shape == null) ? HexShapeType.Hexagon : shape;		
		switch (st)
		{
		case Hexagon:
			if (dimB != null)
				return new CustomOnHex(new DimFunction[]
				{ dimA, dimB });
			else
				return new HexagonOnHex(dimA);
		case Triangle:
			return new TriangleOnHex(dimA);
		case Diamond:
			return new DiamondOnHex(dimA, null);
		case Prism:
			return new DiamondOnHex(dimA, (dimB != null ? dimB : dimA));
		case Star:
			return new StarOnHex(dimA);
		case Limping:
			final DimFunction dimAplus1 = new Add(dimA, new DimConstant(1));
			return new CustomOnHex(new DimFunction[] { dimA, dimAplus1 } );
		case Square:
			return new RectangleOnHex(dimA, dimA);
//			return new CustomOnHex(new Float[][]
//										{
//											{ 0f, 0f },
//											{ 0f, (float)dimA },
//											{ (float)dimA, (float)dimA },
//											{ (float)dimA, 0f }
//										}
//								  );	
		case Rectangle:
			return new RectangleOnHex(dimA, (dimB != null ? dimB : dimA));
			//$CASES-OMITTED$
		default:
			throw new IllegalArgumentException("Shape " + st + " not supported for hex tiling.");
		}
	}

	/**
	 * For defining a hex tiling with a polygon or the number of sides.
	 * 
	 * @param poly  Points defining the board shape.
	 * @param sides Side lengths around board in clockwise order.
	 * 
	 * @example (hex (poly { {1 2} {1 6} {3 6} } ))
	 * @example (hex { 4 3 -1 2 3 })
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
			return new CustomOnHex(poly.polygon());
		else
			return new CustomOnHex(sides);
	}

	//-------------------------------------------------------------------------

	private Hex()
	{
		// Ensure that compiler does not pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Null placeholder to make the grammar recognise Hex
		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D of the centroid.
	 */
	public static Point2D xy(final int row, final int col)
	{
		final double hx = unit * Math.sqrt(3);
		final double hy = unit * 3 / 2;

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
