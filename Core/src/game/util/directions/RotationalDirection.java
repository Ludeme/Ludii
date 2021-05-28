package game.util.directions;

import java.util.BitSet;

import game.Game;

/**
 * Rotational directions.
 * 
 * @author Eric.Piette
 */
public enum RotationalDirection implements DirectionFacing
{
	/** Outwards direction. */
	Out(DirectionUniqueName.Out){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.Out; }}, 
	
	/** Clockwise direction. */
	CW(DirectionUniqueName.CW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.CW; }},
	
	/** Inwards direction. */
	In(DirectionUniqueName.In){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.In; }},
	
	/** Counter-Clockwise direction. */
	CCW(DirectionUniqueName.CCW){ @Override public AbsoluteDirection toAbsolute(){ return AbsoluteDirection.CCW; }},
	;

	/** Left direction of each direction. */
	private static final RotationalDirection[] LEFT;

	/** Right direction of each direction. */
	private static final RotationalDirection[] RIGHT;

	/** Opposite direction of each direction. */
	private static final RotationalDirection[] OPPOSITE;

	static
	{
		LEFT = new RotationalDirection[]
		{ CCW, In, CW, Out };
		RIGHT = new RotationalDirection[]
		{ CW,  In, CCW, Out};
		OPPOSITE = new RotationalDirection[]
		{ In, CCW, Out, CW };
	}

	/** The unique name of each direction. */
	final DirectionUniqueName uniqueName;

	/**
	 * @param origDirnType
	 */
	private RotationalDirection(final DirectionUniqueName uniqueName)
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
		return RotationalDirection.values().length;
	}

	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<WheelDirection>";
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
