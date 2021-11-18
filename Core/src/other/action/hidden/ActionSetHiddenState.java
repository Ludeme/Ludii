package other.action.hidden;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Sets the state hidden information to a graph element type at a specific
 * level for a specific player.
 *
 * @author Eric.Piette
 */
public final class ActionSetHiddenState extends BaseAction
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

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous value. */
	private boolean previousValue;
	
	/** The previous site type. */
	private SiteType previousType;
	
	//-------------------------------------------------------------------------

	/**
	 * @param who   The player index.
	 * @param type  The graph element.
	 * @param to    The index of the site.
	 * @param level The level.
	 * @param value The value to set.
	 */
	public ActionSetHiddenState(final int who, final SiteType type, final int to, final int level,
			final boolean value)
	{
		this.type = type;
		this.to = to;
		this.who = who;
		this.level = level;
		this.value = value;
	}

	/**
	 * Reconstructs an ActionSetHiddenState object from a detailed String
	 * (generated using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetHiddenState(final String detailedString)
	{
		assert (detailedString.startsWith("[SetHiddenState:"));

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
		
		if(!alreadyApplied)
		{
			final int cid = to >= context.containerId().length ? 0 : context.containerId()[to];
			final ContainerState cs = context.state().containerStates()[cid];
			previousValue = cs.isHiddenState(who, to, level, type);
			previousType = type;
			alreadyApplied = true;
		}
		
		context.containerState(context.containerId()[to]).setHiddenState(context.state(), who, to, level, type, value);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		context.containerState(context.containerId()[to]).setHiddenState(context.state(), who, to, level, previousType, previousValue);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetHiddenState:");
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

		if (!(obj instanceof ActionSetHiddenState))
			return false;

		final ActionSetHiddenState other = (ActionSetHiddenState) obj;
		return (decision == other.decision && to == other.to && value == other.value && who == other.who
				&& value == other.value && type.equals(other.type));
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetHiddenState";
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
			sb.append("=HiddenState");
		else
			sb.append("!=HiddenState");

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(HiddenState at ");

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

	@Override
	public ActionType actionType()
	{
		return ActionType.SetHiddenState;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetHiddenState.id(), true);
		return concepts;
	}
}
