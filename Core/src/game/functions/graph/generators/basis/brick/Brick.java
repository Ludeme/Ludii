package game.functions.graph.generators.basis.brick;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.dim.math.Add;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import other.context.Context;

/**
 * Defines a board on a brick tiling using 1x2 rectangular brick tiles.
 * 
 * @author cambolbro
 */
public class Brick extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param shape Board shape [Square].
	 * @param dimA  First board dimension (size or number of rows).
	 * @param dimB  Second dimension (columns) [rows].
	 * @param trim  Whether to clip exposed half bricks [False].
	 * 
	 * @example (brick Diamond 4 trim:True)
	 */
	@SuppressWarnings("javadoc")
	public static GraphFunction construct
	(
		@Opt 	   final BrickShapeType shape,
		   	 	   final DimFunction    dimA,
		@Opt 	   final DimFunction    dimB,
		@Opt @Name final Boolean        trim
	)
	{
		final BrickShapeType st = (shape != null) 
									? shape
									: (dimB == null || dimB == dimA) 
										? BrickShapeType.Square 
										: BrickShapeType.Rectangle;				
		switch (st)
		{
		case Square:
		case Rectangle:
			return new SquareOrRectangleOnBrick(dimA, dimB, trim);
		case Limping:
			final DimFunction dimAplus1 = new Add(dimA, new DimConstant(1));
			return new SquareOrRectangleOnBrick(dimA, dimAplus1, trim);
		case Diamond:
			return new DiamondOrPrismOnBrick(dimA, null, trim);
		case Prism:
			return new DiamondOrPrismOnBrick(dimA, (dimB == null ? dimA : dimB), trim);
		case Spiral:
			return new SpiralOnBrick(dimA);
//		case Circle:
//			return new CircleOnBrick(dimA);
		default:
			throw new IllegalArgumentException("Shape " + st + " not supported for Brick tiling.");
		}
	}

	//-------------------------------------------------------------------------

	private Brick()
	{
		// Ensure that compiler does not pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Null placeholder to make the grammar recognise Brick
		return null;
	}
	
	//-------------------------------------------------------------------------

	static void addBrick(final Graph graph, final int row, final int col)
	{
		final Vertex vertexA = graph.findOrAddVertex(col,   row);
		final Vertex vertexB = graph.findOrAddVertex(col,   row+1);
		final Vertex vertexC = graph.findOrAddVertex(col+1, row+1);
		final Vertex vertexD = graph.findOrAddVertex(col+2, row+1);
		final Vertex vertexE = graph.findOrAddVertex(col+2, row);
		final Vertex vertexF = graph.findOrAddVertex(col+1, row);
				
		graph.findOrAddEdge(vertexA.id(), vertexB.id());
		graph.findOrAddEdge(vertexB.id(), vertexC.id());
		graph.findOrAddEdge(vertexC.id(), vertexD.id());
		graph.findOrAddEdge(vertexD.id(), vertexE.id());
		graph.findOrAddEdge(vertexE.id(), vertexF.id());
		graph.findOrAddEdge(vertexF.id(), vertexA.id());
				
		graph.findOrAddFace(vertexA.id(), vertexB.id(), vertexC.id(), vertexD.id(), vertexE.id(), vertexF.id());
	}

	static void addHalfBrick(final Graph graph, final int row, final int col)
	{
		final Vertex vertexA = graph.findOrAddVertex(col,   row);
		final Vertex vertexB = graph.findOrAddVertex(col,   row+1);
		final Vertex vertexC = graph.findOrAddVertex(col+1, row+1);
		final Vertex vertexD = graph.findOrAddVertex(col+1, row);
				
		graph.findOrAddEdge(vertexA.id(), vertexB.id());
		graph.findOrAddEdge(vertexB.id(), vertexC.id());
		graph.findOrAddEdge(vertexC.id(), vertexD.id());
		graph.findOrAddEdge(vertexD.id(), vertexA.id());
				
		graph.findOrAddFace(vertexA.id(), vertexB.id(), vertexC.id(), vertexD.id());
	}

	static void addVerticalBrick(final Graph graph, final int row, final int col)
	{
		final Vertex vertexA = graph.findOrAddVertex(col,   row);
		final Vertex vertexB = graph.findOrAddVertex(col,   row+1);
		final Vertex vertexC = graph.findOrAddVertex(col,   row+2);
		final Vertex vertexD = graph.findOrAddVertex(col+1, row+2);
		final Vertex vertexE = graph.findOrAddVertex(col+1, row+1);
		final Vertex vertexF = graph.findOrAddVertex(col+1, row);
				
		graph.findOrAddEdge(vertexA.id(), vertexB.id());
		graph.findOrAddEdge(vertexB.id(), vertexC.id());
		graph.findOrAddEdge(vertexC.id(), vertexD.id());
		graph.findOrAddEdge(vertexD.id(), vertexE.id());
		graph.findOrAddEdge(vertexE.id(), vertexF.id());
		graph.findOrAddEdge(vertexF.id(), vertexA.id());
				
		graph.findOrAddFace(vertexA.id(), vertexB.id(), vertexC.id(), vertexD.id(), vertexE.id(), vertexF.id());
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
