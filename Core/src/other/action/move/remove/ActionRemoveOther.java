package other.action.move.remove;

import java.util.BitSet;

import game.Game;
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
import other.state.track.OnTrackIndices;

/**
 * Removes one or more component(s) from a location.
 *
 * @author Eric.Piette
 */
public class ActionRemoveOther extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location where to remove the component(s). */
	private final int to;
	
	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous index of the piece before to be removed. */
	private int previousWhat;
	
	/** The previous owner of the piece before to be removed. */
	private int previousWho;
	
	/** The previous count of the piece before to be removed. */
	private int previousCount;
	
	/** The previous state value of the piece before to be removed. */
	private int previousState;

	/** The previous rotation value of the piece before to be removed. */
	private int previousRotation;

	/** The previous value of the piece before to be removed. */
	private int previousValue;

	/** The previous hidden info values of the piece before to be removed. */
	private boolean[] previousHidden;
	
	/** The previous hidden what info values of the piece before to be removed. */
	private boolean[] previousHiddenWhat;
	
	/** The previous hidden who info values of the piece before to be removed. */
	private boolean[] previousHiddenWho;

	/** The previous hidden count info values of the piece before to be removed. */
	private boolean[] previousHiddenCount;

	/** The previous hidden rotation info values of the piece before to be removed. */
	private boolean[] previousHiddenRotation;

	/** The previous hidden State info values of the piece before to be removed. */
	private boolean[] previousHiddenState;

	/** The previous hidden Value info values of the piece before to be removed. */
	private boolean[] previousHiddenValue;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type    The graph element type.
	 * @param to      Location to remove the component(s).
	 */
	public ActionRemoveOther
	(
		final SiteType type,
		final int to 
	)
	{
		this.to = to;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		final Game game = context.game();
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = to >= context.containerId().length ? 0 : context.containerId()[to];
		final ContainerState cs = context.state().containerStates()[contID];
		
		// Undo save data before to remove.
		if(!alreadyApplied)
		{
			if (context.game().isStacking())
			{
				final int levelToRemove = ((cs.sizeStack(to, type) == 0) ? 0 : cs.sizeStack(to, type) -1);
				
				previousWhat = cs.what(to, levelToRemove, type);
				previousWho = cs.who(to, levelToRemove, type);
				previousState = cs.state(to, levelToRemove, type);
				previousRotation = cs.rotation(to, levelToRemove, type);
				previousValue = cs.value(to, levelToRemove, type);
				
				if(game.hiddenInformation())
				{
					previousHidden = new boolean[context.players().size()];
					previousHiddenWhat = new boolean[context.players().size()];
					previousHiddenWho =  new boolean[context.players().size()];
					previousHiddenCount =  new boolean[context.players().size()];
					previousHiddenState =  new boolean[context.players().size()];
					previousHiddenRotation =  new boolean[context.players().size()];
					previousHiddenValue =  new boolean[context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHidden[pid] = cs.isHidden(pid, to, levelToRemove, type);
						previousHiddenWhat[pid] = cs.isHiddenWhat(pid, to, levelToRemove, type);
						previousHiddenWho[pid] = cs.isHiddenWho(pid, to, levelToRemove, type);
						previousHiddenCount[pid] = cs.isHiddenCount(pid, to, levelToRemove, type);
						previousHiddenState[pid] = cs.isHiddenState(pid, to, levelToRemove, type);
						previousHiddenRotation[pid] = cs.isHiddenRotation(pid, to, levelToRemove, type);
						previousHiddenValue[pid] = cs.isHiddenValue(pid, to, levelToRemove, type);
					}
				}
			}
			else
			{
				previousWhat = cs.what(to, type);
				previousWho = cs.who(to, type);
				previousCount = cs.count(to, type);
				previousState = cs.state(to, type);
				previousRotation = cs.rotation(to, type);
				previousValue = cs.value(to, type);
		
				if(game.hiddenInformation())
				{
					previousHidden = new boolean[context.players().size()];
					previousHiddenWhat = new boolean[context.players().size()];
					previousHiddenWho =  new boolean[context.players().size()];
					previousHiddenCount =  new boolean[context.players().size()];
					previousHiddenState =  new boolean[context.players().size()];
					previousHiddenRotation =  new boolean[context.players().size()];
					previousHiddenValue =  new boolean[context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHidden[pid] = cs.isHidden(pid, to, 0, type);
						previousHiddenWhat[pid] = cs.isHiddenWhat(pid, to, 0, type);
						previousHiddenWho[pid] = cs.isHiddenWho(pid, to, 0, type);
						previousHiddenCount[pid] = cs.isHiddenCount(pid, to, 0, type);
						previousHiddenState[pid] = cs.isHiddenState(pid, to, 0, type);
						previousHiddenRotation[pid] = cs.isHiddenRotation(pid, to, 0, type);
						previousHiddenValue[pid] = cs.isHiddenValue(pid, to, 0, type);
					}
				}
			}
			alreadyApplied = true;
		}
		
		// We remove the piece.
		final int pieceIdx = cs.remove(context.state(), to, type);
		
		// We update the owned structure and the empty bitsets.
		if (context.game().isStacking()) 
		{
			if (pieceIdx > 0)
			{
				final int levelRemoved = cs.sizeStack(to, type);
				final Component piece = context.components()[pieceIdx];
				final int owner = piece.owner();
				context.state().owned().remove(owner, pieceIdx, to, levelRemoved, type);
			}

			if (cs.sizeStack(to, type) == 0)
				cs.addToEmpty(to, type);
		}
		else
		{
			if (pieceIdx > 0)
			{
				final Component piece = context.components()[pieceIdx];
				final int owner = piece.owner();
				context.state().owned().remove(owner, pieceIdx, to, type);
			}
		}

		// We update the structure about track indices if the game uses track.
		if (pieceIdx > 0)
		{
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

		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		final Game game = context.game();
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = to >= context.containerId().length ? 0 : context.containerId()[to];
		final ContainerState cs = context.state().containerStates()[contID];
		
		if (context.game().isStacking()) // We re-insert the removed piece.
		{
			if (previousState != Constants.UNDEFINED || previousRotation != Constants.UNDEFINED || previousValue != Constants.UNDEFINED)
			{
				final int undoStateValue = (previousState == Constants.UNDEFINED) ? 0 : previousState;
				final int undoRotationValue = (previousRotation == Constants.UNDEFINED) ? 0 : previousRotation;
				final int undoValueValue = (previousValue == Constants.UNDEFINED) ? 0 : previousValue;
				cs.addItemGeneric(context.state(), to, previousWhat, previousWho, undoStateValue, undoRotationValue, undoValueValue, context.game(), type);
			}
			else
				cs.addItemGeneric(context.state(), to, previousWhat, previousWho, context.game(), type);

			cs.removeFromEmpty(to, type);
		}
		else
		{
			int currentWhat = 0;
			currentWhat = cs.what(to, type);

			if (currentWhat == 0) // We re-add the piece.
			{
				final int undoValueValue = (context.game().hasDominoes() ? 1 : previousValue);
				cs.setSite(context.state(), to, previousWho, previousWhat, previousCount, previousState, previousRotation, undoValueValue, type);
				
				if(context.game().hasDominoes())
				{
					if (previousWhat != 0)
					{
						Component piece = context.components()[previousWhat];
						if (piece.isDomino())
							context.state().remainingDominoes().remove(piece.index());
					}
				}
			}
			else // We update the count at the previous value.
			{
				final int oldCount = cs.count(to, type);
				final int undoValueValue = (context.game().hasDominoes() ? oldCount : previousValue);
				cs.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, undoValueValue, previousState, previousRotation, previousValue, type);
			}
			
			if(game.hiddenInformation())
			{
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					cs.setHidden(context.state(), pid, to, 0, type, previousHidden[pid]);
					cs.setHiddenWhat(context.state(), pid, to, 0, type, previousHiddenWhat[pid]);
					cs.setHiddenWho(context.state(), pid, to, 0, type, previousHiddenWho[pid]);
					cs.setHiddenCount(context.state(), pid, to, 0, type, previousHiddenCount[pid]);
					cs.setHiddenState(context.state(), pid, to, 0, type, previousHiddenState[pid]);
					cs.setHiddenRotation(context.state(), pid, to, 0, type, previousHiddenRotation[pid]);
					cs.setHiddenValue(context.state(), pid, to, 0, type, previousHiddenValue[pid]);
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
		result = prime * result + 1231;
		result = prime * result + to;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionRemoveOther))
			return false;

		final ActionRemoveOther other = (ActionRemoveOther) obj;
		return (decision == other.decision &&
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
