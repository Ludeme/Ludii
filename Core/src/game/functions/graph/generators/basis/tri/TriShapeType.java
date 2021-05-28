package game.functions.graph.generators.basis.tri;

/**
 * Defines known shapes for the triangular tiling.
 * 
 * @author cambolbro
 */
public enum TriShapeType
{
	/** No shape; custom graph. */
	NoShape,

	/** Square board shape. */
	Square,
	
	/** Rectangular board shape. */
	Rectangle,
	
	/** Diamond board shape. */
	Diamond,

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
		
	/** Diamond shape extended vertically. */
	Prism,
	;
}
