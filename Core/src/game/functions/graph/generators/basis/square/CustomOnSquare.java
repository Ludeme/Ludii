package game.functions.graph.generators.basis.square;

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
public class CustomOnSquare extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final Polygon polygon = new Polygon();
	private final TIntArrayList sides = new TIntArrayList();
	
	private final DiagonalsType diagonals;
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param polygon   The polygons.
	 * @param diagonals The diagonal type.
	 */
	public CustomOnSquare
	(
	   	 final Polygon       polygon,
   	 	 final DiagonalsType diagonals
	)
	{
		this.basis = BasisType.Square;
		this.shape  = ShapeType.Custom;
		
		this.polygon.setFrom(polygon);
		
		this.diagonals = diagonals;
	}

	/**
	 * Hidden constructor, is a helper for Square.
	 * 
	 * @param sides     The sides.
	 * @param diagonals The diagonal type.
	 */
	public CustomOnSquare
	(
	   	 final DimFunction[] sides,
	   	 final DiagonalsType diagonals
	)
	{
		this.basis = BasisType.Square;
		this.shape  = ShapeType.Custom;

		for (int n = 0; n < sides.length; n++)
			this.sides.add(sides[n].eval());		
		
		this.diagonals = diagonals;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (polygon.isEmpty() && !sides.isEmpty())
			polygon.fromSides(sides, Square.steps);		
		
		polygon.inflate(0.1);
		
		final Rectangle2D bounds = polygon.bounds();
		
		final int fromCol = (int)bounds.getMinX() - 2;
		final int fromRow = (int)bounds.getMinY() - 2;
		
		final int toCol = (int)bounds.getMaxX() + 2;
		final int toRow = (int)bounds.getMaxY() + 2;
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int row = fromRow; row <= toRow; row++)
			for (int col = fromCol; col <= toCol; col++)
			{
				final double x = col;
				final double y = row;
				
				if (polygon.contains(x, y))
					vertexList.add(new double[] { x, y });
			}

		final Graph graph = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		
		Square.handleDiagonals(graph, fromRow, toRow, fromCol, toCol, diagonals);  //, 0.0001);

		graph.makeFaces(false);

		graph.setBasisAndShape(basis, shape);
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
		if(diagonals == null)
			concepts.set(Concept.SquareTiling.id(), true);
		else if(diagonals.equals(DiagonalsType.Alternating))
			concepts.set(Concept.AlquerqueTiling.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
