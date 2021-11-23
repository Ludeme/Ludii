package other.action.move.remove;

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
 * Remove later a specific site (sequential capture).
 *
 * @author Eric.Piette
 */
public final class ActionRemoveNonApplied extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location where to remove the component(s). */
	private final int to;

	/** The graph element type. */
	private SiteType type;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type.
	 * @param to   Location to remove the component(s).
	 */
	public ActionRemoveNonApplied
	(
		final SiteType type,
		final int to 
	)
	{
		this.to = to;
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().addSitesToRemove(to);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		context.state().removeSitesToRemove(to);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + to;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionRemoveNonApplied))
			return false;

		final ActionRemoveNonApplied other = (ActionRemoveNonApplied) obj;
		return (decision == other.decision && to == other.to);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Remove:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		sb.append(",applied=" + false);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Remove";
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

		sb.append("-");

		sb.append("...");

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Remove ");

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

		sb.append(" applied = false");

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
	public int levelFrom()
	{
		return Constants.GROUND_LEVEL;
	}

	@Override
	public int levelTo()
	{
		return Constants.GROUND_LEVEL;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Remove;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet ludemeConcept = (movesLudeme != null) ? movesLudeme.concepts(context.game()) : new BitSet();
		final BitSet concepts = new BitSet();

		final int contId = type.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state().containerStates()[contId];
		final int what = cs.what(to, type);

		if (what != 0)
		{
			if (isDecision())
				concepts.set(Concept.RemoveDecision.id(), true);
			else
				concepts.set(Concept.RemoveEffect.id(), true);

			if (ludemeConcept.get(Concept.ReplacementCapture.id()))
				concepts.set(Concept.ReplacementCapture.id(), true);

			if (ludemeConcept.get(Concept.HopCapture.id()))
				concepts.set(Concept.HopCapture.id(), true);

			if (ludemeConcept.get(Concept.DirectionCapture.id()))
				concepts.set(Concept.DirectionCapture.id(), true);

			if (ludemeConcept.get(Concept.EncloseCapture.id()))
				concepts.set(Concept.EncloseCapture.id(), true);

			if (ludemeConcept.get(Concept.CustodialCapture.id()))
				concepts.set(Concept.CustodialCapture.id(), true);

			if (ludemeConcept.get(Concept.InterveneCapture.id()))
				concepts.set(Concept.InterveneCapture.id(), true);

			if (ludemeConcept.get(Concept.SurroundCapture.id()))
				concepts.set(Concept.SurroundCapture.id(), true);

			if (ludemeConcept.get(Concept.CaptureSequence.id()))
				concepts.set(Concept.CaptureSequence.id(), true);

			if (ludemeConcept.get(Concept.SowCapture.id()))
				concepts.set(Concept.SowCapture.id(), true);
		}

		return concepts;
	}
}
