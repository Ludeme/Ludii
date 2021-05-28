package game.util.directions;

import java.util.BitSet;

import game.Game;

/**
 * Describes intercardinal directions extended to 3D.
 * 
 * @author Eric Piette and cambolbro
 * 
 * @remarks Spatial directions are used for 3D tilings such as the Shibumi board.
 */
public enum SpatialDirection implements DirectionFacing
{
	/** Down direction. */
	D(DirectionUniqueName.D){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.D; }},
	/** Down-North direction. */
	DN(DirectionUniqueName.DN){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DN; }},
	/** Down-North-East direction. */
	DNE(DirectionUniqueName.DNE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DNE; }},
	/** Down-East direction. */
	DE(DirectionUniqueName.DE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DE; }},
	/** Down-South-East direction. */
	DSE(DirectionUniqueName.DSE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DSE; }},
	/** Down-South direction. */
	DS(DirectionUniqueName.DS){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DS; }},
	/** Down-South-West direction. */
	DSW(DirectionUniqueName.DSW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DSW; }}, 
	/** Down-West direction. */
	DW(DirectionUniqueName.DW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DW; }},
	/** Down-South-West direction. */
	DNW(DirectionUniqueName.DSW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.DSW; }},

	/** Upwards direction. */
	U(DirectionUniqueName.U){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.U; }},
	/** Upwards-North direction. */
	UN(DirectionUniqueName.UN){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.UN; }},
	/** Upwards-North-East direction. */
	UNE(DirectionUniqueName.UNE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.UNE; }},
	/** Upwards-East direction. */
	UE(DirectionUniqueName.UE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.UE; }},
	/** Upwards-South-East direction. */
	USE(DirectionUniqueName.USE){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.USE; }},
	/** Upwards-South direction. */
	US(DirectionUniqueName.US){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.US; }},
	/** Upwards-South-West direction. */
	USW(DirectionUniqueName.USW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.USW; }},
	/** Upwards-West direction. */
	UW(DirectionUniqueName.UW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.UW; }},
	/** Upwards-North-West direction. */
	UNW(DirectionUniqueName.UNW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.UNW; }}, 
	;

	/** Left direction of each direction. */
	private static final SpatialDirection[] LEFT;

	/** Right direction of each direction. */
	private static final SpatialDirection[] RIGHT;

	/** Opposite direction of each direction. */
	private static final SpatialDirection[] OPPOSITE;

//	/** Upwards directions of each direction. */
//	private static final SpatialDirection[] UP;
//
//	/** Downwards directions of each direction. */
//	private static final SpatialDirection[] DOWN;

	static
	{
		LEFT = new SpatialDirection[]
		{ D, DNW, DN, DNE, DE, DSE, DS, DSW, DW, U, UNW, UN, UNE, UE, USE, US, USW, UW };

		RIGHT = new SpatialDirection[]
		{ D, DNE, DE, DSE, DS, DSW, DW, DNW, DN, U, UNE, UE, USE, US, USW, UW, UNW, UN, };

		OPPOSITE = new SpatialDirection[]
		{ U, US, USW, UW, UNW, UN, UNE, UE, USE, D, DS, DSW, DW, DNW, DN, DNE, DE, DSE };

//		UP = new SpatialDirection[]
//		{ UNW, UNE, USE, USW };
//		DOWN = new SpatialDirection[]
//		{ DNW, DNE, DSE, DSW };
	}

	/** The unique name of each direction. */
	final DirectionUniqueName uniqueName;

	/**
	 * @param origDirnType
	 */
	private SpatialDirection(final DirectionUniqueName uniqueName)
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
		return LEFT[ordinal()];
	}

	@Override
	public DirectionFacing right()
	{
		return RIGHT[ordinal()];
	}

	@Override
	public DirectionFacing rightward()
	{
		return RIGHT[ordinal()];
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
		return SpatialDirection.values().length;
	}

	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<SpatialDirection>";
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
