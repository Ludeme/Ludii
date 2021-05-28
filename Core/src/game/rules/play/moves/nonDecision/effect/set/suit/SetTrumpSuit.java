package game.rules.play.moves.nonDecision.effect.set.suit;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.intArray.IntArrayConstant;
import game.functions.intArray.IntArrayFunction;
import game.functions.intArray.math.Difference;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.cards.ActionSetTrumpSuit;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Chooses a trump suit from a set of suits.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme is used for card games that involve trumps.
 */
@Hide
public final class SetTrumpSuit extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** All the possible suits. */
	private final IntArrayFunction suitsFn;

	//-------------------------------------------------------------------------

	/**
	 * @param suit  The suit to choose.
	 * @param suits The possible suits to choose.
	 * @param then  The moves applied after that move is applied.
	 */
	public SetTrumpSuit
	(
		     @Or final IntFunction suit,
		     @Or final Difference  suits,
		@Opt     final Then        then
	)
	{
		super(then);

		int numNonNull = 0;
		if (suit != null)
			numNonNull++;
		if (suits != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Only one Or parameter must be non-null.");

		if (suits != null)
			this.suitsFn = suits;
		else
			this.suitsFn = new IntArrayConstant(new IntFunction[]
			{ suit });
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		final int[] suits = suitsFn.eval(context);

		for (final int suit : suits)
		{
			final Action action = new ActionSetTrumpSuit(suit);
			if (isDecision())
				action.setDecision(true);
			final Move move = new Move(action);
			move.setFromNonDecision(Constants.OFF);
			move.setToNonDecision(Constants.OFF);
			move.setMover(context.state().mover());
			moves.moves().add(move);
		}

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
		long gameFlags = super.gameFlags(game) | GameType.NotAllPass;

		gameFlags |= suitsFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		boolean gameHasCard = false;
		for (int i = 1; i < game.equipment().components().length; i++)
		{
			final Component component = game.equipment().components()[i];
			if (component.isCard())
			{
				gameHasCard = true;
				break;
			}

		}

		if (!gameHasCard)
		{
			game.addRequirementToReport("The ludeme (set TrumpSuit ...) is used but the equipment has no cards.");
			missingRequirement = true;
		}
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= suitsFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= suitsFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (isDecision())
			concepts.set(Concept.ChooseTrumpSuit.id(), true);

		concepts.set(Concept.Card.id(), true);
		concepts.or(suitsFn.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(suitsFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(suitsFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		return suitsFn.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
	}

}
