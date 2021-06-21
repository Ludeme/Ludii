package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Sets the state of a site.
 * 
 * @author Eric.Piette
 */
public class ActionSetState extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Loc index. */
	private final int to;

	/** Level index. */
	private final int level;

	/** Local state. */
	private final int state;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type.
	 * @param to    The site.
	 * @param level The level.
	 * @param state The state value.
	 */
	public ActionSetState
	(
		final SiteType type,
		final int to,
		final int level,
		final int state
	)
	{
		this.to = to;
		this.level = level;
		this.state = state;
		this.type = type;
	}

	/**
	 * Reconstructs an ActionSetState object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetState(final String detailedString)
	{
		assert (detailedString.startsWith("[SetState:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = (strLevel.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevel);

		final String strState = Action.extractData(detailedString, "state");
		state = Integer.parseInt(strState);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;

		if (context.game().isStacking())
		{
			if (level != Constants.UNDEFINED)
			{
				final int cid = to >= context.containerId().length ? 0 : context.containerId()[to];
				final ContainerState cs = context.state().containerStates()[cid];
				final int stackSize = cs.sizeStack(to, type);
				if (level < stackSize)
				{
					final int what = cs.what(to, level, type);
					final int who = cs.who(to, level, type);
					final int rotation = cs.rotation(to, level, type);
					final int value = cs.value(to, level, type);
					cs.remove(context.state(), to, level, type);
					cs.insert(context.state(), type, to, level, what, who, state, rotation, value, context.game());
				}
			}
			else
			{
				final int cidTo = type.equals(SiteType.Cell) ?  context.containerId()[to] : 0;
				if(to < context.containers()[cidTo].topology().getGraphElements(type).size())
					context.containerState(cidTo).setSite(context.state(), to, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, state, Constants.UNDEFINED, Constants.UNDEFINED, type);
			}
		}
		else
		{
			final int cidTo = type.equals(SiteType.Cell) ?  context.containerId()[to] : 0;
			if(to < context.containers()[cidTo].topology().getGraphElements(type).size())
				context.containerState(cidTo).setSite(context.state(), to, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, state, Constants.UNDEFINED, Constants.UNDEFINED, type);
		}

		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetState:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		if (level != Constants.UNDEFINED)
			sb.append(",level=" + level);

		sb.append(",state=" + state);
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
		result = prime * result + to;
		if (level != Constants.UNDEFINED)
			result = prime * result + level;
		result = prime * result + state;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetState))
			return false;

		final ActionSetState other = (ActionSetState) obj;

		return (to == other.to && level == other.level &&
				state == other.state && type == other.type);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "SetState";
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

		sb.append("=" + state);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(State ");

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

		if (level != Constants.UNDEFINED && context.game().isStacking())
			sb.append("/" + level);

		sb.append("=" + state);

		sb.append(')');

		return sb.toString();
	}
		
	//-------------------------------------------------------------------------

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
	public int state()
	{
		return state;
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

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet ludemeConcept = (movesLudeme != null) ? movesLudeme.concepts(context.game()) : new BitSet();

		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetSiteState.id(), true);

		if (ludemeConcept.get(Concept.Flip.id()))
			concepts.set(Concept.Flip.id(), true);

		return concepts;
	}
}
