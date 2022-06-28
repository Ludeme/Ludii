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
import other.state.State;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;

/**
 * Removes one or more component(s) from a location (always the top piece).
 *
 * @author Eric.Piette
 */
public class ActionRemoveTopPiece extends BaseAction
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
	
	/** Previous What value of the site. */
	private int[] previousWhat;
	
	/** Previous Who value of the site. */
	private int[] previousWho;
	
	/** Previous Site state value of the site. */
	private int[] previousState;

	/** Previous Rotation value of the site. */
	private int[] previousRotation;

	/** Previous Piece value of the site. */
	private int[] previousValue;

	/** Previous Piece count of the site. */
	private int previousCount;

	/** The previous hidden info values of the site before to be removed. */
	private boolean[][] previousHidden;
	
	/** The previous hidden what info values of the site before to be removed. */
	private boolean[][] previousHiddenWhat;
	
	/** The previous hidden who info values of the site before to be removed. */
	private boolean[][] previousHiddenWho;

	/** The previous hidden count info values of the site before to be removed. */
	private boolean[][] previousHiddenCount;

	/** The previous hidden rotation info values of the site before to be removed. */
	private boolean[][] previousHiddenRotation;

	/** The previous hidden State info values of the site before to be removed. */
	private boolean[][] previousHiddenState;

	/** The previous hidden Value info values of the site before to be removed. */
	private boolean[][] previousHiddenValue;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type    The graph element type.
	 * @param to      Location to remove the component(s).
	 */
	public ActionRemoveTopPiece
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
		final boolean requiresStack = game.isStacking();
		
		// Keep in memory the data of the site from and to (for undo method)
		if(!alreadyApplied)
		{
			if(!requiresStack)
			{
				previousCount = cs.count(to, type);
				previousWhat = new int[1];
				previousWho = new int[1];
				previousState = new int[1];
				previousRotation = new int[1];
				previousValue = new int[1];
				previousWhat[0] = cs.what(to, 0, type);
				previousWho[0] = cs.who(to, 0, type);
				previousState[0] = cs.state(to, 0, type);
				previousRotation[0] = cs.rotation(to, 0, type);
				previousValue[0] = cs.value(to, 0, type);
				
				if(context.game().hiddenInformation())
				{
					previousHidden = new boolean[1][context.players().size()];
					previousHiddenWhat = new boolean[1][context.players().size()];
					previousHiddenWho = new boolean[1][context.players().size()];
					previousHiddenCount = new boolean[1][context.players().size()];
					previousHiddenRotation = new boolean[1][context.players().size()];
					previousHiddenState = new boolean[1][context.players().size()];
					previousHiddenValue = new boolean[1][context.players().size()];
					
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHidden[0][pid] = cs.isHidden(pid, to, 0, type);
						previousHiddenWhat[0][pid] = cs.isHiddenWhat(pid, to, 0, type);
						previousHiddenWho[0][pid] = cs.isHiddenWho(pid, to, 0, type);
						previousHiddenCount[0][pid] = cs.isHiddenCount(pid, to, 0, type);
						previousHiddenState[0][pid] = cs.isHiddenState(pid, to, 0, type);
						previousHiddenRotation[0][pid] = cs.isHiddenRotation(pid, to, 0, type);
						previousHiddenValue[0][pid] = cs.isHiddenValue(pid, to, 0, type);
					}
				}
			}
			else // Stacking game.
			{
				final int sizeStackTo = cs.sizeStack(to, type);
				previousWhat = new int[sizeStackTo];
				previousWho = new int[sizeStackTo];
				previousState = new int[sizeStackTo];
				previousRotation = new int[sizeStackTo];
				previousValue = new int[sizeStackTo];
				for(int lvl = 0 ; lvl < sizeStackTo; lvl++)
				{
					previousWhat[lvl] = cs.what(to, lvl, type);
					previousWho[lvl] = cs.who(to, lvl, type);
					previousState[lvl] = cs.state(to, lvl, type);
					previousRotation[lvl] = cs.rotation(to, lvl, type);
					previousValue[lvl] = cs.value(to, lvl, type);
					
					if(context.game().hiddenInformation())
					{
						previousHidden = new boolean[sizeStackTo][context.players().size()];
						previousHiddenWhat = new boolean[sizeStackTo][context.players().size()];
						previousHiddenWho = new boolean[sizeStackTo][context.players().size()];
						previousHiddenCount = new boolean[sizeStackTo][context.players().size()];
						previousHiddenRotation = new boolean[sizeStackTo][context.players().size()];
						previousHiddenState = new boolean[sizeStackTo][context.players().size()];
						previousHiddenValue = new boolean[sizeStackTo][context.players().size()];
						for (int pid = 1; pid < context.players().size(); pid++)
						{
							previousHidden[lvl][pid] = cs.isHidden(pid, to, lvl, type);
							previousHiddenWhat[lvl][pid] = cs.isHiddenWhat(pid, to, lvl, type);
							previousHiddenWho[lvl][pid] = cs.isHiddenWho(pid, to, lvl, type);
							previousHiddenCount[lvl][pid] = cs.isHiddenCount(pid, to, lvl, type);
							previousHiddenState[lvl][pid] = cs.isHiddenState(pid, to, lvl, type);
							previousHiddenRotation[lvl][pid] = cs.isHiddenRotation(pid, to, lvl, type);
							previousHiddenValue[lvl][pid] = cs.isHiddenValue(pid, to, lvl, type);
						}
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
	public Action undo(final Context context, boolean discard)
	{
		final Game game = context.game();
		final int contIdTo = type.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		final State gameState = context.state();
		
		final boolean requiresStack = context.currentInstanceContext().game().isStacking();
		final int sizeStackTo = csTo.sizeStack(to, type);
			
		if(requiresStack) // Stacking undo.
		{
			// We restore the to site
			for(int lvl = sizeStackTo -1 ; lvl >= 0; lvl--)
				csTo.remove(context.state(), to, lvl, type);
				
			for(int lvl = 0 ; lvl < previousWhat.length; lvl++)
			{
				csTo.addItemGeneric(gameState, to, previousWhat[lvl], previousWho[lvl], previousState[lvl], previousRotation[lvl], previousValue[lvl], game, type);
				if(context.game().hiddenInformation())
				{
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						csTo.setHidden(gameState, pid, to, lvl, type, previousHidden[lvl][pid]);
						csTo.setHiddenWhat(gameState, pid, to, lvl, type, previousHiddenWhat[lvl][pid]);
						csTo.setHiddenWho(gameState, pid, to, lvl, type, previousHiddenWho[lvl][pid]);
						csTo.setHiddenCount(gameState, pid, to, lvl, type, previousHiddenCount[lvl][pid]);
						csTo.setHiddenState(gameState, pid, to, lvl, type, previousHiddenState[lvl][pid]);
						csTo.setHiddenRotation(gameState, pid, to, lvl, type, previousHiddenRotation[lvl][pid]);
						csTo.setHiddenValue(gameState, pid, to, lvl, type, previousHiddenValue[lvl][pid]);
					}
				}
			}
		}
		else // Non stacking undo.
		{
			csTo.remove(context.state(), to, type);
			csTo.setSite(context.state(), to, previousWho[0], previousWhat[0], previousCount, previousState[0], previousRotation[0], previousValue[0], type);
			
			if(context.game().hasDominoes())
			{
				if (previousWhat.length > 0 && previousWhat[0] != 0)
				{
					Component piece = context.components()[previousWhat[0]];
					if (piece.isDomino())
						context.state().remainingDominoes().remove(piece.index());
				}
			}
			
			if(context.game().hiddenInformation())
			{
				if(previousHidden.length > 0)
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						csTo.setHidden(gameState, pid, to, 0, type, previousHidden[0][pid]);
						csTo.setHiddenWhat(gameState, pid, to, 0, type, previousHiddenWhat[0][pid]);
						csTo.setHiddenWho(gameState, pid, to, 0, type, previousHiddenWho[0][pid]);
						csTo.setHiddenCount(gameState, pid, to, 0, type, previousHiddenCount[0][pid]);
						csTo.setHiddenState(gameState, pid, to, 0, type, previousHiddenState[0][pid]);
						csTo.setHiddenRotation(gameState, pid, to, 0, type, previousHiddenRotation[0][pid]);
						csTo.setHiddenValue(gameState, pid, to, 0, type, previousHiddenValue[0][pid]);
					}
			}
		}
			
		if (csTo.sizeStack(to, type) != 0)
			csTo.removeFromEmpty(to, type);
		
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

		if (!(obj instanceof ActionRemoveTopPiece))
			return false;

		final ActionRemoveTopPiece other = (ActionRemoveTopPiece) obj;
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
			
			if (ludemeConcept.get(Concept.SowRemove.id()))
				concepts.set(Concept.SowRemove.id(), true);

			if (ludemeConcept.get(Concept.PushEffect.id()))
				concepts.set(Concept.PushEffect.id(), true);
		}

		return concepts;
	}
}
