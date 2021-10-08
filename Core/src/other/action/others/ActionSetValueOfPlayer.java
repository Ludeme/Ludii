package other.action.others;

import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Sets the value of a player in the state.
 *
 * @author Eric.Piette
 */
public final class ActionSetValueOfPlayer extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player. */
	private final int player;

	/** The value */
	private final int value;

	/**
	 * @param player The index of the player.
	 * @param value  The value.
	 */
	public ActionSetValueOfPlayer
	(
		final int player, 
		final int value
	)
	{
		this.player = player;
		this.value = value;
	}

	/**
	 * Reconstructs an ActionSetValueOfPlayer object from a detailed String
	 * (generated using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetValueOfPlayer(final String detailedString)
	{
		assert (detailedString.startsWith("[SetValueOfPlayer:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strValue = Action.extractData(detailedString, "value");
		value = Integer.parseInt(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setValueForPlayer(player, value);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetValueOfPlayer:");
		sb.append("player=" + player);
		sb.append(",value=" + value);
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
		result = prime * result + player;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetValueOfPlayer))
			return false;

		final ActionSetValueOfPlayer other = (ActionSetValueOfPlayer) obj;
		return decision == other.decision && player == other.player && value == other.value;
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "P" + player + " value=" + value;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(value " + "P" + player + "=" + value + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetValueOfPlayer";
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.SetValueOfPlayer;
	}

}