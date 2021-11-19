package other.action.move.move;

import java.util.BitSet;
import java.util.List;

import game.equipment.component.Component;
import game.equipment.container.board.Track;
import game.rules.play.moves.Moves;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Moves a full stack from a site to another.
 *
 * @author Eric.Piette
 */
public final class ActionMoveStacking extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type of the from site. */
	private final SiteType typeFrom;

	/** From site index. */
	private final int from;

	/** From level index (e.g. stacking game). */
	private int levelFrom;

	/** The graph element type of the to site. */
	private final SiteType typeTo;

	/** To site index. */
	private final int to;

	/** To level index (e.g. stacking game). */
	private final int levelTo;

	/** Site state value of the to site. */
	private final int state;

	/** Rotation value of the to site. */
	private final int rotation;

	/** piece value of the to site. */
	private final int value;

	//----------------------Undo Data---------------------------------------------

	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;

	//-- from data
	
	/** Previous Size stack of the from site. */
	private int previousSizeStackFrom;
	
	/** Previous Site state value of the from site. */
	private int previousStateFrom;

	/** Previous Rotation value of the from site. */
	private int previousRotationFrom;

	/** Previous Piece value of the from site. */
	private int previousValueFrom;

	/** The previous hidden info values of the from site before to be removed. */
	private boolean[] previousHiddenFrom;
	
	/** The previous hidden what info values of the from site before to be removed. */
	private boolean[] previousHiddenWhatFrom;
	
	/** The previous hidden who info values of the from site before to be removed. */
	private boolean[] previousHiddenWhoFrom;

	/** The previous hidden count info values of the from site before to be removed. */
	private boolean[] previousHiddenCountFrom;

	/** The previous hidden rotation info values of the from site before to be removed. */
	private boolean[] previousHiddenRotationFrom;

	/** The previous hidden State info values of the from site before to be removed. */
	private boolean[] previousHiddenStateFrom;

	/** The previous hidden Value info values of the from site before to be removed. */
	private boolean[] previousHiddenValueFrom;
	
	//--- to data
	
	/** The previous hidden info values of the to site before to be removed. */
	private boolean[] previousHiddenTo;
	
	/** The previous hidden what info values of the to site before to be removed. */
	private boolean[] previousHiddenWhatTo;
	
	/** The previous hidden who info values of the to site before to be removed. */
	private boolean[] previousHiddenWhoTo;

	/** The previous hidden count info values of the to site before to be removed. */
	private boolean[] previousHiddenCountTo;

	/** The previous hidden rotation info values of the to site before to be removed. */
	private boolean[] previousHiddenRotationTo;

	/** The previous hidden State info values of the to site before to be removed. */
	private boolean[] previousHiddenStateTo;

	/** The previous hidden Value info values of the to site before to be removed. */
	private boolean[] previousHiddenValueTo;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param typeFrom   The graph element type of the from site.
	 * @param from       From site index.
	 * @param levelFrom  From level index.
	 * @param typeTo     The graph element type of the to site.
	 * @param to         To site index.
	 * @param levelTo    To level index.
	 * @param state      The state site of the to site.
	 * @param rotation   The rotation value of the to site.
	 * @param value      The piece value of the to site.
	 */
	public ActionMoveStacking
	(
		final SiteType typeFrom,
		final int from,
		final int levelFrom,
		final SiteType typeTo,
		final int to,
		final int levelTo,
		final int state,
		final int rotation,
		final int value
	)
	{
		this.typeFrom = typeFrom;
		this.from = from;
		this.levelFrom = levelFrom;
		this.typeTo = typeTo;
		this.to = to;
		this.levelTo = levelTo;
		this.state = state;
		this.rotation = rotation;
		this.value = value;
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply
	(
		final Context context,
		final boolean store
	)
	{
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		
		final boolean requiresStack = context.currentInstanceContext().game().isStacking();
		
		int currentStateFrom = Constants.UNDEFINED;
		int currentRotationFrom = Constants.UNDEFINED;
		int currentValueFrom = Constants.UNDEFINED;

		final ContainerState csFrom = context.state().containerStates()[contIdFrom];
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		
		// take the local state of the site from
		currentStateFrom = (levelFrom == Constants.UNDEFINED) ? 
				((csFrom.what(from, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, typeFrom))
				: ((csFrom.what(from, levelFrom, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, levelFrom, typeFrom));
		
		currentRotationFrom = (levelFrom == Constants.UNDEFINED) ? csFrom.rotation(from, typeFrom) : csFrom.rotation(from, levelFrom, typeFrom);
		currentValueFrom =  (levelFrom == Constants.UNDEFINED) ? csFrom.value(from, typeFrom) : csFrom.value(from, levelFrom, typeFrom);
		
		// Keep in memory the data of the site from and to (for undo method)
		if(!alreadyApplied)
		{
			previousStateFrom = currentStateFrom;
			previousRotationFrom = currentRotationFrom;
			previousValueFrom = currentValueFrom;
			previousSizeStackFrom = csFrom.sizeStack(from, typeFrom);
			
			if (!requiresStack)
			{
				if(context.game().hiddenInformation())
				{
					previousHiddenFrom = new boolean[context.players().size()];
					previousHiddenWhatFrom = new boolean[context.players().size()];
					previousHiddenWhoFrom =  new boolean[context.players().size()];
					previousHiddenCountFrom =  new boolean[context.players().size()];
					previousHiddenStateFrom =  new boolean[context.players().size()];
					previousHiddenRotationFrom =  new boolean[context.players().size()];
					previousHiddenValueFrom =  new boolean[context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenFrom[pid] = csFrom.isHidden(pid, from, 0, typeFrom);
						previousHiddenWhatFrom[pid] = csFrom.isHiddenWhat(pid, from, 0, typeFrom);
						previousHiddenWhoFrom[pid] = csFrom.isHiddenWho(pid, from, 0, typeFrom);
						previousHiddenCountFrom[pid] = csFrom.isHiddenCount(pid, from, 0, typeFrom);
						previousHiddenStateFrom[pid] = csFrom.isHiddenState(pid, from, 0, typeFrom);
						previousHiddenRotationFrom[pid] = csFrom.isHiddenRotation(pid, from, 0, typeFrom);
						previousHiddenValueFrom[pid] = csFrom.isHiddenValue(pid, from, 0, typeFrom);
					}
					
					previousHiddenTo = new boolean[context.players().size()];
					previousHiddenWhatTo = new boolean[context.players().size()];
					previousHiddenWhoTo =  new boolean[context.players().size()];
					previousHiddenCountTo =  new boolean[context.players().size()];
					previousHiddenStateTo =  new boolean[context.players().size()];
					previousHiddenRotationTo =  new boolean[context.players().size()];
					previousHiddenValueTo =  new boolean[context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenTo[pid] = csTo.isHidden(pid, to, 0, typeTo);
						previousHiddenWhatTo[pid] = csTo.isHiddenWhat(pid, to, 0, typeTo);
						previousHiddenWhoTo[pid] = csTo.isHiddenWho(pid, to, 0, typeTo);
						previousHiddenCountTo[pid] = csTo.isHiddenCount(pid, to, 0, typeTo);
						previousHiddenStateTo[pid] = csTo.isHiddenState(pid, to, 0, typeTo);
						previousHiddenRotationTo[pid] = csTo.isHiddenRotation(pid, to, 0, typeTo);
						previousHiddenValueTo[pid] = csTo.isHiddenValue(pid, to, 0, typeTo);
					}
				}
			}
			alreadyApplied = true;
		}
		
		if(from == to)
			return this;
			
		final ContainerState containerFrom = context.state().containerStates()[contIdFrom];
		final ContainerState containerTo = context.state().containerStates()[contIdTo];

		final int sizeStackFrom = containerFrom.sizeStack(from, typeFrom);
		for (int slevel = 0; slevel < containerFrom.sizeStack(from, typeFrom); slevel++)
		{
			if (levelTo == Constants.UNDEFINED)
				containerTo.addItemGeneric(context.state(), to, containerFrom.what(from, slevel, typeFrom),
						containerFrom.who(from, slevel, typeFrom), containerFrom.state(from, slevel, typeFrom),
						containerFrom.rotation(from, slevel, typeFrom), containerFrom.value(from, slevel, typeFrom),
						context.game(), typeTo);
			else
			{
				containerTo.insert(context.state(), typeTo, to, levelTo, containerFrom.what(from, slevel, typeFrom),
						containerFrom.who(from, slevel, typeFrom), state, rotation, value,
						context.game());
			}
		}

		// we update owned for loc From.
		for (int level = 0; level < containerFrom.sizeStack(from, typeFrom); level++)
		{
			final int whatFrom = containerFrom.what(from, level, typeFrom);
			if (whatFrom != 0)
			{
				final Component pieceFrom = context.components()[whatFrom];
				final int ownerFrom = pieceFrom.owner();
				if (ownerFrom != 0)
					context.state().owned().remove(ownerFrom, whatFrom, from, typeFrom);
			}
		}
				
		containerFrom.removeStackGeneric(context.state(), from, typeFrom);
		containerFrom.addToEmpty(from, typeFrom);
		containerTo.removeFromEmpty(to, typeTo);

		// we update owned for loc To.
		for (int level = containerTo.sizeStack(to, typeTo) - sizeStackFrom; level < containerTo.sizeStack(to, typeTo); level++)
		{
			if (level < 0)
				continue;
			final int whatTo = containerTo.what(to, level, typeTo);
			if (whatTo != 0)
			{
				final Component pieceTo = context.components()[whatTo];
				final int ownerTo = pieceTo.owner();
				if (ownerTo != 0)
					context.state().owned().add(ownerTo, whatTo, to, level, typeTo);
			}
		}

		return this;
	}

	/**
	 * To update the onTrackIndices after a move.
	 * 
	 * @param what           The index of the component moved.
	 * @param onTrackIndices The structure onTrackIndices
	 * @param tracks         The list of the tracks.
	 */
	public void updateOnTrackIndices(final int what, final OnTrackIndices onTrackIndices, final List<Track> tracks)
	{
		// We update the structure about track indices if the game uses track.
		if (what != 0 && onTrackIndices != null)
		{
			for (final Track track : tracks)
			{
				final int trackIdx = track.trackIdx();
				final TIntArrayList indicesLocA = onTrackIndices.locToIndex(trackIdx, from);

				for (int k = 0; k < indicesLocA.size(); k++)
				{
					final int indexA = indicesLocA.getQuick(k);
					final int countAtIndex = onTrackIndices.whats(trackIdx, what, indicesLocA.getQuick(k));

					if (countAtIndex > 0)
					{
						onTrackIndices.remove(trackIdx, what, 1, indexA);
						final TIntArrayList newWhatIndice = onTrackIndices.locToIndexFrom(trackIdx, to, indexA);

						if (newWhatIndice.size() > 0)
						{
							onTrackIndices.add(trackIdx, what, 1, newWhatIndice.getQuick(0));
						}
						else
						{
							final TIntArrayList newWhatIndiceIfNotAfter = onTrackIndices.locToIndex(trackIdx, to);
							if (newWhatIndiceIfNotAfter.size() > 0)
								onTrackIndices.add(trackIdx, what, 1, newWhatIndiceIfNotAfter.getQuick(0));
						}

						break;
					}
				}

				// If the piece was not in the track but enter on it, we update the structure
				// corresponding to that track.
				if (indicesLocA.size() == 0)
				{
					final TIntArrayList indicesLocB = onTrackIndices.locToIndex(trackIdx, to);
					if (indicesLocB.size() != 0)
						onTrackIndices.add(trackIdx, what, 1, indicesLocB.get(0));
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		// In undo ActionMove from = to and to = from.
		
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		
		if(from == to)
			return this;
			
		final ContainerState containerTo = context.state().containerStates()[contIdTo];
		final ContainerState containerFrom = context.state().containerStates()[contIdFrom];
			
		final int stackSize = containerTo.sizeStack(to, typeTo);

		//final int sizeStackTo = containerTo.sizeStack(to, typeTo);
		for (int slevel = Math.abs(previousSizeStackFrom - stackSize) ; slevel < stackSize ; slevel++)
		{
			if (levelFrom == Constants.UNDEFINED)
					containerFrom.addItemGeneric(context.state(), from, containerTo.what(to, slevel, typeTo),
							containerTo.who(to, slevel, typeTo), containerTo.state(to, slevel, typeTo),
							containerTo.rotation(to, slevel, typeTo), containerTo.value(to, slevel, typeTo), context.game(), typeFrom);
				else
				{
					containerFrom.insert(context.state(), typeFrom, from, levelFrom, containerTo.what(to, slevel, typeTo),
							containerTo.who(to, slevel, typeTo), previousStateFrom, previousRotationFrom, previousValueFrom, context.game());
				}
		}
				
		for (int slevel = Math.abs(previousSizeStackFrom - stackSize) ; slevel < stackSize ; slevel++)
			containerTo.remove(context.state(), to, typeTo);
			
		if(containerTo.sizeStack(to, typeTo) == 0)
			containerTo.addToEmpty(to, typeTo);
	
		containerFrom.removeFromEmpty(from, typeFrom);
		
		return this;
	}

	//-------------------------------------------------------------------------
	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Move:");

		if (typeFrom != null || (context != null && typeFrom != context.board().defaultSite()))
		{
			sb.append("typeFrom=" + typeFrom);
			sb.append(",from=" + from);
		}
		else
			sb.append("from=" + from);

		if (levelFrom != Constants.UNDEFINED)
			sb.append(",levelFrom=" + levelFrom);

		if (typeTo != null || (context != null && typeTo != context.board().defaultSite()))
			sb.append(",typeTo=" + typeTo);

		sb.append(",to=" + to);

		if (levelTo != Constants.UNDEFINED)
			sb.append(",levelTo=" + levelTo);

		if (state != Constants.UNDEFINED)
			sb.append(",state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(",rotation=" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(",value=" + value);

		sb.append(",stack=" + true);
			
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
		result = prime * result + from;
		result = prime * result + levelFrom;
		result = prime * result + to;
		result = prime * result + levelTo;
		result = prime * result + state;
		result = prime * result + rotation;
		result = prime * result + value;
		result = prime * result + 1231;
		result = prime * result + ((typeFrom == null) ? 0 : typeFrom.hashCode());
		result = prime * result + ((typeTo == null) ? 0 : typeTo.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionMoveStacking))
			return false;

		final ActionMoveStacking other = (ActionMoveStacking) obj;

		return (decision == other.decision &&
				from == other.from &&
				levelFrom == other.levelFrom &&
				to == other.to &&
				levelTo == other.levelTo &&
				state == other.state &&
				rotation == other.rotation &&
				value == other.value &&
				typeFrom == other.typeFrom && typeTo == other.typeTo);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "Move";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

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

		if (levelFrom != Constants.UNDEFINED && context.game().isStacking())
			sb.append("/" + levelFrom);

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

		if (state != Constants.UNDEFINED)
			sb.append("=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" r" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(" v" + value);

		sb.append(" ^");

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Move ");

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

		String newTo = to + "";
		if (useCoords)
		{
			final int cid = (typeTo == SiteType.Cell
					|| typeTo == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[to]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeTo != null) ? typeTo : context.board().defaultSite();
				if (to < context.game().equipment().containers()[cid].topology().getGraphElements(realType).size())
					newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
							.label();
				else // The site is not existing.
					newTo = "??";
			}
		}

		if (typeFrom != null && typeTo != null
				&& (!typeTo.equals(context.board().defaultSite()) || !typeFrom.equals(typeTo)))
			sb.append(" - " + typeTo + " " + newTo);
		else
			sb.append("-" + newTo);

		if (levelTo != Constants.UNDEFINED)
			sb.append("/" + levelTo);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" rotation=" + rotation);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		sb.append(" stack=" + true);

		sb.append(')');

		return sb.toString();
	}
		
	//-------------------------------------------------------------------------

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
	public int from()
	{
		return from;
	}

	@Override
	public int to()
	{
		return to;
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
	public int state()
	{
		return state;
	}

	@Override
	public int rotation()
	{
		return rotation;
	}

	@Override
	public int value()
	{
		return value;
	}

	@Override
	public int count()
	{
		return 1;
	}

	@Override
	public boolean isStacking()
	{
		return true;
	}

	@Override
	public void setLevelFrom(final int levelA)
	{
		levelFrom = levelA;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.Move;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet ludemeConcept = (movesLudeme != null) ? movesLudeme.concepts(context.game()) : new BitSet();

		final int contIdA = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdB = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;

		final ContainerState csA = context.state().containerStates()[contIdA];
		final ContainerState csB = context.state().containerStates()[contIdB];

		final int whatA = csA.what(from, typeFrom);
		final int whatB = csB.what(to, typeTo);

		final int whoA = csA.who(from, typeFrom);
		final int whoB = csB.who(to, typeTo);

		final BitSet concepts = new BitSet();

		// ---- Hop concepts

		if (ludemeConcept.get(Concept.HopDecision.id()))
			{
			concepts.set(Concept.HopDecision.id(), true);

			if (whatA != 0)
			{
				final Topology topology = context.topology();
				final TopologyElement fromV = topology.getGraphElements(typeFrom).get(from);

				final List<DirectionFacing> directionsSupported = topology.supportedDirections(RelationType.All,
						typeFrom);
				AbsoluteDirection direction = null;
				int distance = Constants.UNDEFINED;

				for (final DirectionFacing facingDirection : directionsSupported)
				{
					final AbsoluteDirection absDirection = facingDirection.toAbsolute();
					final List<Radial> radials = topology.trajectories().radials(typeFrom, fromV.index(), absDirection);

					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
						{
							final int toRadial = radial.steps()[toIdx].id();
							if (toRadial == to)
							{
								direction = absDirection;
								distance = toIdx;
								break;
							}
						}
						if (direction != null)
							break;
					}
				}

				if (direction != null)
				{
					final List<Radial> radials = topology.trajectories().radials(typeFrom, fromV.index(), direction);

					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < distance; toIdx++)
						{
							final int between = radial.steps()[toIdx].id();
							final int whatBetween = csA.what(between, typeFrom);
							final int whoBetween = csA.who(between, typeFrom);
							if (whatBetween != 0)
							{
								if (areEnemies(context, whoA, whoBetween))
								{
									if (whatB == 0)
										concepts.set(Concept.HopDecisionEnemyToEmpty.id(), true);
									else
									{
										if (areEnemies(context, whoA, whoB))
											concepts.set(Concept.HopDecisionEnemyToEnemy.id(), true);
										else
											concepts.set(Concept.HopDecisionEnemyToFriend.id(), true);
									}
								}
								else
								{
									if (whatB == 0)
										concepts.set(Concept.HopDecisionFriendToEmpty.id(), true);
									else
									{
										if (areEnemies(context, whoA, whoB))
											concepts.set(Concept.HopDecisionFriendToEnemy.id(), true);
										else
											concepts.set(Concept.HopDecisionFriendToFriend.id(), true);
									}
								}
							}
						}
					}
				}
			}
		}

		if (ludemeConcept.get(Concept.HopEffect.id()))
			concepts.set(Concept.HopEffect.id(), true);

		// ---- Step concepts

		if (ludemeConcept.get(Concept.StepEffect.id()))
			concepts.set(Concept.StepEffect.id(), true);

		if (ludemeConcept.get(Concept.StepDecision.id()))
		{
			concepts.set(Concept.StepDecision.id(), true);
			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.StepDecisionToEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.StepDecisionToEnemy.id(), true);
					else
						concepts.set(Concept.StepDecisionToFriend.id(), true);
				}
			}
		}

		// ---- Leap concepts

		if (ludemeConcept.get(Concept.LeapEffect.id()))
			concepts.set(Concept.LeapEffect.id(), true);

		if (ludemeConcept.get(Concept.LeapDecision.id()))
		{
			concepts.set(Concept.LeapDecision.id(), true);
			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.LeapDecisionToEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.LeapDecisionToEnemy.id(), true);
					else
						concepts.set(Concept.LeapDecisionToFriend.id(), true);
				}
			}
		}

		// ---- Slide concepts

		if (ludemeConcept.get(Concept.SlideEffect.id()))
			concepts.set(Concept.SlideEffect.id(), true);

		if (ludemeConcept.get(Concept.SlideDecision.id()))
		{
			concepts.set(Concept.SlideDecision.id(), true);
			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.SlideDecisionToEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.SlideDecisionToEnemy.id(), true);
					else
						concepts.set(Concept.SlideDecisionToFriend.id(), true);
				}
			}
		}

		// ---- FromTo concepts

		if (ludemeConcept.get(Concept.FromToDecision.id()))
		{
			if (contIdA == contIdB)
				concepts.set(Concept.FromToDecisionWithinBoard.id(), true);
			else
				concepts.set(Concept.FromToDecisionBetweenContainers.id(), true);

			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.FromToDecisionEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.FromToDecisionEnemy.id(), true);
					else
						concepts.set(Concept.FromToDecisionFriend.id(), true);
				}
			}
		}

		if (ludemeConcept.get(Concept.FromToEffect.id()))
			concepts.set(Concept.FromToEffect.id(), true);
		
		// ---- Swap Pieces concepts

		if (ludemeConcept.get(Concept.SwapPiecesEffect.id()))
			concepts.set(Concept.SwapPiecesEffect.id(), true);

		if (ludemeConcept.get(Concept.SwapPiecesDecision.id()))
			concepts.set(Concept.SwapPiecesDecision.id(), true);


		// ---- Sow capture concepts

		if (ludemeConcept.get(Concept.SowCapture.id()))
			concepts.set(Concept.SowCapture.id(), true);

		return concepts;
	}

	/**
	 * @param context The context.
	 * @param who1    The id of a player.
	 * @param who2    The id of a player.
	 * @return True if these players are enemies.
	 */
	public static boolean areEnemies(final Context context, final int who1, final int who2)
	{
		if (who1 == 0 || who2 == 0 || who1 == who2)
			return false;

		if (context.game().requiresTeams())
		{
			final TIntArrayList teamMembers = new TIntArrayList();
			final int tid = context.state().getTeam(who1);
			for (int i = 1; i < context.game().players().size(); i++)
				if (context.state().getTeam(i) == tid)
					teamMembers.add(i);
			return !teamMembers.contains(who2);
		}

		return who1 != who2;
	}
}
