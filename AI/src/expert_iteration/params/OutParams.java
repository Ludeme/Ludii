package expert_iteration.params;

import java.io.File;

/**
 * Wrapper around params for output/file writing.
 *
 * @author Dennis Soemers
 */
public class OutParams
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * When do we want to store checkpoints of trained weights?
	 * @author Dennis Soemers
	 */
	public enum CheckpointTypes
	{
		/** Store checkpoint after N self-play training games */
		Game,
		/** Store checkpoint after N weight updates */
		WeightUpdate
	}
	
	//-------------------------------------------------------------------------
	
	/** Output directory */
	public File outDir;
	
	/** When do we store checkpoints of trained weights? */
	public CheckpointTypes checkpointType;
	
	/** Frequency of checkpoint updates */
	public int checkpointFrequency;
	
	/** If true, we suppress a bunch of log messages to a log file. */
	public boolean noLogging;
	
	//-------------------------------------------------------------------------

}
