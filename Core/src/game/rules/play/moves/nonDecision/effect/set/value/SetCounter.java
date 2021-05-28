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
import other.action.BaseAction;
import other.action.state.ActionSetCounter;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets the current counter of the game.
 * 
 * @author Eric.Piette
 * 
 * @remarks The counter is incremented at each move, so to reinitialize it to 0
 *          at the next move, the counter has to be set at -1.
 */
@Hide
public final class SetCounter extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** New value for the counter. */
	private final IntFunction newValue;

	//-------------------------------------------------------------------------

	/**
	 * @param newValue The new counter [-1].
	 * @param then     The moves applied after that move is applied.
	 */
	public SetCounter
	(
			@Opt final IntFunction newValue,
			@Opt final Then        then
	)
	{
		super(then);
		this.newValue = (newValue == null) ? new IntConstant(-1) : newValue;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final BaseAction actionSetCounter = new ActionSetCounter(newValue.eval(context));
		final Move move = new Move(actionSetCounter);
		moves.moves().add(move);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = newValue.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(newValue.concepts(game));
		concepts.set(Concept.InternalCounter.id(), true);
		concepts.set(Concept.SetInternalCounter.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(newValue.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(newValue.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= newValue.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= newValue.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return newValue.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		
		newValue.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "SetCounter(" + newValue + ") Generator";
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "SetCounter";
	}
}
