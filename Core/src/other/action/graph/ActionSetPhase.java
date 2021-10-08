package other.action.graph;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import other.action.Action;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the phase of a graph element.
 *
 * @author Eric.Piette
 */
public final class ActionSetPhase extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The site with the new phase. */
	private final int to;

	/** The phase to set. */
	private final int phase;

	/** The type of the graph element. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type  The graph element type.
	 * @param to    The site.
	 * @param phase The number of the phase.
	 */
	public ActionSetPhase
	(
		final SiteType type, 
		final int to,
		final int phase
	)
	{
		this.type = type;
		this.to = to;
		this.phase = phase;
	}

	/**
	 * Reconstructs an ActionSetPhase object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetPhase(final String detailedString)
	{
		assert (detailedString.startsWith("[SetPhase:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strPhase = Action.extractData(detailedString, "phase");
		phase = Integer.parseInt(strPhase);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		context.topology().getGraphElements(type).get(to).setPhase(phase);
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

		sb.append("[SetPhase:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);
		sb.append(",phase=" + phase);
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

		if (!(obj instanceof ActionSetPhase))
			return false;

		final ActionSetPhase other = (ActionSetPhase) obj;
		return (decision == other.decision && to == other.to && phase == other.phase);
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "SetPhase";
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

		sb.append("=%" + phase);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Phase at ");

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

		sb.append(" = " + phase + ")");

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

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetPhase.id(), true);
		return concepts;
	}

}
