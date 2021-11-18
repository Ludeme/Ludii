package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets a value to be in pending in the state.
 *
 * @author Eric.Piette
 */
public final class ActionSetPending extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The potential pending value. */
	final int value;

	//-------------------------------------------------------------------------
	
	/**
	 * @param value The value.
	 */
	public ActionSetPending
	(
		final int value
	)
	{
		this.value = value;
	}

	/**
	 * Reconstructs an ActionPending object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionSetPending(final String detailedString)
	{
		assert (detailedString.startsWith("[SetPending:"));

		final String strValue = Action.extractData(detailedString, "value");
		value = (strValue.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setPending(value);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		// No need going to be reset in game.undo(...)
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetPending))
			return false;

		final ActionSetPending other = (ActionSetPending) obj;
		return (decision == other.decision && value == other.value);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetPending:");
		if (value != Constants.UNDEFINED)
			sb.append("value=" + value);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "SetPending";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		if(value != Constants.UNDEFINED)
			return "Pending=" + value;
		
		return "Set Pending";
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		if (value != Constants.UNDEFINED)
			return "(Pending = " + value + ")";

		return "(Pending)";
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.SetPending;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetPending.id(), true);
		return concepts;
	}

}
