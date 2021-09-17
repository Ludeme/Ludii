package game.rules.play.moves.nonDecision.effect.set.value;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import main.Constants;
import other.action.state.ActionSetPot;
import other.context.Context;
import other.move.Move;

/**
 * Set the pot value of the state.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetPot extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private final IntFunction value;

	/**
	 * @param value The value to set the pot [-1].
	 * @param then  The moves applied after that move is applied.
	 */
	public SetPot
	(
	    @Opt final IntFunction value,
		@Opt final Then        then
	)
	{
		super(then);
		this.value = (value == null) ? new IntConstant(Constants.UNDEFINED) : value;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final Move move;
		final ActionSetPot actionSetPot = new ActionSetPot(value.eval(context));
		move = new Move(actionSetPot);
		moves.moves().add(move);

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
		if (value != null)
			gameFlags |= value.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (value != null)
			concepts.or(value.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(value.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(value.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		if (value != null)
			missingRequirement |= value.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		if (value != null)
			willCrash |= value.willCrash(game);

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
		if (value != null)
			value.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set the value of the pot to " + value.toEnglish(game) + thenString;
	}
	
	//-------------------------------------------------------------------------

}
