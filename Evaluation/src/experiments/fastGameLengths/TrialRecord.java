package experiments.fastGameLengths;

import other.trial.Trial;

/**
 * Record of a trial result for evaluation experiments.
 * @author cambolbro
 */
public class TrialRecord
{
	private int starter;
	private Trial trial;
	
	public TrialRecord(final int starter, final Trial trial)
	{
		this.starter = starter;
		this.trial = trial;
	}
	
	public int starter()
	{
		return starter;
	}
	
	public Trial trial()
	{
		return trial;
	}
}
