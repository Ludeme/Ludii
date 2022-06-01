package game.functions.graph.generators.basis.tiling.tiling3636;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
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
 * Defines a board on a semi-regular 3.6.3.6 tiling.
 * 
 * @author cambolbro
 * 
 * Tiling 3.6.3.6 is composed of triangles and hexagons.
 */
@Hide
public class Tiling3636 extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * The x unit.
	 */
	public static final double ux = unit * Math.sqrt(3) / 2;

	/**
	 * The y unit.
	 */
	public static final double uy = unit;

	/**
	 * The references.
	 */
	public static final double[][] ref = 
	{
		{  0.0 * ux,  1.0 * uy },
		{  1.0 * ux,  0.5 * uy },
		{  1.0 * ux, -0.5 * uy },
		{  0.0 * ux, -1.0 * uy },
		{ -1.0 * ux, -0.5 * uy },
		{ -1.0 * ux,  0.5 * uy },
	};

	//-------------------------------------------------------------------------

	/**
	 * @param dimA  Primary board dimension; cells or vertices per side.
	 * @param dimB  Secondary Board dimension; cells or vertices per side.
	 */
	public Tiling3636
	(
		   	  final DimFunction dimA,
		@Opt  final DimFunction dimB
	)
	{
		this.basis = BasisType.T3636;
		
		if (dimB == null)
		{
			this.shape = ShapeType.Hexagon;
			this.dim = new int[]{ dimA.eval() };
		}
		else
		{
			this.shape = ShapeType.Rhombus;
			this.dim = new int[]{ dimA.eval(), dimB.eval() };			
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = 2 * dim[0] - 1;
		final int cols = 2 * (dim.length < 2 ? dim[0] : dim[1])  - 1;
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				if (shape == ShapeType.Hexagon && (col > cols / 2 + row || row - col > cols / 2))
					continue;
				if (shape == ShapeType.Triangle && row > col)
					continue;
				
				final Point2D ptRef = Tiling3636.xy(row, col);
				
				for (int n = 0; n < Tiling3636.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling3636.ref[n][1];
					final double y = ptRef.getY() + Tiling3636.ref[n][0];
					
					// See if vertex is already created
					int vid;
					for (vid = 0; vid < vertexList.size(); vid++)
					{
						final double[] ptV = vertexList.get(vid);
						final double dist = MathRoutines.distance(ptV[0], ptV[1], x, y);
						if (dist < 0.1)
							break;
					}
					
					if (vid >= vertexList.size())
						vertexList.add(new double[]{ x, y });
				}
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		graph.reorder();
		
		//System.out.println(graph);
		
		return graph;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param row The row.
	 * @param col The column.
	 * @return The Point2D.
	 */
	public static Point2D xy(final int row, final int col)
	{
		final double hx = 2;
		final double hy = Math.sqrt(3);

		return new Point2D.Double(hx * (col - 0.5 * row), hy * row);
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
