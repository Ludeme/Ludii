package game.functions.ints.board;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;

/**
 * Returns the value corresponding to a specified entry in a map.
 * 
 * @author Eric Piette
 * 
 * @remarks Maps are used to stored mappings from one set of numbers to another.
 *          These maps are defined in the equipment.
 */
public final class MapEntry extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which name of the map. */
	private final String name;

	/** Which key. */
	private final IntFunction key;

	/** Precomputed value if possible. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * Return the corresponding value in the map.
	 * 
	 * @param name    The name of the map.
	 * @param key     The key value to check.
	 * @param keyRole The roleType corresponding to an integer value to check.
	 * 
	 * @example (mapEntry (last To))
	 * @example (mapEntry (trackSite Move steps:(count Pips) ) )
	 */
	public MapEntry
	(
		@Opt     final String      name,
		    @Or	 final IntFunction key,
		    @Or	 final RoleType    keyRole
	)
	{
		int numNonNull = 0;
		if (key != null)
			numNonNull++;
		if (keyRole != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		this.name = name;
		this.key = (key != null) ? key : RoleType.toIntFunction(keyRole);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int keyValue = key.eval(context);
		
		for (final game.equipment.other.Map map : context.game().equipment().maps())
		{
			if (name == null || map.name().equals(name))
			{
				final int tempMapValue = map.to(keyValue);
				// -99 is the value returned by the map if key or value not found
				if (tempMapValue != Constants.OFF && tempMapValue != map.noEntryValue())
				{
					return tempMapValue;
				}
			}
		}

		return keyValue;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return key.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return key.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(key.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(key.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(key.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		key.preprocess(game);
		
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (game.equipment().maps().length == 0)
		{
			game.addRequirementToReport("The ludeme (mapEntry ...) is used but the equipment has no maps.");
			missingRequirement = true;
		}
		missingRequirement |= key.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= key.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return name+ " of "+ key.toEnglish(game);
	}
}
