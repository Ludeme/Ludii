package games.symmetry;

import org.junit.Assert;
import org.junit.Test;

import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.types.board.SiteType;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * JUnit test for the pregeneration of square tiling for a square shape.
 * 
 * @author Eric.Piette
 *
 */
public class TestSquareSymmetries
{
	@Test
	public void testSquareRotations()
	{
		final Board board = createBoard();
		Container.createSymmetries(board.topology());
		
		testOneRotation(board.topology().cellRotationSymmetries());
		testOneRotation(board.topology().edgeRotationSymmetries());
		testOneRotation(board.topology().vertexRotationSymmetries());
	}

	@Test
	public void testSquareReflections()
	{
		final Board board = createBoard();

		Container.createSymmetries(board.topology());

		testOneReflection(board.topology().cellReflectionSymmetries());
		testOneReflection(board.topology().edgeReflectionSymmetries());
		testOneReflection(board.topology().vertexReflectionSymmetries());
	}
	
	private static Board createBoard()
	{
		final Board board = new Board(new RectangleOnSquare(new DimConstant(8), null, null, null), null,
				null, null, null, null, Boolean.FALSE);

		board.createTopology(0, 0);
		final Topology topology = board.topology();
		
		for (final SiteType type : SiteType.values())
		{
			topology.computeRelation(type);

			topology.computeSupportedDirection(type);

			// Convert the properties to the list of each pregeneration.
			for (final TopologyElement element : topology.getGraphElements(type))
				topology.convertPropertiesToList(type, element);

			topology.crossReferencePhases(type);
			
			topology.computeRows(type, false);
			topology.computeColumns(type, false);
			topology.computeLayers(type);
			topology.computeCoordinates(type);
			topology.preGenerateDistanceTables(type);
			topology.computeDoesCross();
		}
		
		topology.optimiseMemory();
		return board;
	}
	

	
	private static void testOneRotation(final int[][] syms)
	{
		Assert.assertEquals("Square rotational symmetries", 4, syms.length);
		
		// Properties = rotation of zero is the identity
		// rotation[1].rotation[3] is the identity
		// rotation[2].rotation[2] is the identity
		
		int different3 = 0;
		int different2 = 0;
		int different1 = 0;
		int different0 = 0;

		for (int idx = 0; idx < syms.length; idx++)
		{
			final int r0 = syms[0][idx];
			final int r1 = syms[1][idx];
			final int r2 = syms[2][idx];
			final int r3 = syms[3][idx];

			if (r0 != idx) different0++;
			if (r1 != idx) different1++;
			if (r2 != idx) different2++;
			if (r3 != idx) different3++;
			
			// Each reflection should be its own dual
			Assert.assertEquals("rotation by 0 is identity", idx, r0);
			Assert.assertEquals("rotation by 1/4 is inverse of rotation by 3/4", idx, syms[3][r1]);
			Assert.assertEquals("rotation by 3/4 is inverse of rotation by 1/4", idx, syms[1][r3]);
			Assert.assertEquals("rotation by 1/2 is inverse of itself", idx, syms[2][r2]);
		}

		Assert.assertTrue("rotation by 0 is identity", different0 == 0);		
		Assert.assertTrue("Some rotations by 1/4 should be different", different1 > 0);		
		Assert.assertTrue("Some rotations by 1/2 should be different", different2 > 0);		
		Assert.assertTrue("Some rotations by 3/4 should be different", different3 > 0);		
		
	}
	
	private static void testOneReflection(final int[][] syms)
	{
		Assert.assertEquals("Reflection symmetries", 4, syms.length);
		
		int different = 0;
		for (final int[] symmetry : syms)
		{
			for (int idx = 0; idx < symmetry.length; idx++)
			{
				final int reflected = symmetry[idx];
				final int restored = symmetry[reflected];
				if (idx!= reflected) different++;

				// Each reflection should be its own dual
				Assert.assertEquals("Invariant under double reflection", idx, restored);
			}
		}
		Assert.assertTrue("Some reflections should be different", different > 0);		
	}
}
