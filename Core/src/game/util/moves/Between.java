package game.util.moves;

import annotations.Name;
import annotations.Opt;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.range.RangeFunction;
import game.rules.play.moves.nonDecision.effect.Apply;
import other.BaseLudeme;

/**
 * Gets all the conditions or effects related to the location between ``from'' and ``to''.
 * 
 * @author Eric.Piette
 */
public class Between extends BaseLudeme
{
	/** The piece to let between the from and to. */
	private final IntFunction trail;
	
	/** The condition applied between the from and to. */
	private final BooleanFunction cond;

	/** The distance before the range locations. */
	private final IntFunction before;

	/** The range of the middle locations. */
	private final RangeFunction range;

	/** The distance after the range locations. */
	private final IntFunction after;

	/** The effect to apply on the locations. */
	private final Apply effect;

	//-------------------------------------------------------------------------

	/**
	 * @param before Lead distance up to ``between'' section.
	 * @param range  Range of the ``between'' section.
	 * @param after  Trailing distance after ``between'' section.
	 * @param If     The condition on the location.
	 * @param trail  The piece to let on the location.
	 * @param effect Actions to apply.
	 * 
	 * @example (between if:(is Enemy (who at:(between))) (apply (remove
	 *          (between))) )
	 */
	public Between
	(
		@Opt @Name final IntFunction     before,
		@Opt       final RangeFunction   range,
		@Opt @Name final IntFunction     after,
		@Opt @Name final BooleanFunction If,
		@Opt @Name final IntFunction     trail,
		@Opt       final Apply           effect
	)
	{
		this.trail = trail;
		this.cond = If;
		this.before = before;
		this.range = range;
		this.after = after;
		this.effect = effect;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The piece to trail
	 */
	public IntFunction trail()
	{
		return trail;
	}
	
	/**
	 * @return The effect to apply on the piece in between
	 */
	public Apply effect()
	{
		return effect;
	}

	/**
	 * @return The distance before the range locations.
	 */
	public IntFunction before()
	{
		return before;
	}

	/**
	 * @return The range of the middle locations.
	 */
	public RangeFunction range()
	{
		return range;
	}

	/**
	 * @return The distance after the range locations.
	 */
	public IntFunction after()
	{
		return after;
	}

	/**
	 * @return The region of the from location.
	 */
	public BooleanFunction condition()
	{
		return cond;
	}
}
