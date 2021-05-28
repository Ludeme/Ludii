package game.functions.booleans.all.values;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns true if all the values of an integer array satisfy a condition.
 * 
 * @author Eric.Piette
 */
@Hide
public final class AllValues extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Integer array to check. */
	private final IntArrayFunction array;

	/** Condition to check. */
	private final BooleanFunction condition;

	/**
	 * @param array The array.
	 * @param If    The condition to satisfy.
	 */
	public AllValues
	(
			  final IntArrayFunction array, 
		@Name final BooleanFunction  If
	)
	{
		this.array = array;
		this.condition = If;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int[] values = array.eval(context);
		final int originValue = context.value();

		for (final int site : values)
		{
			context.setValue(site);
			if (!condition.eval(context))
			{
				context.setValue(originValue);
				return false;
			}
		}

		context.setValue(originValue);
		return true;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return condition.isStatic() && array.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return condition.gameFlags(game) | array.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(condition.concepts(game));
		concepts.or(array.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(array.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Value.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(array.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		condition.preprocess(game);
		array.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= array.missingRequirement(game);
		missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= array.willCrash(game);
		willCrash |= condition.willCrash(game);
		return willCrash;
	}
}