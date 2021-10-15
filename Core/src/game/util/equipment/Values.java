package game.util.equipment;

import game.functions.range.Range;
import game.types.board.SiteType;
import other.BaseLudeme;

/**
 * Defines the set of values of a graph variable in a deduction puzzle.
 * 
 * @author Eric.Piette
 */
public class Values extends BaseLudeme
{
	/** The graph element type. */
	final private SiteType type;

	/** The range of the values. */
	final private Range range;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type.
	 * @param range The range of the values.
	 * @example (values Cell (range 1 9))
	 */
	public Values
	(
		final SiteType type, 
		final Range range
	) 
	{
		this.range = range;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The graph element type.
	 */
	public SiteType type()
	{
		return type;
	}

	/**
	 * @return The range.
	 */
	public Range range()
	{
		return range;
	}
}
