package features.aspatial;

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

}
