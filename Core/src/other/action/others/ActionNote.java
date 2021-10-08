package other.action.others;

import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Sends a message to a player.
 *
 * @author Eric.Piette
 */
public final class ActionNote extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The message. */
	private final String message;

	/** The player index. */
	private final int player;

	/**
	 * @param message The message to send.
	 * @param player  The player to send the message.
	 */
	public ActionNote
	(
		final String message, 
		final int player
	)
	{
		this.message = message;
		this.player = player;
	}

	/**
	 * Reconstructs an ActionNote object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionNote(final String detailedString)
	{
		assert (detailedString.startsWith("[Note:"));

		final String strMessage = Action.extractData(detailedString, "message");
		message = strMessage;

		final String strPlayer = Action.extractData(detailedString, "to");
		player = Integer.parseInt(strPlayer);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().addNote(context.trial().moveNumber(), player, message);
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

		sb.append("[Note:");
		sb.append("message=" + message);
		sb.append(",to=" + player);
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
		result = prime * result + message.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionNote))
			return false;

		final ActionNote other = (ActionNote) obj;
		return decision == other.decision && message.equals(other.message) && player == other.player;
	}

	@Override
	public String getDescription()
	{
		return "Note";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Note P" + player + " \"" + message + "\"";
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Note \"" + message + "\" to " + player + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public int who()
	{
		return player;
	}

	@Override
	public String message()
	{
		return message;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Note;
	}
}