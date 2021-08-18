package game.functions.intArray.array;

import java.util.BitSet;

import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import other.context.Context;

/**
 * Converts a Region Function to an Int Array.
 *
 * @author Dennis Soemers and Eric.Piette
 */
public class Array extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Our region function */
	private final RegionFunction region;
	
	/** Our list of int functions. */
	private final IntFunction[] ints;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For creating an array from a region function.
	 * 
	 * @param region The region function to be converted into an int array.
	 * 
	 * @example (array (sites Board)))
	 */
	public Array(final RegionFunction region)
	{
		this.region = region;
		this.ints = null;
	}
	
	/**
	 * For creating an array from a list of int functions.
	 * 
	 * @param ints The int functions composing the array.
	 * 
	 * @example (array {5 3 2})
	 */
	public Array(final IntFunction[] ints)
	{
		this.region = null;
		this.ints = ints;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		if(region != null)
			return region.eval(context).sites();
		else
		{
			int[] array = new int[ints.length];
			for(int i = 0; i < array.length; i++)
				array[i] = ints[i].eval(context);
			return array;
		}
	}

	@Override
	public long gameFlags(final Game game)
	{
		if(region != null)
			return region.gameFlags(game);
		else
		{
			long gameFlags = 0L;
			for(IntFunction intFn : ints)
				gameFlags |= intFn.gameFlags(game);
			return gameFlags;
		}
	}

	@Override
	public boolean isStatic()
	{
		if(region != null)
			return region.isStatic();
		else
		{
			for(IntFunction intFn : ints)
				if(!intFn.isStatic())
					return false;
			return true;
		}
	}

	@Override
	public void preprocess(final Game game)
	{
		if(region != null)
			region.preprocess(game);
		else
		{
			for(IntFunction intFn : ints)
				intFn.preprocess(game);
		}
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		if(region != null)
			return region.concepts(game);
		else
		{
			final BitSet concepts = new BitSet();
			for(IntFunction intFn : ints)
				concepts.or(intFn.concepts(game));
			return concepts;
		}
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		if(region != null)
			return region.writesEvalContextRecursive();
		else
		{
			final BitSet writeEvalContext = new BitSet();
			for(IntFunction intFn : ints)
				writeEvalContext.or(intFn.writesEvalContextRecursive());
			return writeEvalContext;
		}
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		if(region != null)
			return region.readsEvalContextRecursive();
		else
		{
			final BitSet readEvalContext = new BitSet();
			for(IntFunction intFn : ints)
			readEvalContext.or(intFn.readsEvalContextRecursive());
			return readEvalContext;
		}
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		if(region != null)
			return region.missingRequirement(game);
		else
		{
			for(IntFunction intFn : ints)
				if(intFn.missingRequirement(game))
					return true;
			
			return false;
		}
	}

	@Override
	public boolean willCrash(final Game game)
	{
		if(region != null)
			return region.willCrash(game);
		else
		{
			for(IntFunction intFn : ints)
				if(intFn.willCrash(game))
					return true;
			
			return false;
		}
	}

}
