package game.functions.graph.generators.basis.hex;

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
 * Defines a custom board shape on the hexagonal tiling.
 * 
 * @author cambolbro
 * 
 */
@Hide
public class CustomOnHex extends Basis
{
	private static final long serialVersionUID = 1L;

	private final Polygon polygon = new Polygon();
	private final TIntArrayList sides = new TIntArrayList();

	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Hexagonal.
	 * 
	 * @param polygon The polygon.
	 */
	public CustomOnHex
	(
		final Polygon polygon
	)
	{
		this.basis = BasisType.Hexagonal;
		this.shape  = ShapeType.Custom;

		this.polygon.setFrom(polygon);	
	}

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param sides The indices of the sides.
	 */
	public CustomOnHex
	(
			final DimFunction[] sides
	)
	{
		this.basis = BasisType.Hexagonal;
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
			//polygon.fromSides(sides, Hex.steps);
			polygonFromSides();

		polygon.inflate(0.1);
		
		final Rectangle2D bounds = polygon.bounds();
		
		// If limping boards (or any other shapes) have missing cells,
		// then increase the following margins.
		final int fromCol = (int)bounds.getMinX() - 3;
		final int fromRow = (int)bounds.getMinY() - 3;
		
		final int toCol = (int)bounds.getMaxX() + 3;
		final int toRow = (int)bounds.getMaxY() + 3;

		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = fromRow; r <= toRow; r++)
			for (int c = fromCol; c <= toCol; c++)
			{
				final Point2D ptRef = Hex.xy(r, c);
				
				if (!polygon.contains(ptRef))
					continue;

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
		
//		if (this.shape == ShapeType.Limping)
//		{
//			final int expected = 3 * sides.get(0) * sides.get(0);
//			System.out.println("Limping hex board: " + graph.faces().size() + " found (" + expected + " expected).");
//		}
		
		return graph;
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

		int step = 1;
		
		int row = 0;
		int col = 0;

		polygon.clear();
		polygon.add(Hex.xy(row, col));
		
		for (int n = 0; n < Math.max(5, sides.size()); n++)
		{
			int nextStep = sides.get(n % sides.size());
			
			// Always reduce by 1
			if (nextStep < 0)
				nextStep += 1;
			else
				nextStep -= 1;
			
			if (nextStep < 0)
				step -= 1;
			else
				step += 1;
			
			step = (step + 6) % 6;  // keep in range
			
			if (nextStep > 0)
			{
				row += nextStep * steps[step][0];
				col += nextStep * steps[step][1];
				
				//System.out.println("Hex.xy(row, col) is: " + Hex.xy(row, col));
				
				polygon.add(Hex.xy(row, col));
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
		concepts.set(Concept.HexTiling.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
