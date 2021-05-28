package other.action.others;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import gnu.trove.list.array.TIntArrayList;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Votes on a proposition done previously.
 *
 * @author Eric.Piette
 */
public final class ActionVote extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The vote. */
	private final String vote;
	
	/** The vote represented as an int */
	private final int voteInt;

	/**
	 * @param vote    The vote.
	 * @param voteInt The vote represented by a simple int
	 */
	public ActionVote(final String vote, final int voteInt)
	{
		this.vote = vote;
		this.voteInt = voteInt;
	}

	/**
	 * Reconstructs an ActionVote object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionVote(final String detailedString)
	{
		assert (detailedString.startsWith("[Vote:"));

		final String strVote = Action.extractData(detailedString, "vote");
		vote = strVote;
		
		final String strVoteInt = Action.extractData(detailedString, "voteInt");
		voteInt = Integer.parseInt(strVoteInt);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().votes().add(voteInt);

		final TIntArrayList votes = context.state().votes();
		final int nbPlayers = context.game().players().count();

		// If the vote is over we get the result.
		if (votes.size() == nbPlayers)
		{
			final TIntArrayList votesChecked = new TIntArrayList();
			int countForDecision = 0;
			int decisionindex = 0;
			for (int i = 0; i < votes.size(); i++)
			{
				final int v = votes.getQuick(i);
				int currentCount = 0;
				if (!votesChecked.contains(v))
				{
					votesChecked.add(v);
					for (int j = i; j < votes.size(); j++)
					{
						if (votes.getQuick(j) == v)
							currentCount++;
					}
					if (currentCount > countForDecision)
					{
						countForDecision = currentCount;
						decisionindex = i;
					}
				}
			}

			// Decision takes only by a majority of player.
			if (countForDecision > (nbPlayers / 2))
				context.state().setIsDecided(votes.get(decisionindex));

			context.state().clearPropositions();
			context.state().clearVotes();
		}

		return this;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean isVote()
	{
		return true;
	}

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Vote:");
		sb.append("vote=" + vote);
		sb.append(",voteInt=" + voteInt);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + vote.hashCode();
		result = prime * result + voteInt;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionVote))
			return false;

		final ActionVote other = (ActionVote) obj;
		return decision == other.decision && vote.equals(other.vote) && voteInt == other.voteInt;
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Vote \"" + vote + "\"";
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Vote \"" + vote + "\")";
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "Vote";
	}

	//-------------------------------------------------------------------------

	@Override
	public String vote()
	{
		return vote;
	}
	
	/**
	 * @return Int representation of vote
	 */
	public int voteInt()
	{
		return voteInt;
	}

	@Override
	public boolean isOtherMove()
	{
		return true;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Vote;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Vote.id(), true);
		if (decision)
			concepts.set(Concept.VoteDecision.id(), true);
		return concepts;
	}

}