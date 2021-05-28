package game.util.moves;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import other.BaseLudeme;

/**
 * Specifies operations based on the ``from'' location.
 * 
 * @author cambolbro & Eric.Piette
 */
public class From extends BaseLudeme
{
	/** The from location */
	private final IntFunction loc;
	
	/** The from region */
	private final RegionFunction region;

	/** The level of the from location */
	private final IntFunction level;
	
	/** The condition on the from location */
	private final BooleanFunction cond;

	/** The graph element type of the location. */
	private final SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param type   The graph element type.
	 * @param region The region of the ``from'' location.
	 * @param loc    The ``from'' location.
	 * @param level  The level of the ``from'' location.
	 * @param If     The condition on the ``from'' location.
	 * 
	 * @example (from (last To) level:(level))
	 */
	public From
	(
		@Opt           final SiteType        type,
		@Opt @Or       final RegionFunction  region,
		@Opt @Or       final IntFunction     loc,
		@Opt     @Name final IntFunction     level,
		@Opt     @Name final BooleanFunction If
	)
	{
		this.loc = (region != null) ? null : (loc == null) ? new game.functions.ints.iterator.From(null) : loc;
		this.region = region;
		this.level = level;
		this.cond = If;
		this.type = type;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The from location.
	 */
	public IntFunction loc()
	{
		return loc;
	}
	
	/**
	 * @return The region of the from location.
	 */
	public RegionFunction region()
	{
		return region;
	}

	/**
	 * @return The level.
	 */
	public IntFunction level()
	{
		return level;
	}
	
	/**
	 * @return The condition on the from.
	 */
	public BooleanFunction cond()
	{
		return cond;
	}

	/**
	 * @return The graph element type of the location.
	 */
	public SiteType type()
	{
		return type;
	}
}
