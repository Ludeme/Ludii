package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Remember a value.
 *
 * @author Eric.Piette
 */
public class ActionRememberValue extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to remember. */
	private final int value;

	/** The name of the remembering values. */
	private final String name;

	/**
	 * @param name  The name of the remembering values.
	 * @param value The value to forget.
	 */
	public ActionRememberValue(final String name, final int value)
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
	public ActionRememberValue(final String detailedString)
	{
		assert (detailedString.startsWith("[RememberValue:"));

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
			context.state().rememberingValues().add(value);
		}
		else
		{
			FastTIntArrayList rememberingValues = context.state().mapRememberingValues().get(name);
			if (rememberingValues == null)
			{
				rememberingValues = new FastTIntArrayList();
				context.state().mapRememberingValues().put(name, rememberingValues);
			}
			
			rememberingValues.add(value);
		}
		
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		if (name == null)
		{
			context.state().rememberingValues().remove(value);
		}
		else
		{
			FastTIntArrayList rememberingValues = context.state().mapRememberingValues().get(name);
			if (rememberingValues != null)
			{
				rememberingValues.remove(value);
				if (rememberingValues.isEmpty())
					context.state().mapRememberingValues().remove(name);
			}
		}
		
		return this;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.Remember;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[RememberValue:");
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

		if (!(obj instanceof ActionRememberValue))
			return false;

		final ActionRememberValue other = (ActionRememberValue) obj;

		if (name != null && other.name != null)
			if (!name.equals(other.name))
				return false;

		return (decision == other.decision && value == other.value);
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "RememberValue";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "RememberedValues+=" + value;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Remember Value " + ((name != null) ? "'" + name + "' " : "") + value + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.RememberValues.id(), true);
		return concepts;
	}
}
