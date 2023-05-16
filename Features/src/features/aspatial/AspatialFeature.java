package features.aspatial;

import features.Feature;
import other.move.Move;
import other.state.State;

/**
 * An aspatial (i.e., not spatial) state-action feature. These features are not
 * necessarily binary, i.e. they can return floats.
 *
 * @author Dennis Soemers
 */
public abstract class AspatialFeature extends Feature
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state
	 * @param move
	 * @return Feature value for given move in given state.
	 */
	public abstract float featureVal(final State state, final Move move);

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------

}
