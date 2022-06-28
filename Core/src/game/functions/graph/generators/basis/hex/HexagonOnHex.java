package game.functions.graph.generators.basis.hex;

import java.awt.geom.Point2D;
import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a hexhex board.
 * 
 * @author cambolbro
 */
@Hide
public class HexagonOnHex extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Hex.
	 * 
	 * @param dim The dimension.
	 */
	public HexagonOnHex(final DimFunction dim)
	{
		this.basis = BasisType.Hexagonal;
		this.shape  = ShapeType.Hexagon;
		this.dim = new int[]{ dim.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = 2 * dim[0] - 1;
		final int cols = 2 * dim[0] - 1;
		
//		// Create vertices
//		final List<double[]> vertexList = new ArrayList<double[]>();		
//		for (int row = 0; row < rows; row++)
//			for (int col = 0; col < cols; col++)
//			{
//				if (col > cols / 2 + row || row - col > cols / 2)
//					continue;
//				
//				final Point2D ptRef = Hex.xy(row, col);
//				
//				for (int n = 0; n < Hex.ref.length; n++)
//				{
//					final double x = ptRef.getX() + Hex.ref[n][0];
//					final double y = ptRef.getY() + Hex.ref[n][1];
//					
//					// See if vertex already created
//					int vid;
//					for (vid = 0; vid < vertexList.size(); vid++)
//					{
//						final double[] ptV = vertexList.get(vid);
//						final double dist = MathRoutines.distance(ptV[0], ptV[1], x, y);
//						if (dist < 0.1)
//							break;
//					}
//					
//					if (vid >= vertexList.size())
//						vertexList.add(new double[]{ x, y });
//				}
//			}
//
//		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);

		final Graph graph = new Graph();
		
		final double[][] pts = new double[6][2];
		final Vertex[] verts = new Vertex[6];
		
		for (int row = 0; row <=  rows / 2; row++)
			for (int col = 0; col < cols; col++)
			{
				if (col > cols / 2 + row || row - col > cols / 2)
					continue;
				
				final Point2D ptRef = Hex.xy(row, col);
				
				for (int n = 0; n < Hex.ref.length; n++)
				{
					pts[n][0] = ptRef.getX() + Hex.ref[n][0];
					pts[n][1] = ptRef.getY() + Hex.ref[n][1];
				}
				
					verts[4] = graph.addVertex(pts[4][0], pts[4][1]);
					verts[3] = graph.addVertex(pts[3][0], pts[3][1]);
	
					if (col + 1 > cols / 2 + row)
						verts[2] = graph.addVertex(pts[2][0], pts[2][1]);

					//graph.addEdge(verts[3].id(), verts[4].id());			
			}
		
		for (int row = rows / 2; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				if (col > cols / 2 + row || row - col > cols / 2)
					continue;
				
				final Point2D ptRef = Hex.xy(row, col);
				
				for (int n = 0; n < Hex.ref.length; n++)
				{
					pts[n][0] = ptRef.getX() + Hex.ref[n][0];
					pts[n][1] = ptRef.getY() + Hex.ref[n][1];
				}
				
				verts[5] = graph.addVertex(pts[5][0], pts[5][1]);
				verts[0] = graph.addVertex(pts[0][0], pts[0][1]);
	
				if (col == cols - 1)
					verts[1] = graph.addVertex(pts[1][0], pts[1][1]);

				//graph.addEdge(verts[5].id(), verts[0].id());			
			}
			
		// Edges along rows (bottom half)
		int vid = 0;
		for (int row = 0; row <= rows / 2; row++)
		{
			for (int col = 0; col < cols / 2 + 1 + row; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + 1));
				vid++;
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + 1));
				vid++;
			}
			vid++;
		}

		// Edges along rows (top half)
		for (int row = rows / 2; row < rows; row++)
		{
			for (int col = 0; col < cols + rows / 2 - row; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + 1));
				vid++;
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + 1));
				vid++;
			}
			vid++;
		}

		// Edges between rows (bottom half)
		vid = 0;
		for (int row = 0; row < rows / 2; row++)
		{
			final int off = rows + 2 * row + 3;
			for (int col = 0; col <= cols / 2 + 1 + row; col++)
			{
				graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + off));
				vid += 2;
			}
			vid--;
		}
		
		// Edges along middle
		final int offM = 2 * rows + 1;
		for (int col = 0; col < cols + 1; col++)
		{
			graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + offM));
			vid+=2;
		}
		
		// Edges between rows (upper half)
		for (int row = rows / 2; row < rows; row++)
		{
			final int off = 3 * rows - 1 - 2 * row;
			for (int col = 0; col < cols + rows / 2 - row; col++)
			{
				if (vid + off < graph.vertices().size())
					graph.addEdge(graph.vertices().get(vid), graph.vertices().get(vid + off));
				vid += 2;
			}
			vid++;
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
		concepts.set(Concept.HexTiling.id(), true);
		concepts.set(Concept.HexShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
