package game.functions.graph.generators.basis.celtic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.GraphElement;
import game.util.graph.MeasureGraph;
import game.util.graph.Poly;
import game.util.graph.Vertex;
import gnu.trove.list.array.TIntArrayList;
import main.math.MathRoutines;
import main.math.Polygon;
import main.math.Vector;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board based on Celtic knotwork.
 * 
 * @author cambolbro
 * 
 * @remarks Celtic knotwork typically has a small number of continuous paths 
 *          crossing the entire area -- usually just one -- making these 
 *          designs an interesting choice for path-based games.
 */
public class Celtic extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final Polygon polygon = new Polygon();
	private final TIntArrayList sides = new TIntArrayList();

	//-------------------------------------------------------------------------

	/**
	 * For defining a celtic tiling with the number of rows and the number of
	 * columns.
	 * 
	 * @param rows    Number of rows.
	 * @param columns Number of columns.
	 * 
	 * @example (celtic 3)
	 */
	public Celtic
	(
			 final DimFunction rows,
		@Opt final DimFunction columns
	)
	{
		this.basis = BasisType.Celtic;
		this.shape = (columns == null || rows == columns) ? ShapeType.Square : ShapeType.Rectangle;

		if (columns == null)
			this.dim = new int[] { rows.eval(), rows.eval() };
		else 
			this.dim = new int[] { rows.eval(), columns.eval() };
	}

	/**
	 * For defining a celtic tiling with a polygon or the number of sides.
	 * 
	 * @param poly  Points defining the board shape.
	 * @param sides Length of consecutive sides of outline shape.
	 * 
	 * @example (celtic (poly { {1 2} {1 6} {3 6} {3 4} {4 4} {4 2} }))
	 * @example (celtic { 4 3 -1 2 3 })
	 */
	public Celtic
	(
		@Or final Poly     	    poly,
		@Or final DimFunction[] sides
	)
	{
		int numNonNull = 0;
		if (poly != null)
			numNonNull++;
		if (sides != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Exactly one array parameter must be non-null.");
		
		this.basis = BasisType.Celtic;
		this.shape = ShapeType.NoShape;

		if (poly != null)
		{
			this.polygon.setFrom(poly.polygon());
		}
		else
		{
			for (int n = 0; n < sides.length; n++)
				this.sides.add(sides[n].eval());		
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		//final double tolerance = 0.001;
		
		final double uu  = unit / Math.sqrt(2);
		final double uu2 = 2 * uu;

		final double[][] steps =  { {uu2, 0}, {0, uu2}, {-uu2, 0}, {0, -uu2} };

		final double[][] ref = { {uu, 0}, {0, uu}, {-uu, 0}, {0, -uu} };
		
		int fromCol = 0;
		int fromRow = 0;
		int toCol = 0;
		int toRow = 0;
		
		if (polygon.isEmpty() && !sides.isEmpty())
			polygon.fromSides(sides, steps);		
		
		if (polygon.isEmpty())
		{
			toCol = dim[1] - 1;
			toRow = dim[0] - 1;
		}
		else
		{
			polygon.inflate(0.1);
		
			final Rectangle2D bounds = polygon.bounds();
			
			fromCol = (int)bounds.getMinX() - 1;
			fromRow = (int)bounds.getMinY() - 1;
			
			toCol = (int)bounds.getMaxX() + 1;
			toRow = (int)bounds.getMaxY() + 1;
		}

		final Graph graph = new Graph();

		for (int row = fromRow; row < toRow + 1; row++)
			for (int col = fromCol; col < toCol + 1; col++)
			{
				// Determine reference octagon position
				final Point2D ptRef = new Point2D.Double(col * uu2, row * uu2);
				
				if (!polygon.isEmpty() && !polygon.contains(ptRef))
					continue;
				
				// Add satellite points (vertices of octagon)
				for (int n = 0; n < ref.length; n++)
				{
					final double x = ptRef.getX() + ref[n][0];
					final double y = ptRef.getY() + ref[n][1];
					
					graph.findOrAddVertex(x, y);
				}
			}
		graph.makeEdges();
		graph.makeFaces(true);
		
		// Get list of perimeter vertices
		MeasureGraph.measurePerimeter(graph);		
		final List<GraphElement> list = graph.perimeters().get(0).elements();
		
		// Determine which perimeter points to add curves to
		final BitSet keypoints = new BitSet();
		int flatRun = 0;
		
		for (int n = 0; n < list.size(); n++)
		{
			final Vertex vl = (Vertex)list.get((n - 1 + list.size()) % list.size());
			final Vertex vm = (Vertex)list.get(n);
			final Vertex vn = (Vertex)list.get((n + 1) % list.size());
			
			final double diff = MathRoutines.angleDifference(vl.pt2D(), vm.pt2D(), vn.pt2D());
			
			if (diff > 0.25 * Math.PI)
			{
				// Convex corner
				keypoints.set(n);
				flatRun = 0;
			}
			else if (Math.abs(diff) < 0.1 * Math.PI)
			{
				// Flat step
				flatRun++;
				if (flatRun % 2 == 0)
					keypoints.set(n);
			}
		}

		// Add curves for relevant keypoints
		for (int n = keypoints.nextSetBit(0); n >= 0; n = keypoints.nextSetBit(n + 1))
		{
			final Vertex vb = (Vertex)list.get((n - 1 + list.size()) % list.size());
			final Vertex vc = (Vertex)list.get(n);
			final Vertex vd = (Vertex)list.get((n + 1) % list.size());
			final Vertex ve = (Vertex)list.get((n + 2) % list.size());
		
			final double diffC = MathRoutines.angleDifference(vb.pt2D(), vc.pt2D(), vd.pt2D());			
			final double diffD = MathRoutines.angleDifference(vc.pt2D(), vd.pt2D(), ve.pt2D());
			
			if (diffD < -0.25 * Math.PI)
			{
				// Double step: join C to E 
//				System.out.println("Joining V" + vc.id() + " to V" + ve.id() + " (double step)...");
				
				final Vector tangentA = new Vector(vd.pt(), ve.pt());
				final Vector tangentB = new Vector(vd.pt(), vc.pt());
				
				tangentA.normalise();
				tangentB.normalise();

				tangentA.scale(1.25);
				tangentB.scale(1.25);

				graph.addEdge(vc.id(), ve.id(), tangentA, tangentB);
				graph.findOrAddFace(vd.id(), vc.id(), ve.id());
			}
			else
			{
				// Single step: join C to D and add a corner
//				System.out.println("Joining V" + vc.id() + " to V" + vd.id() + " (single step)...");
				
				// Add corner
				Vector tangentAX = new Vector(vb.pt2D(), vc.pt2D());
				Vector tangentBX = new Vector(vb.pt2D(), vc.pt2D());
				
				if (Math.abs(diffC) < 0.1 * Math.PI)
				{
					// Is flat step: override tangents
					tangentAX = new Vector(vc.pt2D(), vd.pt2D());
					tangentBX = new Vector(vc.pt2D(), vd.pt2D());
					
					tangentAX.perpendicular();
					tangentBX.perpendicular();

					tangentAX.reverse();
					tangentBX.reverse();
				}

				final Point2D midCD = new Point2D.Double((vc.pt().x() + vd.pt().x()) / 2, (vc.pt().y() + vd.pt().y()) / 2);
				
				final Point2D ptV = new Point2D.Double(midCD.getX() + 0.5 * tangentAX.x(), midCD.getY() + 0.5 * tangentAX.y());
				final Point2D ptX = new Point2D.Double(midCD.getX() + 0.9 * tangentAX.x(), midCD.getY() + 0.9 * tangentAX.y());
				
				final Vertex vx = graph.addVertex(ptX);
									
				tangentAX.normalise();
				tangentBX.normalise();
				
				tangentAX.scale(1.333);
				tangentBX.scale(1.333);
				
				final Vector tangentXA = new Vector(ptV, vc.pt2D());
				final Vector tangentXB = new Vector(ptV, vd.pt2D());

				tangentXA.normalise();
				tangentXB.normalise();

				graph.addEdge(vc.id(), vx.id(), tangentAX, tangentXA);
				graph.addEdge(vx.id(), vd.id(), tangentXB, tangentBX);
				graph.findOrAddFace(vc.id(), vx.id(), vd.id());
			}
		}
		
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
		// Nothing to do.
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.CelticTiling.id(), true);
		if (shape.equals(ShapeType.Square))
			concepts.set(Concept.SquareShape.id(), true);
		else
			concepts.set(Concept.RectangleShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
