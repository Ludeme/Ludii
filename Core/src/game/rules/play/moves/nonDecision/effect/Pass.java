package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.state.GameType;
import main.Constants;
import other.action.others.ActionPass;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Passes this turn.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class Pass extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (pass)
	 */
	public Pass
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
		final ActionPass actionPass = new ActionPass(false);
		if (isDecision())
			actionPass.setDecision(true);
		final Move move = new Move(actionPass);
		move.setFromNonDecision(Constants.OFF);
		move.setToNonDecision(Constants.OFF);
		move.setMover(context.state().mover());
		moves.moves().add(move);
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

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

		// If only one player but pass is a legal move, we do not stop the game if the
		// player passes.
		if (game.players().count() == 1)
			gameFlags |= GameType.NotAllPass;

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (isDecision())
			concepts.set(Concept.PassDecision.id(), true);

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
		return super.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
	}
	
}
