package game.rules.play.moves.nonDecision.effect.requirement;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Filters the legal moves to avoid reaching a specific state.
 * 
 * @author Eric.Piette
 * 
 * @remarks Example: Arimaa.
 */
public final class AvoidStoredState extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The possible moves. */
	private final Moves moves;

	/**
	 * @param moves The moves to filter.
	 * @param then  The moves applied after that move is applied.
	 * @example (avoidStoredState (forEach Piece))
	 */
	public AvoidStoredState
	(
			 final Moves  moves,
		@Opt final Then   then
	)
	{
		super(then);

		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves returnMoves = new BaseMoves(super.then());

		final Moves movesToEval = moves.eval(context);

		final long stateToCompare = context.state().storedState();

		for (int i = 0; i < movesToEval.moves().size(); i++)
		{
			final Move m = movesToEval.moves().get(i);
			final Context newContext = new TempContext(context);
			m.apply(newContext, true);

			if (newContext.state().stateHash() != stateToCompare)
				returnMoves.moves().add(m);
		}

		// Store the Moves in the computed moves.
		for (int j = 0; j < returnMoves.moves().size(); j++)
			returnMoves.moves().get(j).setMovesLudeme(this);
		
		// Store the Moves in the computed moves.
		for (int j = 0; j < returnMoves.moves().size(); j++)
			returnMoves.moves().get(j).setMovesLudeme(this);

		return returnMoves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = moves.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.PositionalSuperko.id(), true);
		concepts.or(moves.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(moves.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(moves.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= moves.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= moves.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		final boolean isStatic = moves.isStatic();
		return isStatic;
	}

	@Override
	public void preprocess(final Game game)
	{
		moves.preprocess(game);
	}

	@Override
	public String toEnglish(final Game game) 
	{
		return "Filter the legal moves to avoid reaching a specific state";
	}
	
}
