package game.functions.graph.generators.basis.tiling.tiling31212;

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
 * Defines a board on the semi-regular 3.12.12 tiling.
 * 
 * @author cambolbro
 */
@Hide
public class Tiling31212 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * The x unit.
	 */
	public static final double ux = unit / 2;

	/**
	 * The y unit.
	 */
	public static final double uy = unit * Math.sqrt(3) / 2;

	/**
	 * The references.
	 */
	public static final double[][] ref = new double[12][2];
	{
		final double r = 1 / Math.sqrt(2 - Math.sqrt(3));
		final double off = Math.PI / 12 ;
		
		for (int s = 0; s < 12; s++)
		{
			final double t = s / 12.0;
			final double theta = off + t * 2 * Math.PI;
			
			ref[s][0] = r * Math.cos(theta);
			ref[s][1] = r * Math.sin(theta);
			
			//System.out.println("x=" + ref[s][0] + ", y=" + ref[s][1]);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param dim Size of board (sequencedecagons per side).
	 */
	public Tiling31212
	(
		 final DimFunction dim
	)
	{
		this.basis = BasisType.T31212;
		this.shape  = ShapeType.Hexagon;
		
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
				final Point2D ptRef = Tiling31212.xy(row, col);
								
				// Add satellite points (squares and triangles)
				for (int n = 0; n < Tiling31212.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling31212.ref[n][0];
					final double y = ptRef.getY() + Tiling31212.ref[n][1];
					
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
		final double dx = unit * 3.7320508;
		final double dy = dx * Math.sqrt(3) / 2;

		return new Point2D.Double((col - 0.5 * row) * dx, row * dy);
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
