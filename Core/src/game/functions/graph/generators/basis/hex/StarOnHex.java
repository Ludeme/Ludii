package game.functions.graph.generators.basis.hex;

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
 * Defines a six-pointed star board on a hexagonal grid.
 * 
 * @author cambolbro
 */
@Hide
public class StarOnHex extends Basis
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Hex.
	 * 
	 * @param dim The dimension.
	 */
	public StarOnHex(final DimFunction dim)
	{
		this.basis = BasisType.Hexagonal;
		this.shape  = ShapeType.Star;
		
		this.dim = new int[] { dim.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int d = dim[0];
		final int rows = 4 * dim[0] + 1;
		final int cols = 4 * dim[0] + 1;
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				if (r < d)
				{
					if (c < d || c - r > d)
						continue;
				}
				else if (r <= 2 * d)
				{
					if (r - c > d || c >= cols - d)
						continue;
				}
				else if (r <= 3 * d)
				{
					if (c < d || c - r > d)
						continue;
				}
				else
				{
					if (c > 3 * d || r - c > d)
						continue;
				}
				
				final Point2D ptRef = Hex.xy(r, c);
				for (int n = 0; n < 6; n++)
				{
					final double x = ptRef.getX() + Hex.ref[n][0];
					final double y = ptRef.getY() + Hex.ref[n][1];
					
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
		concepts.set(Concept.StarShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
