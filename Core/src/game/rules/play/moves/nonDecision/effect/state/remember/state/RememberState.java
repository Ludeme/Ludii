package game.rules.play.moves.nonDecision.effect.state.remember.state;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import other.action.state.ActionStoreStateInContext;
import other.context.Context;
import other.move.Move;

/**
 * Stores the current state.
 * 
 * @author Eric.Piette
 */
@Hide
public final class RememberState extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The moves applied after that move is applied.
	 */
	public RememberState
	(
		@Opt final Then then
	)
	{
		super(then);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final ActionStoreStateInContext action = new ActionStoreStateInContext();
		final Move move = new Move(action);
		moves.moves().add(move);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "remember the current state";
	}
	
	//-------------------------------------------------------------------------
	
}