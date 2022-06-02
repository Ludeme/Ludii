package game.functions.graph.generators.basis.tiling.tiling3464;

import java.awt.geom.Point2D;
import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.dim.math.Add;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.util.graph.Poly;
import other.concept.Concept;

//-----------------------------------------------------------------------------

/**
 * Defines a board on a rhombitrihexahedral tiling (semi-regular tiling 3.4.6.4), 
 * such as the Kensington board.
 * 
 * @author cambolbro
 */
@Hide
public class Tiling3464 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The x unit.
	 */
	public static final double ux = unit;

	/**
	 * The y unit.
	 */
	public static final double uy = unit * Math.sqrt(3) / 2;

	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		// Store major hexagon point position
		{ -0.5 * ux,  1.0 * uy },
		{  0.5 * ux,  1.0 * uy },
		{  1.0 * ux,  0.0 * uy },
		{  0.5 * ux, -1.0 * uy },
		{ -0.5 * ux, -1.0 * uy },
		{ -1.0 * ux,  0.0 * uy },
		{ 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
		{ 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
	};
	{		
		// Calculate outer square point positions
		final double a = unit + Math.sqrt(3) / 2.0;
		final double h = a / Math.cos(Math.toRadians(15));
		
		for (int n = 0; n < 12; n++)
		{
			final double theta = Math.toRadians(15 + n * 30);
			
			ref[6 + n][0] = h * Math.cos(theta);
			ref[6 + n][1] = h * Math.sin(theta);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param shape Board shape [Hexagon].
	 * @param dimA  Board dimension; major hex cells per side.
	 * @param dimB  Board dimension; major hex cells per side.
	 * 
	 * @example (tiling3464 2)
	 * @example (tiling3464 Limping 3)
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
		@Opt       final Tiling3464ShapeType shape,
		   	       final DimFunction      		 dimA,
		@Opt       final DimFunction      		 dimB
	)
	{
		final Tiling3464ShapeType st = (shape == null) ? Tiling3464ShapeType.Hexagon : shape;		
		switch (st)
		{
		case Hexagon:
			return new HexagonOn3464(dimA);
		case Triangle:
			return new TriangleOn3464(dimA);
		case Diamond:
			return new DiamondOn3464(dimA, null);
		case Prism:
			return new DiamondOn3464(dimA, (dimB != null ? dimB : dimA));
		case Star:
			return new StarOn3464(dimA);
		case Limping:
			final DimFunction dimAplus1 = new Add(dimA, new DimConstant(1));
			return new CustomOn3464(new DimFunction[] { dimA, dimAplus1 } );
		case Square:
			return new RectangleOn3464(dimA, dimA);
		case Rectangle:
			return new RectangleOn3464(dimA, (dimB != null ? dimB : dimA));
		default:
			throw new IllegalArgumentException("Shape " + st + " not supported for tiling3464.");
		}
	}

	/**
	 * @param poly  Points defining the board shape.
	 * @param sides Side lengths around board in clockwise order.
	 * 
	 * @example (tiling3464 (poly { {1 2} {1 6} {3 6} {3 4} } ))
	 * @example (tiling3464 { 4 3 -1 2 3 })
	 */
	@SuppressWarnings("javadoc")
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
			return new CustomOn3464(poly.polygon());
		else
			return new CustomOn3464(sides);
	}

	//-------------------------------------------------------------------------

	private Tiling3464()
	{
		// Ensure that compiler does not pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D.
	 */
	public static Point2D xy(final int row, final int col)
	{
		final double hx = unit * (1 + Math.sqrt(3));
		final double hy = unit * (3 + Math.sqrt(3)) / 2;

		return new Point2D.Double(hx * (col - row), hy * (row + col) * 0.5);
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
