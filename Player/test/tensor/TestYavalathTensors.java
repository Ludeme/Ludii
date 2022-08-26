package tensor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import utils.LudiiGameWrapper;
import utils.LudiiStateWrapper;

/**
 * Unit test to test a bunch of things in Yavalath's tensors
 *
 * @author Dennis Soemers
 */
public class TestYavalathTensors
{
	
	@Test
	public static void test()
	{
		final LudiiGameWrapper game = LudiiGameWrapper.construct("Yavalath.lud");
		final LudiiStateWrapper state = new LudiiStateWrapper(game);
		
		final int[] stateTensorsShape = game.stateTensorsShape();
		final int[] moveTensorsShape = game.moveTensorsShape();
				
		assertArrayEquals(stateTensorsShape, new int[]{10, 9, 17});
		assertArrayEquals(moveTensorsShape, new int[]{3, 9, 17});
		
		float[][][] stateTensor = state.toTensor();
		
		// State tensor channels:
		// 0: Piece Type 1 (Ball1)
		// 1: Piece Type 2 (Ball2)
		// 2: Is Player 1 the current mover?
		// 3: Is Player 2 the current mover?
		// 4: Did Swap Occur?
		// 5: Does position exist in container 0 (Board)?
		// 6: Last move's from-position
		// 7: Last move's to-position
		// 8: Second-to-last move's from-position
		// 9: Second-to-last move's to-position
		
		// First two tensors must be all-zeros: no pieces on board yet
		assertAllZero(stateTensor[0]);
		assertAllZero(stateTensor[1]);
		
		// Third tensor must be all-ones: it's Player 1's turn
		assertAllOne(stateTensor[2]);
		
		// Fourth tensor must be all-zeros: it's not Player 2's turn
		assertAllZero(stateTensor[3]);
		
		// No swap occurred, so fifth tensor all 0s
		assertAllZero(stateTensor[4]);
		
		// Board has 61 cells, so must have 61 1s in sixth tensor
		assertEquals(countOnes(stateTensor[5]), 61);
		
		// First and last column have 5 cells, giving this pattern: 0,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0
		assertArrayEquals(stateTensor[5][0], new float[]{0,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0}, 0.0001f);
		assertArrayEquals(stateTensor[5][8], new float[]{0,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0}, 0.0001f);
		
		// Second and second-to-last column have 6 cells, giving this pattern: 0,0,0,1,0,1,0,1,0,1,0,1,0,1,0,0,0
		assertArrayEquals(stateTensor[5][1], new float[]{0,0,0,1,0,1,0,1,0,1,0,1,0,1,0,0,0}, 0.0001f);
		assertArrayEquals(stateTensor[5][7], new float[]{0,0,0,1,0,1,0,1,0,1,0,1,0,1,0,0,0}, 0.0001f);
		
		// Third and third-to-last column have 7 cells, giving this pattern: 0,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0
		assertArrayEquals(stateTensor[5][2], new float[]{0,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0}, 0.0001f);
		assertArrayEquals(stateTensor[5][6], new float[]{0,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0}, 0.0001f);
		
		// Fourth and fourth-to-last column have 8 cells, giving this pattern: 0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0
		assertArrayEquals(stateTensor[5][3], new float[]{0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0}, 0.0001f);
		assertArrayEquals(stateTensor[5][5], new float[]{0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0}, 0.0001f);
		
		// Middle column has 9 cells, giving this pattern: 1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1
		assertArrayEquals(stateTensor[5][4], new float[]{1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1}, 0.0001f);
		
		// All remaining channels should be all-zero, since no moves played
		assertAllZero(stateTensor[6]);
		assertAllZero(stateTensor[7]);
		assertAllZero(stateTensor[8]);
		assertAllZero(stateTensor[9]);
		
		// Should have 61 legal moves, game not over
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 61);
		
		// Player 1 will play move 30 (which also happens to be in Cell 30)
		state.applyNthMove(30);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 61);	// Swap included!
		assertAllZero(state.toTensor()[2]);			// Not Player 1's turn anymore
		assertAllOne(state.toTensor()[3]);			// Player 2's turn now
		assertAllZero(state.toTensor()[4]);			// No swap occured
		
		// Player 2 plays move 60 (swap)
		state.applyNthMove(60);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 60);
		assertAllOne(state.toTensor()[2]);			// Player 1's turn now
		assertAllZero(state.toTensor()[3]);			// Not Player 2's turn anymore
		assertAllOne(state.toTensor()[4]);			// Swap occured
		
		// Black (previous Player 1, now Player 2) plays move 38 (= Cell 39)
		state.applyNthMove(38);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 59);
		assertAllZero(state.toTensor()[2]);			// Not Player 1's turn anymore
		assertAllOne(state.toTensor()[3]);			// Player 2's turn now
		assertAllOne(state.toTensor()[4]);			// Swap occured
		
		// White move 22 (= Cell 22)
		state.applyNthMove(22);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 58);
		assertAllOne(state.toTensor()[2]);			// Player 1's turn now
		assertAllZero(state.toTensor()[3]);			// Not Player 2's turn anymore
		assertAllOne(state.toTensor()[4]);			// Swap occured
		
		// Black move 29 (= Cell 31)
		state.applyNthMove(29);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 57);
		assertAllZero(state.toTensor()[2]);			// Not Player 1's turn anymore
		assertAllOne(state.toTensor()[3]);			// Player 2's turn now
		assertAllOne(state.toTensor()[4]);			// Swap occured
		
		// White move 9 (= Cell 9)
		state.applyNthMove(9);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 56);
		assertAllOne(state.toTensor()[2]);			// Player 1's turn now
		assertAllZero(state.toTensor()[3]);			// Not Player 2's turn anymore
		assertAllOne(state.toTensor()[4]);			// Swap occured
		
		// Black move 15 (= Cell 16)
		state.applyNthMove(15);
		assertFalse(state.isTerminal());
		assertEquals(state.numLegalMoves(), 55);
		assertAllZero(state.toTensor()[2]);			// Not Player 1's turn anymore
		assertAllOne(state.toTensor()[3]);			// Player 2's turn now
		assertAllOne(state.toTensor()[4]);			// Swap occured

		// White move 14 (= Cell 15)
		state.applyNthMove(14);
		assertTrue(state.isTerminal());
		
		// Get the new state tensor for this terminal state
		stateTensor = state.toTensor();
		
		// In the final game state, there must be 4 pieces of type Ball1...
		assertEquals(countOnes(stateTensor[0]), 4);
		// ... in these four positions:
		assertEquals(stateTensor[0][7][11], 1.f, 0.0001f);
		assertEquals(stateTensor[0][6][10], 1.f, 0.0001f);
		assertEquals(stateTensor[0][5][9], 1.f, 0.0001f);
		assertEquals(stateTensor[0][4][8], 1.f, 0.0001f);
		
		// And 3 of type Ball2...
		assertEquals(countOnes(stateTensor[1]), 3);
		// ... in these three positions:
		assertEquals(stateTensor[1][6][12], 1.f, 0.0001f);
		assertEquals(stateTensor[1][4][10], 1.f, 0.0001f);
		assertEquals(stateTensor[1][3][9], 1.f, 0.0001f);
		
		// White won ("Player 1"), but that's the person who was originally Player 2
		assertEquals(state.returns(0), -1.0, 0.0001);
		assertEquals(state.returns(1), 1.0, 0.0001);
	}
	
	/**
	 * Asserts that given plane contains only 0s
	 * @param plane
	 */
	private static void assertAllZero(final float[][] plane)
	{
		for (int x = 0; x < plane.length; ++x)
		{
			for (int y = 0; y < plane[x].length; ++y)
			{
				assertEquals(plane[x][y], 0.f, 0.f);
			}
		}
	}
	
	/**
	 * Asserts that given plane contains only 1s
	 * @param plane
	 */
	private static void assertAllOne(final float[][] plane)
	{
		for (int x = 0; x < plane.length; ++x)
		{
			for (int y = 0; y < plane[x].length; ++y)
			{
				assertEquals(plane[x][y], 1.f, 0.f);
			}
		}
	}
	
	/**
	 * @param plane
	 * @return Number of 1.f entries in given plane
	 */
	private static int countOnes(final float[][] plane)
	{
		int count = 0;
		for (int x = 0; x < plane.length; ++x)
		{
			for (int y = 0; y < plane[x].length; ++y)
			{
				if (plane[x][y] == 1.f)
					++count;
			}
		}
		return count;
	}

}
