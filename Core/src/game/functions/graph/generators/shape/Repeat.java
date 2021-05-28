package game.functions.graph.generators.shape;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import annotations.Name;
import annotations.Or;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.types.board.BasisType;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import game.util.graph.Poly;
import main.math.Polygon;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Repeats specified shape(s) to define the board tiling.
 * 
 * @author cambolbro
 */
public class Repeat extends BaseGraphFunction //implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final int 	  rows;
	private final int 	  columns;
	private final Point2D stepColumn;
	private final Point2D stepRow;
	private List<Polygon> polygons = new ArrayList<Polygon>();
	
	//-------------------------------------------------------------------------

	/**
	 * @param rows    Number of rows to repeat.
	 * @param columns Number of columns to repeat.
	 * @param step    Vectors defining steps to the next column and row.
	 * @param poly    The shape to repeat.
	 * @param polys   The set of shapes to repeat.
	 * 
	 * @example (repeat 3 4 step:{{-.5 .75} {1 0}} (poly {{0 0} {0 1} {1 1} {1 0}}))
	 */
	public Repeat
	(
				  final DimFunction  rows,
				  final DimFunction  columns,
			@Name final Float[][]    step,
		@Or 	  final Poly	     poly,
		@Or 	  final Poly[]	     polys
	)
	{
		this.basis = BasisType.NoBasis;
		this.shape = ShapeType.NoShape;

		this.rows    = rows.eval();
		this.columns = columns.eval();

		if (step.length < 2 || step[0].length < 2 || step[1].length < 2)
		{
			System.out.println("** Repeat: Step should contain two pairs of values.");
			this.stepColumn = new Point2D.Double(1, 0);
			this.stepRow    = new Point2D.Double(0, 1);
		}
		else
		{
			this.stepColumn = new Point2D.Double(step[0][0].floatValue(), step[0][1].floatValue());
			this.stepRow    = new Point2D.Double(step[1][0].floatValue(), step[1][1].floatValue());
		}
		
		if (poly != null)
		{	
			polygons.add(poly.polygon());
		}
		else 
		{
			for (final Poly ply : polys)
				polygons.add(ply.polygon());
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final Graph graph = new Graph();
		
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < columns; col++)
			{
				final Point2D ptRef = new Point2D.Double
										(
											col * stepColumn.getX() + row * stepRow.getX(),
											col * stepColumn.getY() + row * stepRow.getY()												
										);
				
				for (final Polygon polygon : polygons)
				{
//					final Point2D ptL = polygon.points().get(polygon.points().size() - 1);
//					Vertex prev = graph.findOrAddVertex(ptRef.getX() + ptL.getX(), ptRef.getY() + ptL.getY());
//					
//					for (final Point2D pt : polygon.points())
//					{
//						final Vertex next = graph.findOrAddVertex(ptRef.getX() + pt.getX(), ptRef.getY() + pt.getY());
//						graph.findOrAddEdge(prev, next);
//						prev = next;
//					}
					for (int n = 0; n < polygon.points().size(); n++)
					{
						final Point2D ptA = polygon.points().get(n);
						final Point2D ptB = polygon.points().get((n + 1) % polygon.points().size());
						
						final Vertex vertexA = graph.findOrAddVertex(ptRef.getX() + ptA.getX(), ptRef.getY() + ptA.getY());
						final Vertex vertexB = graph.findOrAddVertex(ptRef.getX() + ptB.getX(), ptRef.getY() + ptB.getY());
					
						graph.findOrAddEdge(vertexA, vertexB);
					}
				}
			}
		//graph.makeEdges();
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

	//-------------------------------------------------------------------------

}
