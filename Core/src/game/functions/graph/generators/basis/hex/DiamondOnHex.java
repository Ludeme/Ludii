package game.functions.graph.generators.basis.hex;

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
 * Defines a diamond (i.e. rhombus) shaped hex board on a hexagonal tiling.
 * 
 * @author cambolbro
 * 
 * @remarks A diamond on a hexagonal grid is a rhombus, as used in the game Hex.
 */
@Hide
public class DiamondOnHex extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Hex.
	 * 
	 * @param dimA The dimension A.
	 * @param dimB The dimension B.
	 */
	public DiamondOnHex
	(
	   	 	  final DimFunction dimA,
	   	 @Opt final DimFunction dimB
	)
	{
		this.basis = BasisType.Hexagonal;
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
		
		final int maxRows = (isPrism) ? rows + cols - 1 : rows;
		final int maxCols = (isPrism) ? rows + cols - 1 : cols;
		
		for (int row = 0; row < maxRows; row++)
			for (int col = 0; col < maxCols; col++)
			{
				if (isPrism && Math.abs(row - col) >= rows)
					continue;
				
				final Point2D ptRef = xy(row, col);
				
				for (int n = 0; n < Hex.ref.length; n++)
				{
					final double x = ptRef.getX() + Hex.ref[n][1];
					final double y = ptRef.getY() + Hex.ref[n][0];
					
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
	
		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		
		graph.reorder();
		
		return graph;
	}

	//-------------------------------------------------------------------------

	static Point2D xy(final int row, final int col)
	{
		final double hx = unit * Math.sqrt(3);
		final double hy = unit * 3 / 2;

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
		concepts.set(Concept.HexTiling.id(), true);
		if (shape.equals(ShapeType.Diamond))
			concepts.set(Concept.DiamondShape.id(), true);
		else
			concepts.set(Concept.PrismShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
