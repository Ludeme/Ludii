package game.rules.start.set.remember;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.start.StartRule;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.IntArrayFromRegion;
import other.action.state.ActionRememberValue;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Set a remembered value.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetRememberValue extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The values to remember. */
	private final IntArrayFromRegion values;
	
	/** If True we remember it only if not only remembered. */
	private final BooleanFunction uniqueFn;

	/** The name of the remembering values. */
	private final String name;

	/**
	 * @param name        The name of the remembering values.
	 * @param value       The value to remember.
	 * @param regionValue The values to remember.
	 * @param unique      If True we remember a value only if not already remembered
	 *                    [False].
	 */
	public SetRememberValue
	(
	    @Opt            final String          name,
			      @Or   final IntFunction     value,
			      @Or   final RegionFunction  regionValue,
		@Opt @Name      final BooleanFunction unique
	)
	{
		this.values = new IntArrayFromRegion(value, regionValue);
		this.uniqueFn = (unique == null) ? new BooleanConstant(false) : unique;
		this.name = name;
	}

	@Override
	public void eval(final Context context)
	{
		final int[] valuesToRemember = values.eval(context);
		final boolean hasToBeUnique = uniqueFn.eval(context);
		for (final int valueToRemember : valuesToRemember)
		{
			boolean isUnique = true;
			if (hasToBeUnique)
			{
				final TIntArrayList valuesInMemory = (name == null) ? context.state().rememberingValues()
						: context.state().mapRememberingValues().get(name);
				if (valuesInMemory != null)
					for (int i = 0; i < valuesInMemory.size(); i++)
					{
						final int valueInMemory = valuesInMemory.get(i);
						if (valueInMemory == valueToRemember)
						{
							isUnique = false;
							break;
						}
					}
			}

			if (!hasToBeUnique || (hasToBeUnique && isUnique))
			{
				final ActionRememberValue action = new ActionRememberValue(name, valueToRemember);
				action.apply(context, true);
				context.trial().addMove(new Move(action));
				context.trial().addInitPlacement();
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = GameType.RememberingValues | values.gameFlags(game) | uniqueFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(values.concepts(game));
		concepts.or(uniqueFn.concepts(game));
		concepts.or(super.concepts(game));
		concepts.set(Concept.Variable.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(values.writesEvalContextRecursive());
		writeEvalContext.or(uniqueFn.writesEvalContextRecursive());
		writeEvalContext.or(super.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(values.readsEvalContextRecursive());
		readEvalContext.or(uniqueFn.readsEvalContextRecursive());
		readEvalContext.or(super.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		values.preprocess(game);
		uniqueFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= values.missingRequirement(game);
		missingRequirement |= uniqueFn.missingRequirement(game);
		missingRequirement |= super.missingRequirement(game);
		return missingRequirement;
	}
}
