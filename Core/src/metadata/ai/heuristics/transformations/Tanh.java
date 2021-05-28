package metadata.ai.heuristics.transformations;

import other.context.Context;

/**
 * Transforms heuristic scores by applying the $\\tanh$ to them:
 * $f(x) = \\tanh(x)$.
 * 
 * @remarks This guarantees that all transformed heuristic scores will lie
 * in $[-1, 1]$. May map too many different values only to the limits of this
 * interval in practice.
 *
 * @author Dennis Soemers
 */
public class Tanh implements HeuristicTransformation
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @example (tanh)
	 */
	public Tanh()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float transform(final Context context, final float heuristicScore)
	{
		return (float) Math.tanh(heuristicScore);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(tanh)";
	}
	
	//-------------------------------------------------------------------------
	
}
