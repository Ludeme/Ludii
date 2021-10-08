package other.action.die;

import game.equipment.container.other.Dice;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Specifies to the state that all the dice are equals.
 *
 * @author Eric.Piette
 */
public final class ActionSetDiceAllEqual extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to set. */
	private final boolean value;

	//-------------------------------------------------------------------------

	/**
	 * @param value The value to set.
	 */
	public ActionSetDiceAllEqual
	(
		final boolean value
	)
	{
		this.value = value;
	}

	/**
	 * Reconstructs an ActionSetDiceAllEqual object from a detailed String
	 * (generated using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetDiceAllEqual(final String detailedString)
	{
		assert (detailedString.startsWith("[SetDiceAllEqual:"));

		final String strValue = Action.extractData(detailedString, "value");
		value = Boolean.parseBoolean(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	// -------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setDiceAllEqual(value);
		
		// To update the sum of the dice container.
		for (int i = 0; i < context.handDice().size(); i++)
		{
			final Dice dice = context.handDice().get(i);
			final int siteFrom = context.sitesFrom()[dice.index()];
			final int siteTo = context.sitesFrom()[dice.index()] + dice.numSites();
			int sum = 0;
			for (int site = siteFrom; site < siteTo; site++)
				sum += context.state().currentDice()[i][site - siteFrom];
			context.state().sumDice()[i] = sum;
		}
		
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		return this;
	}

	// -------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetDiceAllEqual:");
		sb.append("value=" + value);
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
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetDiceAllEqual))
			return false;

		final ActionSetDiceAllEqual other = (ActionSetDiceAllEqual) obj;

		return (decision == other.decision && value == other.value);
	}

	// -------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetDiceAllEqual";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		if (value)
			return "Dice Equal";

		return "Dice Not Equal";
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		if (value)
			return "(Dice Equal)";

		return "(Dice Not Equal)";
	}

	// -------------------------------------------------------------------------

	@Override
	public ActionType actionType()
	{
		return ActionType.SetDiceAllEqual;
	}
}
