package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Forgets a value remembered before.
 *
 * @author Eric.Piette
 */
public class ActionForgetValue extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to forget. */
	private final int value;

	/** The name of the remembering values. */
	private final String name;

	/**
	 * @param name  The name of the remembering values.
	 * @param value The value to forget.
	 */
	public ActionForgetValue(final String name, final int value)
	{
		this.name = name;
		this.value = value;
	}

	/**
	 * Reconstructs an ActionForgetValue object from a detailed String (generated
	 * using toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionForgetValue(final String detailedString)
	{
		assert (detailedString.startsWith("[ForgetValue:"));

		final String strName = Action.extractData(detailedString, "name");
		name = strName;

		final String strValue = Action.extractData(detailedString, "value");
		value = Integer.parseInt(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if (name == null)
		{
			context.state().rememberingValues().remove(value);
		}
		else
		{
			final FastTIntArrayList rememberingValues = context.state().mapRememberingValues().get(name);
			if (rememberingValues != null)
			{
				rememberingValues.remove(value);
				if (rememberingValues.isEmpty())
					context.state().mapRememberingValues().remove(name);
			}
		}
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[ForgetValue:");
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
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionForgetValue))
			return false;

		final ActionForgetValue other = (ActionForgetValue) obj;
		if (name != null && other.name != null)
			if (!name.equals(other.name))
				return false;

		return (decision == other.decision && value == other.value);
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "ForgetValue";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "RememberedValues-=" + value;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Forget Value " + ((name != null) ? "'" + name + "' " : "") + value + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.ForgetValues.id(), true);
		return concepts;
	}
}
