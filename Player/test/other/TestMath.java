package other;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import main.collections.ListUtils;

/**
 * Some tests for math functions
 *
 * @author Dennis Soemers
 */
public class TestMath
{
	
	@Test
	public static void testCombinations()
	{
		assertEquals(ListUtils.numCombinationsWithReplacement(5, 1), 5);
		assertEquals(ListUtils.numCombinationsWithReplacement(5, 2), 15);
		assertEquals(ListUtils.numCombinationsWithReplacement(5, 3), 35);
		assertEquals(ListUtils.numCombinationsWithReplacement(5, 4), 70);
		assertEquals(ListUtils.numCombinationsWithReplacement(5, 5), 126);
		assertEquals(ListUtils.numCombinationsWithReplacement(5, 8), 495);
		assertEquals(ListUtils.numCombinationsWithReplacement(49, 2), 1225);
	}

}
