package other.action.graph;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the cost of a graph element.
 *
 * @author Eric.Piette
 */
public final class ActionSetCost extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the graph element. */
	private final int to;

	/** The cost to set. */
	private final int cost;

	/** The type of the graph element. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous cost. */
	private int previousCost;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element.
	 * @param to   The index of the site.
	 * @param cost The cost.
	 */
	public ActionSetCost
	(
		final SiteType type, 
		final int to, 
		final int cost
	)
	{
		this.type = type;
		this.to = to;
		this.cost = cost;
	}

	/**
	 * Reconstructs an ActionSetCost object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetCost(final String detailedString)
	{
		assert (detailedString.startsWith("[SetCost:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strCost = Action.extractData(detailedString, "cost");
		cost = Integer.parseInt(strCost);

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
			previousCost = context.topology().getGraphElements(type).get(to).cost();
			alreadyApplied = true;
		}
		
		context.topology().getGraphElements(type).get(to).setCost(cost);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		context.topology().getGraphElements(type).get(to).setCost(previousCost);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetCost:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);
		sb.append(",cost=" + cost);
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
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetCost))
			return false;

		final ActionSetCost other = (ActionSetCost) obj;
		return (decision == other.decision && to == other.to && cost == other.cost
				&& type.equals(other.type));
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetCost";
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

		sb.append("=$" + cost);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Cost at ");

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

		sb.append(" = " + cost + ")");

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
		return ActionType.SetCost;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetCost.id(), true);
		return concepts;
	}
}
