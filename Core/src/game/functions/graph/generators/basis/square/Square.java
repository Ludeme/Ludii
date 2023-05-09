package game.functions.graph.generators.basis.square;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.dim.math.Add;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Poly;
import game.util.graph.Vertex;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board on a square tiling.
 * 
 * @author cambolbro
 */
public class Square extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * The steps.
	 */
	public static final int[][] steps =  { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };

	/**
	 * The diagonal steps.
	 */
	public static final int[][] diagonalSteps = { { 1, 1 }, { 1,-1 } };

	//-------------------------------------------------------------------------

	/**
	 * For defining a square tiling with the dimension.
	 * 
	 * @param shape     Board shape [Square].
	 * @param dim       Board dimension; cells or vertices per side.
	 * @param diagonals How to handle diagonals between opposite corners [Implied].
	 * @param pyramidal Whether this board allows a square pyramidal stacking.
	 * 
	 * @example (square Diamond 4)
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
		@Opt       	   final SquareShapeType shape,
		   	       	   final DimFunction     dim,
		@Opt @Or @Name final DiagonalsType   diagonals,
		@Opt @Or @Name final Boolean         pyramidal   	 
	)
	{
		int numNonNull = 0;
		if (diagonals != null)
			numNonNull++;
		if (pyramidal != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of 'diagonals' and 'pyramidal' can be true.");

		final SquareShapeType st = (shape == null) ? SquareShapeType.Square : shape;		
		switch (st)
		{
		case Square:
			return new RectangleOnSquare(dim, dim, diagonals, pyramidal);
		case Limping:
			final DimFunction dimAplus1 = new Add(dim, new DimConstant(1));
			return new RectangleOnSquare(dim, dimAplus1, diagonals, pyramidal);
		case Diamond:
			return new DiamondOnSquare(dim, diagonals);
			//$CASES-OMITTED$
		default:
			throw new IllegalArgumentException("Shape " + st + " not supported for square tiling.");
		}
	}

	/**
	 * For defining a square tiling with a polygon or the number of sides.
	 * 
	 * @param poly      Points defining the board shape.
	 * @param sides     Length of consecutive sides of outline shape.
	 * @param diagonals How to handle diagonals between opposite corners [Implied].
	 *
	 * @example (square (poly { {1 2} {1 6} {3 6} {3 4} {4 4} {4 2} }))
	 * @example (square { 4 3 -1 2 3 })
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
			 @Or 	   final Poly     	   poly,
			 @Or 	   final DimFunction[] sides,
		@Opt     @Name final DiagonalsType diagonals
	)
	{
		int numNonNull = 0;
		if (poly != null)
			numNonNull++;
		if (sides != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Exactly one array parameter must be non-null.");
		
		if (poly != null)
			return new CustomOnSquare(poly.polygon(), diagonals);
		else
			return new CustomOnSquare(sides, diagonals);
	}

	//-------------------------------------------------------------------------

	private Square()
	{
		// Ensure that compiler does not pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Null placeholder to make the grammar recognise Square
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param graph     The graph.
	 * @param fromRow   The origin row.
	 * @param toRow     The target row.
	 * @param fromCol   The origin column.
	 * @param toCol     The target column.
	 * @param diagonals The type of diagonal.
	 */
	public static void handleDiagonals
	(
		final Graph graph, 
		final int fromRow, final int toRow, final int fromCol, final int toCol, 
		final DiagonalsType diagonals
	)
	{
		if (diagonals == null)
			return;  // no diagonals: do nothing
			
		if (diagonals == DiagonalsType.Alternating)
		{
			// Add diagonal edges to alternating cells
			for (int r = fromRow; r <= toRow; r++)
				for (int c = fromCol; c <= toCol; c++)
				{
					// Add diagonals from this vertex
					final Vertex vertexA = graph.findVertex(c,   r);
					final Vertex vertexB = graph.findVertex(c,   r+1);
					final Vertex vertexC = graph.findVertex(c+1, r+1);
					final Vertex vertexD = graph.findVertex(c+1, r);

					if (vertexA == null || vertexB == null || vertexC == null || vertexD == null)
						continue;

					if ((r + c) % 2 == 0)
						graph.findOrAddEdge(vertexA, vertexC);		
					else
						graph.findOrAddEdge(vertexB, vertexD);		
				}
		}
		else if (diagonals == DiagonalsType.Solid)
		{
			// Add diagonal edges to all cells
			for (int r = fromRow; r <= toRow; r++)
				for (int c = fromCol; c <= toCol; c++)
				{
					// Add central vertex and edges to corner vertices
					final Vertex vertexA = graph.findVertex(c,   r);
					final Vertex vertexB = graph.findVertex(c,   r+1);
					final Vertex vertexC = graph.findVertex(c+1, r+1);
					final Vertex vertexD = graph.findVertex(c+1, r);

					if (vertexA == null || vertexB == null || vertexC == null || vertexD == null)
						continue;
					
					final Vertex vertexX = graph.findOrAddVertex(c + 0.5, r + 0.5);
					
					if (vertexX == null)
						continue;
					
					graph.findOrAddEdge(vertexA, vertexX);		
					graph.findOrAddEdge(vertexB, vertexX);		
					graph.findOrAddEdge(vertexC, vertexX);		
					graph.findOrAddEdge(vertexD, vertexX);		
				}
		}
		else if (diagonals == DiagonalsType.SolidNoSplit)
		{
			// Add diagonal edges to all cells
			for (int r = fromRow; r <= toRow; r++)
				for (int c = fromCol; c <= toCol; c++)
				{
					// Add central vertex and edges to corner vertices
					final Vertex vertexA = graph.findVertex(c,   r);
					final Vertex vertexB = graph.findVertex(c,   r+1);
					final Vertex vertexC = graph.findVertex(c+1, r+1);
					final Vertex vertexD = graph.findVertex(c+1, r);

					if (vertexA == null || vertexB == null || vertexC == null || vertexD == null)
						continue;
					
					graph.findOrAddEdge(vertexA, vertexC);		
					graph.findOrAddEdge(vertexB, vertexD);		
				}
		}
		else if (diagonals == DiagonalsType.Concentric)
		{
			// Add diagonal edges to concentric squares around centre
			final int midRow = (toRow + fromRow) / 2;
			final int midCol = (toCol + fromCol) / 2;
			
			for (int r = fromRow; r <= toRow; r++)
				for (int c = fromCol; c <= toCol; c++)
				{
					// Add diagonals from this vertex
					final Vertex vertexA = graph.findVertex(c,   r);
					final Vertex vertexB = graph.findVertex(c,   r+1);
					final Vertex vertexC = graph.findVertex(c+1, r+1);
					final Vertex vertexD = graph.findVertex(c+1, r);

					if (vertexA == null || vertexB == null || vertexC == null || vertexD == null)
						continue;

					if (r < midRow && c < midCol || r >= midRow && c >= midCol)
						graph.findOrAddEdge(vertexB, vertexD);		
					else
						graph.findOrAddEdge(vertexA, vertexC);		
				}
		}
		else if (diagonals == DiagonalsType.Radiating)
		{
			// Add diagonal edges radiating from centre
			final int[][] dsteps = { { 1, 1 }, { 1,-1 }, {-1, -1}, {-1, 1} };

			final int midRow = (toRow + fromRow) / 2;
			final int midCol = (toCol + fromCol) / 2;
			
			final int numSteps = Math.max((toRow - fromRow) / 2, (toCol - fromCol) / 2) + 1;
			
			for (int n = 0; n < numSteps; n++)
			{
				// Add diagonals from this vertex
				for (int d = 0; d < dsteps.length; d++)
				{
					final Vertex vertexA = graph.findVertex(midRow + n * dsteps[d][0], midCol + n * dsteps[d][1]);
					final Vertex vertexB = graph.findVertex(midRow + (n + 1) * dsteps[d][0], midCol + (n + 1) * dsteps[d][1]);
		
					if (vertexA == null || vertexB == null)
						continue;

					graph.findOrAddEdge(vertexA, vertexB);		
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(Game game)
	{
		// Nothing to do.
	}
	
	//-------------------------------------------------------------------------
	
}
