package game.functions.graph.generators.basis.tiling.tiling3464;

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
 * Defines a diamond (i.e. rhombus) shaped hex board on a 3.4.6.4 tiling.
 * 
 * @author cambolbro
 * 
 * @remarks A diamond on a hexagonal grid is a rhombus, as used in the game Hex.
 */
@Hide
public class DiamondOn3464 extends Basis
{
	private static final long serialVersionUID = 1L;

	private static final double ux = unit;
	private static final double uy = unit * Math.sqrt(3) / 2.0;

	private static final double[][] ref = 
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
	 * Hidden constructor, is a helper for Hex.
	 * 
	 * @param dimA The dimension A.
	 * @param dimB The dimension B.
	 */
	public DiamondOn3464
	(
	   	 	  final DimFunction dimA,
	   	 @Opt final DimFunction dimB
	)
	{
		this.basis = BasisType.T3464;
		this.shape  = (dimB == null) ? ShapeType.Diamond : ShapeType.Prism;
		
		this.dim = (dimB == null)
					? new int[]{ dimA.eval() }
					: new int[]{ dimA.eval(), dimB.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final boolean isPrism = (shape == ShapeType.Prism);
		
		final int rows = dim[0];
		final int cols = (isPrism ? dim[1] : dim[0]);
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		
		if (isPrism)
		{
			for (int r = 0; r < rows + cols - 1; r++)
				for (int c = 0; c < cols + rows - 1; c++)
				{
					if (Math.abs(r - c) >= rows)
						continue;
					addVertex(r, c, vertexList);					
				}
		}
		else
		{
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
					addVertex(r, c, vertexList);
		}

		final Graph result = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		result.reorder();
		
		return result;
	}

	//-------------------------------------------------------------------------

	static void addVertex(final int row, final int col, final List<double[]> vertexList)
	{
		final Point2D ptRef = xy(row, col);
		
		for (int n = 0; n < ref.length; n++)
		{
			final double x = ptRef.getX() + ref[n][0];
			final double y = ptRef.getY() + ref[n][1];
			
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
				vertexList.add(new double[]{ x, y });
		}
	}
	
	//-------------------------------------------------------------------------

	static Point2D xy(final int row, final int col)
	{
		final double hx = unit * (1 + Math.sqrt(3));
		final double hy = unit * (3 + Math.sqrt(3)) / 2;

		return new Point2D.Double(hy * (col - row), hx * (row + col) * 0.5);
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
		if (shape.equals(ShapeType.Diamond))
			concepts.set(Concept.DiamondShape.id(), true);
		else
			concepts.set(Concept.PrismShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
