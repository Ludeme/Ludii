package other.action.state;

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
 * Sets the piece value of a site.
 * 
 * @author Eric.Piette
 */
public class ActionSetValue extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Loc index. */
	private final int to;

	/** Level index. */
	private final int level;

	/** Piece value. */
	private final int value;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous value. */
	private int previousValue;
	
	/** The previous site type. */
	private SiteType previousType;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type.
	 * @param to    The site.
	 * @param level The level.
	 * @param value The piece value.
	 */
	public ActionSetValue
	(
		final SiteType type,
		final int to,
		final int level,
		final int value
	)
	{
		this.to = to;
		this.level = level;
		this.value = value;
		this.type = type;
	}

	/**
	 * Reconstructs an ActionSetValue object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetValue(final String detailedString)
	{
		assert (detailedString.startsWith("[SetValue:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = (strLevel.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevel);

		final String strValue = Action.extractData(detailedString, "value");
		value = Integer.parseInt(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if(to < 0)
			return this;
		
		type = (type == null) ? context.board().defaultSite() : type;
		final int cid = to >= context.containerId().length ? 0 : context.containerId()[to];
		final ContainerState cs = context.state().containerStates()[cid];
		
		if (context.game().isStacking())
		{
			if (level != Constants.UNDEFINED)
			{
				final int stackSize = cs.sizeStack(to, type);
				if (level < stackSize)
				{
					if(!alreadyApplied)
					{
						previousValue = cs.value(to, level, type);
						previousType = type;
						alreadyApplied = true;
					}
					
					final int what = cs.what(to, level, type);
					final int who = cs.who(to, level, type);
					final int rotation = cs.rotation(to, level, type);
					final int state = cs.state(to, level, type);
					cs.remove(context.state(), to, level);
					cs.insert(context.state(), type, to, level, what, who, state, rotation, value, context.game());
				}
			}
			else
			{
				if(!alreadyApplied)
				{
					previousValue = cs.value(to, type);
					previousType = type;
					alreadyApplied = true;
				}
				
				cs.setSite(context.state(), to, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, value,
						type);
			}
		}
		else
		{
			if(!alreadyApplied)
			{
				previousValue = cs.value(to, type);
				previousType = type;
				alreadyApplied = true;
			}
			
			cs.setSite(context.state(), to, Constants.UNDEFINED,
				Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, value, type);
		}
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		if(to < 0)
			return this;
		
		final int cid = to >= context.containerId().length ? 0 : context.containerId()[to];
		final ContainerState cs = context.state().containerStates()[cid];
		
		if (context.game().isStacking())
		{
			final int stackSize = cs.sizeStack(to, type);
			
			if (level != Constants.UNDEFINED)
			{
				if (level < stackSize)
				{
					final int what = cs.what(to, level, type);
					final int who = cs.who(to, level, type);
					final int rotation = cs.rotation(to, level, type);
					final int state = cs.state(to, level, type);
					cs.remove(context.state(), to, level);
					cs.insert(context.state(), previousType, to, level, what, who, state, rotation, previousValue, context.game());
				}
			}
			else
			{
				cs.setSite(context.state(), to, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousValue,
						previousType);
			}
		}
		else
		{
			cs.setSite(context.state(), to, Constants.UNDEFINED,
				Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousValue, previousType);
		}
		
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetValue:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		if (level != Constants.UNDEFINED)
			sb.append(",level=" + level);

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
		result = prime * result + to;
		if (level != Constants.UNDEFINED)
			result = prime * result + level;
		result = prime * result + value;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetValue))
			return false;

		final ActionSetValue other = (ActionSetValue) obj;

		return (to == other.to && level == other.level &&
				value == other.value && type == other.type);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "SetValue";
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

		sb.append("=" + value);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Value ");

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

		sb.append("=" + value);

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
	public int value()
	{
		return value;
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
	public ActionType actionType()
	{
		return ActionType.SetValue;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetValue.id(), true);

		return concepts;
	}
}
