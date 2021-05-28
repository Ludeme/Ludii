package game.rules.play.moves.nonDecision.effect;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.others.ActionPropose;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Is used to propose something to the other players.
 * 
 * @author Eric.Piette and cambolbro
  */
public final class Propose extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** All the propositions. */
	private final String[] propositions;
	
	/** The int representations of all the propositions */
	private final int[] propositionInts;

	//-------------------------------------------------------------------------

	/**
	 * @param proposition  The proposition.
	 * @param propositions The propositions.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (propose "End")
	 */
	public Propose
	(
		     @Or final String   proposition,
		     @Or final String[] propositions,
		@Opt     final Then     then
	)
	{
		super(then);

		int numNonNull = 0;
		if (proposition != null)
			numNonNull++;
		if (propositions != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one Or parameter can be non-null.");

		if (propositions != null)
		{
			this.propositions = propositions;
		}
		else
		{
			this.propositions = new String[1];
			this.propositions[0] = proposition;
		}
		
		this.propositionInts = new int[this.propositions.length];
		Arrays.fill(propositionInts, Constants.UNDEFINED);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		for (final int proposition : propositionInts)
		{
			// NOTE: if we have a -1 here, that means that preprocess() was not called!
			final Action action = new ActionPropose(context.game().voteString(proposition), proposition);
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
		long gameFlags = super.gameFlags(game) | GameType.Vote;

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
			concepts.set(Concept.ProposeDecision.id(), true);

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
		final boolean isStatic = false;
		return isStatic;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		
		for (int i = 0; i < propositions.length; ++i)
		{
			propositionInts[i] = game.registerVoteString(propositions[i]);
		}
	}

}
