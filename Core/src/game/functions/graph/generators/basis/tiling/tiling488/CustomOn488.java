package game.functions.graph.generators.basis.tiling.tiling488;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import gnu.trove.list.array.TIntArrayList;
import main.math.MathRoutines;
import main.math.Polygon;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a custom board on the 4.8.8 tiling.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class CustomOn488 extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final Polygon polygon = new Polygon();
	private final TIntArrayList sides = new TIntArrayList();
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param polygon The polygon.
	 */
	public CustomOn488(final Polygon polygon)
	{
		this.basis = BasisType.T488;
		this.shape = ShapeType.Custom;

		this.polygon.setFrom(polygon);
	}

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param sides The indices of the sides.
	 */
	public CustomOn488
	(
	   	 final DimFunction[] sides
	)
	{
		this.basis = BasisType.T488;
		this.shape = ShapeType.Custom;

		for (int n = 0; n < sides.length; n++)
			this.sides.add(sides[n].eval());
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int[][] steps =  { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
	
		if (polygon.isEmpty() && !sides.isEmpty())
			polygon.fromSides(sides, steps);
		
		polygon.inflate(0.1);

		final Rectangle2D bounds = polygon.bounds();
		
		final int fromCol = (int)bounds.getMinX() - 2;
		final int fromRow = (int)bounds.getMinY() - 2;
		
		final int toCol = (int)bounds.getMaxX() + 2;
		final int toRow = (int)bounds.getMaxY() + 2;

		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = fromRow; r <= toRow; r++)
			for (int c = fromCol; c <= toCol; c++)
			{
				// Determine reference octagon position
				final Point2D.Double ptRef = Tiling488.xy(r, c);	
				if (polygon.contains(ptRef))
				{
					// Add satellite points (octagon vertices)
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
							vertexList.add(new double[]{ x, y });
					}
				}
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, BaseGraphFunction.unit, basis,
				shape);
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
		// ...
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
