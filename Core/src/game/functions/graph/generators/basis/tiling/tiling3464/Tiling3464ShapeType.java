package game.functions.graph.generators.basis.tiling.tiling3464;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines known shapes for the rhombitrihexahedral (semi-regular 3.4.6.4) tiling.
 * 
 * @author cambolbro
 */
public enum Tiling3464ShapeType implements GraphicsItem
{
//	/** No defined shape. */
//	NoShape,

	/** Custom board shape. */
	Custom,
	
	/** Square board shape. */
	Square,
	
	/** Rectangular board shape. */
	Rectangle,
	
	/** Diamond board shape. */
	Diamond,

	/** Diamond board shape extended vertically. */
	Prism,

	/** Triangular board shape. */
	Triangle,
	
	/** Hexagonal board shape. */
	Hexagon,
	
//	/** Cross board shape. */
//	Cross,
//		
//	/** Rhombus board shape. */
//	Rhombus,
//	
//	/** Wheel board shape. */
//	Wheel,
//	
//	/** Spiral board shape. */
//	Spiral,
//			
//	/** Shape is derived from another graph. */
//	Dual,
//			
//	/** Wedge shape of height N with 1 vertex at the top and 3 vertices on the bottom, for Alquerque boards. */
//	Wedge,
					
	/** Multi-pointed star shape. */
	Star,	
		
	/** Alternating sides are staggered. */
	Limping,
	;

	//-------------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}
}
