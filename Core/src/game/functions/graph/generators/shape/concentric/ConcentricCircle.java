package game.functions.graph.generators.shape.concentric;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import main.math.Vector;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board based on a circular tiling.
 * 
 * @author cambolbro
 * 
 * @remarks Circular tilings are centred around a ``pivot'' point. 
 *          The first ring value should be 0 (centre point) or 1 (centre cell).
 */
@Hide
public class ConcentricCircle extends Basis
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * Sample position for circular graph vertex.
	 */
	public class Sample
	{
		/**
		 * The x coordinate.
		 */
		public double x;

		/**
		 * The y coordinate.
		 */
		public double y;

		/**
		 * The theta value.
		 */
		public double theta;
		
		/**
		 * @param x     The x coordinate.
		 * @param y     The y coordinate.
		 * @param theta The theta value.
		 */
		public Sample(final double x, final double y, final double theta)
		{
			this.x = x;
			this.y = y;
			this.theta = theta;
		}
	}
	
	//-------------------------------------------------------------------------

	/** Number of cells per concentric ring. */
	private final int[] cellsPerRing;
	
	/** Whether to offset cells in adjacent rings relative to each other. */
	private final BooleanFunction staggerFn;
	private boolean stagger;
	
	//-------------------------------------------------------------------------

	/**
	 * @param cellsPerRing  Number of cells per concentric ring.
	 * @param stagger 		Whether to stagger cells in adjacent rings [False].
	 */
	public ConcentricCircle
	(
				   final int[] cellsPerRing,
		@Opt @Name final BooleanFunction stagger
	)
	{
		this.basis = BasisType.Concentric;
		this.shape = ShapeType.Circle;
		
		this.cellsPerRing = cellsPerRing;
		
		//this.stagger = (stagger == null) ? false : stagger.eval();
		this.staggerFn = stagger;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		this.stagger = (staggerFn == null) ? false : staggerFn.eval(context);
		
		final Graph graph = new Graph();
				
		if (siteType == SiteType.Cell) 
			generateForCells(graph);
		else
			generateForVertices(graph);
				
		//graph.reorder();
		//System.out.println(graph);
		
		return graph;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param graph Graph to be generated for playing on the cells.
	 */
	public void generateForCells(final Graph graph)
	{
		final int numRings = cellsPerRing.length;	
		if (numRings < 1)
			return;
		
		final Vertex pivot = graph.addVertex(0, 0);
		
		@SuppressWarnings("unchecked")
		final List<Sample>[] samples = new ArrayList[numRings + 1];			
		for (int ring = 0; ring < numRings + 1; ring++)
			samples[ring] = new ArrayList<Sample>();

		final double ref = Math.PI / 2;  // orient to start at top
		
//		System.out.println("numRings=" + numRings);
		
		// Create samples
		for (int ring = 0; ring < numRings; ring++)
		{
			final int cellsThisRing = Math.abs(cellsPerRing[ring]);
			
			final double rI = Math.max(0, ring - 0.5);
			final double rO = ring + 0.5;
			
			final double ringOffset = (stagger && ring % 2 == 1) 
										? 2 * Math.PI / cellsThisRing / 2   // half a cell width
										: 0;
			if (cellsThisRing < 2)
				continue;
										
			for (int step = 0; step < cellsThisRing; step++)
			{
				double theta = ref + ringOffset - 2 * Math.PI * step / cellsThisRing; 
				
				while (theta < -Math.PI)
					theta += 2 * Math.PI;
				
				while (theta > Math.PI)
					theta -= 2 * Math.PI;
				
				//System.out.println("theta=" + theta);
				
				final double ix = rI * Math.cos(theta);
				final double iy = rI * Math.sin(theta);
				final double ox = rO * Math.cos(theta);
				final double oy = rO * Math.sin(theta);
							
				samples[ring].add(new Sample(ix, iy, theta));			
				samples[ring + 1].add(new Sample(ox, oy, theta));
			}
		}
					
		// Order samples by angle within each ring
		for (int ring = 0; ring < numRings + 1; ring++)
			Collections.sort(samples[ring], new Comparator<Sample>() 
			{
				@Override
				public int compare(final Sample a, final Sample b)
				{
					return (a.theta == b.theta) 
							? 0
							: (a.theta < b.theta) ? -1 : 1;
				}
			});
		
		// Remove duplicate samples
		for (int ring = 0; ring < numRings + 1; ring++)
			for (int n = samples[ring].size() - 1; n > 0; n--)
			{
				final Sample sampleA = samples[ring].get(n);
				final Sample sampleB = samples[ring].get((n + samples[ring].size() - 1) % samples[ring].size());
				
				if (MathRoutines.distance(sampleA.x, sampleA.y, sampleB.x, sampleB.y) < tolerance)
					samples[ring].remove(n);
			}

		// Now create vertices
		for (int ring = 0; ring < numRings + 1; ring++)
			for (final Sample sample : samples[ring])
				graph.findOrAddVertex(sample.x, sample.y);
		
		// Create concentric edges around rings (curved)
		for (int ring = 0; ring < numRings + 1; ring++)
		{
			final int ringSize = samples[ring].size();
			if (ringSize < 2)
				continue;  // not enough samples
			
			for (int n = 0; n < ringSize; n++)
			{
				final Sample sampleA = samples[ring].get(n);
				final Sample sampleB = samples[ring].get((n + 1) % ringSize);
				
				final Vertex vertexA = graph.findVertex(sampleA.x, sampleA.y);
				final Vertex vertexB = graph.findVertex(sampleB.x, sampleB.y);
			
				if (vertexA.id() == vertexB.id())
					continue;
				
				final Sample sampleAA = samples[ring].get((n - 1 + ringSize) % ringSize);
				final Sample sampleBB = samples[ring].get((n + 2) % ringSize);
				 
				final Vector tangentA = new Vector(sampleB.x - sampleAA.x, sampleB.y - sampleAA.y);
				final Vector tangentB = new Vector(sampleA.x - sampleBB.x, sampleA.y - sampleBB.y);
				
				tangentA.normalise();
				tangentB.normalise();
				
				graph.findOrAddEdge(vertexA, vertexB, tangentA, tangentB);  // set as curved
			}
		}
	
		// Create perpendicular edges between rings (not curved)
		for (int ring = 0; ring < numRings; ring++)
		{
			final int cellsThisRing = Math.abs(cellsPerRing[ring]);
			
			final double rI = Math.max(0, ring - 0.5);
			final double rO = ring + 0.5;
			
			final double ringOffset = (stagger && ring % 2 == 1) 
										? 2 * Math.PI / cellsThisRing / 2   // half a cell width
										: 0;
			if (cellsThisRing < 2)
				continue;
							
			for (int step = 0; step < cellsThisRing; step++)
			{
				final double theta = ref + ringOffset - 2 * Math.PI * step / cellsThisRing; 
				
				final double ix = rI * Math.cos(theta);
				final double iy = rI * Math.sin(theta);
				final double ox = rO * Math.cos(theta);
				final double oy = rO * Math.sin(theta);
			
				final Vertex vertexA = graph.findOrAddVertex(ix, iy);
				final Vertex vertexB = graph.findOrAddVertex(ox, oy);
				
				if (vertexA.id() == vertexB.id())
						continue;

				graph.findOrAddEdge(vertexA, vertexB);
			}
		}
		
		// Set pivot
		for (final Vertex vertex : graph.vertices())
			vertex.setPivot(pivot);
		
		graph.makeFaces(true);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param graph Graph to be generated for playing on the vertices.
	 */
	public void generateForVertices(final Graph graph)
	{
		if (cellsPerRing.length == 0)
			return;  // nothing to do
	
		if (cellsPerRing.length == 1)
		{
			// Shape is a simple polygon
			simplePolygon(graph);
			return;
		}
		
		int[] vertsPerRing;
		if (Math.abs(cellsPerRing[0]) > 1)
		{
			// No centre: add a default ring of 0
			vertsPerRing = new int[cellsPerRing.length + 1];
			vertsPerRing[0] = 0;
			for (int r = 0; r < cellsPerRing.length; r++)
				vertsPerRing[r + 1] = cellsPerRing[r]; 
		}
		else
		{
			vertsPerRing = new int[cellsPerRing.length];
			for (int r = 0; r < cellsPerRing.length; r++)
				vertsPerRing[r] = cellsPerRing[r]; 
		}
		
		final boolean noPivot = (vertsPerRing[0] == 0);
		
		final int numRings = vertsPerRing.length;
		if (numRings < 1)
			return;
			
		//final Vertex pivot = graph.addVertex(0, 0);

		@SuppressWarnings("unchecked")
		final List<Sample>[] samples = new ArrayList[numRings];			
		for (int ring = 0; ring < numRings; ring++)
			samples[ring] = new ArrayList<Sample>();

		// Always have centre vertex
		//samples[0].add(new Sample(0, 0, 0));
	
		final double ref = Math.PI / 2;  // orient to start at top
		
		// Create samples
		for (int ring = 0; ring < numRings; ring++)
		{
			final int vertsThisRing = Math.abs(vertsPerRing[ring]);
			
//			System.out.println("Ring " + ring + " has " + vertsThisRing + " cells.");
			
			final double r = ring;
			
			final double ringOffset = (stagger && ring % 2 == 1) 
										? 2 * Math.PI / vertsThisRing / 2  // half a cell width
										: 0;
			//if (vertsThisRing < 2)
			//	continue;
										
			for (int step = 0; step < vertsThisRing; step++)
			{
				final double theta = ref + ringOffset - 2 * Math.PI * step / vertsThisRing; 
				
				final double x = r * Math.cos(theta);
				final double y = r * Math.sin(theta);
			
				samples[ring].add(new Sample(x, y, theta));
			
				//graph.findOrAddVertex(x, y);
			}
		}
					
		// Order samples by angle
		for (int ring = 1; ring < numRings; ring++)
			Collections.sort(samples[ring], new Comparator<Sample>() 
			{
				@Override
				public int compare(final Sample a, final Sample b)
				{
					return (a.theta == b.theta) 
							? 0
							: (a.theta < b.theta) ? -1 : 1;
				}
			});
		
		// Remove duplicate samples
		for (int ring = 1; ring < numRings; ring++)
			for (int n = samples[ring].size() - 1; n > 0; n--)
			{
				final Sample sampleA = samples[ring].get(n);
				final Sample sampleB = samples[ring].get((n + samples[ring].size() - 1) % samples[ring].size());
				
				if (MathRoutines.distance(sampleA.x, sampleA.y, sampleB.x, sampleB.y) < 0.1)
					samples[ring].remove(n);
			}

		// Now create vertices
		for (int ring = 0; ring < numRings; ring++)
			for (final Sample sample : samples[ring])
				graph.findOrAddVertex(sample.x, sample.y);

		// Create concentric edges around rings (curved)
		for (int ring = 1; ring < numRings; ring++)
		{
//			System.out.println("Ring " + ring + " (" + samples[ring].size() + "):");
			
			if (vertsPerRing[ring] < 2)
				continue;  // no edges around this ring
			
			final int ringSize = samples[ring].size();
			if (ringSize < 2)
				continue;  // not enough samples anyway
			
			for (int n = 0; n < ringSize; n++)
			{
				final Sample sampleA = samples[ring].get(n);
				final Sample sampleB = samples[ring].get((n + 1) % ringSize);
				
				final Vertex vertexA = graph.findOrAddVertex(sampleA.x, sampleA.y);
				final Vertex vertexB = graph.findOrAddVertex(sampleB.x, sampleB.y);
			
				if (vertexA.id() == vertexB.id())
					continue;
			
//					System.out.print(" " + vertexA.id() + "-" + vertexB.id());
				
				final Sample sampleAA = samples[ring].get((n - 1 + ringSize) % ringSize);
				final Sample sampleBB = samples[ring].get((n + 2) % ringSize);
				 
				final Vector tangentA = new Vector(sampleB.x - sampleAA.x, sampleB.y - sampleAA.y);
				final Vector tangentB = new Vector(sampleA.x - sampleBB.x, sampleA.y - sampleBB.y);
				
				tangentA.normalise();
				tangentB.normalise();
				
				graph.findOrAddEdge(vertexA, vertexB, tangentA, tangentB);  // set as curved
			}
			
//			System.out.println();
		}
	
		// Create perpendicular edges between rings (not curved)
		for (int ring = 0; ring < numRings - 1; ring++)
		{
			for (final Sample sample : samples[ring])
			{
				// See if vertex on next layer lines up 
				final Vertex vertexA = graph.findVertex(sample.x, sample.y);
				if (vertexA == null)
				{
					System.out.println("** Couldn't find vertex (" + sample.x + "," + sample.y + ").");
					continue;
				}
				
				if (ring == 0)
				{
					// Join to all vertices on ring 1
					for (final Sample sampleB : samples[1])
					{
						final Vertex vertexB = graph.findVertex(sampleB.x, sampleB.y);
						if (vertexB != null)
							graph.findOrAddEdge(vertexA, vertexB);
					}
				}
				else
				{
					// Only match vertices in this direction
					final double ratio = (ring + 1) / (double)ring;
					
					final double xB = sample.x * ratio;
					final double yB = sample.y * ratio;
					
					final Vertex vertexB = graph.findVertex(xB, yB);
					if (vertexB != null)
					{
						graph.findOrAddEdge(vertexA, vertexB);
						if (noPivot)
						{
							// Set inner vertex to be pivot of outer vertex
							vertexB.setPivot(vertexA);
						}
					}
				}
			}
		}

		if (vertsPerRing[0] != 0)
		{
			// Find and set pivot
			final Vertex pivot = graph.findVertex(0, 0);
			if (pivot == null)
			{
				System.out.println("** Null pivot in Circle graph.");
				return;
			}
			for (final Vertex vertex : graph.vertices())
				vertex.setPivot(pivot);
		}
	}
	
	//-------------------------------------------------------------------------

	private void simplePolygon(final Graph graph)
	{
		final int numSides = cellsPerRing[0];
		if (numSides < 3)
		{
			System.out.println("** Bad number of circle sides: " + numSides);
		}
		
		final double r = numSides / (2 * Math.PI);
		final double offset = (numSides == 4) ? Math.PI / 4 : Math.PI / 2;
			
		for (int n = 0; n < numSides; n++)
		{
			final double theta = offset + (double)n / numSides * 2 * Math.PI;
			
			final double x = r * Math.cos(theta);
			final double y = r * Math.sin(theta);
			
			graph.addVertex(x, y);
		}
		
		// Simple polygon
		for (int n = 0; n < numSides; n++)
			graph.addEdge(n, (n + 1) % numSides);
					
		graph.reorder();
		
//		System.out.println(graph);
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
		concepts.set(Concept.ConcentricTiling.id(), true);
		concepts.set(Concept.CircleTiling.id(), true);
		concepts.set(Concept.CircleShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
