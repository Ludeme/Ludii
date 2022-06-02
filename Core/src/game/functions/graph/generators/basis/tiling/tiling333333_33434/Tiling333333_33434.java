package game.functions.graph.generators.basis.tiling.tiling333333_33434;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import main.math.MathRoutines;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board on the semi-regular 3.3.3.3.6 tiling, which is made up of 
 * triangles around hexagons.
 * 
 * @author cambolbro
 */
@Hide
public class Tiling333333_33434 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * The x unit.
	 */
	public static final double ux = unit;

	/**
	 * The y unit.
	 */
	public static final double uy = unit * Math.sqrt(3) / 2;

	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		// Store major hexagon point position
		{ -0.5 * ux,  1.0 * uy },
		{  0.5 * ux,  1.0 * uy },
		{  1.0 * ux,  0.0 * uy },
		{  0.5 * ux, -1.0 * uy },
		{ -0.5 * ux, -1.0 * uy },
		{ -1.0 * ux,  0.0 * uy },
		{ 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
		{ 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
	};
	{		
		// Calculate outer square point positions
		final double a = unit + Math.sqrt(3) / 2.0;
		final double h = a / Math.cos(Math.toRadians(15));
		
		for (int n = 0; n < 12; n++)
		{
			final double theta = Math.toRadians(15 + n * 30);
			
			ref[6 + n][0] = h * Math.cos(theta);
			ref[6 + n][1] = h * Math.sin(theta);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param dim Size of board (hexagons per side).
	 * 
	 * @example (tiling333333\\_33434 3)
	 */
	public Tiling333333_33434
	(
			final DimFunction dim
	)
	{
		this.basis = BasisType.T333333_33434;
		this.shape = ShapeType.Hexagon;
		
		this.dim = new int[] { dim.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = dim[0] * 2 - 1;
		final int cols = dim[0] * 2 - 1;
		
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				if (Math.abs(row - col) > rows / 2)
					continue;
				
				// Determine reference octagon position
				final Point2D ptRef = Tiling333333_33434.xy(row, col);
								
				// Add satellite points (squares and triangles)
				for (int n = 0; n < Tiling333333_33434.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling333333_33434.ref[n][0];
					final double y = ptRef.getY() + Tiling333333_33434.ref[n][1];
					
					// See if vertex already created
					int vid;
					for (vid = 0; vid < vertexList.size(); vid++)
					{
						final double[] ptV = vertexList.get(vid);
						final double dist = MathRoutines.distance(ptV[0], ptV[1], x, y);
						if (dist < 0.1)
							break;
					}
					
					if (vid >= vertexList.size())
						vertexList.add(new double[] { x, y });
				}
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		graph.reorder();
		
		return graph;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D.
	 */
	public static Point2D.Double xy(final int row, final int col)
	{
		final double hx = unit * (1.5 + Math.sqrt(3));
		final double hy = unit * (2 + Math.sqrt(3));

		return new Point2D.Double(hx * (col - row), hy * (row + col) * 0.5);
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
		concepts.set(Concept.SemiRegularTiling.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
