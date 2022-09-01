package other;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import gnu.trove.list.array.TIntArrayList;
import main.collections.ChunkSet;

/**
 * Some tests for ChunkSets
 *
 * @author Dennis Soemers
 */
@SuppressWarnings("static-method")
public class TestChunkSets
{
	@Test
	public void testViolatesNotA()
	{
		// State chunkset: 		0011 1111 0011 0001 0111
		final ChunkSet s = new ChunkSet(4, 5);
		s.setChunk(0, 3);
		s.setChunk(1, 15);
		s.setChunk(2, 3);
		s.setChunk(3, 1);
		s.setChunk(4, 7);
		
		// Pattern chunkset:	1111 0111 0000 0111 0111		
		final ChunkSet p = new ChunkSet(4, 5);
		p.setChunk(0, 15);
		p.setChunk(1, 7);
		p.setChunk(2, 0);
		p.setChunk(3, 7);
		p.setChunk(4, 7);
		
		// Mask chunkset:		1111 1111 1111 1111 1111
		final ChunkSet m = new ChunkSet(4, 5);
		m.set(0, 4 * 5);
		
		// The last chunk is same for both state and pattern, so we violate the "not" pattern
		assert(s.violatesNot(m, p));
	}
	
	@Test
	public void testViolatesNotB()
	{
		// State chunkset: 		0011 1111 0011 0001 0111
		final ChunkSet s = new ChunkSet(4, 5);
		s.setChunk(0, 3);
		s.setChunk(1, 15);
		s.setChunk(2, 3);
		s.setChunk(3, 1);
		s.setChunk(4, 7);
		
		// Pattern chunkset:	1111 0111 0000 0111 0110		
		final ChunkSet p = new ChunkSet(4, 5);
		p.setChunk(0, 15);
		p.setChunk(1, 7);
		p.setChunk(2, 0);
		p.setChunk(3, 7);
		p.setChunk(4, 6);
		
		// Mask chunkset:		1111 1111 1111 1111 1111
		final ChunkSet m = new ChunkSet(4, 5);
		m.set(0, 4 * 5);
		
		// No chunks same for both state and pattern, so we do not violate the "not" pattern
		assert(!s.violatesNot(m, p));
	}
	
	@Test
	public void testViolatesNotC()
	{
		// State chunkset: 		0011 1111 0011 0001 0111
		final ChunkSet s = new ChunkSet(4, 5);
		s.setChunk(0, 3);
		s.setChunk(1, 15);
		s.setChunk(2, 3);
		s.setChunk(3, 1);
		s.setChunk(4, 7);
		
		// Pattern chunkset:	1111 0111 0000 0111 0000		
		final ChunkSet p = new ChunkSet(4, 5);
		p.setChunk(0, 15);
		p.setChunk(1, 7);
		p.setChunk(2, 0);
		p.setChunk(3, 7);
		p.setChunk(4, 0);
		
		// Mask chunkset:		1111 1111 1111 1111 1111
		final ChunkSet m = new ChunkSet(4, 5);
		m.set(0, 4 * 5);
		
		// No chunks same for both state and pattern, so we do not violate the "not" pattern
		assert(!s.violatesNot(m, p));
	}
	
	@Test
	public void testViolatesNotD()
	{
		// State chunkset: 		0011 1111 0011 0000 0111
		final ChunkSet s = new ChunkSet(4, 5);
		s.setChunk(0, 3);
		s.setChunk(1, 15);
		s.setChunk(2, 3);
		s.setChunk(3, 0);
		s.setChunk(4, 7);
		
		// Pattern chunkset:	1111 0111 0000 0111 0110		
		final ChunkSet p = new ChunkSet(4, 5);
		p.setChunk(0, 15);
		p.setChunk(1, 7);
		p.setChunk(2, 0);
		p.setChunk(3, 7);
		p.setChunk(4, 6);
		
		// Mask chunkset:		1111 1111 1111 1111 1111
		final ChunkSet m = new ChunkSet(4, 5);
		m.set(0, 4 * 5);
		
		// No chunks same for both state and pattern, so we do not violate the "not" pattern
		assert(!s.violatesNot(m, p));
	}
	
	@Test
	public void testViolatesNotE()
	{
		// State chunkset: 		0011 1111 0011 0000 0111
		final ChunkSet s = new ChunkSet(4, 5);
		s.setChunk(0, 3);
		s.setChunk(1, 15);
		s.setChunk(2, 3);
		s.setChunk(3, 0);
		s.setChunk(4, 7);
		
		// Pattern chunkset:	1111 0111 0000 0000 0110		
		final ChunkSet p = new ChunkSet(4, 5);
		p.setChunk(0, 15);
		p.setChunk(1, 7);
		p.setChunk(2, 0);
		p.setChunk(3, 0);
		p.setChunk(4, 6);
		
		// Mask chunkset:		1111 1111 1111 1111 1111
		final ChunkSet m = new ChunkSet(4, 5);
		m.set(0, 4 * 5);
		
		// The fourth chunk is same for both state and pattern, so we violate the "not" pattern
		assert(s.violatesNot(m, p));
	}
	
	@Test
	public void testGetNonzeroChunks()
	{
		// Pick a random power of 2 (up to and including 4, for max chunksize of 16)
		final int power = ThreadLocalRandom.current().nextInt(5);
		final int chunkSize = 1 << power;
		
		// Pick a random number of chunks between 1 and 100 (should be no need to test excessively big ones)
		final int numChunks = ThreadLocalRandom.current().nextInt(1, 101);
		
		final ChunkSet chunkset = new ChunkSet(chunkSize, numChunks);
		
		// Randomly pick a number of chunks to set to non-zero values
		final int numNonzeroChunks = ThreadLocalRandom.current().nextInt(numChunks + 1);
		
		// Randomly pick which chunks are nonzero
		final TIntArrayList nonzeroChunks = new TIntArrayList(numChunks);
		for (int i = 0; i < numChunks; ++i)
		{
			nonzeroChunks.add(i);
		}
		while (nonzeroChunks.size() > numNonzeroChunks)
		{
			nonzeroChunks.removeAt(ThreadLocalRandom.current().nextInt(nonzeroChunks.size()));
		}
		
		// Set random values to all selected nonzero chunks
		for (int i = 0; i < nonzeroChunks.size(); ++i)
		{
			chunkset.setChunk(nonzeroChunks.getQuick(i), ThreadLocalRandom.current().nextInt(1, (1 << chunkSize)));
		}
		
		final TIntArrayList nonzeroChunkIndices = chunkset.getNonzeroChunks();
		assertEquals(nonzeroChunkIndices.size(), numNonzeroChunks);
		
		for (int i = 0; i < numNonzeroChunks; ++i)
		{
			assertEquals(nonzeroChunkIndices.getQuick(i), nonzeroChunks.getQuick(i));
		}
	}

}
