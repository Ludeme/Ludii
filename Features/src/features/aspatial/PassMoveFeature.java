package features.aspatial;

import game.Game;
import other.move.Move;
import other.state.State;

/**
 * Binary feature that has a value of 1.0 for any move that is a pass move.
 *
 * @author Dennis Soemers
 */
public class PassMoveFeature extends AspatialFeature
{
	
	//-------------------------------------------------------------------------
	
	/** The singleton instance */
	private static final PassMoveFeature INSTANCE = new PassMoveFeature();

	//-------------------------------------------------------------------------

	/**
	 * Private: singleton
	 */
	private PassMoveFeature()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float featureVal(final State state, final Move move)
	{
		if (move.isPass())
			return 1.f;
		else
			return 0.f;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "PassMove";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String generateTikzCode(final Game game)
	{
		return "\\node[rectangle,draw{,REL_POS}] ({LABEL}) {Pass};";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The singleton instance
	 */
	public static PassMoveFeature instance()
	{
		return INSTANCE;
	}
	
	//-------------------------------------------------------------------------

}
