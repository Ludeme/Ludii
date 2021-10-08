package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the temporary value of the state.
 *
 * @author Eric.Piette
 */
public final class ActionSetTemp extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The new temporary value. */
	private final int temp;

	//-------------------------------------------------------------------------

	/**
	 * @param temp The temporary value.
	 */
	public ActionSetTemp(final int temp)
	{
		this.temp = temp;
	}

	/**
	 * Reconstructs an ActionSetTemp object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetTemp(final String detailedString)
	{
		assert (detailedString.startsWith("[SetTemp:"));

		final String strTemp = Action.extractData(detailedString, "temp");
		temp = Integer.parseInt(strTemp);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setTemp(temp);
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

		sb.append("[SetTemp:");
		sb.append("temp=" + temp);
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
		result = prime * result + temp;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetTemp))
			return false;

		final ActionSetTemp other = (ActionSetTemp) obj;
		return (decision == other.decision && temp == other.temp);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "SetTemp";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Temp=" + temp;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Temp = " + temp + ")";
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetVar.id(), true);
		return concepts;
	}
}
