package game.functions.graph.generators.shape.concentric;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.dim.DimFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board based on a tiling of concentric shapes.
 * 
 * @author cambolbro
 * 
 * @remarks Morris or Merels boards are examples of square concentric boards. 
 *          Circular tilings are centred around a ``pivot'' point. 
 *          For circular boards defined by a list of cell counts, 
 *          the first count should be 0 (centre point) or 1 (centre cell).
 */
public class Concentric extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param shape			Shape of board rings.
	 * @param sides         Number of sides (for polygonal shapes).
	 * @param cells         Number of cells per circular ring.
	 * @param rings         Number of rings [3].
	 * @param steps         Number of steps for target boards with multiple sites per ring [null].
	 * @param midpoints     Whether to add vertices at edge midpoints [True].
	 * @param joinMidpoints Whether to join concentric midpoints [True].
	 * @param joinCorners   Whether to join concentric corners [False].
	 * @param stagger       Whether to stagger cells in concentric circular rings [False].
	 * 
	 * @example (concentric Square rings:3)
	 * @example (concentric Triangle rings:3 joinMidpoints:False joinCorners:True)
	 * @example (concentric Hexagon rings:3 joinMidpoints:False joinCorners:True)
	 * @example (concentric sides:5 rings:3)
	 *
	 * @example (concentric { 8 })
	 * @example (concentric { 0 8 })
	 * @example (concentric { 1 4 8 } stagger:True)
	 * 
	 * @example (concentric Target rings:4)
	 * @example (concentric Target rings:4 steps:16)
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
		@Or   			final ConcentricShapeType shape,
		@Or  	  @Name	final DimFunction   	  sides,
		@Or    			final DimFunction[] 	  cells,
	 	 	 @Opt @Name	final DimFunction   	  rings, 
	 	 	 @Opt @Name	final DimFunction   	  steps, 
			 @Opt @Name	final BooleanFunction 	  midpoints,
			 @Opt @Name	final BooleanFunction 	  joinMidpoints,
			 @Opt @Name	final BooleanFunction 	  joinCorners,
			 @Opt @Name	final BooleanFunction	  stagger
	)
	{
		int numNonNull = 0;
		if (sides != null)
			numNonNull++;
		if (cells != null)
			numNonNull++;
		
		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of 'shape', 'sides' or 'cells' can be non-null.");
		
		if (shape != null && rings != null)
		{
			// Known shape (including target)
			if (shape == ConcentricShapeType.Triangle)
			{
				return new ConcentricRegular(3, rings.eval(), midpoints, joinMidpoints, joinCorners);
			}
			else if (shape == ConcentricShapeType.Square)
			{
				return new ConcentricRegular(4, rings.eval(), midpoints, joinMidpoints, joinCorners);
			}
			else if (shape == ConcentricShapeType.Hexagon)
			{
				return new ConcentricRegular(6, rings.eval(), midpoints, joinMidpoints, joinCorners);
			}
			else if (shape == ConcentricShapeType.Target)
			{
				// Is a target board
				if (steps != null)
				{
					// Make a wheel board
						final int[] cellsPerRing = new int[rings.eval()];
					for (int c = 0; c < cellsPerRing.length; c++)
						cellsPerRing[c] = steps.eval();
					return new ConcentricCircle(cellsPerRing, null);
				}
				else
				{
					// Make a target board (one site per cell)
					return new ConcentricTarget(rings.eval());
				}
			}
		}
		else if (sides != null && rings != null)
		{
			// Regular polygon
			return new ConcentricRegular(sides.eval(), rings.eval(), midpoints, joinMidpoints, joinCorners);
		}
		else if (cells != null)
		{
			// Circle (wheel) board
			final int[] cellsPerRing = new int[cells.length];
			for (int c = 0; c < cells.length; c++)
				cellsPerRing[c] = cells[c].eval();
			return new ConcentricCircle(cellsPerRing, stagger);
		}

		// Should never reach this point
		throw new IllegalArgumentException("Concentric board must specify sides, cells or rings.");
	}

	//-------------------------------------------------------------------------

	private Concentric()
	{
		// Ensure that compiler does not pick up default constructor.
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		return null;  // null placeholder to make the grammar recognise Concentric
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
