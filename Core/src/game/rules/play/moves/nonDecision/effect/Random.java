package game.rules.play.moves.nonDecision.effect;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Name;
import game.Game;
import game.functions.floats.FloatFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
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

	//-------------------------------------------------------------------------

	/** Which probas. */
	final FloatFunction[] probaFn;

	/** Which moves. */
	final Moves[] moves;

	//-------------------------------------------------------------------------
	
	/** Which number to return. */
	final IntFunction num;

	/** Which move ludeme. */
	final Moves moveLudeme;

	//-------------------------------------------------------------------------

	/**
	 * For making probabilities on different set of moves to return.
	 * 
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
		this.moveLudeme = null;
		this.num = null;
	}
	
	/**
	 * For returning a specific number of random selected moves in a set of moves.
	 * 
	 * @param moves A list of moves.
	 * @param num   The number of moves to return from that list (less if the number of legal moves is lower).
	 * 
	 * @example (random (forEach Piece) num:2)
	 */
	public Random
	(
		      final Moves moves,
		@Name final IntFunction num
	)
	{
		super(null);
		this.probaFn = null;
		this.moves = null;
		this.moveLudeme = moves;
		this.num = num;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		if (moves != null)
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
		else
		{
			final Moves legalMoves = moveLudeme.eval(context);
			int numToReturn = Math.max(0, Math.min(num.eval(context), legalMoves.moves().size()));

			final Moves movesToReturn = new BaseMoves(super.then());
			TIntArrayList previousIndices = new TIntArrayList();
			while (numToReturn > 0)
			{
				int randomIndex = context.rng().nextInt(legalMoves.moves().size());
				if (previousIndices.contains(randomIndex))
					continue;
				
				movesToReturn.moves().add(legalMoves.moves().get(randomIndex));
				numToReturn --;
			}
			
			return movesToReturn;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long flags = super.gameFlags(game) | GameType.Stochastic;

		if (probaFn != null)
			for (final FloatFunction floatFn : probaFn)
				flags |= floatFn.gameFlags(game);

		if (moves != null)
			for (final Moves move : moves)
				flags |= move.gameFlags(game);

		if (num != null)
			flags |= num.gameFlags(game);
		
		if (moveLudeme != null)
			flags |= moveLudeme.gameFlags(game);
		
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Stochastic.id(), true);

		if (probaFn != null)
			for (final FloatFunction floatFn : probaFn)
				concepts.or(floatFn.concepts(game));

		if (moves != null)
			for (final Moves move : moves)
				concepts.or(move.concepts(game));

		if (num != null)
			concepts.or(num.concepts(game));
		
		if (moveLudeme != null)
			concepts.or(moveLudeme.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (moves != null)
			for (final Moves move : moves)
				writeEvalContext.or(move.writesEvalContextRecursive());

		if (moveLudeme != null)
			writeEvalContext.or(moveLudeme.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (moves != null)
			for (final Moves move : moves)
				readEvalContext.or(move.readsEvalContextRecursive());
		
		if (moveLudeme != null)
			readEvalContext.or(moveLudeme.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (probaFn != null)
			for (final FloatFunction floatFn : probaFn)
				missingRequirement |= floatFn.missingRequirement(game);

		if (moves != null)
			for (final Moves move : moves)
				missingRequirement |= move.missingRequirement(game);

		if (num != null)
			missingRequirement |= num.missingRequirement(game);
		
		if (moveLudeme != null)
			missingRequirement |= moveLudeme.missingRequirement(game);
		
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (probaFn != null)
			for (final FloatFunction floatFn : probaFn)
				willCrash |= floatFn.willCrash(game);

		if (moves != null)
			for (final Moves move : moves)
				willCrash |= move.willCrash(game);
		
		if (num != null)
			willCrash |= num.willCrash(game);
		
		if (moveLudeme != null)
			willCrash |= moveLudeme.willCrash(game);
		
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

		if (probaFn != null)
			for (final FloatFunction floatFn : probaFn)
				floatFn.preprocess(game);

		if (moves != null)
			for (final Moves move : moves)
				move.preprocess(game);
		
		if (num != null)
			num.preprocess(game);
		
		if (moveLudeme != null)
			moveLudeme.preprocess(game);
	}
}
