package game.util.directions;

import annotations.Hide;

/**
 * provides that for efficiency rather than necessity, each direction has a unique name
 * @remarks  this is the canonical list,
 * 	     Feel free to ad extra names as required, but these should be actual directions not filters.
 * 
 * @author mrraow
 */
@Hide
public enum DirectionUniqueName
{
	/** North direction. */
	N,

	/** North-North-East direction. */
	NNE,
	
	/** North-East direction. */
	NE,
	
	/** East direction. */
	E,
	
	/** South-South-East direction. */
	SSE,
	
	/** South-East direction. */
	SE,
	
	/** South direction. */
	S,
	
	/** South-South-West direction. */
	SSW,
	
	/** South-West direction. */
	SW,
	
	/** West direction. */
	W,
	
	/** North-West direction. */
	NW,
	
	/** North-North-West direction. */
	NNW,
	
	/** West-North-West direction. */
	WNW,
	
	/** East-North-East direction. */
	ENE,
	
	/** East-South-East direction. */
	ESE,
	
	/** West-South-West direction. */
	WSW,
	
	/** Clockwise direction. */
	CW,
	
	/** Outwards direction. */
	Out,
	
	/** Counter-Clockwise direction. */
	CCW,
	
	/** Inwards direction. */
	In,
	
	/** Upper-North-West direction. */
	UNW,
	
	/** Upper-North-East direction. */
	UNE,
	
	/** Upper-South-East direction. */
	USE,
	
	/** Upper-South-West direction. */
	USW,
	
	/** Down-North-west direction. */
	DNW,
	
	/** Down-North-East direction. */
	DNE,
	
	/** Down-South-East direction. */
	DSE,
	
	/** Down-South-West direction. */
	DSW,

	/** Upper direction. */
	U,

	/** Upper-North direction. */
	UN,

	/** Upper-West direction. */
	UW,

	/** Upper-East direction. */
	UE,

	/** Upper-South direction. */
	US,

	/** Down direction. */
	D,

	/** Down-North direction. */
	DN,

	/** Down-West direction. */
	DW,

	/** Down-East direction. */
	DE,

	/** Down-South direction. */
	DS,
}
