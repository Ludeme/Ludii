package other.action.hidden;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the hidden information to a graph element type at a specific level for a
 * specific player.
 *
 * @author Eric.Piette
 */
public final class ActionSetHidden extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the graph element. */
	private final int to;

	/** The cost to set. */
	private int level = Constants.UNDEFINED;
	
	/** The value to set */
	private final boolean value;
	
	/** The id of the player */
	private final int who;

	/** The type of the graph element. */
	private SiteType type;

	// -------------------------------------------------------------------------

	/**
	 * @param who   The player index.
	 * @param type  The graph element.
	 * @param to    The index of the site.
	 * @param level The level.
	 * @param value The value to set.
	 */
	public ActionSetHidden
	(
		final int who,
		final SiteType type, 
		final int to, 
		final int level,
		final boolean value
	)
	{
		this.type = type;
		this.to = to;
		this.who = who;
		this.level = level;
		this.value = value;
	}

	/**
	 * Reconstructs an ActionSetHidden object from a detailed String (generated
	 * using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetHidden(final String detailedString)
	{
		assert (detailedString.startsWith("[SetHidden:"));

		final String strWho = Action.extractData(detailedString, "who");
		who = Integer.parseInt(strWho);

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = (strLevel.isEmpty()) ? 0 : Integer.parseInt(strLevel);

		final String strValue = Action.extractData(detailedString, "value");
		value = Boolean.parseBoolean(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		context.containerState(context.containerId()[to]).setHidden(context.state(), who, to, level, type, value);
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

		sb.append("[SetHidden:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		if (level != Constants.UNDEFINED)
			sb.append(",level=" + level);
		sb.append(",who=" + who);
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
		result = prime * result + to;
		result = prime * result + (value ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + who;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetHidden))
			return false;

		final ActionSetHidden other = (ActionSetHidden) obj;
		return (decision == other.decision && to == other.to && value == other.value && who == other.who
				&& value == other.value && type.equals(other.type));
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetHidden";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		String newTo = to + "";
		if (useCoords)
		{
			final int cid = (type == SiteType.Cell || type == null && context.board().defaultSite() == SiteType.Cell)
					? context.containerId()[to]
					: 0;
			if (cid == 0)
			{
				final SiteType realType = (type != null) ? type : context.board().defaultSite();
				newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
						.label();
			}
		}

		if (type != null && !type.equals(context.board().defaultSite()))
			sb.append(type + " " + newTo);
		else
			sb.append(newTo);

		if (level != Constants.UNDEFINED)
			sb.append("/" + level);

		sb.append("P" + who);
		if (value)
			sb.append("=Hidden");
		else
			sb.append("!=Hidden");

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Hidden at ");

		String newTo = to + "";
		if (useCoords)
		{
			final int cid = (type == SiteType.Cell || type == null && context.board().defaultSite() == SiteType.Cell)
					? context.containerId()[to]
					: 0;
			if (cid == 0)
			{
				final SiteType realType = (type != null) ? type : context.board().defaultSite();
				newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
						.label();
			}
		}

		if (type != null && !type.equals(context.board().defaultSite()))
			sb.append(type + " " + newTo);
		else
			sb.append(newTo);

		if (level != Constants.UNDEFINED)
			sb.append("/" + level);

		sb.append(" to P" + who);
		sb.append(" = " + value + ")");
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	@Override
	public int from()
	{
		return to;
	}

	@Override
	public int to()
	{
		return to;
	}

	@Override
	public int levelFrom()
	{
		return (level == Constants.UNDEFINED) ? Constants.GROUND_LEVEL : level;
	}

	@Override
	public int levelTo()
	{
		return (level == Constants.UNDEFINED) ? Constants.GROUND_LEVEL : level;
	}

	@Override
	public SiteType fromType()
	{
		return type;
	}

	@Override
	public SiteType toType()
	{
		return type;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetInvisible.id(), true);
		return concepts;
	}
}
