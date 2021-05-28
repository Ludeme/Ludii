package game.util.equipment;

import java.io.Serializable;

import annotations.Hide;
import annotations.Or;
import game.types.board.TrackStepType;
import game.util.directions.CompassDirection;
import other.BaseLudeme;

//-----------------------------------------------------------------------------

/**
 * Defines a step within a track.
 * 
 * @author cambolbro
 * 
 * @remarks Track steps may be specified by number, dim function, compass direction 
 *          or constant attribute (End/Off/Repeat).
 */
@Hide
public final class TrackStep extends BaseLudeme implements Serializable
{
	/** */
	private static final long serialVersionUID = 1L;

	/** Dim function or integer. */
	private final Integer dim;
	
	/** Compass direction. */
	private final CompassDirection dirn;
	
	/** Constant value: Off/End/Repeat. */
	private final TrackStepType step;
	
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
		@Or final Integer dim,
		@Or final CompassDirection dirn,
		@Or final TrackStepType step
	)
	{
		int numNonNull = 0;
		if (dim != null)
			numNonNull++;
		if (dirn != null)
			numNonNull++;
		if (step != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("TrackStep(): Exactly one parameter must be non-null.");

		this.dim  = dim;
		this.dirn = dirn;
		this.step = step;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Integer value or dim function.
	 */
	public Integer dim()
	{
		return dim;
	}
	
	/**
	 * @return Compass direction.
	 */
	public CompassDirection dirn()
	{
		return dirn;
	}
	
	/**
	 * @return Track step type: Off/End/Repeat.
	 */
	public TrackStepType step()
	{
		return step;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		if (dim != null)
			return dim.hashCode();
		if (dirn != null)
			return dirn.hashCode();
		if (step != null)
			return step.hashCode();
		return 0;
	}

	@Override
	public boolean equals(Object other)
	{
		return 
			other instanceof TrackStep 
			&& 
			(dim == null  && ((TrackStep)other).dim  == null || dim.equals(((TrackStep)other).dim))
			&& 
			(dirn == null && ((TrackStep)other).dirn == null || dirn.equals(((TrackStep)other).dirn))
			&& 
			(step == null && ((TrackStep)other).step == null || step.equals(((TrackStep)other).step));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "A TrackStep...";
	}

	//-------------------------------------------------------------------------

}
