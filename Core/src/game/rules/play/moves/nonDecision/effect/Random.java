package game.rules.play.moves.nonDecision.effect;

import java.util.Arrays;
import java.util.BitSet;

import game.Game;
import game.functions.floats.FloatFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns a set of moves according to a set of probabilities.
 * 
 * @author Eric.Piette
 */
public final class Random extends Moves
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Which probas. */
	final FloatFunction[] probaFn;

	/** Which moves. */
	final Moves[] moves;

	//-------------------------------------------------------------------------

	/**
	 * @param probas The different probabilities for each move.
	 * @param moves  The different possible moves.
	 * 
	 * @example (random {0.01 0.99} {(forEach Piece) (move Pass)})
	 */
	public Random
	(
		final FloatFunction[] probas, 
		final Moves[]         moves
	)
	{
		super(null);
		final int minLength = Math.min(probas.length, moves.length);
		this.probaFn = Arrays.copyOf(probas, minLength);
		this.moves = Arrays.copyOf(moves, minLength);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final double[] probas = new double[probaFn.length];

		if (probas.length == 0)
			return new BaseMoves(super.then());

		for (int i = 0; i < probas.length; i++)
			probas[i] = probaFn[i].eval(context);

		double sumProba = 0;
		for (final double prob : probas)
			sumProba += prob;

		final double[] probasNorm = new double[probaFn.length];

		for (int i = 0; i < probas.length; i++)
			probasNorm[i] = probas[i] / sumProba;

		double randomValue = context.rng().nextDouble();

		int returnedIndex = 0;
		for (; returnedIndex < probasNorm.length; returnedIndex++)
		{
			randomValue -= probasNorm[returnedIndex];
			if (randomValue <= 0)
				break;
		}

		return moves[returnedIndex].eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long flags = super.gameFlags(game) | GameType.Stochastic;

		for (final FloatFunction floatFn : probaFn)
			flags |= floatFn.gameFlags(game);

		for (final Moves move : moves)
			flags |= move.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Stochastic.id(), true);

		for (final FloatFunction floatFn : probaFn)
			concepts.or(floatFn.concepts(game));

		for (final Moves move : moves)
			concepts.or(move.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		for (final Moves move : moves)
			writeEvalContext.or(move.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		for (final Moves move : moves)
			readEvalContext.or(move.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		for (final FloatFunction floatFn : probaFn)
			missingRequirement |= floatFn.missingRequirement(game);

		for (final Moves move : moves)
			missingRequirement |= move.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		for (final FloatFunction floatFn : probaFn)
			willCrash |= floatFn.willCrash(game);

		for (final Moves move : moves)
			willCrash |= move.willCrash(game);
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

		for (final FloatFunction floatFn : probaFn)
			floatFn.preprocess(game);

		for (final Moves move : moves)
			move.preprocess(game);
	}
}
