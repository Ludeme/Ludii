package utils.data_structures.experience_buffers;

import expert_iteration.ExItExperience;

/**
 * Interface for experience buffers. Declares common methods
 * that we expect in uniform as well as Prioritized Experience Replay
 * buffers.
 * 
 * @author Dennis Soemers
 */
public interface ExperienceBuffer
{
	
	/**
	 * Adds a new sample of experience.
	 * Defaulting to the max observed priority level in the case of PER.
	 * 
	 * @param experience
	 */
	public void add(final ExItExperience experience);
	
	/**
	 * @param batchSize
	 * @return A batch of the given batch size, sampled uniformly with 
	 * replacement.
	 */
	public ExItExperience[] sampleExperienceBatch(final int batchSize);
	
	/**
	 * @param batchSize
	 * @return Sample of batchSize tuples of experience, sampled uniformly
	 */
	public ExItExperience[] sampleExperienceBatchUniformly(final int batchSize);
	
	/**
	 * Writes this complete buffer to a binary file
	 * @param filepath
	 */
	public void writeToFile(final String filepath);

}
