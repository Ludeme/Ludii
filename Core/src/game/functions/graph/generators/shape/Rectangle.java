package game.functions.graph.generators.shape;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.RectangleOnSquare;

//-----------------------------------------------------------------------------

/**
 * Defines a rectangular board.
 * 
 * @author cambolbro
 * 
 */
@SuppressWarnings("javadoc")
public class Rectangle extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param dimA      Number of rows.
	 * @param dimB      Number of columns.
	 * @param diagonals Which type of diagonals to create, if any.
	 * 
	 * @example (rectangle 4 6)
	 */
	public static GraphFunction construct
	(
				   final DimFunction dimA,
		@Opt	   final DimFunction dimB,
		@Opt @Name final DiagonalsType diagonals
	)
	{
		return new RectangleOnSquare(dimA, (dimB == null ? dimA : dimB), diagonals, null);
	}

	//-------------------------------------------------------------------------

	private Rectangle()
	{
		//super(null, null, null, null);  //TilingType.Square, ShapeType.NoShape, Integer.valueOf(0), null);
		this.basis = null;
		this.shape = null;
		this.dim = null;  //new int[] { rows.eval(), columns == null ? rows.eval() : columns.eval() };

		// Ensure that compiler does not pick up default constructor
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
