package other.action.move;

import java.util.BitSet;

import game.equipment.component.Component;
import game.equipment.container.board.Track;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.stacking.BaseContainerStateStacking;
import other.state.track.OnTrackIndices;

/**
 * Removes one or more component(s) from a location.
 *
 * @author Eric.Piette
 */
public final class ActionRemove extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location where to remove the component(s). */
	private final int to;
	
	/** Level where to remove the component(s). */
	private final int level;

	/** Useful in case of remove all a sequence (e.g. draughts) */
	private final boolean applied;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type    The graph element type.
	 * @param to      Location to remove the component(s).
	 * @param level   Level to remove the component(s).
	 * @param applied True if the action has to be applied immediately.
	 */
	public ActionRemove
	(
		final SiteType type,
		final int to, 
		final int level, 
		final boolean applied
	)
	{
		this.to = to;
		this.level = level;
		this.applied = applied;
		this.type = type;
	}

	/**
	 * Reconstructs a ActionRemove object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionRemove(final String detailedString)
	{
		assert (detailedString.startsWith("[Remove:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = (strLevel.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevel);

		final String strApplied = Action.extractData(detailedString, "applied");
		applied = (strApplied.isEmpty()) ? true : Boolean.parseBoolean(strApplied);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = to >= context.containerId().length ? 0 : context.containerId()[to];
		final int site = to;
				
		// For the capture sequence mechanism.
		if (!applied)
		{
			context.state().addPieceToRemove(site);
			return this;
		}
		
		final ContainerState cs = context.state().containerStates()[contID];
		
		final int pieceIdx = (level == Constants.UNDEFINED) ? cs.remove(context.state(), site, type)
				: cs.remove(context.state(), site, level, type);
		
		if (context.game().isStacking())
		{
			final BaseContainerStateStacking csStack = (BaseContainerStateStacking) cs;
			if (pieceIdx > 0)
			{
				final Component piece = context.components()[pieceIdx];
				final int owner = piece.owner();
				context.state().owned().remove(owner, pieceIdx, site,
						(level == Constants.UNDEFINED) ? csStack.sizeStack(site, type) : level, type);
			}

			if (csStack.sizeStack(site, type) == 0)
				csStack.addToEmpty(site, type);
		}
		else
		{
			if (pieceIdx > 0)
			{
				final Component piece = context.components()[pieceIdx];
				final int owner = piece.owner();
				context.state().owned().remove(owner, pieceIdx, site, type);
			}
		}

		if (pieceIdx > 0)
		{
			// We update the structure about track indices if the game uses track.
			final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
			if (onTrackIndices != null)
			{
				for (final Track track : context.board().tracks())
				{
					final int trackIdx = track.trackIdx();
					final TIntArrayList indices = onTrackIndices.locToIndex(trackIdx, site);

					for (int i = 0; i < indices.size(); i++)
						onTrackIndices.remove(trackIdx, pieceIdx, 1, indices.getQuick(i));
				}
			}
		}

		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + (applied ? 1231 : 1237);
		result = prime * result + to;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionRemove))
			return false;

		final ActionRemove other = (ActionRemove) obj;
		return (decision == other.decision &&
				applied == other.applied &&
				to == other.to && type == other.type);
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

		if (level != Constants.UNDEFINED)
			sb.append(",level=" + level);

		if (!applied)
			sb.append(",applied=" + applied);
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

		if (level != Constants.UNDEFINED)
			sb.append("/" + level);

		sb.append("-");

		if (!applied)
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

		if (level != Constants.UNDEFINED)
			sb.append("/" + level);

		if (!applied)
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
		return ActionType.Remove;
	}

	// -------------------------------------------------------------------------

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
