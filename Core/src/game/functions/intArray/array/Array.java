package game.functions.intArray.array;

import java.util.BitSet;

import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.region.RegionFunction;
import other.context.Context;

/**
 * Converts a Region Function to an Int Array.
 *
 * @author Dennis Soemers
 */
public class Array extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Our region function */
	private final RegionFunction region;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param region The region function to be converted into an int array.
	 * 
	 * @example (array (sites Board)))
	 */
	public Array(final RegionFunction region)
	{
		this.region = region;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		return region.eval(context).sites();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return region.gameFlags(game);
	}

	@Override
	public boolean isStatic()
	{
		return region.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		region.preprocess(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		return region.concepts(game);
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		return region.writesEvalContextRecursive();
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		return region.readsEvalContextRecursive();
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		return region.missingRequirement(game);
	}

	@Override
	public boolean willCrash(final Game game)
	{
		return region.willCrash(game);
	}

}
