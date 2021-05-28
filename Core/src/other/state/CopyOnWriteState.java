package other.state;

import other.state.track.OnTrackIndices;
import other.state.track.OnTrackIndicesCOW;

/**
 * A subclass of State, with copy-on-write optimisations. Note
 * that changes to the state that we copy from may seep through
 * into this copy.
 *
 * @author Dennis Soemers
 */
public final class CopyOnWriteState extends State
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * Copy constructor (with some copy-on-write behaviour)
	 * @param other
	 */
	public CopyOnWriteState(final State other)
	{
		super(other);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected OnTrackIndices copyOnTrackIndices(final OnTrackIndices otherOnTrackIndices)
	{
		return otherOnTrackIndices == null ? null : new OnTrackIndicesCOW(otherOnTrackIndices);
	}
	
	//-------------------------------------------------------------------------

}
