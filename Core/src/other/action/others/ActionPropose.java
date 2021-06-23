package other.action.others;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Proposes a subject to vote.
 *
 * @author Eric.Piette
 */
public final class ActionPropose extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The proposition. */
	private final String proposition;
	
	/** The proposition represented as an int */
	private final int propositionInt;

	/**
	 * @param proposition The proposition.
	 * @param propositionInt The proposition represented by a simple int
	 */
	public ActionPropose
	(
		final String proposition, 
		final int propositionInt
	)
	{
		this.proposition = proposition;
		this.propositionInt = propositionInt;
	}

	/**
	 * Reconstructs an ActionPropose object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionPropose(final String detailedString)
	{
		assert (detailedString.startsWith("[Propose:"));

		final String strProposition = Action.extractData(detailedString, "proposition");
		proposition = strProposition;
		
		final String strPropositionInt = Action.extractData(detailedString, "propositionInt");
		propositionInt = Integer.parseInt(strPropositionInt);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().propositions().add(propositionInt);
		return this;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean isPropose()
	{
		return true;
	}

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Propose:");
		sb.append("proposition=" + proposition);
		sb.append(",propositionInt=" + propositionInt);
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
		result = prime * result + proposition.hashCode();
		result = prime * result + propositionInt;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionPropose))
			return false;

		final ActionPropose other = (ActionPropose) obj;
		return decision == other.decision && proposition.equals(other.proposition) && propositionInt == other.propositionInt;
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Propose \"" + proposition + "\"";
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Propose \"" + proposition + "\")";
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "Propose";
	}

	//-------------------------------------------------------------------------

	@Override
	public String proposition()
	{
		return proposition;
	}
	
	/**
	 * @return Int representation of proposition
	 */
	public int propositionInt()
	{
		return propositionInt;
	}

	@Override
	public boolean isOtherMove()
	{
		return true;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Propose;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		if (decision)
			concepts.set(Concept.ProposeDecision.id(), true);
		else
			concepts.set(Concept.ProposeEffect.id(), true);
		
		return concepts;
	}
}