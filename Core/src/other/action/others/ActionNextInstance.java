package other.action.others;

import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Moves the state on to the next instance in a Match.
 *
 * @author Eric.Piette
 */
public final class ActionNextInstance extends BaseAction  
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public ActionNextInstance()
	{
		// ...
	}

	/**
	 * Reconstructs an ActionNextInstance object from a detailed String (generated
	 * using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionNextInstance(final String detailedString)
	{
		assert (detailedString.startsWith("[NextInstance:"));

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
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

		sb.append("[NextInstance:");
		if (decision)
			sb.append("decision=" + decision);
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

		if (!(obj instanceof ActionNextInstance))
			return false;

		final ActionNextInstance other = (ActionNextInstance) obj;
		return decision == other.decision;
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Next Game";
	}
	
	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Next Game)";
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "NextInstance";
	}
		
	//-------------------------------------------------------------------------


	@Override
	public boolean containsNextInstance()
	{
		return true;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.NextInstance;
	}
}
