package utils.data_structures.experience_buffers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import game.equipment.container.Container;
import other.state.container.ContainerState;
import training.expert_iteration.ExItExperience;
import training.expert_iteration.ExItExperience.ExItExperienceState;

/**
 * Replay Buffer for Prioritized Experience Replay, as described
 * by Schaul et a.l (2015).
 * 
 * Implementation based on that from Dopamine (but translated to Java):
 * https://github.com/google/dopamine/blob/master/dopamine/replay_memory/prioritized_replay_buffer.py
 * 
 * Implementation afterwards also adjusted to bring back in some of the
 * hyperparameters from the original publication. Changes also inspired by stable-baselines implementation:
 * https://github.com/hill-a/stable-baselines/blob/master/stable_baselines/deepq/replay_buffer.py
 * 
 *
 * @author Dennis Soemers
 */
public class PrioritizedReplayBuffer implements Serializable, ExperienceBuffer
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** Our maximum capacity */
	protected final int replayCapacity;
	
	/** Our sum tree data structure */
	protected final SumTree sumTree;
	
	/** This contains our data */
	protected final ExItExperience[] buffer;
	
	/** How many elements did we add? */
	protected long addCount;
	
	/** Hyperparameter for sampling. 0 --> uniform, 1 --> proportional to priorities */
	protected final double alpha;
	
	/** Hyperparameter for importance sampling. 0 --> no correction, 1 --> full correction for bias */
	protected final double beta;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * Default hyperparam values of 0.5 for alpha as well as beta,
	 * based on the defaults in Dopamine.
	 * 
	 * @param replayCapacity Maximum capacity of our buffer
	 */
	public PrioritizedReplayBuffer(final int replayCapacity)
	{
		this(replayCapacity, 0.5, 0.5);
	}
	
	/**
	 * Constructor
	 * @param replayCapacity Maximum capacity of our buffer
	 * @param alpha
	 * @param beta
	 */
	public PrioritizedReplayBuffer(final int replayCapacity, final double alpha, final double beta)
	{
		this.replayCapacity = replayCapacity;
		this.sumTree = new SumTree(replayCapacity);
		buffer = new ExItExperience[replayCapacity];		
		addCount = 0L;
		this.alpha = alpha;
		this.beta = beta;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void add(final ExItExperience experience)
	{
		sumTree.set(cursor(), sumTree.maxRecordedPriority());
		buffer[cursor()] = experience;
		++addCount;
	}
	
	/**
	 * Adds a new sample of experience, with given priority level.
	 * @param experience
	 * @param priority
	 */
	public void add(final ExItExperience experience, final float priority)
	{
		sumTree.set(cursor(), (float) Math.pow(priority, alpha));
		buffer[cursor()] = experience;
		++addCount;
	}
	
	/**
	 * @param indices
	 * @return Array of priorities for the given indices
	 */
	public float[] getPriorities(final int[] indices)
	{
		final float[] priorities = new float[indices.length];
		for (int i = 0; i < indices.length; ++i)
		{
			priorities[i] = sumTree.get(indices[i]);
		}
		return priorities;
	}
	
	/**
	 * @return True if we're empty
	 */
	public boolean isEmpty()
	{
		return addCount == 0L;
	}
	
	/**
	 * @return True if we're full
	 */
	public boolean isFull()
	{
		return addCount >= replayCapacity;
	}
	
	/**
	 * @return Number of samples we currently contain
	 */
	public int size()
	{
		if (isFull())
			return replayCapacity;
		else
			return (int) addCount;
	}
	
	/**
	 * @param batchSize
	 * @return Sample of batchSize indices (stratified).
	 */
	public int[] sampleIndexBatch(final int batchSize)
	{
		return sumTree.stratifiedSample(batchSize);
	}
	
	@Override
	public List<ExItExperience> sampleExperienceBatch(final int batchSize)
	{
		final int numSamples = (int) Math.min(batchSize, addCount);
		final List<ExItExperience> batch = new ArrayList<ExItExperience>(numSamples);
		final int[] indices = sampleIndexBatch(numSamples);
		
		final double[] weights = new double[batchSize];
		double maxWeight = Double.NEGATIVE_INFINITY;
		final int maxIdx = Math.min(replayCapacity, (int) addCount) - 1;
		
		for (int i = 0; i < numSamples; ++i)
		{
			// in very rare cases (when query val approximates 1.0 very very closely)
			// we'll sample an invalid index; we'll just round down to the last valid
			// index
			if (indices[i] > maxIdx)
				indices[i] = maxIdx;
		}
		
		final float[] priorities = getPriorities(indices);
		
		for (int i = 0; i < numSamples; ++i)
		{
			batch.add(buffer[indices[i]]);
			double prob = priorities[i] / sumTree.totalPriority();			
			weights[i] = Math.pow((1.0 / size()) * (1.0 / prob), beta);
			maxWeight = Math.max(maxWeight, weights[i]);
		}
		
		for (int i = 0; i < numSamples; ++i)
		{
			batch.get(i).setWeightPER((float) (weights[i] / maxWeight));
			batch.get(i).setBufferIdx(indices[i]);
		}
		
		return batch;
	}
	
	@Override
	public List<ExItExperience> sampleExperienceBatchUniformly(final int batchSize)
	{
		final int numSamples = (int) Math.min(batchSize, addCount);
		final List<ExItExperience> batch = new ArrayList<ExItExperience>(numSamples);
		final int bufferSize = size();
		
		for (int i = 0; i < numSamples; ++i)
		{
			batch.add(buffer[ThreadLocalRandom.current().nextInt(bufferSize)]);
		}

		return batch;
	}
	
	@Override
	public ExItExperience[] allExperience()
	{
		return buffer;
	}
	
	/**
	 * Sets priority levels 
	 * @param indices
	 * @param priorities
	 */
	public void setPriorities(final int[] indices, final float[] priorities)
	{
		assert (indices.length == priorities.length);
		
		for (int i = 0; i < indices.length; ++i)
		{
			if (indices[i] >= 0)
				sumTree.set(indices[i], (float) Math.pow(priorities[i], alpha));
		}
	}
	
	/**
	 * @return Our sum tree data structure.
	 */
	public SumTree sumTree()
	{
		return sumTree;
	}
	
	/**
	 * @return Alpha hyperparam
	 */
	public double alpha()
	{
		return alpha;
	}
	
	/**
	 * @return Beta hyperparam
	 */
	public double beta()
	{
		return beta;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Number of samples we have added
	 */
	public long addCount()
	{
		return addCount;
	}
	
	/**
	 * @return Index of next location that we'll write to
	 */
	public int cursor()
	{
		return (int) (addCount % replayCapacity);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @param filepath
	 * @return Experience buffer restored from binary file
	 */
	public static PrioritizedReplayBuffer fromFile
	(
		final Game game,
		final String filepath
	)
	{
		try 
		(
			final ObjectInputStream reader = 
				new ObjectInputStream(new BufferedInputStream(new FileInputStream(filepath)))
		)
		{
			final PrioritizedReplayBuffer buffer = (PrioritizedReplayBuffer) reader.readObject();
			
			// special handling of objects that contain game states;
			// we need to fix their references to containers for all ItemStates
			for (final ExItExperience exp : buffer.buffer)
			{
				if (exp == null)
					continue;

				final ExItExperienceState state = exp.state();
				final ContainerState[] containerStates = state.state().containerStates();
				
				for (final ContainerState containerState : containerStates)
				{
					if (containerState != null)
					{
						final String containerName = containerState.nameFromFile();
						
						for (final Container container : game.equipment().containers())
						{
							if (container != null && container.name().equals(containerName))
							{
								containerState.setContainer(container);
								break;
							}
						}
					}
				}
			}
			
			return buffer;
		} 
		catch (final IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void writeToFile(final String filepath)
	{
		try 
		(
			final ObjectOutputStream out = 
				new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filepath)))
		)
		{
			out.writeObject(this);
			out.flush();
			out.close();
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
