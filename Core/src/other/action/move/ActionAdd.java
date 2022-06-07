package other.action.move;

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
 * Add one or more piece(s) to a site.
 *
 * @author Eric.Piette
 */
public final class ActionAdd extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The type of the graph element. */
	private SiteType type;

	/** Location index. */
	private final int to;

	/** Item index */
	private final int what;

	/** Count. */
	private final int count;

	/** The site state. */
	private final int state;

	/** The rotation state. */
	private final int rotation;

	/** The value of the piece. */
	private final int value;

	/** True if the pieces have to be added in a stack. */
	private final boolean onStack;

	/** The level to add the piece in case of a stack. */
	private int level = Constants.UNDEFINED;

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
	
	private boolean actionLargePiece = false;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type       The type of the graph element.
	 * @param to         The site to add component to.
	 * @param what       The index of the item.
	 * @param count      The number of item to place.
	 * @param state      The state of the site to modify.
	 * @param rotation   The rotation state of the site.
	 * @param value       The piece value of the site.
	 * @param onStacking True we add the pieces in a stack.
	 */
	public ActionAdd
	(
		final SiteType type, 
		final int to, 
		final int what, 
		final int count, 
		final int state, 
		final int rotation,
		final int value,
		final Boolean onStacking
	)
	{
		this.to = to;
		this.what = what;
		this.count = count;
		this.state = state;
		this.rotation = rotation;
		this.onStack = (onStacking == null) ? false : onStacking.booleanValue();
		this.type = type;
		this.value = value;
	}

	/**
	 * Reconstructs an ActionAdd object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionAdd(final String detailedString)
	{
		assert (detailedString.startsWith("[Add:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = (strLevel.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevel);

		final String strWhat = Action.extractData(detailedString, "what");
		what = Integer.parseInt(strWhat);

		final String strCount = Action.extractData(detailedString, "count");
		count = (strCount.isEmpty()) ? 1 : Integer.parseInt(strCount);

		final String strState = Action.extractData(detailedString, "state");
		state = (strState.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strState);

		final String strRotation = Action.extractData(detailedString, "rotation");
		rotation = (strRotation.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strRotation);

		final String strValue = Action.extractData(detailedString, "value");
		value = (strValue.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strValue);

		final String strStack = Action.extractData(detailedString, "stack");
		onStack = (strStack.isEmpty()) ? false : Boolean.parseBoolean(strStack);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if (to < 0)
			return this;

		type = (type == null) ? context.board().defaultSite() : type;
		
		// If the site is not supported by the type, that's a cell of another container.
		if (to >= context.board().topology().getGraphElements(type).size())
			type = SiteType.Cell;

		final Game game = context.game();
		final int contID = (type == SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state().containerStates()[contID];
		final int who = (what < 1) ? 0 : context.components()[what].owner();
		final boolean requiresStack = game.isStacking();
		final boolean hiddenInfoGame = game.hiddenInformation();
		
		// Keep in memory the data of the site from and to (for undo method)
		if (!alreadyApplied)
		{
			if (!requiresStack)
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
				
				if (hiddenInfoGame)
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
				for (int lvl = 0 ; lvl < sizeStackTo; lvl++)
				{
					previousWhat[lvl] = cs.what(to, lvl, type);
					previousWho[lvl] = cs.who(to, lvl, type);
					previousState[lvl] = cs.state(to, lvl, type);
					previousRotation[lvl] = cs.rotation(to, lvl, type);
					previousValue[lvl] = cs.value(to, lvl, type);
					
					if (hiddenInfoGame)
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
		
		if (requiresStack)
			applyStack(context, cs);

		int currentWhat = 0;
		currentWhat = cs.what(to, type);

		if (currentWhat == 0)
		{
			cs.setSite(context.state(), to, who, what, count, state, rotation, (game.hasDominoes() ? 1 : value), type);
			Component piece = null;
			// to keep the site of the item in cache for each player
			if (what != 0)
			{
				piece = context.components()[what];
				final int owner = piece.owner();
				context.state().owned().add(owner, what, to, type);
				if (piece.isDomino())
					context.state().remainingDominoes().remove(piece.index());
			}

			// If large piece we need to update the other sites used by the large piece.
			applyLargePiece(context, piece, cs);
		}
		else
		{
			final int oldCount = cs.count(to, type);
			cs.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, (game.requiresCount() ? oldCount + count : 1), state, rotation, value,type);
		}
		
		updateTrackIndices(context);
		
		return this;
	}

	/**
	 * Add a piece to a stack.
	 * 
	 * @param context The context.
	 * @param cs      The container state.
	 */
	public void applyStack(final Context context, final ContainerState cs)
	{
		final int who = (what < 1) ? 0 : context.components()[what].owner();
		level = cs.sizeStack(to, type);
		Component piece = null;
		if (state != Constants.UNDEFINED || rotation != Constants.UNDEFINED || value != Constants.UNDEFINED)
		{
			cs.addItemGeneric(context.state(), to, what, who, (state == Constants.UNDEFINED) ? 0 : state,
					(rotation == Constants.UNDEFINED) ? 0 : rotation, (value == Constants.UNDEFINED) ? 0 : value, context.game(), type);
		}
		else
		{
			cs.addItemGeneric(context.state(), to, what, who, context.game(), type);
		}

		cs.removeFromEmpty(to, type); 

		if (what != 0)
		{
			piece = context.components()[what];
			final int owner = piece.owner();
			context.state().owned().add(owner, what, to, cs.sizeStack(to, type) - 1, type);
		}

		updateTrackIndices(context);
	}

	/**
	 * We update the state in case of a large piece.
	 * 
	 * @param context The context.
	 * @param piece   The piece.
	 * @param cs      The container state.
	 */
	public void applyLargePiece(final Context context, final Component piece, final ContainerState cs)
	{
		actionLargePiece = true;
		if (piece != null && piece.isLargePiece() && to < context.containers()[0].numSites())
		{
			final Component largePiece = piece;
			final TIntArrayList locs = largePiece.locs(context, to, state, context.topology());

			for (int i = 0; i < locs.size(); i++)
			{
				cs.removeFromEmpty(locs.getQuick(i), SiteType.Cell);
				cs.setCount(context.state(), locs.getQuick(i), 1);
			}

			if (largePiece.isDomino())
			{
				for (int i = 0; i < 4; i++)
				{
					cs.setValueCell(context.state(), locs.getQuick(i), largePiece.getValue());
					cs.setPlayable(context.state(), locs.getQuick(i), false);
				}

				for (int i = 4; i < 8; i++)
				{
					cs.setValueCell(context.state(), locs.getQuick(i), largePiece.getValue2());
					cs.setPlayable(context.state(), locs.getQuick(i), false);
				}
			}
		}
	}
	
	/**
	 * To update the track indices.
	 * 
	 * @param context The context.
	 */
	public void updateTrackIndices(final Context context)
	{
		final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
		if (onTrackIndices != null)
		{
			for (final Track track : context.board().tracks())
			{
				final int trackIdx = track.trackIdx();
				final TIntArrayList indices = onTrackIndices.locToIndex(trackIdx, to);

				if (indices.size() > 0)
					onTrackIndices.add(trackIdx, what, count, indices.getQuick(0));
			}
		}
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		final Game game = context.game();
		final int contIdTo = type.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		final State gameState = context.state();
		final boolean hiddenInfoGame = game.hiddenInformation();
		
		if (actionLargePiece)
		{
			int pieceIdx = 0;
			pieceIdx = csTo.remove(context.state(), to, type);
			Component piece = context.components()[pieceIdx];
			undoLargePiece(context, piece, csTo);
		}
		else
		{
			final boolean requiresStack = context.currentInstanceContext().game().isStacking();
			final int sizeStackTo = csTo.sizeStack(to, type);
			
			if (requiresStack) // Stacking undo.
			{
				// We restore the to site
				for (int lvl = sizeStackTo -1 ; lvl >= 0; lvl--)
					csTo.remove(context.state(), to, lvl, type);
				
				for (int lvl = 0 ; lvl < previousWhat.length; lvl++)
				{
					csTo.addItemGeneric(gameState, to, previousWhat[lvl], previousWho[lvl], previousState[lvl], previousRotation[lvl], previousValue[lvl], game, type);
					if (hiddenInfoGame)
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
				
				if (hiddenInfoGame)
				{
					if (previousHidden.length > 0)
					{
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
			}
		}
		
		if (csTo.sizeStack(to, type) == 0)
			csTo.addToEmpty(to, type);
		
		return this;
	}
	
	/**
	 * We undo the state in case of a large piece.
	 * 
	 * @param context The context.
	 * @param piece   The piece.
	 * @param cs      The container state.
	 */
	public void undoLargePiece(final Context context, final Component piece, final ContainerState cs)
	{
		if (piece != null && piece.isLargePiece() && to < context.containers()[0].numSites())
		{
			final Component largePiece = piece;
			final TIntArrayList locs = largePiece.locs(context, to, state, context.topology());

			for (int i = 0; i < locs.size(); i++)
			{
				cs.addToEmpty(locs.getQuick(i), SiteType.Cell);
				cs.setCount(context.state(), locs.getQuick(i), 0);
			}

			if (largePiece.isDomino())
			{
				for (int i = 0; i < 4; i++)
				{
					cs.setValueCell(context.state(), locs.getQuick(i), 0);
					//cs.setPlayable(context.state(), locs.getQuick(i), false);
				}

				for (int i = 4; i < 8; i++)
				{
					cs.setValueCell(context.state(), locs.getQuick(i), 0);
					//cs.setPlayable(context.state(), locs.getQuick(i), false);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Add:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		if (level != Constants.UNDEFINED)
			sb.append(",level=" + level);

		sb.append(",what=" + what);

		if (count > 1)
			sb.append(",count=" + count);

		if (state != Constants.UNDEFINED)
			sb.append(",state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(",rotation=" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(",value=" + value);

		if (onStack)
			sb.append(",stack=" + onStack);

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
		result = prime * result + (onStack ? 1231 : 1237);
		result = prime * result + state;
		result = prime * result + rotation;
		result = prime * result + value;
		result = prime * result + what;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionAdd))
			return false;

		final ActionAdd other = (ActionAdd) obj;

		return (count == other.count &&
				decision == other.decision &&
				to == other.to &&
				onStack == other.onStack &&
				state == other.state &&
				rotation == other.rotation &&
				value == other.value &&
				what == other.what && type == other.type);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "Add";
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

		if (onStack)
			sb.append("^");
		else
			sb.append("+");

		if (what > 0 && what < context.components().length)
		{
			sb.append(context.components()[what].name());
			if (count > 1)
				sb.append("x" + count);
		}

		if (state != Constants.UNDEFINED)
			sb.append("=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" r" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(" v" + value);

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

		if (level != Constants.UNDEFINED)
			sb.append("/" + level);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" rotation=" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(" value=" + value);

		if (onStack)
			sb.append(" on stack");

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
	public int what()
	{
		return what;
	}

	@Override
	public int state()
	{
		if (state == Constants.UNDEFINED)
			return Constants.DEFAULT_STATE;

		return state;
	}

	@Override
	public int rotation()
	{
		if (rotation == Constants.UNDEFINED)
			return Constants.DEFAULT_ROTATION;

		return rotation;
	}

	@Override
	public int value()
	{
		if (value == Constants.UNDEFINED)
			return Constants.DEFAULT_VALUE;

		return value;
	}

	@Override
	public int count()
	{
		return count;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Add;
	}

	@Override
	public boolean isStacking()
	{
		return onStack;
	}

	@Override
	public void setLevelFrom(final int level)
	{
		this.level = level;
	}

	@Override
	public void setLevelTo(final int level)
	{
		this.level = level;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet ludemeConcept = (movesLudeme != null) ? movesLudeme.concepts(context.game()) : new BitSet();

		final BitSet concepts = new BitSet();

		// ---- Shoot concepts
		if (ludemeConcept.get(Concept.ShootDecision.id()))
			concepts.set(Concept.ShootDecision.id(), true);

		if (ludemeConcept.get(Concept.ShootEffect.id()))
			concepts.set(Concept.ShootEffect.id(), true);

		// ---- Take Control concepts

		if (ludemeConcept.get(Concept.TakeControl.id()))
			concepts.set(Concept.TakeControl.id(), true);
		
		// ---- Push concepts

		if (ludemeConcept.get(Concept.PushEffect.id()))
			concepts.set(Concept.PushEffect.id(), true);

		// ---- Add concepts

		if (concepts.isEmpty())
		{
			if (decision)
				concepts.set(Concept.AddDecision.id(), true);
			else
				concepts.set(Concept.AddEffect.id(), true);
		}

		return concepts;
	}

}
