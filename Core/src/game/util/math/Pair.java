package game.util.math;

import java.util.BitSet;

import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.board.LandmarkType;
import game.types.play.RoleType;
import main.Constants;
import other.BaseLudeme;

/**
 * Defines a pair of two integers, two strings or one integer and a string.
 * 
 * @author Eric.Piette
 * @remarks This is used for the map ludeme.
 */
public class Pair extends BaseLudeme
{
	/** The integer key of the pair. */
	final IntFunction intKey;

	/** The string key of the pair. */
	final String stringKey;

	/** The integer value of the pair. */
	final IntFunction intValue;

	/** The string value of the pair. */
	final String stringValue;

	/** The landmark of the value. */
	final LandmarkType landmark;

	/** The key roleType to check in the warning. */
	final RoleType roleTypeKey;

	/** The value roleType to check in the warning. */
	final RoleType roleTypeValue;

	/**
	 * For a pair of integers.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair 5 10)
	 */
	public Pair
	(
		final IntFunction key, 
		final IntFunction value
	)
	{
		this.intKey = key;
		this.intValue = value;
		this.stringKey = null;
		this.stringValue = null;
		this.landmark = null;
		this.roleTypeKey = null;
		this.roleTypeValue = null;
	}

	/**
	 * For a pair of a RoleType and an Integer.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair P1 10)
	 */
	public Pair
	(
		final RoleType key, 
		final IntFunction value
	)
	{
		this.intKey = RoleType.toIntFunction(key);
		this.intValue = value;
		this.stringKey = null;
		this.stringValue = null;
		this.landmark = null;
		this.roleTypeKey = key;
		this.roleTypeValue = null;
	}

	/**
	 * For a pair of tow RoleTypes.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair P1 P2)
	 */
	public Pair
	(
		final RoleType key, 
		final RoleType value
	)
	{
		this.intKey = RoleType.toIntFunction(key);
		this.intValue = RoleType.toIntFunction(value);
		this.stringKey = null;
		this.stringValue = null;
		this.landmark = null;
		this.roleTypeKey = key;
		this.roleTypeValue = value;
	}

	/**
	 * For a pair of two strings.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair "A1" "C3")
	 */
	public Pair
	(
		final String key, 
		final String value
	)
	{
		this.intKey = null;
		this.intValue = null;
		this.stringKey = key;
		this.stringValue = value;
		this.landmark = null;
		this.roleTypeKey = null;
		this.roleTypeValue = null;
	}

	/**
	 * For a pair of an integer and a string.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair 0 "A1")
	 */
	public Pair
	(
		final IntFunction key, 
		final String value
	)
	{
		this.intKey = key;
		this.intValue = null;
		this.stringKey = null;
		this.stringValue = value;
		this.landmark = null;
		this.roleTypeKey = null;
		this.roleTypeValue = null;
	}

	/**
	 * For a pair of a RoleType and a string.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair P1 "A1")
	 */
	public Pair
	(
		final RoleType key, 
		final String value
	)
	{
		this.intKey = RoleType.toIntFunction(key);
		this.intValue = null;
		this.stringKey = null;
		this.stringValue = value;
		this.landmark = null;
		this.roleTypeKey = key;
		this.roleTypeValue = null;
	}

	/**
	 * For a pair of a RoleType and an ordered graph element type.
	 * 
	 * @param key      The key of the pair.
	 * @param landmark The landmark of the value.
	 * 
	 * @example (pair P1 LeftSite)
	 */
	public Pair
	(
		final RoleType key, 
		final LandmarkType landmark
	)
	{
		this.intKey = RoleType.toIntFunction(key);
		this.intValue = null;
		this.stringKey = null;
		this.stringValue = null;
		this.landmark = landmark;
		this.roleTypeKey = key;
		this.roleTypeValue = null;
	}

	/**
	 * For a pair of a RoleType and a value.
	 * 
	 * @param key   The key of the pair.
	 * @param value The corresponding value.
	 * 
	 * @example (pair "A1" P1)
	 */
	public Pair
	(
		final String key, 
		final RoleType value
	)
	{
		this.intKey = null;
		this.intValue = RoleType.toIntFunction(value);
		this.stringKey = key;
		this.stringValue = null;
		this.landmark = null;
		this.roleTypeKey = null;
		this.roleTypeValue = value;
	}

	/**
	 * @return The integer value.
	 */
	public IntFunction intValue()
	{
		if (intValue != null)
			return intValue;
		return new IntConstant(Constants.UNDEFINED);
	}

	/**
	 * @return The integer key.
	 */
	public IntFunction intKey()
	{
		if (intKey != null)
			return intKey;
		return new IntConstant(Constants.UNDEFINED);
	}

	/**
	 * @return The string value.
	 */
	public String stringValue()
	{
		return stringValue;
	}

	/**
	 * @return The landmark of the value.
	 */
	public LandmarkType landmarkType()
	{
		return landmark;
	}

	/**
	 * @return The string key.
	 */
	public String stringKey()
	{
		return stringKey;
	}

	/**
	 * @param game The game.
	 * @return The corresponding gameFlags.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (intKey != null)
			gameFlags |= intKey.gameFlags(game);

		if (intValue != null)
			gameFlags |= intValue.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();

		if (intKey != null)
			writeEvalContext.or(intKey.writesEvalContextRecursive());

		if (intValue != null)
			writeEvalContext.or(intValue.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();

		if (intKey != null)
			readEvalContext.or(intKey.readsEvalContextRecursive());

		if (intValue != null)
			readEvalContext.or(intValue.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (intKey != null)
			concepts.or(intKey.concepts(game));

		if (intValue != null)
			concepts.or(intValue.concepts(game));

		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (intKey != null)
			missingRequirement |= intKey.missingRequirement(game);

		if (intValue != null)
			missingRequirement |= intValue.missingRequirement(game);

		// We check if the key roleType is correct.
		if (roleTypeKey != null)
		{
			final int indexOwnerPhase = roleTypeKey.owner();
			if (((indexOwnerPhase < 1 && !roleTypeKey.equals(RoleType.Shared)) && !roleTypeKey.equals(RoleType.Neutral)
					&& !roleTypeKey.equals(RoleType.All)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"The key of a pair in the map is using a wrong roletype which is " + roleTypeKey + ".");
				missingRequirement = true;
			}
		}

		// We check if the value roleType is correct.
		if (roleTypeValue != null)
		{
			final int indexOwnerPhase = roleTypeValue.owner();
			if (((indexOwnerPhase < 1 && !roleTypeValue.equals(RoleType.Shared))
					&& !roleTypeValue.equals(RoleType.Neutral) && !roleTypeKey.equals(RoleType.All))
					|| indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"The value of a pair in the map is using a wrong roletype which is " + roleTypeValue + ".");
				missingRequirement = true;
			}
		}

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;

		if (intKey != null)
			willCrash |= intKey.willCrash(game);

		if (intValue != null)
			willCrash |= intValue.willCrash(game);

		return willCrash;
	}
}
