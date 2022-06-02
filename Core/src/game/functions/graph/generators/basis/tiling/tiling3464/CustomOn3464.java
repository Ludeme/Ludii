package game.functions.graph.generators.basis.tiling.tiling3464;

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
 * Defines a custom board for the 3.4.6.4 tiling.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class CustomOn3464 extends Basis
{
	private static final long serialVersionUID = 1L;

	private final Polygon polygon = new Polygon();
	private final TIntArrayList sides = new TIntArrayList();

	private static final double[][] ref = 
	{
		// Store major hexagon point position
		{ -0.5 * Tiling3464.ux,  1.0 * Tiling3464.uy },
		{  0.5 * Tiling3464.ux,  1.0 * Tiling3464.uy },
		{  1.0 * Tiling3464.ux,  0.0 * Tiling3464.uy },
		{  0.5 * Tiling3464.ux, -1.0 * Tiling3464.uy },
		{ -0.5 * Tiling3464.ux, -1.0 * Tiling3464.uy },
		{ -1.0 * Tiling3464.ux,  0.0 * Tiling3464.uy },
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
	 * Hidden constructor, is a helper for Tiling3464.
	 * 
	 * @param polygon The polygon.
	 */
	public CustomOn3464(final Polygon polygon)
	{
		this.basis = BasisType.T3464;
		this.shape = ShapeType.Custom;

		this.polygon.setFrom(polygon);
	}

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param sides The indices of the sides.
	 */
	public CustomOn3464(final DimFunction[] sides)
	{
		this.basis = BasisType.T3464;
		this.shape = (sides.length == 2 && sides[0].eval() == sides[1].eval() - 1)
							? ShapeType.Limping 
							: ShapeType.Custom;

		for (int n = 0; n < sides.length; n++)
			this.sides.add(sides[n].eval());
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (polygon.isEmpty() && !sides.isEmpty())
			//polygon.fromSides(sides, Tiling3464.steps);
			polygonFromSides();

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
				final Point2D ptRef = xy(r, c);
				
				if (!polygon.contains(ptRef))
					continue;

				for (int n = 0; n < ref.length; n++)
				{
					final double x = ptRef.getX() + ref[n][1];
					final double y = ptRef.getY() + ref[n][0];
					
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

		final Graph result = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		result.reorder();
		
		return result;
	}

	//-------------------------------------------------------------------------

	/**
	 * Generate polygon from the description of sides.
	 * 
	 * Can't really distinguish Cell from Vertex versions here (-ve turns make
	 * ambiguous cases) so treat both the same.
	 */
	void polygonFromSides()
	{
		final int[][] steps = { {1, 0}, {1, 1}, {0, 1}, {-1, 0}, {-1, -1}, {0, -1} };
		
		int dirn = 1;
		
		int row = 0;
		int col = 0;

		polygon.clear();
		polygon.add(Tiling3464.xy(row, col));
		
		for (int n = 0; n < Math.max(5, sides.size()); n++)
		{
			int nextStep = sides.get(n % sides.size());
			
			// Always reduce by 1
			if (nextStep < 0)
				nextStep += 1;
			else
				nextStep -= 1;
			
			if (nextStep < 0)
				dirn -= 1;
			else
				dirn += 1;
			
			dirn = (dirn + 6) % 6;  // keep in range
			
			if (nextStep > 0)
			{
				row += nextStep * steps[dirn][0];
				col += nextStep * steps[dirn][1];

				polygon.add(xy(row, col));
			}
		}		
	}
	
	//-------------------------------------------------------------------------

	static Point2D xy(final int row, final int col)
	{
		final double hx = unit * (1 + Math.sqrt(3));
		final double hy = unit * (3 + Math.sqrt(3)) / 2;

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
