package game.types.board;

/**
 * Defines shape types for known board shapes.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum ShapeType
{
	/** No defined board shape. */
	NoShape,
	
	/** Custom board shape defined by the user. */
	Custom,

	/** Square board shape. */
	Square,
	
	/** Rectangular board shape. */
	Rectangle,
	
	/** Triangular board shape. */
	Triangle,
	
	/** Hexagonal board shape. */
	Hexagon,
	
	/** Cross board shape. */
	Cross,
	
	/** Diamond board shape. */
	Diamond,
				
	/** Diamond board shape extended vertically. */
	Prism,

	/** General quadrilateral board shape. */
	Quadrilateral,
	
	/** Rhombus board shape. */
	Rhombus,
	
	/** Wheel board shape. */
	Wheel,

	/** Circular board shape. */
	Circle,

	/** Spiral board shape. */
	Spiral,
			
//	/** Shape is derived from another graph. */
//	Dual,
			
	/** Wedge shape of height N with 1 vertex at the top and 3 vertices on the bottom, for Alquerque boards. */
	Wedge,
			
	/** Multi-pointed star shape. */
	Star,
	
	/** Alternating sides are staggered. */
	Limping,
	
	/** Regular polygon with sides of the same length. */
	Regular,
	
	/** General polygon. */
	Polygon,
	;

}
