package games.symmetry;

import org.junit.Assert;
import org.junit.Test;

import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.basis.hex.HexagonOnHex;
import game.types.board.SiteType;
import other.topology.Topology;
import other.topology.TopologyElement;

public class TestHexSymmetries
{
	@Test
	public static void testHexRotations()
	{
		final Board board = createBoard();

		Container.createSymmetries(board.topology());

		testOneRotation(board.topology().cellRotationSymmetries());
		testOneRotation(board.topology().edgeRotationSymmetries());
		testOneRotation(board.topology().vertexRotationSymmetries());
	}

	@Test
	public static void testHexReflections()
	{
		final Board board = createBoard();

		Container.createSymmetries(board.topology());

		testOneReflection(board.topology().cellReflectionSymmetries());
		testOneReflection(board.topology().edgeReflectionSymmetries());
		testOneReflection(board.topology().vertexReflectionSymmetries());
	}

	private static Board createBoard()
	{
		final Board board = new Board(new HexagonOnHex(new DimConstant(5)), null, null, null, null, SiteType.Cell, Boolean.FALSE);

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
		Assert.assertEquals("Hexagonal rotational symmetries", 6, syms.length);
		
		// Properties = rotation of zero is the identity
		// rotation[1].rotation[3] is the identity
		// rotation[2].rotation[2] is the identity
		
		int different5 = 0;
		int different4 = 0;
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
			final int r4 = syms[4][idx];
			final int r5 = syms[5][idx];

			if (r0 != idx) different0++;
			if (r1 != idx) different1++;
			if (r2 != idx) different2++;
			if (r3 != idx) different3++;
			if (r4 != idx) different4++;
			if (r5 != idx) different5++;
			
			// Each reflection should be its own dual
			Assert.assertEquals("rotation by 0 is identity", idx, r0);
			Assert.assertEquals("rot(1/6)+rot(5/6) is identity", idx, syms[1][r5]);
			Assert.assertEquals("rot(2/6)+rot(4/6) is identity", idx, syms[2][r4]);
			Assert.assertEquals("rot(3/6)+rot(3/6) is identity", idx, syms[3][r3]);
			Assert.assertEquals("rot(4/6)+rot(2/6) is identity", idx, syms[4][r2]);
			Assert.assertEquals("rot(5/6)+rot(1/6) is identity", idx, syms[5][r1]);
		}

		Assert.assertTrue("rotation by 0 is identity", different0 == 0);		
		Assert.assertTrue("Some rotations by 1/6 should be different", different1 > 0);		
		Assert.assertTrue("Some rotations by 2/6 should be different", different2 > 0);		
		Assert.assertTrue("Some rotations by 3/6 should be different", different3 > 0);		
		Assert.assertTrue("Some rotations by 4/6 should be different", different4 > 0);		
		Assert.assertTrue("Some rotations by 5/6 should be different", different5 > 0);				
	}
	
	private static void testOneReflection(final int[][] syms)
	{
		Assert.assertEquals("Hexagonal reflection symmetries", 6, syms.length);
		
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
