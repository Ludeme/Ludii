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
public final class ActionSetVar extends BaseAction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The name of the var */
	private final String name;

	/** The new value. */
	private final int value;

	// -------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous value. */
	private int previousValue;
	
	// -------------------------------------------------------------------------

	/**
	 * @param name  The name of the var.
	 * @param value The value.
	 */
	public ActionSetVar(final String name, final int value)
	{
		this.name = name;
		this.value = value;
	}

	/**
	 * Reconstructs an ActionSetTemp object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetVar(final String detailedString)
	{
		assert (detailedString.startsWith("[SetVar:"));

		final String strName = Action.extractData(detailedString, "name");
		name = strName;

		final String strValue = Action.extractData(detailedString, "value");
		value = Integer.parseInt(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	// -------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if(!alreadyApplied)
		{
			previousValue = context.state().getValue(name);
			alreadyApplied = true;
		}
		
		context.state().setValue(name, value);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		context.state().setValue(name, previousValue);
		return this;
	}

	// -------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetVar:");
		sb.append("name=" + name);
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
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetVar))
			return false;

		final ActionSetVar other = (ActionSetVar) obj;
		return (decision == other.decision && value == other.value && name.equals(other.name));
	}

	// -------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetVar";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return name + "=" + value;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(" + name + "= " + value + ")";
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
