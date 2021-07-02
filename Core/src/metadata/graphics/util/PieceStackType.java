package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines different ways of visualising stacks of pieces.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum PieceStackType implements GraphicsItem 
{
	/** Stacked one above the other (with offset). */
	Default,
	
	/** Spread on the ground, e.g. Snakes and Ladders or Pachisi. */
	Ground,
	
	/** Spread on the ground, but position based on size of stack. */
	GroundDynamic,
	
	/** Reverse stacking downwards. */
	Reverse,
	
	/** Spread to show each component like a hand of cards. */
	Fan,
	
	/** Spread to show each component like a hand of cards, alternating left and right side of centre. */
	FanAlternating,
	
	/** No visible stacking. */
	None,
	
	/** Stacked Backgammon-style in lines of five. */
	Backgammon,
	
	/** Show just top piece, with the stack value as number. */
	Count,
	
	/** Show just top piece, with the stack value as number(s), coloured by who. */
	CountColoured,
	
	/** Stacked Ring-style around cell perimeter. */
	Ring,
	
	/** Stacked towards the center of the board. */
	TowardsCenter,
	;
	
	//-------------------------------------------------------------------------

	/**
	 * @param value The value.
	 * @return The PieceStackType.
	 */
	public static PieceStackType getTypeFromValue(final int value)
	{
	    for (final PieceStackType type : values())
	        if (type.ordinal() == value)
	            return type;
	    return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the stack is horizontal
	 */
	public boolean horizontalStack()
	{
		if (equals(PieceStackType.Fan))
			return true;
		if (equals(PieceStackType.FanAlternating))
			return true;
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the stack is vertical
	 */
	public boolean verticalStack()
	{
		if (equals(PieceStackType.Default))
			return true;
		if (equals(PieceStackType.Reverse))
			return true;
		if (equals(PieceStackType.Backgammon))
			return true;
		return false;
	}

	//-------------------------------------------------------------------------

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
