package features.aspatial;

import game.Game;
import other.move.Move;
import other.state.State;

/**
 * Binary feature that has a value of 1.0 for any move that is a swap move.
 *
 * @author Dennis Soemers
 */
public class SwapMoveFeature extends AspatialFeature
{
	
	//-------------------------------------------------------------------------
	
	/** The singleton instance */
	private static final SwapMoveFeature INSTANCE = new SwapMoveFeature();

	//-------------------------------------------------------------------------

	/**
	 * Private: singleton
	 */
	private SwapMoveFeature()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float featureVal(final State state, final Move move)
	{
		if (move.isSwap())
			return 1.f;
		else
			return 0.f;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "SwapMove";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String generateTikzCode(final Game game)
	{
		return "\\node[rectangle,draw{,REL_POS}] ({LABEL}) {Swap};";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The singleton instance
	 */
	public static SwapMoveFeature instance()
	{
		return INSTANCE;
	}
	
	//-------------------------------------------------------------------------

}
