package metadata.ai.heuristics.transformations;

import metadata.ai.AIItem;
import other.context.Context;

/**
 * Interface for transformations of heuristics (generally functions
 * intended to map the scores of a heuristic to some different range).
 *
 * @author Dennis Soemers
 */
public interface HeuristicTransformation extends AIItem
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param heuristicScore
	 * @return Transformed version of given score
	 */
	public float transform(final Context context, final float heuristicScore);
	
	//-------------------------------------------------------------------------

}
