package game.functions.graph.generators.shape;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.Vector;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board based on a spiral tiling, e.g. the Mehen board.
 * 
 * @author cambolbro
 */
public class Spiral extends Basis
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param turns     Number of turns of the spiral.
	 * @param sites     Number of sites to generate in total.
	 * @param clockwise Whether the spiral should turn clockwise or not [True]. 
	 * 
	 * @example (spiral turns:4 sites:80)
	 */
	public Spiral
	(
         @Name	 	 final DimFunction  turns,
         @Name 		 final DimFunction  sites,
         @Opt  @Name Boolean 		clockwise
	)
	{
		this.basis = BasisType.Spiral;
		this.shape = ShapeType.Spiral;
		
		final int dirn = (clockwise == null) ? 1 : (clockwise.booleanValue() ? 1 : 0);

		this.dim = new int[] { turns.eval(), sites.eval(), dirn };	
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int numTurns = dim[0];
		final int numSites = dim[1];
		final boolean clockwise = dim[2] != 0;
	
		if (numSites > 1000)
			throw new IllegalArgumentException(numSites + " sites in spiral exceeds limit of 1000.");
		
		final Graph graph = new Graph();
	
		// Create vertices
		final double a = 0;
		final double b = 1;  //numTurns;
		
		final double x0 = 0;
		final double y0 = 0;
		
		final Vertex pivot = graph.addVertex(0, 0);  // start with central vertex

		// Twice the number of vertices per ring, offset between rings
		final int base = baseNumber(numTurns, numSites);

		final double[] thetas = new double[4 * numSites];
		int index = 1;

		int steps = base;  // number of steps per ring
		for (int ring = 1; ring <= numTurns + 1; ring++)
		{
			final double dTheta = Math.PI * 2 / steps;
			double theta = Math.PI * 2 * ring;
			
			if (ring <= 2 || ring % 2 == 1)
				theta -= dTheta / 2;  // offset so that lines don't coincide between rings
			
			for (int step = 0; step < steps; step++)
			{	
				thetas[index++] = theta;
				theta += dTheta;
			}
			if (ring <= 2)
				steps *= 2;
		}

		// Smoothing passes to reduce unevenness between steps
		for (int vid = 2; vid < numSites; vid++)
			thetas[vid] = (thetas[vid-1] + thetas[vid+1]) / 2.0;
		
		for (int vid = 2; vid < numSites; vid++)
			thetas[vid] = (thetas[vid-1] + thetas[vid+1]) / 2.0;
		
		thetas[1] -= 0.5 * (thetas[2] - thetas[1]);  // fudge to nudge vertex 1 into place
		
		for (int vid = 1; vid < numSites; vid++)
		{
			final double theta = thetas[vid];
			final double r = a + b * theta;
			final double x = clockwise ? (x0 - r * Math.cos(theta)) : (x0 + r * Math.cos(theta));
			final double y = y0 + r * Math.sin(theta);

			graph.addVertex(x, y);
		}		

		// Create edges
		for (int vid = 0; vid < graph.vertices().size() - 1; vid++)
		{
			final Vertex vertexN = graph.vertices().get(vid);
			final Vertex vertexO = graph.vertices().get(vid + 1);
			
			final Vertex vertexM = graph.vertices().get(Math.max(0, vid - 1));
			final Vertex vertexP = graph.vertices().get(Math.min(graph.vertices().size() - 1, vid + 1));
						 
			final Vector tangentN = new Vector
										(
											vertexO.pt2D().getX() - vertexM.pt2D().getX(),
											vertexO.pt2D().getY() - vertexM.pt2D().getY()
										);
			final Vector tangentO = new Vector
										(
											vertexP.pt2D().getX() - vertexN.pt2D().getX(),
											vertexP.pt2D().getY() - vertexN.pt2D().getY()
										);
			tangentN.normalise();
			tangentO.normalise();
			
			graph.addEdge(vertexN, vertexO, tangentN, tangentO);  // set as curved
		}

		// Set pivot
		for (final Vertex vertex : graph.vertices())
			vertex.setPivot(pivot);

		graph.setBasisAndShape(basis, shape);

		// Don't reorder Spiral graphs?

		//System.out.println(graph);
		
		return graph;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Number of cells in the inner ring, doubling with each layer.
	 */
	private static int baseNumber(final int numTurns, final int numSites)
	{
		for (int base = 1; base < numSites; base++)
		{
			// Try this base
			int steps = base;
			int total = 1;
			
//			System.out.println("~~~~~~~~~~~~~\nbase: " + base);
			
			for (int ring = 1; ring < numTurns; ring++)
			{
				total += steps;
				
//				System.out.println("-- ring " + ring + " total: " + total);
				
				if (total > numSites)
				{
					if (ring <= numTurns)
						return base - 1;
					break;
				}
				if (ring <= 2)
					steps *= 2;
			}
		}
		
		System.out.println("** Spiral.baseNumber(): Couldn't find base number for spiral.");
		return 0;
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
		concepts.set(Concept.SpiralTiling.id(), true);
		concepts.set(Concept.SpiralShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
