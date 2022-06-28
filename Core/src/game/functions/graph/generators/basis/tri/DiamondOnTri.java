package game.functions.graph.generators.basis.tri;

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
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a diamond (i.e. rhombus) shaped hex board on a triangular tiling.
 * 
 * @author cambolbro
 * 
 * @remarks A diamond on a triangular grid is a rhombus, as used in the game Hex.
 */
@Hide
public class DiamondOnTri extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Tri.
	 * 
	 * @param dimA The dimension A.
	 * @param dimB The dimension B.
	 */
	public DiamondOnTri
	(
	   	 	  final DimFunction dimA,
	   	 @Opt final DimFunction dimB
	)
	{
		this.basis = BasisType.Triangular;
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
		
		final int rows = dim[0] + (siteType == SiteType.Cell ? 1 : 0);
		final int cols = (isPrism ? dim[1] : dim[0]) + (siteType == SiteType.Cell ? 1 : 0);
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		
		if (isPrism)
		{
			for (int r = 0; r < rows + cols - 1; r++)
				for (int c = 0; c < cols + rows - 1; c++)
				{
					if (Math.abs(r - c) >= rows)
						continue;
						
					final Point2D pt = xy(r, c);
					vertexList.add(new double[] { pt.getX(), pt.getY() });
				}
		}
		else
		{
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{					
					final Point2D pt = xy(r, c);
					vertexList.add(new double[] { pt.getX(), pt.getY() });
				}			
		}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		
		graph.reorder();
			
		return graph;
	}

	//-------------------------------------------------------------------------

	static Point2D xy(final int row, final int col)
	{
		final double hx = unit;
		final double hy = Math.sqrt(3) / 2.0;

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
		concepts.set(Concept.TriangleTiling.id(), true);
		if (shape.equals(ShapeType.Diamond))
			concepts.set(Concept.DiamondShape.id(), true);
		else
			concepts.set(Concept.PrismShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
