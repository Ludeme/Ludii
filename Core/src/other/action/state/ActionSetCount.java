package other.action.state;

import java.util.BitSet;

import game.equipment.component.Component;
import game.equipment.container.board.Track;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;

/**
 * Sets the count of a site.
 *
 * @author Eric.Piette
 */
public class ActionSetCount extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** What index. */
	private final int what;

	/** Site index. */
	private final int to;

	/** Count. */
	private final int count;

	/** The Graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous count. */
	private int previousCount;
	
	/** The previous site type. */
	private SiteType previousType;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type.
	 * @param to    Site to modify the count.
	 * @param what  The index of the component.
	 * @param count the number of item to place
	 */
	public ActionSetCount
	(
		final SiteType type,
		final int to,
		final int what,
		final int count
	)
	{
		this.to = to;
		this.count = count;
		this.type = type;
		this.what = what;
	}

	/**
	 * Reconstructs an ActionSetCount object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionSetCount(final String detailedString)
	{
		assert (detailedString.startsWith("[SetCount:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strWhat = Action.extractData(detailedString, "what");
		what = Integer.parseInt(strWhat);

		final String strCount = Action.extractData(detailedString, "count");
		count = Integer.parseInt(strCount);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;

		// If the site is not supported by the type, that's a cell of another container.
		if (to >= context.board().topology().getGraphElements(type).size())
			type = SiteType.Cell;

		final int contID = (type == SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state().containerStates()[contID];

		if (what != 0 && cs.count(to, type) == 0 && count > 0)
		{
			final Component piece = context.components()[what];
			final int owner = piece.owner();
			context.state().owned().add(owner, what, to, type);
		}
		
		if(!alreadyApplied)
		{
			previousCount = cs.count(to, type);
			previousType = type;
			alreadyApplied = true;
		}
		
		if (count > 0)
		{
			cs.setSite(context.state(), to, Constants.UNDEFINED, what, count, Constants.UNDEFINED, Constants.UNDEFINED,
					Constants.UNDEFINED, type);
		}
		else
		{
			final int pieceIdx = cs.remove(context.state(), to, type);
			if (pieceIdx > 0) // a piece was removed.
			{
				final Component piece = context.components()[pieceIdx];
				final int owner = piece.owner();
				context.state().owned().remove(owner, pieceIdx, to, type);

				// We update the structure about track indices if the game uses track.
				final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
				if (onTrackIndices != null)
				{
					for (final Track track : context.board().tracks())
					{
						final int trackIdx = track.trackIdx();
						final TIntArrayList indices = onTrackIndices.locToIndex(trackIdx, to);

						for (int i = 0; i < indices.size(); i++)
							onTrackIndices.remove(trackIdx, pieceIdx, 1, indices.getQuick(i));
					}
				}
			}
		}

		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		final int contID = (previousType == SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state().containerStates()[contID];

		if (what != 0 && cs.count(to, type) == 0 && previousCount > 0)
		{
			final Component piece = context.components()[what];
			final int owner = piece.owner();
			context.state().owned().add(owner, what, to, previousType);
		}
		
		if (previousCount > 0)
		{
			cs.setSite(context.state(), to, Constants.UNDEFINED, what, previousCount, Constants.UNDEFINED, Constants.UNDEFINED,
					Constants.UNDEFINED, previousType);
		}
		else
		{
			final int pieceIdx = cs.remove(context.state(), to, previousType);
			if (pieceIdx > 0) // a piece was removed.
			{
				final Component piece = context.components()[pieceIdx];
				final int owner = piece.owner();
				context.state().owned().remove(owner, pieceIdx, to, previousType);

				// We update the structure about track indices if the game uses track.
				final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
				if (onTrackIndices != null)
				{
					for (final Track track : context.board().tracks())
					{
						final int trackIdx = track.trackIdx();
						final TIntArrayList indices = onTrackIndices.locToIndex(trackIdx, to);

						for (int i = 0; i < indices.size(); i++)
							onTrackIndices.remove(trackIdx, pieceIdx, 1, indices.getQuick(i));
					}
				}
			}
		}
		
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetCount:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		sb.append(",what=" + what);
		sb.append(",count=" + count);
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
		result = prime * result + count;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + to;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetCount))
			return false;

		final ActionSetCount other = (ActionSetCount) obj;

		return (decision == other.decision &&
				to == other.to &&
				count == other.count && type == other.type);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "SetCount";
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

		sb.append("+");

		if (what > 0 && what < context.components().length)
		{
			sb.append(context.components()[what].name());
			if (count > 1)
				sb.append("x" + count);
		}

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Add ");

		if (what > 0 && what < context.components().length)
		{
			sb.append(context.components()[what].name());
			if (count > 1)
				sb.append("x" + count);
		}

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
	public int what()
	{
		return what;
	}

	@Override
	public int count()
	{
		return count;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetCount.id(), true);
		return concepts;
	}

}
