package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the counter of the state.
 *
 * @author Eric.Piette
 */
public final class ActionSetCounter extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The new counter. */
	private final int counter;

	//-------------------------------------------------------------------------

	/**
	 * @param counter The new counter.
	 */
	public ActionSetCounter
	(
		final int counter
	)
	{
		this.counter = counter;
	}

	/**
	 * Reconstructs an ActionSetCounter object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionSetCounter(final String detailedString)
	{
		assert (detailedString.startsWith("[SetCounter:"));

		final String strCounter = Action.extractData(detailedString, "counter");
		counter = Integer.parseInt(strCounter);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setCounter(counter);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetCounter:");
		sb.append("counter=" + counter);
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
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetCounter))
			return false;

		final ActionSetCounter other = (ActionSetCounter) obj;
		return (decision == other.decision && counter == other.counter);
	}


	@Override
	public String getDescription()
	{
		return "SetCounter";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Counter=" + counter;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Counter = " + counter + ")";
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetInternalCounter.id(), true);
		return concepts;
	}
}
