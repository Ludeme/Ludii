package other.action.state;

import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Sets the pot.
 *
 * @author Eric.Piette
 */
public final class ActionSetPot extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The pot. */
	private final int pot;

	//-------------------------------------------------------------------------

	/**
	 * @param pot The new value of the pot.
	 */
	public ActionSetPot
	(
		final int pot
	)
	{
		this.pot = pot;
	}

	/**
	 * Reconstructs an ActionSetPot object from a detailed String (generated using
	 * toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionSetPot(final String detailedString)
	{
		assert (detailedString.startsWith("[SetPot:"));

		final String strBet = Action.extractData(detailedString, "pot");
		pot = Integer.parseInt(strBet);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setPot(pot);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + pot;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetPot))
			return false;

		final ActionSetPot other = (ActionSetPot) obj;
		return (decision == other.decision && pot == other.pot);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetPot:");
		sb.append("pot=" + pot);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "Pot";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Pot " + " $" + pot;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Pot = " + pot + ")";
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isOtherMove()
	{
		return true;
	}

	@Override
	public int count()
	{
		return pot;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Bet;
	}
}
