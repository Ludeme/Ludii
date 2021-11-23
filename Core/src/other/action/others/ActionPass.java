package other.action.others;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Pass the turn.
 *
 * @author Eric.Piette
 */
public final class ActionPass extends BaseAction
{
	private static final long serialVersionUID = 1L;

	/** To specify if that pass action is forced. */
	private final boolean forced;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param forced True if forced pass
	 */
	public ActionPass
	(
		final boolean forced
	)
	{
		this.forced = forced;
	}

	/**
	 * Reconstructs an ActionPass object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionPass(final String detailedString)
	{
		assert (detailedString.startsWith("[Pass:"));

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
		
		final String strForced = Action.extractData(detailedString, "forced");
		forced = (strForced.isEmpty()) ? false : Boolean.parseBoolean(strForced);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		// Nothing to do
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Pass:");
		if (decision)
			sb.append("decision=" + decision);

		if (forced)
			sb.append(",forced=" + forced);

		sb.append(']');

		return sb.toString();
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionPass))
			return false;

		final ActionPass other = (ActionPass) obj;
		return decision == other.decision && forced == other.forced;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Pass";
	}
	
	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Pass)";
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription() 
	{
		return "Pass";
	}

	@Override
	public boolean isPass()
	{
		return true;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Pass;
	}

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		if (decision)
			concepts.set(Concept.PassDecision.id(), true);
		else
			concepts.set(Concept.PassEffect.id(), true);
		
		return concepts;
	}
	
	@Override
	public boolean isForced()
	{
		return forced;
	}
}
