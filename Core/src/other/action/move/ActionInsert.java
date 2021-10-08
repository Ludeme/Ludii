package other.action.move;

import game.equipment.component.Component;
import game.equipment.container.board.Track;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.BaseAction;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;

/**
 * Inserts a component inside a stack.
 *
 * @author Eric.Piette
 */
public final class ActionInsert extends BaseAction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;

	/** Site index. */
	private final int to;

	/** Level index. */
	private final int level;

	/** Component index. */
	private final int what;

	/** State of the site. */
	private final int state;

	// -------------------------------------------------------------------------

	/**
	 * @param type  The graph element type.
	 * @param to    The site to insert
	 * @param level The level to insert.
	 * @param what  The index of the component.
	 * @param state The state value.
	 */
	public ActionInsert
	(
		final SiteType type,
		final int to, 
		final int level, 
		final int what, 
		final int state
	)
	{
		this.type = type;
		this.to = to;
		this.level = level;
		this.what = what;
		this.state = state;
	}

	/**
	 * Reconstructs an ActionInsert object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionInsert(final String detailedString)
	{
		assert (detailedString.startsWith("[Insert:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = Integer.parseInt(strLevel);

		final String strWhat = Action.extractData(detailedString, "what");
		what = Integer.parseInt(strWhat);

		final String strState = Action.extractData(detailedString, "state");
		state = (strState.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strState);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	// -------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;

		// If the site is not supported by the type, that's a cell of another container.
		if (to >= context.board().topology().getGraphElements(type).size())
			type = SiteType.Cell;
		
		final int contID = (type == SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state()
				.containerStates()[contID];
		final int who = (what < 1) ? 0 : context.components()[what].owner();
		final int sizeStack = cs.sizeStack(to, type);

		if (level == sizeStack)
		{
			// We insert the new piece.
			cs.insert(context.state(), type, to, level, what, who, state, Constants.UNDEFINED,
					Constants.UNDEFINED,
					context.game());

			// we update the empty list
			cs.removeFromEmpty(to, type);

			// we update the own list with the new piece
			if (what != 0)
			{
				final Component piece = context.components()[what];
				final int owner = piece.owner();
				context.state().owned().add(owner, what, to, cs.sizeStack(to, type) - 1, type);
			}
		}
		else
		{
			// we update the own list of the pieces on the top of that piece inserted.
			for (int lvl = sizeStack - 1; lvl >= level; lvl--)
			{
				final int owner = cs.who(to, lvl, type);
				final int piece = cs.what(to, lvl, type);
				context.state().owned().removeNoUpdate(owner, piece, to, lvl, type);
				context.state().owned().add(owner, piece, to, lvl + 1, type);
			}

			// We insert the new piece.
			cs.insert(context.state(), type, to, level, what, who, state, Constants.UNDEFINED,
					Constants.UNDEFINED, context.game());

			// we update the own list with the new piece
			final Component piece = context.components()[what];
			final int owner = piece.owner();
			context.state().owned().add(owner, what, to, level, type);
		}

		// We update the structure about track indices if the game uses track.
		final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
		if (onTrackIndices != null)
		{
			for (final Track track : context.board().tracks())
			{
				final int trackIdx = track.trackIdx();
				final TIntArrayList indices = onTrackIndices.locToIndex(trackIdx, to);

				if (indices.size() > 0)
					onTrackIndices.add(trackIdx, what, 1, indices.getQuick(0));
			}
		}

		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		return this;
	}

	// -------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + to;
		result = prime * result + level;
		result = prime * result + state;
		result = prime * result + what;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionInsert))
			return false;

		final ActionInsert other = (ActionInsert) obj;
		return (decision == other.decision && to == other.to && level == other.level && state == other.state
				&& what == other.what && type.equals(other.type));
	}

	// -------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Insert:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		sb.append(",level=" + level);
		sb.append(",what=" + what);

		if (state != Constants.UNDEFINED)
			sb.append(",state=" + state);

		if (decision)
			sb.append(",decision=" + decision);

		sb.append(']');

		return sb.toString();
	}

	// -------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "Insert";
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

		sb.append("^");

		if (what > 0 && what < context.components().length)
			sb.append(context.components()[what].name());

		if (state != Constants.UNDEFINED)
			sb.append("=" + state);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Insert ");

		if (what > 0 && what < context.components().length)
			sb.append(context.components()[what].name());

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
			sb.append(" to " + type + " " + newTo);
		else
			sb.append(" to " + newTo);

		sb.append("/" + level);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		sb.append(')');

		return sb.toString();
	}

	// -------------------------------------------------------------------------

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
		return level;
	}

	@Override
	public int levelTo()
	{
		return level;
	}

	@Override
	public int what()
	{
		return what;
	}

	@Override
	public int state()
	{
		return state;
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
}
