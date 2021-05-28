package game.util.moves;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.state.Rotations;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.nonDecision.effect.Apply;
import game.types.board.SiteType;
import other.BaseLudeme;

/**
 * Specifies operations based on the ``to'' location.
 * 
 * @author cambolbro & Eric.Piette
 */
public class To extends BaseLudeme
{
	/** The to location */
	private final IntFunction loc;

	/** The to region */
	private final RegionFunction region;

	/** The level of the from location */
	private final IntFunction level;
	
	/** The condition on the from location */
	private final BooleanFunction cond;

	/** The rotations of the to location. */
	private final Rotations rotations;
	
	/** The graph element type of the location. */
	private final SiteType type;

	/** The effect to apply on the locations. */
	private final Apply effect;

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param type      The graph element type.
	 * @param region    The region of ``to'' the location.
	 * @param loc       The ``to'' location.
	 * @param level     The level of the ``to'' location.
	 * @param rotations Rotations of the ``to'' location.
	 * @param If        The condition on the ``to'' location.
	 * @param effect    Effect to apply to the ``to'' location.
	 * 
	 * @example (to (last To) level:(level))
	 */
	public To
	(
		@Opt           final SiteType        type,
		@Opt @Or       final RegionFunction  region,
		@Opt @Or       final IntFunction     loc,
		@Opt     @Name final IntFunction     level,
		@Opt           final Rotations       rotations,
		@Opt     @Name final BooleanFunction If,
		@Opt           final Apply          effect
	)
	{
		this.loc = (region != null) ? null : (loc == null) ? game.functions.ints.iterator.To.construct() : loc;
		this.region = region;
		this.level = level;
		this.cond = If;
		this.rotations = rotations;
		this.type = type;
		this.effect = effect;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The to location.
	 */
	public IntFunction loc()
	{
		return loc;
	}
	
	/**
	 * @return The region of the to location.
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
	 * @return The condition on the to..
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

	/**
	 * @return The rotation of the to element.
	 */
	public Rotations rotations()
	{
		return rotations;
	}

	/**
	 * @return The effect.
	 */
	public Apply effect()
	{
		return effect;
	}
}
