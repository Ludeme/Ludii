package other.trial;

import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Record of a complete match.
 * 
 * @author cambolbro
 */
public final class MatchTrial
{
	/** List of instances making up this match. */
	protected final List<Trial> trials = new ArrayList<Trial>();

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param trial
	 */
	public MatchTrial(final Trial trial)
	{
		trials.add(trial);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return List of instances.
	 */
	public List<Trial> trials()
	{
		return trials;
	}

	//-------------------------------------------------------------------------

	/**
	 * Clear this match.
	 */
	public void clear()
	{
		trials.clear();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Current episode being played.
	 */
	public Trial currentTrial()
	{
		return trials.isEmpty() ? null : trials.get(trials.size() - 1);
	}

	//-------------------------------------------------------------------------

}
