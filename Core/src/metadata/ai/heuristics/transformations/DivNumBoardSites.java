package metadata.ai.heuristics.transformations;

import other.context.Context;

/**
 * Transforms heuristic scores by dividing them by the number of sites
 * in a game's board. 
 * 
 * @remarks Can be used to approximately standardise heuristic values across
 * games with different board sizes.
 *
 * @author Dennis Soemers
 */
public class DivNumBoardSites implements HeuristicTransformation
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @example (divNumBoardSites)
	 */
	public DivNumBoardSites()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float transform(final Context context, final float heuristicScore)
	{
		return heuristicScore / context.game().board().numSites();
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(divNumBoardSites)";
	}
	
	//-------------------------------------------------------------------------
	
}
