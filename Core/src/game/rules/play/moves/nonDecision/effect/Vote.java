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
import other.action.others.ActionVote;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

//-----------------------------------------------------------------------------

/**
 * Is used to propose something to the other players.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class Vote extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** All the votes. */
	private final String[] votes;
	
	/** The int representations of all the votes */
	private final int[] voteInts;
	
	//-------------------------------------------------------------------------

	/**
	 * @param vote  The vote.
	 * @param votes The votes.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (vote "End")
	 */
	public Vote
	(
		     @Or final String   vote, 
		     @Or final String[] votes,
		@Opt     final Then     then
	)
	{
		super(then);

		int numNonNull = 0;
		if (vote != null)
			numNonNull++;
		if (votes != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one Or parameter can be non-null.");

		if (votes != null)
		{
			this.votes = votes;
		}
		else
		{
			this.votes = new String[1];
			this.votes[0] = vote;
		}
		
		this.voteInts = new int[this.votes.length];
		Arrays.fill(voteInts, Constants.UNDEFINED);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		for (final int vote : voteInts)
		{
			// NOTE: if we have a -1 here, that means that preprocess() was not called!
			final Action action = new ActionVote(context.game().voteString(vote), vote);
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

		return moves;
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
			concepts.set(Concept.VoteDecision.id(), true);

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
		
		for (int i = 0; i < votes.length; ++i)
		{
			voteInts[i] = game.registerVoteString(votes[i]);
		}
	}

}
