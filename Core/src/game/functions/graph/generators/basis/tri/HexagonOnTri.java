package game.functions.graph.generators.basis.tri;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a hexagonal board on a triangular tiling.
 * 
 * @author cambolbro
 */
@Hide
public class HexagonOnTri extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Tri.
	 * 
	 * @param dim The dimension.
	 */
	public HexagonOnTri
	(
	   	 final DimFunction dim
	)
	{
		this.basis = BasisType.Triangular;
		this.shape  = ShapeType.Hexagon;	
		this.dim = new int[]{ dim.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int d = dim[0] + (siteType == SiteType.Cell ? 1 : 0);
		
		final int rows = 2 * d - 1;
		final int cols = 2 * d - 1;
		
//		// Create vertices
//		final List<double[]> vertexList = new ArrayList<double[]>();
//		for (int r = 0; r < rows; r++)
//			for (int c = 0; c < cols; c++)
//			{
//				if (c > cols / 2 + r || r - c > cols / 2)
//					continue;
//				final Point2D pt = Tri.xy(r, c);
//				vertexList.add(new double[]{ pt.getX(), pt.getY() });
//			}
//
//		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		
		final Graph graph = new Graph();
		
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				if (col > cols / 2 + row || row - col > cols / 2)
					continue;
				graph.addVertex(Tri.xy(row, col));
			}	
			
		// Edges along rows (bottom half)
		int vid = 0;
		for (int n = 0; n <= rows / 2; n++)
		{
			for (int col = 0; col < cols / 2 + n; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + 1));
				vid++;
			}
			vid++;
		}

		// Edges along rows (top half)
		for (int n = 0; n < rows / 2; n++)
		{
			for (int col = 0; col < cols - 2 - n; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + 1));
				vid++;
			}
			vid++;
		}

		// Edges between rows (bottom half)
		vid = 0;
		for (int n = 0; n < rows / 2; n++)
		{
			final int off = rows / 2 + 1 + n;
			for (int col = 0; col < cols / 2 + 1 + n; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + off));
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + off + 1));
				vid++;
			}
		}
		vid += cols;
		
		// Edges between rows (top half)
		for (int n = 0; n < rows / 2; n++)
		{
			final int off = rows - n;
			for (int col = 0; col < cols - 1 - n; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid - off));
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid - off + 1));
				vid++;
			}
		}
		
		graph.makeFaces(false);
		graph.reorder();

		return graph;
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

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.TriangleTiling.id(), true);
		concepts.set(Concept.HexShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
