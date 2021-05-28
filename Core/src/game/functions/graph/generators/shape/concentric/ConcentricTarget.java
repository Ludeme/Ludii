package game.functions.graph.generators.shape.concentric;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Face;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.Vector;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a board formed by a set of concentric rings.
 * 
 * @author cambolbro
 * 
 * @remarks Target boards are played on the cells only.
 */
@Hide
public class ConcentricTarget extends Basis
{
	private static final long serialVersionUID = 1L;

	/** Number of concentric rings. */
	private final int numRings;
	
	//-------------------------------------------------------------------------

	/**
	 * @param rings Number of concentric rings.
	 */
	public ConcentricTarget
	(
		final int rings
	)
	{
		this.basis = BasisType.Concentric;
		this.shape = ShapeType.Circle;
		
		this.numRings = rings;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final Graph graph = new Graph();
				
		if (numRings < 1)
			return graph;
		
		if (siteType != SiteType.Cell)
		{
			throw new IllegalArgumentException("Concentric target board must use cells only.");
		}
		
//		final Vertex pivot = graph.addVertex(0, 0);
		
//		@SuppressWarnings("unchecked")
//		final List<Sample>[] samples = new ArrayList[numRings + 1];			
//		for (int ring = 0; ring < numRings + 1; ring++)
//			samples[ring] = new ArrayList<Sample>();
//
//		final double ref = Math.PI / 2;  // orient to start at top
//		
////		System.out.println("numRings=" + numRings);
//		
//		// Create samples
//		for (int ring = 0; ring < numRings; ring++)
//		{
//			final int cellsThisRing = Math.abs(cellsPerRing[ring]);
//			
//			final double rI = Math.max(0, ring - 0.5);
//			final double rO = ring + 0.5;
//			
//			final double ringOffset = (stagger && ring % 2 == 1) 
//										? 2 * Math.PI / cellsThisRing / 2   // half a cell width
//										: 0;
//			if (cellsThisRing < 2)
//				continue;
//										
//			for (int step = 0; step < cellsThisRing; step++)
//			{
//				double theta = ref + ringOffset - 2 * Math.PI * step / cellsThisRing; 
//				
//				while (theta < -Math.PI)
//					theta += 2 * Math.PI;
//				
//				while (theta > Math.PI)
//					theta -= 2 * Math.PI;
//				
//				//System.out.println("theta=" + theta);
//				
//				final double ix = rI * Math.cos(theta);
//				final double iy = rI * Math.sin(theta);
//				final double ox = rO * Math.cos(theta);
//				final double oy = rO * Math.sin(theta);
//							
//				samples[ring].add(new Sample(ix, iy, theta));			
//				samples[ring + 1].add(new Sample(ox, oy, theta));
//			}
//		}
//					
//		// Order samples by angle within each ring
//		for (int ring = 0; ring < numRings + 1; ring++)
//			Collections.sort(samples[ring], new Comparator<Sample>() 
//			{
//				@Override
//				public int compare(final Sample a, final Sample b)
//				{
//					return (a.theta == b.theta) 
//							? 0
//							: (a.theta < b.theta) ? -1 : 1;
//				}
//			});
//		
//		// Remove duplicate samples
//		for (int ring = 0; ring < numRings + 1; ring++)
//			for (int n = samples[ring].size() - 1; n > 0; n--)
//			{
//				final Sample sampleA = samples[ring].get(n);
//				final Sample sampleB = samples[ring].get((n + samples[ring].size() - 1) % samples[ring].size());
//				
//				if (MathRoutines.distance(sampleA.x, sampleA.y, sampleB.x, sampleB.y) < tolerance)
//					samples[ring].remove(n);
//			}

		// Each ring
		final int numSteps = 8;
		final double arc = 2 * Math.PI / numSteps;
		final double off = Math.PI / 2;
		
		for (int ring = 0; ring < numRings; ring++)
		{	
			final double radius = ring + 1;
			
			final int baseV = graph.vertices().size();
			
			// Create vertices around this ring
			for (int step = 0; step < numSteps; step++)
			{
				final double x = radius * Math.cos(off + arc * step);
				final double y = radius * Math.sin(off + arc * step);
				
				graph.findOrAddVertex(x, y);
			}
			
			// Create edges around this ring
			for (int va = baseV; va < graph.vertices().size(); va++)
			{
				final int vb = (va < graph.vertices().size() - 1) ? va + 1 : baseV;	
				
				final Vertex vertexA = graph.vertices().get(va);
				final Vertex vertexB = graph.vertices().get(vb);
				
				final Vector tangentA = new Vector(vertexA.pt2D());
				tangentA.normalise();
				tangentA.perpendicular();
				
				
				final Vector tangentB = new Vector(vertexB.pt2D());
				tangentB.normalise();
				tangentB.perpendicular();
				tangentB.reverse();
				
				graph.addEdge(vertexA, vertexB, tangentA, tangentB);
			}
		
			// Manually create face for this ring here?
		}
		
		graph.makeFaces(true);
		
		// Reposition each face's reference point to avoid overlap
		for (int f = 0; f < graph.faces().size(); f++)
		{
			final Face face = graph.faces().get(f);
			final double y = (f == 0) ? 0 : -f - 0.5;			
			face.setPt(0, y, 0);
		}
		
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
		concepts.set(Concept.ConcentricTiling.id(), true);
		concepts.set(Concept.TargetShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
