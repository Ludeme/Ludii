package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Triggers an event for a player.
 *
 * @author Eric.Piette
 */
public final class ActionTrigger extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Index of the player. */
	private final int player;

	/** The event to trigger. */
	private final String event;

	// -------------------------------------------------------------------------
	
	/**
	 * @param player The player related to the event.
	 * @param event  The event to trigger.
	 */
	public ActionTrigger
	(
		final String event,
		final int player
	)
	{
		this.player = player;
		this.event = event;
	}

	/**
	 * Reconstructs a ActionTrigger object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionTrigger(final String detailedString)
	{
		assert (detailedString.startsWith("[Trigger:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strEvent = Action.extractData(detailedString, "event");
		event = strEvent;

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		// Event to add.
		context.state().triggers(player, true);
		return this;
	}	
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		context.state().triggers(player, false);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + player;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionTrigger))
			return false;

		final ActionTrigger other = (ActionTrigger) obj;
		return (decision == other.decision &&
				player == other.player && event.equals(other.event));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Trigger:");
		sb.append("event=" + event);
		sb.append(",player=" + player);
		if (decision)
			sb.append(",decision=" + decision);

		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Trigger";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Trigger " + event + " P" + player;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Trigger " + event + " P" + player + ")";
	}
		
	//-------------------------------------------------------------------------

	@Override
	public int who()
	{
		return player;
	}

	@Override
	public int what()
	{
		return player;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Trigger.id(), true);
		return concepts;
	}
}
