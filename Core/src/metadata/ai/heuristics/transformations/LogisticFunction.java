package metadata.ai.heuristics.transformations;

import other.context.Context;

/**
 * Transforms heuristic scores by applying the logistic function to them:
 * $f(x) = \\frac{1}{1 + \\exp(x)}$.
 * 
 * @remarks This guarantees that all transformed heuristic scores will lie
 * in $[0, 1]$. May map too many different values only to the limits of this
 * interval in practice.
 *
 * @author Dennis Soemers
 */
public class LogisticFunction implements HeuristicTransformation
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @example (logisticFunction)
	 */
	public LogisticFunction()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float transform(final Context context, final float heuristicScore)
	{
		return (float) (1.0 / (1.0 + Math.exp(heuristicScore)));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(logisticFunction)";
	}
	
	//-------------------------------------------------------------------------
	
}
