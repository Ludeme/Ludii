package game.functions.graph.generators.basis.tiling.tiling33434;

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
 * Defines a board on the 3.3.4.3.4 tiling, which is made up of squares and  
 * pairs of triangles.
 * 
 * @author cambolbro
 * 
 * @remarks This semi-regular tiling is the dual of the Cairo tiling.
 */
@Hide
public class Tiling33434 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private static final double u2 = unit / 2;
	private static final double u3 = unit * Math.sqrt(3) / 2;

	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		// Store sub-tile point positions
		{ -1 * u2 + 0 * u3, -1 * u2 - 1 * u3 }, 	
		{  1 * u2 + 0 * u3, -1 * u2 - 1 * u3 }, 	
		{ -1 * u2 - 1 * u3,  0 * u2 - 1 * u3 }, 	
		{  1 * u2 + 1 * u3,  0 * u2 - 1 * u3 }, 	
		{  0 * u2 + 0 * u3, -1 * u2 + 0 * u3 }, 	
		{ -2 * u2 - 1 * u3,  0 * u2 + 0 * u3 }, 	
		{  0 * u2 - 1 * u3,  0 * u2 + 0 * u3 }, 	
		{  0 * u2 + 1 * u3,  0 * u2 + 0 * u3 }, 	
		{  2 * u2 + 1 * u3,  0 * u2 + 0 * u3 }, 		
		{  0 * u2 + 0 * u3,  1 * u2 + 0 * u3 }, 	
		{ -1 * u2 - 1 * u3,  0 * u2 + 1 * u3 }, 	
		{  1 * u2 + 1 * u3,  0 * u2 + 1 * u3 }, 	
		{ -1 * u2 + 0 * u3,  1 * u2 + 1 * u3 }, 	
		{  1 * u2 + 0 * u3,  1 * u2 + 1 * u3 }, 	
	};
	
	//-------------------------------------------------------------------------

	/**
	 * @param dim Size of board.
	 * 
	 * @example (tiling33434 4)
	 */
	public Tiling33434
	(
		 final DimFunction dim
	)
	{
		this.basis = BasisType.T33434;
		this.shape = ShapeType.Diamond;
		
		this.dim = new int[] { dim.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = dim[0];
		final int cols = dim[0];
		
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				// Determine reference octagon position
				final Point2D ptRef = Tiling33434.xy(r, c);
								
				// Add satellite points (squares and triangles)
				for (int n = 0; n < Tiling33434.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling33434.ref[n][0];
					final double y = ptRef.getY() + Tiling33434.ref[n][1];
					
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
		final double hx  = unit * (1 + Math.sqrt(3)) / 2;
		final double hy  = hx;

		return new Point2D.Double(hx * (col - row), hy * (row + col));
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
