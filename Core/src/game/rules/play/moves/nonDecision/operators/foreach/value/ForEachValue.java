package game.rules.play.moves.nonDecision.operators.foreach.value;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import main.collections.FastArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;

/**
 * Applies a move for each value from a value to another (included).
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachValue extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value from. */
	private final IntFunction minFn;
	
	/** The value to. */
	private final IntFunction maxFn;

	/** The IntArrayFunction to get the values. */
	private final IntArrayFunction valuesFn;

	/** The moves to apply. */
	private final Moves generator;

	/**
	 * @param values    The values.
	 * @param generator The move to apply.
	 * @param then      The moves applied after that move is applied.
	 */
	public ForEachValue
	(
			 final IntArrayFunction values, 
			 final Moves            generator, 
		@Opt final Then             then
	)
	{
		super(then);
		minFn = null;
		maxFn = null;
		valuesFn = values;
		this.generator = generator;
	}

	/**
	 * @param min       The minimal value.
	 * @param max       The maximal value.
	 * @param generator The move to apply.
	 * @param then      The moves applied after that move is applied.
	 */
	public ForEachValue
	(
			@Name final IntFunction min,
			@Name final IntFunction max,
		          final Moves generator,
		@Opt 	  final Then then
	)
	{
		super(then);
		minFn = min;
		maxFn = max;
		valuesFn = null;
		this.generator = generator;
	}

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final int savedValue = context.value();
		
		if (valuesFn != null)
		{
			final int[] values = valuesFn.eval(context);
			for (final int value : values)
			{
				context.setValue(value);
				final FastArrayList<Move> generatedMoves = generator.eval(context).moves();
				moves.moves().addAll(generatedMoves);
			}
		}
		else
		{
			final int min = minFn.eval(context);
			final int max = maxFn.eval(context);
			
			for (int to = min; to <= max; to++)
			{
				context.setValue(to);
				final FastArrayList<Move> generatedMoves = generator.eval(context).moves();
				moves.moves().addAll(generatedMoves);
			}
		}
		
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		context.setValue(savedValue);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = generator.gameFlags(game)
				| super.gameFlags(game);

		if (maxFn != null)
			gameFlags |= maxFn.gameFlags(game);

		if (minFn != null)
			gameFlags |= minFn.gameFlags(game);

		if (valuesFn != null)
			gameFlags |= valuesFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

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
		if (valuesFn != null)
			concepts.or(valuesFn.concepts(game));
		concepts.or(generator.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

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
		if (valuesFn != null)
			writeEvalContext.or(valuesFn.writesEvalContextRecursive());
		writeEvalContext.or(generator.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
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
		if (valuesFn != null)
			readEvalContext.or(valuesFn.readsEvalContextRecursive());
		readEvalContext.or(generator.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
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
		if (valuesFn != null)
			missingRequirement |= valuesFn.missingRequirement(game);
		missingRequirement |= generator.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
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
		if (valuesFn != null)
			willCrash |= valuesFn.willCrash(game);
		willCrash |= generator.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
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
		super.preprocess(game);
		if (minFn != null)
			minFn.preprocess(game);
		if (maxFn != null)
			maxFn.preprocess(game);
		if (valuesFn != null)
			valuesFn.preprocess(game);
		generator.preprocess(game);
	}
	
	//------------------------------------------------------------------------


	@Override
	public String toEnglish(final Game game)
	{
		String rangeString = "";
		if (valuesFn != null)
			rangeString = "in " + valuesFn.toEnglish(game);
		else
			rangeString = "between " + minFn.toEnglish(game) + " and " + maxFn.toEnglish(game);
		
		return "for all values " + rangeString + " " + generator.toEnglish(game);
	}
	
	//--------------------------------------------------------------------------

}
