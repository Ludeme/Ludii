package game.functions.trackStep;

import java.util.BitSet;

import annotations.Hide;
import annotations.Or;
import game.Game;
import game.functions.dim.DimFunction;
import game.types.board.TrackStepType;
import game.util.directions.CompassDirection;
import other.context.Context;

/**
 * Returns a track step entry.
 * 
 * @author cambolbro
 */
@Hide
public final class TrackStep extends BaseTrackStepFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor defining either a number, a direction or a step type.
	 * 
	 * @param dim  Dim function or integer.
	 * @param dirn Compass direction.    
	 * @param step Track step type: Off/End/Repeat.
	 */
	public TrackStep
	(
		@Or final DimFunction dim,
		@Or final CompassDirection dirn,
		@Or final TrackStepType step
	)
	{
		super(dim, dirn, step);
		
		int numNonNull = 0;
		if (dim != null)
			numNonNull++;
		if (dirn != null)
			numNonNull++;
		if (step != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("TrackStep(): Exactly one parameter must be non-null.");
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * For a range between two int functions.
//	 * 
//	 * @param min Lower extent of range (inclusive).
//	 * @param max Upper extent of range (inclusive) [same as min].
//	 *
//	 * @example (range (from) (to))
//	 */
//	public TrackStep
//	(
//			 final IntFunction min,
//		@Opt final IntFunction max
//	)
//	{
//		super(min, max == null ? min : max);
//	}
//
//	/**
//	 * For a range between two integers.
//	 * 
//	 * @param min Lower extent of range (inclusive).
//	 * @param max Upper extent of range (inclusive).
//	 *
//	 * @example (range 1 9)
//	 */
//	public Range
//	(
//		final Integer min, 
//		final Integer max
//	) 
//	{
//		super(new IntConstant(min.intValue()), new IntConstant(max.intValue()));
//	}
	
	//-------------------------------------------------------------------------

	@Override
	public TrackStep eval(final Context context)
	{
		if (precomputedTrackStep != null)
			return precomputedTrackStep;

		return this;  //new game.util.math.Range(Integer.valueOf(minFn.eval(context)), Integer.valueOf(maxFn.eval(context)));
	}

	//-------------------------------------------------------------------------

//	/**
//	 * @param context The context.
//	 * 
//	 * @return The minimum of the range.
//	 */
//	public int min(final Context context)
//	{
//		return minFn.eval(context);
//	}
//
//	/**
//	 * @param context The context.
//	 * 
//	 * @return The maximum of the range.
//	 */
//	public int max(final Context context)
//	{
//		return maxFn.eval(context);
//	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return 
			dim != null && dim.isStatic()
			|| 
			dirn != null   // always static 
			||
			step != null;  // always static 
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		
		if (dim != null)
			flags |= dim.gameFlags(game);
		
		// TODO...
//		if (dirn != null)
//			flags |= dirn.gameFlags(game);
//		
//		if (step != null)
//			flags |= step.gameFlags(game);
		
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		// TODO...
//		if (dim != null)
//			concepts.or(dim.concepts(game));
//		
//		if (dirn != null)
//			concepts.or(dirn.concepts(game));
//		
//		if (step != null)
//			concepts.or(step.concepts(game));
		
		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// TODO...
//		if (dim != null)
//			missingRequirement |= dim.missingRequirement(game);
//		
//		if (dirn != null)
//			missingRequirement |= dirn.missingRequirement(game);
//		
//		if (step != null)
//			missingRequirement |= step.missingRequirement(game);
		
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;

		// TODO...
//		if (dim != null)
//			willCrash |= dim.willCrash(game);
//		
//		if (dirn != null)
//			willCrash |= dirn.willCrash(game);
//		
//		if (step != null)
//			willCrash |= step.willCrash(game);
		
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (dim != null)
			dim.preprocess(game);

		// TODO...
//		if (dirn != null)
//			dirn.preprocess(game);
//		
//		if (step != null)
//			step.preprocess(game);
		
		if (isStatic())
			precomputedTrackStep = eval(new Context(game, null));
	}
	
	@Override
	public String toString()
	{
		String str = "[";
		
		if (dim != null)
			str += dim.toString();
		
		if (dirn != null)
			str += dirn.toString();
		
		if (step != null)
			str += step.toString();
		
		str += "]";
		return str;
	}

	//-------------------------------------------------------------------------
	
}
