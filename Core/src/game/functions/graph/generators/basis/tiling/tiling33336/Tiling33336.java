package game.functions.graph.generators.basis.tiling.tiling33336;

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
public class Tiling33336 extends Basis
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
	public static final double[][] ref = 
	{
		// Central hexagon
		{  -1 * ux,  1 * uy },
		{   1 * ux,  1 * uy },
		{   2 * ux,  0 * uy },
		{   1 * ux, -1 * uy },
		{  -1 * ux, -1 * uy },
		{  -2 * ux,  0 * uy },
		
		// Surrounding triangles
		{  -2 * ux,  2 * uy },
		{   0 * ux,  2 * uy },
		{   2 * ux,  2 * uy },
		{   3 * ux,  1 * uy },
		{   4 * ux,  0 * uy },
		{   3 * ux, -1 * uy },
		{   2 * ux, -2 * uy },
		{   0 * ux, -2 * uy },
		{  -2 * ux, -2 * uy },
		{  -3 * ux, -1 * uy },
		{  -4 * ux,  0 * uy },
		{  -3 * ux,  1 * uy },
	};
	
	//-------------------------------------------------------------------------

	/**
	 * @param dim Size of board (hexagons per side).
	 * 
	 * @example (tiling33336 3)
	 */
	public Tiling33336
	(
		 final DimFunction dim
	)
	{
		this.basis = BasisType.T33336;
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
				final Point2D ptRef = Tiling33336.xy(row, col);
								
				// Add satellite points (squares and triangles)
				for (int n = 0; n < Tiling33336.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling33336.ref[n][0];
					final double y = ptRef.getY() + Tiling33336.ref[n][1];
					
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
		return new Point2D.Double(col * 5 * ux - row * 4 * ux, row * 2 * uy + col * uy);
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
