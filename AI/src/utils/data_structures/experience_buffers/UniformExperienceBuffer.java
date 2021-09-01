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

import expert_iteration.ExItExperience;
import expert_iteration.ExItExperience.ExItExperienceState;
import game.Game;
import game.equipment.container.Container;
import other.state.container.ContainerState;

/**
 * A size-restricted, FIFO buffer to contain samples of experience.
 * 
 * @author Dennis Soemers
 */
public class UniformExperienceBuffer implements Serializable, ExperienceBuffer
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** 
	 * Maximum number of elements the buffer can contain before removing 
	 * elements from the front.
	 */
	protected final int replayCapacity;
	
	/** This contains our data */
	protected final ExItExperience[] buffer;
	
	/** How many elements did we add? */
	protected long addCount;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param replayCapacity
	 */
	public UniformExperienceBuffer(final int replayCapacity)
	{
		this.replayCapacity = replayCapacity;
		buffer = new ExItExperience[replayCapacity];
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void add(final ExItExperience experience)
	{
		buffer[cursor()] = experience;
		++addCount;
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public List<ExItExperience> sampleExperienceBatch(final int batchSize)
	{
		return sampleExperienceBatchUniformly(batchSize);
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
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Index of next location that we'll write to
	 */
	private int cursor()
	{
		return (int) (addCount % replayCapacity);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @param filepath
	 * @return Experience buffer restored from binary file
	 */
	public static UniformExperienceBuffer fromFile
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
			final UniformExperienceBuffer buffer = (UniformExperienceBuffer) reader.readObject();
			
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
							if (container != null && 
									container.name().equals(containerName))
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
