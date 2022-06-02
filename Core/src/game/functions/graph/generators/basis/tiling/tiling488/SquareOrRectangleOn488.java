package game.functions.graph.generators.basis.tiling.tiling488;

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
 * Defines a square or rectangular board on the 4.8.8 tiling.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class SquareOrRectangleOn488 extends Basis
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param dimA The dimension A.
	 * @param dimB The dimension B.
	 */
	public SquareOrRectangleOn488
	(
	   	 final DimFunction dimA,
	   	 final DimFunction dimB
	)
	{	
		this.basis = BasisType.T488;
		
		this.shape  = (dimB == null || dimA == dimB) ? ShapeType.Square : ShapeType.Rectangle;
		if (dimB == null || dimA == dimB)
			this.dim = new int[] { dimA.eval() };
		else
			this.dim = new int[] { dimA.eval(), dimB.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = dim[0];
		final int cols = (dim.length > 1) ? dim[1] : dim[0];
		
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				// Determine reference octagon position
				final Point2D ptRef = Tiling488.xy(r, c);
				
				// Add satellite points (vertices of octagon)
				for (int n = 0; n < Tiling488.ref.length; n++)
				{
					final double x = ptRef.getX() + Tiling488.ref[n][0];
					final double y = ptRef.getY() + Tiling488.ref[n][1];
					
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

		return BaseGraphFunction.createGraphFromVertexList(vertexList, 1, basis, shape);
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
		if (shape.equals(ShapeType.Square))
			concepts.set(Concept.SquareShape.id(), true);
		else
			concepts.set(Concept.RectangleShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
