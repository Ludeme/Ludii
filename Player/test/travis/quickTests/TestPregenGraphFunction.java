package travis.quickTests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.hex.HexagonOnHex;
import game.functions.graph.generators.basis.hex.StarOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tiling.Tiling;
import game.functions.graph.generators.basis.tiling.TilingType;
import game.functions.graph.generators.basis.tri.TriangleOnTri;
import game.functions.graph.operators.Merge;
import game.functions.graph.operators.Shift;
import game.functions.graph.operators.Union;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.GraphElement;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import other.topology.TopologyElement;

/**
 * Test checking the pregeneration with the graph functions.
 * 
 * @author Eric.Piette and cambolbro
 */
public class TestPregenGraphFunction
{
	/** The board to test. */
	private Board board;
	
	/** To know if the test is ok */
	private boolean testSucceed = true;

	// Expected Cells.
	private int[] cellsCorners;
	private int[] cellsOuter;
	private int[] cellsPerimeter;
	private int[] cellsInner;
	private int[] cellsCentre;
	private int[] cellsTop;
	private int[] cellsBottom;
	private int[] cellsLeft;
	private int[] cellsRight;
	
	// Expected Vertices.
	private int[] verticesCorners;
	private int[] verticesOuter;
	private int[] verticesPerimeter;
	private int[] verticesInner;
	private int[] verticesCentre;
	private int[] verticesTop;
	private int[] verticesBottom;
	private int[] verticesLeft;
	private int[] verticesRight;
	
	// Expected Edges.
	private int[] edgesCorners;
	private int[] edgesOuter;
	private int[] edgesPerimeter;
	private int[] edgesInner;
	private int[] edgesCentre;
	private int[] edgesTop;
	private int[] edgesBottom;
	private int[] edgesLeft;
	private int[] edgesRight;
	
	// ---------------------------------------------------------------------

	@Test
	public void testSquareTilingSquare()
	{
		makeTheBoard(new RectangleOnSquare(new DimConstant(3), null, null, null), SiteType.Cell);
		final String nameTest = "Square tiling by Square";

		// Cells
		cellsCorners = new int[] { 0, 2, 6, 8 };
		cellsOuter = new int[] { 0, 1, 2, 3, 5, 6, 7, 8 };
		cellsPerimeter = new int[] { 0, 1, 2, 3, 5, 6, 7, 8 };
		cellsInner = new int[] { 4 };
		cellsCentre = new int[] { 4 };
		cellsTop = new int[] { 6, 7, 8 };
		cellsBottom = new int[] { 0, 1, 2 };
		cellsLeft = new int[] { 0, 3, 6 };
		cellsRight = new int[] { 2, 5, 8 };
		
		// Vertices
		verticesCorners = new int[] { 0, 3, 12, 15 };
		verticesOuter = new int[] { 0, 1, 2, 3, 4, 7, 8, 11, 12, 13, 14, 15 };
		verticesPerimeter = new int[] { 0, 1, 2, 3, 4, 7, 8, 11, 12, 13, 14, 15 };
		verticesInner = new int[] { 5, 6, 9, 10 };
		verticesCentre = new int[] { 5, 6, 9, 10 };
		verticesTop = new int[] { 12, 13, 14, 15 };
		verticesBottom = new int[] { 0, 1, 2, 3 };
		verticesLeft = new int[] { 0, 4, 8, 12 };
		verticesRight = new int[] { 3, 7, 11, 15 };
		
		// Edges
		edgesCorners = new int[] {0, 2, 3, 6, 17, 20, 21, 23};
		edgesOuter = new int[] {0, 1, 2, 3, 6, 10, 13, 17, 20, 21, 22, 23};
		edgesPerimeter = new int[] {0, 1, 2, 3, 6, 10, 13, 17, 20, 21, 22, 23};
		edgesInner = new int[] {4, 5, 7, 8, 9, 11, 12, 14, 15, 16, 18, 19};
		edgesCentre = new int[] {8, 11, 12, 15};
		edgesTop = new int[] { 21, 22, 23 };
		edgesBottom = new int[] {0, 1, 2};
		edgesLeft = new int[] {3, 10, 17};
		edgesRight = new int[] { 6, 13, 20 };

		runAllTests(nameTest);
		
		// For the square Tiling we also test some radials.
		final int[][] allRadials = new int[8][2];
		allRadials[0] = new int[] {4, 7};
		allRadials[1] = new int[] {4, 8};
		allRadials[2] = new int[] {4, 5};
		allRadials[3] = new int[] {4, 2};
		allRadials[4] = new int[] {4, 1};
		allRadials[5] = new int[] {4, 0};
		allRadials[6] = new int[] {4, 3};
		allRadials[7] = new int[] {4, 6};
		
		final int[][] orthogonalRadials = new int[4][2];
		orthogonalRadials[0] = new int[] {4, 7};
		orthogonalRadials[1] = new int[] {4, 5};
		orthogonalRadials[2] = new int[] {4, 1};
		orthogonalRadials[3] = new int[] {4, 3};

		final int[][] diagonalRadials = new int[4][2];
		diagonalRadials[0] = new int[] {4, 8};
		diagonalRadials[1] = new int[] {4, 2};
		diagonalRadials[2] = new int[] {4, 0};
		diagonalRadials[3] = new int[] {4, 6};
		

		final int[][] northRadials = new int[1][3];
		northRadials[0] = new int[] {1,4,7};
		
		testRadials(AbsoluteDirection.All, SiteType.Cell, 4, allRadials);
		testRadials(AbsoluteDirection.Orthogonal, SiteType.Cell, 4, orthogonalRadials);
		testRadials(AbsoluteDirection.Diagonal, SiteType.Cell, 4, diagonalRadials);
		testRadials(AbsoluteDirection.N, SiteType.Cell, 1, northRadials);

		if(!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}

	// ---------------------------------------------------------------------
	
	@Test
	public void testHexTilingHex()
	{
		makeTheBoard(new HexagonOnHex(new DimConstant(5)), SiteType.Cell);
		final String nameTest = "Hex tiling by Hex";

		// Cells
		cellsCorners = new int[] {0, 4, 26, 34, 56, 60};
		cellsOuter = new int[] {0, 1, 2, 3, 4, 5, 10, 11, 17, 18, 25, 26, 34, 35, 42, 43, 49, 50, 55, 56, 57, 58, 59, 60};
		cellsPerimeter = new int[] {0, 1, 2, 3, 4, 5, 10, 11, 17, 18, 25, 26, 34, 35, 42, 43, 49, 50, 55, 56, 57, 58, 59, 60};
		cellsInner = new int[] {6, 7, 8, 9, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 27, 28, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 44, 45, 46, 47, 48, 51, 52, 53, 54};
		cellsCentre = new int[] { 30 };
		cellsTop = new int[] { 56,57,58,59,60 };
		cellsBottom = new int[] {0, 1, 2, 3, 4};
		cellsLeft = new int[] { 26 };
		cellsRight = new int[] { 34 };
		
		// Vertices
//		verticesCorners = new int[] { }; // NOT SURE ???
		verticesOuter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 16, 17, 23, 24, 30, 31, 38, 39, 46, 47, 55, 56, 64, 65, 74, 75, 84, 85, 93, 94, 102, 103, 110, 111, 118, 119, 125, 126, 132, 133, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149};
		verticesPerimeter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 16, 17, 23, 24, 30, 31, 38, 39, 46, 47, 55, 56, 64, 65, 74, 75, 84, 85, 93, 94, 102, 103, 110, 111, 118, 119, 125, 126, 132, 133, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149};
		final TIntArrayList outer = new TIntArrayList(verticesOuter);
		final TIntArrayList inner = new TIntArrayList();
		for (int i = 0; i < 149; i++)
			if (!outer.contains(i))
				inner.add(i);
		verticesInner = inner.toArray();
		verticesCentre = new int[] {60, 69, 70, 79, 80, 89};
		verticesTop = new int[] {145, 146, 147, 148, 149};
		verticesBottom = new int[] {0, 1, 2, 3, 4};
		verticesLeft = new int[] {65, 75};
		verticesRight = new int[] {74, 84};

		// TODO the edges.

		runAllTests(nameTest);
		
		if(!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}

	// ---------------------------------------------------------------------
	
	@Test
	public void testTriangleTilingTriangle()
	{
		makeTheBoard(new TriangleOnTri(new DimConstant(4)), SiteType.Cell);
		final String nameTest = "Triangle tiling by Triangle";

		// Cells
		cellsCorners = new int[] { 0, 3, 15 };
		cellsOuter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15};
		cellsPerimeter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15};
		cellsInner = new int[] { 8 };
		cellsCentre = new int[] { 8 };
		cellsTop = new int[] { 15 };
		cellsBottom = new int[] { 0, 1, 2, 3 };
		cellsLeft = new int[] { 0 };
		cellsRight = new int[] { 3 };

		// Vertices
		verticesCorners = new int[] { 0, 4, 14 };	
		verticesOuter = new int[] { 0,1,2,3,4,5,8,9,11,12,13,14 };
		verticesPerimeter = new int[] { 0,1,2,3,4,5,8,9,11,12,13,14 };
		verticesInner = new int[] { 6, 7, 10 };
		verticesCentre = new int[] { 6, 7, 10 };
		verticesTop = new int[] { 14 };
		verticesBottom = new int[] { 0,1,2,3,4 };
		verticesLeft = new int[] { 0 };
		verticesRight = new int[] { 4 };
		
		// Edges
		edgesCorners = new int[] { 0, 4, 3, 11, 28, 29 };
		edgesOuter = new int[] {0, 1, 2, 3, 4, 11, 15, 20, 23, 26, 28, 29};
		edgesPerimeter = new int[] {0, 1, 2, 3, 4, 11, 15, 20, 23, 26, 28, 29};
		edgesInner = new int[] {5, 6, 7, 8, 9, 10, 12, 13, 14, 16, 17, 18, 19, 21, 22, 24, 25, 27};
		edgesCentre = new int[] { 13, 17, 18};
		edgesTop = new int[] { 28, 29 };
		edgesBottom = new int[] { 0, 1, 2, 3 };
		edgesLeft = new int[] { 4 };
		edgesRight = new int[] { 11 };
		
		runAllTests(nameTest);
		
		if(!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}

	// ---------------------------------------------------------------------
	
	@Test
	public void testTiling3464()
	{
		makeTheBoard(Tiling.construct(TilingType.T3464, new DimConstant(2), null), SiteType.Cell);
		final String nameTest = "Tiling3464";

		// Cells
		cellsCorners = new int[] { 1, 7, 9, 12, 15, 27, 33, 45, 48, 51, 53, 59 };
		cellsOuter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 15, 16, 22, 23, 26, 27, 33, 34, 37, 38, 44, 45, 48, 49, 50, 51, 53, 54, 55, 56, 57, 58, 59, 60};
		cellsPerimeter = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 15, 16, 22, 23, 26, 27, 33, 34, 37, 38, 44, 45, 48, 49, 50, 51, 53, 54, 55, 56, 57, 58, 59, 60};
		final TIntArrayList outer = new TIntArrayList(cellsOuter);
		final TIntArrayList inner = new TIntArrayList();
		for (int i = 0; i < 60; i++)
			if (!outer.contains(i))
				inner.add(i);
		cellsInner = inner.toArray();
		cellsCentre = new int[] { 30 };
		cellsTop = new int[] { 58,59,60 };
		cellsBottom = new int[] { 0,1,2};
		cellsLeft = new int[] { 16,38 };
		cellsRight = new int[] { 22,44 };

		// TODO the vertices.
		// TODO the edges.
		
		runAllTests(nameTest);
		
		if(!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}
	
	// ---------------------------------------------------------------------
	
	@Test
	public void testCross()
	{
		makeTheBoard(new Merge(new GraphFunction[]
		{ new Shift(new FloatConstant((float) 3.0), new FloatConstant((float) 0.0), null,
				new RectangleOnSquare(new DimConstant(12), new DimConstant(2), null, null)),
				new Shift(new FloatConstant((float) 0.0), new FloatConstant((float) 7.0), null,
						new RectangleOnSquare(new DimConstant(2), new DimConstant(8), null, null)) 
		}, Boolean.valueOf(false)), SiteType.Cell
		);
		final String nameTest = "Cross tiling by Square";

		// Cells
		cellsCorners = new int[] { 0, 1, 14, 16, 18, 20, 26, 27, 28, 31, 32, 35 };
		cellsOuter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
		cellsPerimeter = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
		cellsInner = new int[] {  };
		cellsCentre = new int[] { 12,13 };
		cellsTop = new int[] { 26,27 };
		cellsBottom = new int[] { 0,1 };
		cellsLeft = new int[] { 28,32 };
		cellsRight = new int[] { 31,35 };
		
		// TODO the vertices.
		// TODO the edges.
		
		runAllTests(nameTest);
		
		if(!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}

	// ---------------------------------------------------------------------

	@Test
	public void testHexStar()
	{
		makeTheBoard(new Merge(new GraphFunction[]
		{ new StarOnHex(new DimConstant(2)) }, Boolean.valueOf(false)), SiteType.Cell);
		final String nameTest = "Star tiling by Hex";

		// Cells
		cellsCorners = new int[] {0, 3, 5, 7, 9, 16, 20, 27, 29, 31, 33, 36};
		cellsOuter = new int[] {0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 15, 16, 20, 21, 26, 27, 28, 29, 31, 32, 33, 34, 35, 36};
		cellsPerimeter = new int[] {0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 15, 16, 20, 21, 26, 27, 28, 29, 31, 32, 33, 34, 35, 36};
		cellsInner = new int[] {6, 11, 12, 13, 14, 17, 18, 19, 22, 23, 24, 25, 30};
		cellsCentre = new int[] { 18 };
		cellsTop = new int[] { 36 };
		cellsBottom = new int[] { 0 };
		cellsLeft = new int[] {3, 27};
		cellsRight = new int[] {9, 33};

		// TODO the vertices.
		// TODO the edges.

		runAllTests(nameTest);

		if (!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}
	
	// ---------------------------------------------------------------------

//	@Test
//	public void testMorris()
//	{
//		makeTheBoard(new Morris(new DimConstant(3), BooleanConstant.construct(true)), SiteType.Cell);
//		final String nameTest = "Morris Tiling with Join Corners";
//
//		// Vertices
//		verticesCorners = new int[] { 0,2,21,23 };
//		verticesOuter = new int[] { 0,1,2,9,14,23,22,21 };
//		verticesPerimeter = new int[] { 0,1,2,9,14,23,22,21 };
//		verticesInner = new int[] { 3,4,5,6,7,8,10,11,12,13,15,16,17,18,19,20 };
//		verticesCentre = new int[] { 7, 11, 12, 16};
//		verticesTop = new int[] { 21,22,23 };
//		verticesBottom = new int[] { 0,1,2 };
//		verticesLeft = new int[] { 0,9,21 };
//		verticesRight = new int[] { 2,14,23 };
//
//		// TODO the cells.
//		// TODO the edges.
//
//		runAllTests(nameTest);
//
//		if (!testSucceed)
//			fail("Pregeneration for " + nameTest + " is failing");
//	}
	
	// ---------------------------------------------------------------------

	@Test
	public void testUnion()
	{
		makeTheBoard(new Union(new GraphFunction[] { 
				new RectangleOnSquare(new DimConstant(6), new DimConstant(2), null, null), 
				new Shift(new FloatConstant((float) 2.0), new FloatConstant((float) 2.0), null, 
				new RectangleOnSquare(new DimConstant(3), null, null, null)), 
				new RectangleOnSquare(new DimConstant(2), new DimConstant(6), null, null) 
		}, Boolean.valueOf(false)), SiteType.Vertex);
		final String nameTest = "Union of two boards tiling by square";

		// Cells
		cellsCorners = new int[] { 0,13,4,5,6,7,8,9};
		cellsOuter = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13 };
		cellsPerimeter = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13 };
		cellsInner = new int[] { };
		//cellsCentre = new int[] {  }; // Not sure?
		cellsTop = new int[] { 4 };
		cellsBottom = new int[]
		{ 0, 10, 11, 12, 13, 9 };
		cellsLeft = new int[] {0,1,2,3,4,9 };
		cellsRight = new int[] { 13 };
		
		// TODO the vertices.
		// TODO the edges.

		runAllTests(nameTest);

		if (!testSucceed)
			fail("Pregeneration for " + nameTest + " is failing");
	}

	// ---------------------------------------------------------------------
	// --------------------------Helper methods-----------------------------
	// ---------------------------------------------------------------------
	
	/**
	 * Run all the tests.
	 * 
	 * @param nameTest The name of the test.
	 */
	public void runAllTests(final String nameTest)
	{
		testCorners(SiteType.Cell, cellsCorners, nameTest);
		testOuter(SiteType.Cell, cellsOuter, nameTest);
		testPerimeter(SiteType.Cell, cellsPerimeter, nameTest);
		testInner(SiteType.Cell, cellsInner, nameTest);
		testCentre(SiteType.Cell, cellsCentre, nameTest);
		testTop(SiteType.Cell, cellsTop, nameTest);
		testBottom(SiteType.Cell, cellsBottom, nameTest);
		testLeft(SiteType.Cell, cellsLeft, nameTest);
		testRight(SiteType.Cell, cellsRight, nameTest);

		testCorners(SiteType.Vertex, verticesCorners, nameTest);
		testOuter(SiteType.Vertex, verticesOuter, nameTest);
		testPerimeter(SiteType.Vertex, verticesPerimeter, nameTest);
		testInner(SiteType.Vertex, verticesInner, nameTest);
		testCentre(SiteType.Vertex, verticesCentre, nameTest);
		testTop(SiteType.Vertex, verticesTop, nameTest);
		testBottom(SiteType.Vertex, verticesBottom, nameTest);
		testLeft(SiteType.Vertex, verticesLeft, nameTest);
		testRight(SiteType.Vertex, verticesRight, nameTest);

		testCorners(SiteType.Edge, edgesCorners, nameTest);
		testOuter(SiteType.Edge, edgesOuter, nameTest);
		testPerimeter(SiteType.Edge, edgesPerimeter, nameTest);
		testInner(SiteType.Edge, edgesInner, nameTest);
		testCentre(SiteType.Edge, edgesCentre, nameTest);
		testTop(SiteType.Edge, edgesTop, nameTest);
		testBottom(SiteType.Edge, edgesBottom, nameTest);
		testLeft(SiteType.Edge, edgesLeft, nameTest);
		testRight(SiteType.Edge, edgesRight, nameTest);
	}

	/**
	 * Run the test for the centre sites.
	 * 
	 * @param type The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest The name of the current test.
	 */
	public void testCentre(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().centre(type), "Centre");
	}
	
	/**
	 * Run the test for the right sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testRight(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().right(type), "Right");
	}
	
	/**
	 * Run the test for the left sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testLeft(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().left(type), "Left");
	}
	
	/**
	 * Run the test for the bottom sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testBottom(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().bottom(type), "Bottom");
	}
	
	/**
	 * Run the test for the top sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testTop(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().top(type), "Top");
	}
	
	/**
	 * Run the test for the perimeter sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testPerimeter(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().perimeter(type), "Perimeter");
	}
	
	/**
	 * Run the test for the inner sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testInner(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().inner(type), "Inner");
	}
	
	/**
	 * Run the test for the outer sites.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testOuter(final SiteType type, final int[] correctSites, final String nameTest)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().outer(type), "Outer");
	}

	/**
	 * Run the test for the corners.
	 * 
	 * @param type         The type of site to test.
	 * @param correctSites The expected sites.
	 * @param nameTest     The name of the current test.
	 */
	public void testCorners
	(
		final SiteType type, 
		final int[] correctSites, 
		final String nameTest
	)
	{
		if(correctSites != null)
			runTest(type, correctSites, nameTest, board.topology().corners(type), "Corners");
	}

	/**
	 * Run the test.
	 * 
	 * @param type           The type of site to test.
	 * @param correctSites   The expected sites.
	 * @param nameTest       The name of the current test.
	 * @param pregenElements The list of the elements generated.
	 * @param pregenTest     The type of pregeneration to test.
	 */
	public void runTest
	(
		final SiteType type, 
		final int[] correctSites, 
		final String nameTest,
		final List<TopologyElement> pregenElements, 
		final String pregenTest
	)
	{
		final TIntArrayList expectedSites = new TIntArrayList(correctSites);
		if (!check(transformList(pregenElements), expectedSites))
		{
			System.out.println(nameTest + ": ");
			System.out.println(type.toString() + " " + pregenTest + " are: " + transformList(pregenElements));
			System.out.println("They should be: " + expectedSites + " \n");
			this.testSucceed = false;
		}
	}

	/**
	 * Init the test.
	 */
	public void init()
	{
		this.testSucceed = true;

		// Cells
		cellsCorners = null;
		cellsOuter = null;
		cellsPerimeter = null;
		cellsInner = null;
		cellsCentre = null;
		cellsTop = null;
		cellsBottom = null;
		cellsLeft = null;
		cellsRight = null;

		// Vertices
		verticesCorners = null;
		verticesOuter = null;
		verticesPerimeter = null;
		verticesInner = null;
		verticesCentre = null;
		verticesTop = null;
		verticesBottom = null;
		verticesLeft = null;
		verticesRight = null;

		// Edges
		edgesCorners = null;
		edgesOuter = null;
		edgesPerimeter = null;
		edgesInner = null;
		edgesCentre = null;
		edgesTop = null;
		edgesBottom = null;
		edgesLeft = null;
		edgesRight = null;
	}

	/**
	 * Compute the board.
	 * 
	 * @param function The graphFunction to test.
	 */
	public void makeTheBoard(final GraphFunction function, final SiteType useType)
	{
		init();
		board = new Board(function, null, null, null, null, useType, Boolean.FALSE);
		board.createTopology(0, 0);
		for (final SiteType type : SiteType.values())
			for (final TopologyElement element : board.topology().getGraphElements(type))
				board.topology().convertPropertiesToList(type, element);
	}

	/**
	 * @param graphElements
	 * 
	 * @return A list with all the indices of a list of graph element.
	 */
	public static TIntArrayList transformList(final List<? extends TopologyElement> graphElements)
	{
		final TIntArrayList result = new TIntArrayList();
		for (final TopologyElement element : graphElements)
			result.add(element.index());

		return result;
	}

	/**
	 * @param indices
	 * @param expectedIndices
	 * @return True if the list has the same indices on it and the same size.
	 */
	public static boolean check(final TIntArrayList indices, final TIntArrayList expectedIndices)
	{
		if (indices.size() != expectedIndices.size())
			return false;

		for (int i = 0; i < indices.size(); i++)
		{
			final int index = indices.get(i);
			boolean found = false;

			for (int j = 0; j < expectedIndices.size(); j++)
			{
				final int value = expectedIndices.get(j);
				if (value == index)
				{
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	/**
	 * To test the radials from a specific graph element with a specific
	 * absolute direction.
	 * 
	 * @param absoluteDirection The absolute direction.
	 * @param type The type of the graph element.
	 * @param origin The index of the origin of the radials.
	 * @param expectedRadials Th expected radials.
	 */
	public void testRadials(final AbsoluteDirection absoluteDirection, final SiteType type, final int origin,
			final int[][] expectedRadials)
	{
		final List<Radial> radials = board.topology().trajectories().radials(type, origin, absoluteDirection);
		for (int i = 0; i < radials.size(); i++)
		{
			final Radial radial = radials.get(i);
			final List<TopologyElement> elementInRadial = new ArrayList<TopologyElement>();
			for (final GraphElement graphElement : radial.steps())
				elementInRadial.add(board.topology().getGraphElement(type, graphElement.id()));
			if (!check(transformList(elementInRadial), new TIntArrayList(expectedRadials[i])))
			{
				System.out.println(
						"Square tiling by Square: The radials in absolute direction " + absoluteDirection.toString()
								+ " from the " + type.toString() + " " + origin
								+ " are wrong. They should be (in this order):");

				for (int j = 0; j < expectedRadials.length; j++)
					System.out.println("- " + new TIntArrayList(expectedRadials[j]));

				System.out.println("But these radials are: ");
				for (int k = 0; k < radials.size(); k++)
				{
					final Radial wrongRadial = radials.get(k);
					final List<TopologyElement> elementInWrongRadial = new ArrayList<TopologyElement>();
					for (final GraphElement graphElement : wrongRadial.steps())
						elementInWrongRadial.add(board.topology().getGraphElement(type, graphElement.id()));

					System.out.println("- " + transformList(elementInWrongRadial));
				}
				System.out.println();

				testSucceed = false;
				break;
			}
		}
	}
	
}
