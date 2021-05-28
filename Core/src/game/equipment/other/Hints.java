package game.equipment.other;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.equipment.Item;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.equipment.Hint;
import main.Constants;
import other.ItemType;
import other.concept.Concept;

/**
 * Defines the hints of a deduction puzzle.
 * 
 * @author Eric.Piette
 * 
 * @remarks Used for any deduction puzzle with hints.
 */
public class Hints extends Item
{
	/** Which vars. */
	private final Integer[][] where;

	/** Which values. */
	private final Integer[] values;

	/** On what kind of vars have hint. */
	private final SiteType type;

	//-------------------------------------------------------------------------
	
	/**
	 * @param label   The name of these hints.
	 * @param records The different hints.
	 * @param type    The graph element type of the sites.
	 * @example (hints { (hint {0 5 10 15} 3 ) (hint {1 2 3 4} 4 ) (hint {6 11 16} 3
	 *          ) (hint {7 8 9 12 13 14} 4 ) (hint {17 18 19} 3 ) (hint {20 21 22} 3
	 *          ) (hint {23 24} 1 ) })* 
	 */
	public Hints
	(
		@Opt final String   label,
			 final Hint[]   records,
		@Opt final SiteType type
	)
	{
		super(label, Constants.UNDEFINED, RoleType.Neutral);
		
		if (records == null)
		{
			this.values = null;
			this.where = null;
		}
		else
		{
			this.values = new Integer[records.length];
			this.where = new Integer[records.length][];

			for (int n = 0; n < records.length; n++)
			{
				this.where[n] = records[n].region();
				this.values[n] = records[n].hint();
			}
		}
		this.type = (type == null) ? SiteType.Cell : type;
		setType(ItemType.Hints);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Hints.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (hints ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		willCrash |= super.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return where
	 */
	public Integer[][] where()
	{
		return this.where;
	}

	/**
	 * @return values
	 */
	public Integer[] values()
	{
		return this.values;
	}

	/**
	 * @return type
	 */
	public SiteType getType()
	{
		return type;
	}
}
