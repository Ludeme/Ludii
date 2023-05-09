package game.functions.graph.generators.basis.square;

/**
 * Defines how to handle diagonal relations on the Square tiling.
 * 
 * @author cambolbro
 */
public enum DiagonalsType //implements GraphicsItem
{
	/** Diagonal connections (not edges) between opposite corners. */
	Implied,

	/** Solid edges between opposite diagonals, which split the square into four triangles. */
	Solid,
	
	/** Solid edges between opposite diagonals, but do not split the square into four triangles. */
	SolidNoSplit,
	
	/** Every second diagonal is a solid edge, as per Alquerque boards. */
	Alternating,
	
	/** Concentric diagonal rings from the centre. */
	Concentric,
	
	/** Diagonals radiating from the centre. */
	Radiating,
}
