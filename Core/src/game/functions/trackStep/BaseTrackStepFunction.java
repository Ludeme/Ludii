package game.functions.trackStep;

import annotations.Hide;
import game.functions.dim.DimFunction;
import game.types.board.TrackStepType;
import game.util.directions.CompassDirection;
import other.BaseLudeme;
import other.context.Context;

/**
 * Common functionality for TrackStepFunction - override where necessary.
 * 
 * @author cambolbro
 */
@Hide
public abstract class BaseTrackStepFunction extends BaseLudeme implements TrackStepFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Dim function (includes positive integers). */
	protected final DimFunction dim;
	
	/** Compass direction. */
	protected final CompassDirection dirn;
	
	/** Constant value: Off/End/Repeat. */
	protected final TrackStepType step;

	/** Precompute once and cache if possible. */
	protected TrackStep precomputedTrackStep = null;

	//-------------------------------------------------------------------------

	/**
	 * The base range function.
	 * 
	 * @param dim Dimension function.
	 * @param dirn Compass direction.
	 * @param step Constant (Off/End/Repeat).
	 */
	public BaseTrackStepFunction
	(
		final DimFunction 	   dim,
		final CompassDirection dirn,
		final TrackStepType    step

	)
	{
		this.dim  = dim;
		this.dirn = dirn;
		this.step = step;
	}

	//-------------------------------------------------------------------------

	@Override
	public TrackStep eval(final Context context)
	{
		System.out.println("BaseRangeFunction.eval(): Should not be called directly; call subclass.");		
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Integer value or dim function.
	 */
	@Override
	public DimFunction dim()
	{
		return dim;
	}
	
	/**
	 * @return Compass direction.
	 */
	@Override
	public CompassDirection dirn()
	{
		return dirn;
	}
	
	/**
	 * @return Track step type: Off/End/Repeat.
	 */
	@Override
	public TrackStepType step()
	{
		return step;
	}

	//-------------------------------------------------------------------------

}
