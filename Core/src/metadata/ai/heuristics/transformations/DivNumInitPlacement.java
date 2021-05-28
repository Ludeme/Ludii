package metadata.ai.heuristics.transformations;

import other.context.Context;

/**
 * Transforms heuristic scores by dividing them by the number of pieces
 * placed in a game's initial game state.
 * 
 * @remarks Can be used to approximately standardise heuristic values across
 * games with different initial numbers of pieces.
 *
 * @author Dennis Soemers
 */
public class DivNumInitPlacement implements HeuristicTransformation
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @example (divNumInitPlacement)
	 */
	public DivNumInitPlacement()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float transform(final Context context, final float heuristicScore)
	{
		final int numInitPlacement = Math.max(1, context.trial().numInitPlacement());
		return heuristicScore / numInitPlacement;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(divNumInitPlacement)";
	}
	
	//-------------------------------------------------------------------------

}
