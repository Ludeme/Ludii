package game.functions.graph.generators.basis.mesh;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Poly;
import main.math.Polygon;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Creates a graph based on a random spread of points within a given shape.
 * 
 * @author cambolbro
 * 
 * The mesh is based on a Delaunay triangulation of the points.
 */
@SuppressWarnings("javadoc")
@Hide
public class Mesh extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param numVertices Number of vertices to generate.
	 * @param poly        Outline shape to fill [square].
	 * 
	 * @example (mesh 50)
	 * @example (mesh 64 (poly { {0 0} {0 3} {2 2} {2 0} } ))
	 */
	public static GraphFunction construct
	(
			 final DimFunction numVertices,
		@Opt final Poly    poly
	)
	{
		return new 	CustomOnMesh
					(
						numVertices, 
						(poly == null ? new Polygon(4) : poly.polygon()), 
						null
					);
	}

	/**
	 * @param points      Actual (x,y) point positions to use.
	 * 
	 * @example (mesh { {0 0} {0 1} {0.5 0.5} {1 1} {1 0} } )
	 */
	public static GraphFunction construct
	(
		final Float[][] points
	)
	{
		final List<Point2D> pointsList = new ArrayList<Point2D>();
		for (final Float[] xy : points)
		{
			if (xy.length < 2)
			{
				System.out.println("** Mesh: Points should have two values.");
				continue;
			}
			pointsList.add(new Point2D.Double(xy[0].floatValue(), xy[1].floatValue()));
		}
			
		return new CustomOnMesh(null, null, pointsList);
	}

	//-------------------------------------------------------------------------

//	/**
//	 * @param poly Vertex positions.
//	 * 
//	 * @example (mesh { {0 0} {0 1} {0.5 0.5} {1 1} {1 0} } )
//	 */
//	public static GraphFunction construct
//	(
//		final Float[][] points 
//	)
//	{
//		return new CustomOnMesh(null, points);
//	}

	//-------------------------------------------------------------------------

	private Mesh()
	{
		// Ensure that compiler does not pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Null placeholder to make the grammar recognise Mesh
		return null;
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
