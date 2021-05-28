package other.trial;

import other.move.MoveSequence;

/**
 * A temporary version of a Trial. Can only be constructed by "copying" another
 * trial. Contains optimisations that may make it invalid for long-term use,
 * only intended to be used shortly and temporarily after creation. Changes to
 * the source-Trial may also seep through into this temp Trial, making it 
 * potentially invalid.
 *
 * @author Dennis Soemers
 */
public final class TempTrial extends Trial
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param other
	 */
	public TempTrial(final Trial other)
	{
		super(other);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected MoveSequence copyMoveSequence(final MoveSequence otherSequence)
	{
		return new MoveSequence(otherSequence, true);
	}
	
	//-------------------------------------------------------------------------

}
