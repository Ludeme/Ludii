package game.rules.start.forEach.value;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.start.StartRule;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Applies a move for each value from a value to another (included).
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachValue extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value from. */
	private final IntFunction minFn;
	
	/** The value to. */
	private final IntFunction maxFn;

	/** The starting rule to apply. */
	private final StartRule startRule;

	/**
	 * @param min       The minimal value.
	 * @param max       The maximal value.
	 * @param startRule The starting rule to apply.
	 */
	public ForEachValue
	(
		@Name final IntFunction min,
		@Name final IntFunction max,
			  final StartRule   startRule
	)
	{
		minFn = min;
		maxFn = max;
		this.startRule = startRule;
	}

	@Override
	public void eval(final Context context)
	{
		final int savedValue = context.value();
		
		final int min = minFn.eval(context);
		final int max = maxFn.eval(context);
		
		for (int to = min; to <= max; to++)
		{
			context.setValue(to);
			startRule.eval(context);
		}
		context.setValue(savedValue);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (maxFn != null)
			gameFlags |= maxFn.gameFlags(game);

		if (minFn != null)
			gameFlags |= minFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (minFn != null)
			concepts.or(minFn.concepts(game));
		if (maxFn != null)
			concepts.or(maxFn.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		if (minFn != null)
			writeEvalContext.or(minFn.writesEvalContextRecursive());
		if (maxFn != null)
			writeEvalContext.or(maxFn.writesEvalContextRecursive());
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
		readEvalContext.or(super.readsEvalContextRecursive());
		if (minFn != null)
			readEvalContext.or(minFn.readsEvalContextRecursive());
		if (maxFn != null)
			readEvalContext.or(maxFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		if (minFn != null)
			missingRequirement |= minFn.missingRequirement(game);
		if (maxFn != null)
		missingRequirement |= maxFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		if (minFn != null)
			willCrash |= minFn.willCrash(game);
		if (maxFn != null)
			willCrash |= maxFn.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (minFn != null)
			minFn.preprocess(game);
		if (maxFn != null)
			maxFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

}
