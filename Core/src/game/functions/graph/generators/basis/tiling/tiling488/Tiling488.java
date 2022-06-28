package game.functions.graph.generators.basis.tiling.tiling488;

import java.awt.geom.Point2D;
import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.graph.generators.basis.Basis;
import other.concept.Concept;

//-----------------------------------------------------------------------------

/**
 * Defines a board on the 4.8.8 tiling, which is made up of octagons with 
 * squares in the interstitial gaps.
 * 
 * @author cambolbro
 * 
 * @remarks Rotating this semi-regular tiling 45 degrees leave the octagons 
 *          in the same relative relation, but the square angled.
 */
@Hide
public class Tiling488 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private static final double u = unit;
	private static final double v = unit * (1 + 2 / Math.sqrt(2));

	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		// Store major octagon point positions
		{  u / 2,  v / 2 },
		{  v / 2,  u / 2 },
		{  v / 2, -u / 2 },
		{  u / 2, -v / 2 },
		{ -u / 2, -v / 2 },
		{ -v / 2, -u / 2 },
		{ -v / 2,  u / 2 },
		{ -u / 2,  v / 2 },
	};
		
	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D.
	 */
	public static Point2D.Double xy(final int row, final int col)
	{
		return new Point2D.Double(col * v, row * v);
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
