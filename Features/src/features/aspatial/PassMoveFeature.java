package features.aspatial;

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

}
