package game.functions.graph.generators.basis.tri;

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
import main.math.Polygon;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a rectangular board.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class CustomOnTri extends Basis
{
	private static final long serialVersionUID = 1L;

	private final Polygon polygon = new Polygon();
	private final TIntArrayList sides = new TIntArrayList();

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Triangular.
	 * 
	 * @param polygon The polygon.
	 */
	public CustomOnTri
	(
	   	 final Polygon polygon
	)
	{
		this.basis = BasisType.Triangular;
		this.shape  = ShapeType.Custom;

		this.polygon.setFrom(polygon);
	}

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param sides The indices of the sides.
	 */
	public CustomOnTri
	(
			final DimFunction[] sides
	)
	{
		this.basis = BasisType.Triangular;
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
			polygonFromSides(siteType);

		polygon.inflate(0.1);
		final Rectangle2D bounds = polygon.bounds();
		
		// If limping boards (or any other shapes) have missing cells,
		// then increase the following margins.
		final int margin = (shape == ShapeType.Limping && sides != null) ? sides.get(0) : 2;
		
		final int fromCol = (int)bounds.getMinX() - margin;
		final int fromRow = (int)bounds.getMinY() - margin;
		
		final int toCol = (int)bounds.getMaxX() + margin;
		final int toRow = (int)bounds.getMaxY() + margin;

		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = fromRow; r <= toRow; r++)
			for (int c = fromCol; c <= toCol; c++)
			{
				final Point2D pt = Tri.xy(r, c);
				if (!polygon.contains(pt))
					continue;
				vertexList.add(new double[]{ pt.getX(), pt.getY() });
			}
		
		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		
		graph.reorder();

		return graph;
	}

	//-------------------------------------------------------------------------

	/**
	 * Generate polygon from the description of sides.
	 * 
	 * Can't really distinguish Cell from Vertex versions here (-ve turns make
	 * ambiguous cases) so treat both the same.
	 */
	void polygonFromSides(final SiteType siteType)
	{		
		final int[][] steps = { {1, 0}, {1, 1}, {0, 1}, {-1, 0}, {-1, -1}, {0, -1} };

		int dirn = 1;
		
		int row = 0;
		int col = 0;

		polygon.clear();
		polygon.add(Tri.xy(row, col));
		
		for (int n = 0; n < Math.max(5, sides.size()); n++)
		{
			int nextStep = sides.get(n % sides.size());
			
			// Always reduce by 1
			//if (siteType != SiteType.Cell)
			{
				if (nextStep < 0)
					nextStep += 1;
				else
					nextStep -= 1;
			}
			
			if (nextStep < 0)
				dirn -= 1;
			else
				dirn += 1;
			
			dirn = (dirn + 6) % 6;  // keep in range
			
			if (nextStep > 0)
			{
				row += nextStep * steps[dirn][0];
				col += nextStep * steps[dirn][1];

				polygon.add(Tri.xy(row, col));
			}
		}
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
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
