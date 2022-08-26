package travis.quickTests.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import main.collections.FVector;
import training.expert_iteration.ExItExperience;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;

/**
 * Unit tests for Prioritized Experience Replay implementation.
 * 
 * Using similar unit tests (where applicable) as in Dopamine:
 * https://github.com/google/dopamine/blob/master/tests/dopamine/replay_memory/prioritized_replay_buffer_test.py
 *
 * @author Dennis Soemers
 */
@SuppressWarnings("static-method")
public class TestPrioritizedExperienceReplay
{

	/** Capacity of replay buffers in unit tests */
	private static final int REPLAY_CAPACITY = 100;
	
	/**
	 * @return A default buffer for testing purposes
	 */
	private static PrioritizedReplayBuffer createDefaultMemory()
	{
		return new PrioritizedReplayBuffer(REPLAY_CAPACITY);
	}
	
	/**
	 * @param memory Replay buffer to add blank experience to
	 * @return Index at which the new experience should be in the buffer
	 */
	private static int addBlank(final PrioritizedReplayBuffer memory)
	{
		return addBlank(memory, 1.f);
	}
	
	/**
	 * @param memory Replay buffer to add blank experience to
	 * @param priority Priority with which to add new sample
	 * @return Index at which the new experience should be in the buffer
	 */
	private static int addBlank(final PrioritizedReplayBuffer memory, final float priority)
	{
		memory.add(new ExItExperience(null, null, null, null, null, 1.f), priority);
		return (memory.cursor() - 1) % REPLAY_CAPACITY;
	}
	
	@Test
	public void testAdd()
	{
		System.out.println("Running testAdd()");
		final PrioritizedReplayBuffer memory = createDefaultMemory();
		assertEquals(memory.cursor(), 0);
		
		addBlank(memory);
		assertEquals(memory.cursor(), 1);
		assertEquals(memory.addCount(), 1L);
		System.out.println("Finished testAdd()");
	}
	
	@Test
	public void testSetAndGetPriority()
	{
		System.out.println("Running testSetAndGetPriority()");
		final PrioritizedReplayBuffer memory = createDefaultMemory();
		final int batchSize = 7;
		final int[] indices = new int[batchSize];
		
		for (int index = 0; index < batchSize; ++index)
		{
			indices[index] = addBlank(memory);
		}
		
		final float[] priorities = new float[batchSize];
		for (int i = 0; i < batchSize; ++i)
		{
			priorities[i] = i;
		}
		
		memory.setPriorities(indices, priorities);
		
		// We send the indices in reverse order and verify
		// that the priorities come back in that same order
		final int[] reversedIndices = new int[batchSize];
		
		for (int i = 0; i < batchSize; ++i)
		{
			reversedIndices[i] = indices[batchSize - i - 1];
		}
		
		final float[] fetchedPriorities = memory.getPriorities(reversedIndices);
		
		for (int i = 0; i < batchSize; ++i)
		{
			assertEquals((float) Math.pow(priorities[i], memory.alpha()), fetchedPriorities[batchSize - 1 - i], 0.0001f);
		}
		
		System.out.println("Finished testSetAndGetPriority()");
	}
	
	@Test
	public void testLowPriorityElementNotFrequentlySampled()
	{
		System.out.println("Running testLowPriorityElementNotFrequentlySampled()");
		final PrioritizedReplayBuffer memory = createDefaultMemory();
		
		// add 0-priority sample
		addBlank(memory, 0.f);
		
		// add more items with default priority (of 1)
		for (int i = 0; i < 3; ++i)
		{
			memory.add(new ExItExperience(null, null, null, null, new FVector(0), 1.f), 1.f);
		}
		
		// this test should always pass
		for (int i = 0; i < 100; ++i)
		{
			final List<ExItExperience> batch = memory.sampleExperienceBatch(2);
			
			for (final ExItExperience sample : batch)
			{
				assertNotNull(sample.expertValueEstimates());
			}
		}
		
		System.out.println("Finished testLowPriorityElementNotFrequentlySampled()");
	}
	
	public void testNoIdxOutOfBounds()
	{
		System.out.println("Running testNoIdxOutOfBounds()");
		for (int rep = 0; rep < 100; ++rep)
		{
			// random capacity in [100, 4500]
			final int capacity = (int) (4400 * Math.random() + 100);
			
			// create buffer
			final PrioritizedReplayBuffer buffer = new PrioritizedReplayBuffer(capacity);
			
			// fill up replay buffer with random priorities
			for (int i = 0; i < capacity; ++i)
			{
				addBlank(buffer, (float) Math.random());
			}
			
			// sample indices, make sure we never exceed capacity
			final int[] indices = buffer.sampleIndexBatch(Math.min(500, capacity));
			for (final int idx : indices)
			{
				assert (idx >= 0);
				assert (idx < capacity);
			}
		}
		System.out.println("Finished testNoIdxOutOfBounds()");
	}

}
