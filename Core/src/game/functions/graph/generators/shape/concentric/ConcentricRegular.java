package game.functions.graph.generators.shape.concentric;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a regular polygonal concentric board.
 * 
 * @author cambolbro
 * 
 * @remarks Morris or Merels boards examples of concentric square boards 
 *          and typically played on the vertices rather than the cells.
 */
@Hide
public class ConcentricRegular extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final int numSides;
	private final int numRings;
	private final BooleanFunction midpointsFn;
	private final BooleanFunction joinMidpointsFn;
	private final BooleanFunction joinCornersFn;
	
	//-------------------------------------------------------------------------

	/**
	 * @param sides			Number of sides per ring.
	 * @param rings			Number of rings [3].
	 * @param midpoints		Whether to generate a midpoint for each side [True].
	 * @param joinMidpoints Whether to join midpoints in adjacent rings [True].
	 * @param joinCorners	Whether to join corners [False].
	 */
	public ConcentricRegular
	(
		final int sides,
	 	final int rings, 
		final BooleanFunction midpoints,
		final BooleanFunction joinMidpoints,
		final BooleanFunction joinCorners
	)
	{
		this.basis = BasisType.Concentric;
		
		// Check number of sides, in case users uses sides:4 instead of Square
		switch (sides)
		{
		case 3:  this.shape = ShapeType.Triangle; break;
		case 4:  this.shape = ShapeType.Square; break;
		case 6:  this.shape = ShapeType.Hexagon; break;
		default: this.shape = ShapeType.Regular; break;
		}
		
		this.numSides = sides;
		this.numRings = rings;
		this.midpointsFn   = midpoints;
		this.joinMidpointsFn   = joinMidpoints;
		this.joinCornersFn = joinCorners;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (numSides < 3)
			throw new IllegalArgumentException("Concentric board shape must have at least three sides.");
		
		final Graph graph = new Graph();
		
		if (numRings < 1)
			return graph;
		
		final boolean midpoints     =     (midpointsFn != null) ? midpointsFn.eval(context) : true;
		final boolean joinMidpoints = (joinMidpointsFn != null) ? joinMidpointsFn.eval(context) : true;
		final boolean joinCorners   =   (joinCornersFn != null) ? joinCornersFn.eval(context) : false;

		final double arc = 2 * Math.PI / numSides;
		final double ref = Math.PI / 2 + (numSides % 2 == 0 ? arc / 2 : 0);  // starting theta
		
		// Create vertices and edges along each rings
		int baseV = 0;
		for (int ring = 0; ring < numRings; ring++)
		{
			final double radius = 1 + ring * (numSides == 3 ? 2 : 1);
			
			// Create vertices along this ring
			for (int side = 0; side < numSides; side++)
			{
				final double xa = radius * Math.cos(ref + arc * side);
				final double ya = radius * Math.sin(ref + arc * side);
				final double xc = radius * Math.cos(ref + arc * (side + 1));
				final double yc = radius * Math.sin(ref + arc * (side + 1));
				final double xb = (xa + xc) / 2.0;
				final double yb = (ya + yc) / 2.0;
			
				graph.findOrAddVertex(xa, ya);
							
				if (midpoints)
					graph.findOrAddVertex(xb, yb);
			}
			
			// Create edges along this ring
			for (int v = baseV; v < graph.vertices().size(); v++)
			{
				final int vv = (v < graph.vertices().size() - 1) ? v + 1 : baseV;	
				graph.addEdge(v, vv);
			}
			
			baseV = graph.vertices().size();
		}
			
		// Create edges between rings
		if (midpoints && joinMidpoints || joinCorners)
		{
			final int edgesPerSide = midpoints ? 2 : 1;
			final int edgesPerRing = numSides * edgesPerSide;
			
			for (int ring = 0; ring < numRings - 1; ring++)
			{
				for (int side = 0; side < numSides; side++)
				{
					if (joinCorners)
					{
						// Corner vertex
						final int v  = ring * edgesPerRing + side * edgesPerSide;
						final int vv = v + edgesPerRing;
					
						graph.addEdge(v, vv);
					}
					
					if (joinMidpoints)
					{
						// Midpoint vertex
						final int v  = ring * edgesPerRing + side * edgesPerSide + 1;
						final int vv = v + edgesPerRing;
					
						graph.addEdge(v, vv);
					}
				}
			}
		}
		
		graph.setBasisAndShape(basis, shape);
		graph.reorder();
		
		if (siteType == SiteType.Cell)
			graph.makeFaces(true);
		
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
		concepts.set(Concept.MorrisTiling.id(), true);
		concepts.set(Concept.RegularShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		
		switch (numSides)
		{
		case 3:  concepts.set(Concept.TriangleShape.id(), true); break;
		case 4:  concepts.set(Concept.SquareShape.id(), true); break;
		case 6:  concepts.set(Concept.HexShape.id(), true); break;
		default: concepts.set(Concept.RegularShape.id(), true); break;
		}
		
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
