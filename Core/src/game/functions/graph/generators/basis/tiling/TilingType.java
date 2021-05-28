package game.functions.graph.generators.basis.tiling;

/**
 * Defines known tiling types for boards (apart from regular tilings).
 * 
 * @author cambolbro
 */
public enum TilingType
{
	// Semi-regular tilings
	
	/** Semi-regular tiling made up of triangles and dodecagons. */
	T31212,

	/** Rhombitrihexahedral tiling (e.g. Kensington). */
	T3464,
	
	/** Semi-regular tiling made up of octagons with squares in the interstitial gaps. */
	T488,
	
	/** Semi-regular tiling made up of squares and pairs of triangles. */
	T33434,

	/** Semi-regular tiling made up of triangles around hexagons. */
	T33336,
	
	/** Semi-regular tiling made up of alternating rows of squares and triangles. */
	T33344,
	
	/** Semi-regular tiling made up of triangles and hexagons. */
	T3636,	

	/** Semi-regular tiling made up of squares, hexagons and dodecagons. */
	T4612,
	
	// Other tilings
	
//	/** Pentagonal tiling p4 (442). */
//	TilingP4_442,
//	
	/** Tiling 3.3.3.3.3.3,3.3.4.3.4. */
	T333333_33434,
	;

}
