package features.aspatial;

import game.Game;
import other.move.Move;
import other.state.State;

/**
 * Intercept feature (always a value of 1.0)
 *
 * @author Dennis Soemers
 */
public class InterceptFeature extends AspatialFeature
{
	
	//-------------------------------------------------------------------------
	
	/** The singleton instance */
	private static final InterceptFeature INSTANCE = new InterceptFeature();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Private: singleton
	 */
	private InterceptFeature()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float featureVal(final State state, final Move move)
	{
		return 1.f;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "Intercept";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String generateTikzCode(final Game game)
	{
		return "\\node[rectangle,draw{,REL_POS}] ({LABEL}) {Intercept};";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The singleton instance
	 */
	public static InterceptFeature instance()
	{
		return INSTANCE;
	}
	
	//-------------------------------------------------------------------------

}
