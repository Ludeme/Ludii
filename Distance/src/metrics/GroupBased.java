package metrics;

import java.util.ArrayList;

import common.LudRul;
import utils.data_structures.support.DistanceProgressListener;

public interface GroupBased
{
	public DistanceMetric getPlaceHolder();
	/**
	 * Some metrices only work within a static set of games.
	 * Those need to return true. 
	 * 
	 * @return if this metric instance is initialized.
	 */
	public boolean isInitialized(ArrayList<LudRul> candidates);
	public boolean typeNeedsToBeInitialized();
	public void init(final ArrayList<LudRul> candidates,boolean forceRecalculation, final DistanceProgressListener dpl);

	public default DistanceMetric getDefaultInstance()
	{
		return getPlaceHolder();
	}
}
