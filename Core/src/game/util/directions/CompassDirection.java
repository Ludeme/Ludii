package game.util.directions;

import java.util.BitSet;

import game.Game;

/**
 * Compass directions.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum CompassDirection implements DirectionFacing
{
	/** North. */
	N(DirectionUniqueName.N){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.N; }}, 
	
	/** North-North-East. */
	NNE(DirectionUniqueName.NNE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.NNE; }}, 
	
	/** North-East. */
	NE(DirectionUniqueName.NE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.NE; }},
	
	/** East-North-East. */
	ENE(DirectionUniqueName.ENE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.ENE; }},
	
	/** East. */
	E(DirectionUniqueName.E){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.E; }}, 
	
	/** East-South-East. */
	ESE(DirectionUniqueName.ESE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.ESE; }},
	
	/** South-East. */
	SE(DirectionUniqueName.SE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.SE; }},
	
	/** South-South-East. */
	SSE(DirectionUniqueName.SSE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.SSE; }},
	
	/** South. */
	S(DirectionUniqueName.S){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.S; }},
	
	/** South-South-West. */
	SSW(DirectionUniqueName.SSW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.SSW; }},
	
	/** South-West. */
	SW(DirectionUniqueName.SW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.SW; }},
	
	/** West-South-West. */
	WSW(DirectionUniqueName.WSW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.WSW; }},
	
	/** West. */
	W(DirectionUniqueName.W){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.W; }},
	
	/** West-North-West. */
	WNW(DirectionUniqueName.WNW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.WNW; }},
	
	/** North-West. */
	NW(DirectionUniqueName.NW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.NW; }},
	
	/** North-North-West. */
	NNW(DirectionUniqueName.NNW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.NNW; }},
	;

	/** Left direction of each direction. */
	private static final CompassDirection[] LEFT;

	/** Leftward direction of each direction. */
	private static final CompassDirection[] LEFTWARD;

	/** Right direction of each direction. */
	private static final CompassDirection[] RIGHT;

	/** Rightward direction of each direction. */
	private static final CompassDirection[] RIGHTWARD;

	/** Opposite direction of each direction. */
	private static final CompassDirection[] OPPOSITE;

	static
	{
		LEFT = new CompassDirection[]
		{ NNW, N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW };
		LEFTWARD = new CompassDirection[]
		{ W, WNW, NW, NNW, N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW };
		RIGHT = new CompassDirection[]
		{ NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW, N };
		RIGHTWARD = new CompassDirection[]
		{ E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW, N, NNE, NE, ENE };
		OPPOSITE = new CompassDirection[]
		{ S, SSW, SW, WSW, W, WNW, NW, NNW, N, NNE, NE, ENE, E, ESE, SE, SSE };
	}

	/** The unique name of each direction. */
	final DirectionUniqueName uniqueName;

	/**
	 * @param origDirnType
	 */
	private CompassDirection(final DirectionUniqueName uniqueName)
	{
		this.uniqueName = uniqueName;
	}

	@Override
	public DirectionFacing left()
	{
		return LEFT[ordinal()];
	}

	@Override
	public DirectionFacing leftward()
	{
		return LEFTWARD[ordinal()];
	}

	@Override
	public DirectionFacing right()
	{
		return RIGHT[ordinal()];
	}

	@Override
	public DirectionFacing rightward()
	{
		return RIGHTWARD[ordinal()];
	}

	@Override
	public DirectionFacing opposite()
	{
		return OPPOSITE[ordinal()];
	}

	@Override
	public int index()
	{
		return ordinal();
	}

	@Override
	public DirectionUniqueName uniqueName()
	{
		return uniqueName;
	}

	@Override
	public int numDirectionValues()
	{
		return CompassDirection.values().length;
	}

	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<CompassDirection>";
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		return new BitSet();
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		return new BitSet();
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		return new BitSet();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		return new BitSet();
	}

	@Override
	public BitSet writesEvalContextFlat()
	{
		return new BitSet();
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		return false;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		return false;
	}
}
