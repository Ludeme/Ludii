package other.action.move;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Selects the from/to sites of the move.
 *
 * @author Eric.Piette
 */
public final class ActionSelect extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location From index. */
	private final int from;

	/** Location To index. */
	private final int to;

	/** Level From. */
	private final int levelFrom;

	/** Level To. */
	private final int levelTo;

	/** Add on Vertex/Edge/Face. */
	private final SiteType typeFrom;

	/** Add on Vertex/Edge/Face. */
	private final SiteType typeTo;

	//-------------------------------------------------------------------------

	/**
	 * @param typeFrom  The graph element type of the origin.
	 * @param from      The site of the origin.
	 * @param levelFrom The level of the origin.
	 * @param typeTo    The graph element type of the target.
	 * @param to        The site of the target.
	 * @param levelTo   The level of the target.
	 */
	public ActionSelect
	(
		final SiteType typeFrom,
		final int from, 
		final int levelFrom, 
		final SiteType typeTo,
		final int to, 
		final int levelTo
	)
	{
		this.from = from;
		this.to = to;
		this.typeFrom = typeFrom;
		this.typeTo = (typeTo != null) ? typeTo : typeFrom;
		this.levelFrom = levelFrom;
		this.levelTo = levelTo;
	}

	/**
	 * Reconstructs an ActionSelect object from a detailed String (generated using
	 * toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionSelect(final String detailedString)
	{
		assert (detailedString.startsWith("[Select:"));

		final String strTypeFrom = Action.extractData(detailedString, "typeFrom");
		typeFrom = (strTypeFrom.isEmpty()) ? null : SiteType.valueOf(strTypeFrom);

		final String strFrom = Action.extractData(detailedString, "from");
		from = (strFrom.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strFrom);

		final String strLevelFrom = Action.extractData(detailedString, "levelFrom");
		levelFrom = (strLevelFrom.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevelFrom);

		final String strTypeTo = Action.extractData(detailedString, "typeTo");
		typeTo = (strTypeTo.isEmpty()) ? typeFrom : SiteType.valueOf(strTypeTo);

		final String strTo = Action.extractData(detailedString, "to");
		to = (strTo.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strTo);

		final String strLevelTo = Action.extractData(detailedString, "levelTo");
		levelTo = (strLevelTo.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevelTo);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		// do nothing
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, final boolean discard)
	{
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + from;
		result = prime * result + to;
		result = prime * result + ((typeFrom == null) ? 0 : typeFrom.hashCode());
		result = prime * result + ((typeTo == null) ? 0 : typeTo.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSelect))
			return false;

		final ActionSelect other = (ActionSelect) obj;

		return (decision == other.decision &&
				from == other.from && to == other.to && typeFrom == other.typeFrom && typeTo == other.typeTo);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Select:");

		if (typeFrom != null || (context != null && typeFrom != context.board().defaultSite()))
		{
			sb.append("typeFrom=" + typeFrom);
			sb.append(",from=" + from);
		}
		else
			sb.append("from=" + from);

		if (levelFrom != Constants.UNDEFINED)
			sb.append(",levelFrom=" + levelFrom);

		if (to != Constants.UNDEFINED)
		{
			if (typeTo != null)
				sb.append(",typeTo=" + typeTo);
			sb.append(",to=" + to);
			if (levelTo != Constants.UNDEFINED)
				sb.append(",levelTo=" + levelTo);
		}

		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Select";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("*");
		String newFrom = from + "";
		if (useCoords)
		{
			final int cid = (typeFrom == SiteType.Cell
					|| typeFrom == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[from]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeFrom != null) ? typeFrom : context.board().defaultSite();
				newFrom = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(from)
						.label();
			}
		}

		if (typeFrom != null && !typeFrom.equals(context.board().defaultSite()))
			sb.append(typeFrom + " " + newFrom);
		else
			sb.append(newFrom);

		if (levelFrom != Constants.UNDEFINED)
			sb.append("/" + levelFrom);

		if (to != Constants.UNDEFINED)
		{
			String newTo = to + "";
			if (useCoords)
			{
				final int cid = (typeTo == SiteType.Cell
						|| typeTo == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[to]
								: 0;
				if (cid == 0)
				{
					final SiteType realType = (typeTo != null) ? typeTo : context.board().defaultSite();
					newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
							.label();
				}
			}

			if (typeTo != null && !typeTo.equals(context.board().defaultSite()))
				sb.append("-" + typeTo + " " + newTo);
			else
				sb.append("-" + newTo);

			if (levelTo != Constants.UNDEFINED)
				sb.append("/" + levelTo);
		}

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Select ");

		String newFrom = from + "";
		if (useCoords)
		{
			final int cid = (typeFrom == SiteType.Cell
					|| typeFrom == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[from]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeFrom != null) ? typeFrom : context.board().defaultSite();
				newFrom = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(from)
						.label();
			}
		}

		if (typeFrom != null && typeTo != null
				&& (!typeFrom.equals(context.board().defaultSite()) || !typeFrom.equals(typeTo)))
			sb.append(typeFrom + " " + newFrom);
		else
			sb.append(newFrom);

		if (levelFrom != Constants.UNDEFINED)
			sb.append("/" + levelFrom);

		if (to != Constants.UNDEFINED)
		{
			String newTo = to + "";
			if (useCoords)
			{
				final int cid = (typeTo == SiteType.Cell
						|| typeTo == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[to]
								: 0;
				if (cid == 0)
				{
					final SiteType realType = (typeTo != null) ? typeTo : context.board().defaultSite();
					newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
							.label();
				}
			}

			if (typeFrom != null && typeTo != null
					&& (!typeTo.equals(context.board().defaultSite()) || !typeFrom.equals(typeTo)))
				sb.append(" - " + typeTo + " " + newTo);
			else
				sb.append("-" + newTo);

			if (levelTo != Constants.UNDEFINED)
				sb.append("/" + levelTo);
		}

		sb.append(')');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet ludemeConcept = (movesLudeme != null) ? movesLudeme.concepts(context.game()) : new BitSet();
		final BitSet concepts = new BitSet();

		// ---- Swap Pieces concepts

		if (ludemeConcept.get(Concept.SwapPiecesEffect.id()))
			concepts.set(Concept.SwapPiecesEffect.id(), true);

		if (ludemeConcept.get(Concept.SwapPiecesDecision.id()))
			concepts.set(Concept.SwapPiecesDecision.id(), true);

		return concepts;
	}
		
	//-------------------------------------------------------------------------

	@Override
	public int from()
	{
		return from;
	}

	@Override
	public int to()
	{
		return (to == Constants.UNDEFINED) ? from : to;
	}

	@Override
	public int levelFrom()
	{
		return (levelFrom == Constants.UNDEFINED) ? Constants.GROUND_LEVEL : levelFrom;
	}

	@Override
	public int levelTo()
	{
		return (levelTo == Constants.UNDEFINED) ? Constants.GROUND_LEVEL : levelTo;
	}

	@Override
	public SiteType fromType()
	{
		return typeFrom;
	}

	@Override
	public SiteType toType()
	{
		return typeTo;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Select;
	}

}
