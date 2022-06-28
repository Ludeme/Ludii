package game.functions.graph.generators.basis.square;

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
 * Defines a rectangular board on a square grid.
 * 
 * @author cambolbro
 */
@Hide
public class RectangleOnSquare extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final DiagonalsType diagonals;
	private final boolean       pyramidal;
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for the Square tiling.
	 * 
	 * @param rows      The number of rows.
	 * @param columns   The number of columns.
	 * @param diagonals The type of the diagonals.
	 * @param pyramidal True if this is a pyramidal rectangle.
	 */
	public RectangleOnSquare
	(
	   	 final DimFunction   rows,
	   	 final DimFunction   columns,
	   	 final DiagonalsType diagonals,
	   	 final Boolean       pyramidal
	)
	{
		this.basis = BasisType.Square;
		this.shape = (columns == null) ? ShapeType.Square : ShapeType.Rectangle;
		
		this.diagonals = diagonals;
		this.pyramidal = (pyramidal == null) ? false : pyramidal.booleanValue();
		
		this.dim = new int[] { rows.eval(), (columns == null) ? rows.eval() : columns.eval() };	
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final Graph graph = new Graph();

		// Add 1 if playing on the cells, as the number of cells in each 
		// direction is 1 less than the number of vertices.
		final int rows = dim[0] + (siteType == SiteType.Cell ? 1 : 0);
		final int cols = dim[1] + (siteType == SiteType.Cell ? 1 : 0);

		// Create vertices
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				final Point2D pt = new Point2D.Double(col, row);
				graph.addVertex(pt);
			}

		// Create edges
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
			 	final Vertex vertexA = graph.findVertex(col, row);

				for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
				{
					final int rr = row + Square.steps[dirn][0];
					final int cc = col + Square.steps[dirn][1];
					
					if (rr < 0 || rr >= rows || cc < 0 || cc >= cols)
						continue;

					final Vertex vertexB = graph.findVertex(cc, rr);
				
					if (vertexA != null && vertexB != null)
						graph.findOrAddEdge(vertexA, vertexB);
				}
			}
		
		if (pyramidal)
		{
			// Equilateral square pyramid (all sides of unit length)
			final double dz = 1.0 / Math.sqrt(2); 
			
			// Create vertices and edges for higher layers 
			final int layers = rows;
			for (int layer = 1; layer < layers; layer++)
			{
				final double offX = layer * 0.5;
				final double offY = layer * 0.5;
				final double offZ = layer * dz;
				
				// Create vertices for this layer
				for (int row = 0; row < rows - layer; row++)
					for (int col = 0; col < cols - layer; col++)
					{
						// Create this pyramidal vertex
						final double x = offX + col;
						final double y = offY + row;
						final double z = offZ;
						
						graph.findOrAddVertex(x, y, z);
					}
			}
			
			// Add pyramidal edges
			graph.makeEdges();
			//graph.reorder();
		}
		
		Square.handleDiagonals(graph, 0, rows, 0, cols, diagonals);

		//if (siteType == SiteType.Cell)
			graph.makeFaces(false);
		
		graph.setBasisAndShape(basis, shape);
		graph.reorder();

		//System.out.println(graph);
		
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

		if (diagonals == null)
			concepts.set(Concept.SquareTiling.id(), true);
		else if (diagonals.equals(DiagonalsType.Alternating))
			concepts.set(Concept.AlquerqueTiling.id(), true);

		if (dim[0] == dim[1])
		{
			if(pyramidal)
				concepts.set(Concept.SquarePyramidalShape.id(), true);
			else
				concepts.set(Concept.SquareShape.id(), true);
		}
		else
		{
			if(pyramidal)
				concepts.set(Concept.RectanglePyramidalShape.id(), true);
			else
				concepts.set(Concept.RectangleShape.id(), true);
		}

		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		
		return concepts;
	}

	//-------------------------------------------------------------------------

}
