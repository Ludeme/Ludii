package other.context;

import other.state.CopyOnWriteState;
import other.state.State;
import other.trial.TempTrial;
import other.trial.Trial;

/**
 * A temporary version of a context. Can only be constructed by "copying" another
 * context. Contains optimisations that may make it invalid for long-term use,
 * only intended to be used shortly and temporarily after creation. Changes to
 * the source-context may also seep through into this temp context due to
 * copy-on-write optimisations, making it potentially invalid.
 *
 * @author Dennis Soemers
 */
public final class TempContext extends Context
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param other
	 */
	public TempContext(final Context other)
	{
		super(other);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected State copyState(final State otherState)
	{
		return otherState == null ? null : new CopyOnWriteState(otherState);
	}
	
	@Override
	protected Trial copyTrial(final Trial otherTrial)
	{
		return new TempTrial(otherTrial);
	}
	
	//-------------------------------------------------------------------------

}
