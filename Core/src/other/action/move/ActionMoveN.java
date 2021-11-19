package other.action.move;

import java.util.BitSet;
import java.util.List;

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
 * Moves many pieces from a site to another (but not in a stack).
 *
 * @author Eric.Piette
 */
public final class ActionMoveN extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type of the from site. */
	private final SiteType typeFrom;

	/** From site index. */
	private final int from;

	/** The graph element type of the to site. */
	private final SiteType typeTo;

	/** To site index. */
	private final int to;

	/** The number of pieces to move. */
	private final int count;

	//-------------------------------------------------------------------------

	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** Previous What of the from site. */
	private int previousWhatFrom;
	
	/** Previous Who of the from site. */
	private int previousWhoFrom;
	
	/** Previous Site state value of the from site. */
	private int previousStateFrom;

	/** Previous Rotation value of the from site. */
	private int previousRotationFrom;

	/** Previous Piece value of the from site. */
	private int previousValueFrom;
	
	/** Previous Piece value of the from site. */
	private int previousCountFrom;
	
	/** Previous Site state value of the to site. */
	private int previousStateTo;

	/** Previous Rotation value of the to site. */
	private int previousRotationTo;

	/** Previous Piece value of the to site. */
	private int previousValueTo;
	
	/** Previous What of the to site. */
	private int previousWhatTo;
	
	/** Previous Who of the to site. */
	private int previousWhoTo;
	
	/** Previous Piece value of the from site. */
	private int previousCountTo;
	
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
	 * @param typeFrom The graph element type of the origin.
	 * @param from     From site of the move.
	 * @param typeTo   The graph element type of the target.
	 * @param to       To site of the move.
	 * @param count    The number of pieces to move.
	 */
	public ActionMoveN
	(
		final SiteType typeFrom,
		final int from, 
		final SiteType typeTo,
		final int to, 
		final int count
	)
	{
		this.from = from;
		this.to = to;
		this.count = count;
		this.typeFrom = typeFrom;
		this.typeTo = typeTo;
	}

	/**
	 * Reconstructs an ActionMoveN object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionMoveN(final String detailedString)
	{
		assert (detailedString.startsWith("[Move:"));

		final String strTypeFrom = Action.extractData(detailedString, "typeFrom");
		typeFrom = (strTypeFrom.isEmpty()) ? null : SiteType.valueOf(strTypeFrom);

		final String strFrom = Action.extractData(detailedString, "from");
		from = Integer.parseInt(strFrom);

		final String strTypeTo = Action.extractData(detailedString, "typeTo");
		typeTo = (strTypeTo.isEmpty()) ? null : SiteType.valueOf(strTypeTo);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strCount = Action.extractData(detailedString, "count");
		count = Integer.parseInt(strCount);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		final int cidFrom = context.containerId()[from];
		final int cidTo = context.containerId()[to];
		final ContainerState csFrom = context.state().containerStates()[cidFrom];
		final ContainerState csTo = context.state().containerStates()[cidTo];

		final int what = csFrom.what(from, typeFrom);
		final int who = (what < 1) ? 0 : context.components()[what].owner();
		int currentStateFrom = Constants.UNDEFINED;
		int currentRotationFrom = Constants.UNDEFINED;
		int currentValueFrom = Constants.UNDEFINED;

		// take the local state of the site from
		currentStateFrom = (csFrom.what(from, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, typeFrom);
		currentRotationFrom = csFrom.rotation(from, typeFrom);
		currentValueFrom = csFrom.value(from, typeFrom);
		
		if(!alreadyApplied)
		{
			previousStateFrom = currentStateFrom;
			previousCountFrom = csFrom.count(from, typeFrom);
			previousRotationFrom = currentRotationFrom;
			previousValueFrom = currentValueFrom;
			previousWhoFrom = csFrom.who(from, typeFrom);
			previousWhatFrom = csFrom.what(from, typeFrom);
			previousStateTo = (csTo.what(to, typeTo) == 0) ? Constants.UNDEFINED : csTo.state(to, typeTo);
			previousRotationTo = csTo.rotation(to, typeTo);
			previousValueTo = csTo.value(to, typeTo);
			previousWhoTo = csTo.who(to, typeTo);
			previousWhatTo = csTo.what(to, typeTo);
			previousCountTo = csTo.count(to, typeTo);
			
			if(context.game().hiddenInformation())
			{
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
			alreadyApplied = true;
		}
		
		// modification on From
		if (csFrom.count(from, typeFrom) - count <= 0)
			csFrom.remove(context.state(), from, typeFrom);
		else
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, csFrom.count(from, typeFrom) - count, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, typeFrom);

		// modification on To
		if (csTo.count(to, typeTo) == 0)
		{
			csTo.setSite(context.state(), to, who, what, count, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, typeTo);
		}
		else if (csTo.what(to, typeTo) == what)
		{
			if((csTo.count(to, typeTo) + count) <= context.game().maxCount())
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, csTo.count(to, typeTo) + count, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, typeTo);
		}

//		Component piece = null;
		// to keep the site of the item in cache for each player
		if (what != 0 && who !=0)
		{
			if(csFrom.count(from, typeFrom) == 0)
				context.state().owned().remove(who, what, from, typeFrom);
			context.state().owned().add(who, what, to, typeTo);
		}

		final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
		// We update the structure about track indices if the game uses track.
		if (what != 0 && onTrackIndices != null)
		{
			for (final Track track : context.board().tracks())
			{
				final int trackIdx = track.trackIdx();
				final TIntArrayList indicesLocFrom = onTrackIndices.locToIndex(trackIdx, from);

				for (int k = 0; k < indicesLocFrom.size(); k++)
				{
					final int indexA = indicesLocFrom.getQuick(k);
					final int countAtIndex = onTrackIndices.whats(trackIdx, what, indicesLocFrom.getQuick(k));

					if (countAtIndex > 0)
					{
						onTrackIndices.remove(trackIdx, what, this.count, indexA);
						final TIntArrayList newWhatIndice = onTrackIndices.locToIndexFrom(trackIdx, to, indexA);

						if (newWhatIndice.size() > 0)
						{
							onTrackIndices.add(trackIdx, what, this.count, newWhatIndice.getQuick(0));
						}
						else
						{
							final TIntArrayList newWhatIndiceIfNotAfter = onTrackIndices.locToIndex(trackIdx, to);
							if (newWhatIndiceIfNotAfter.size() > 0)
								onTrackIndices.add(trackIdx, what, this.count, newWhatIndiceIfNotAfter.getQuick(0));
						}

						break;
					}
				}

				// If the piece was not in the track but enter on it, we update the structure
				// corresponding to that track.
				if (indicesLocFrom.size() == 0)
				{
					final TIntArrayList indicesLocTo = onTrackIndices.locToIndex(trackIdx, to);
					if (indicesLocTo.size() != 0)
						onTrackIndices.add(trackIdx, what, 1, indicesLocTo.getQuick(0));
				}
			}
		}

		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		final int cidFrom = context.containerId()[from];
		final int cidTo = context.containerId()[to];
		final ContainerState csFrom = context.state().containerStates()[cidFrom];
		final ContainerState csTo = context.state().containerStates()[cidTo];

		// modification on To
		if (previousCountTo <= 0)
			csTo.remove(context.state(), to, typeTo);
		else
			csTo.setSite(context.state(), to, previousWhoTo, previousWhatTo, previousCountTo, previousStateTo, previousRotationTo, previousValueTo, typeTo);

		// modification on From
		if (csFrom.count(from, typeFrom) == 0)
			csFrom.setSite(context.state(), from, previousWhoFrom, previousWhatFrom, previousCountFrom, previousStateFrom, previousRotationFrom, previousValueFrom, typeFrom);
		else if (csFrom.what(from, typeFrom) == previousWhatFrom)
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, previousCountFrom, previousStateFrom, previousRotationFrom, previousValueFrom, typeFrom);
		
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

		if (typeTo != null || (context != null && typeTo != context.board().defaultSite()))
			sb.append(",typeTo=" + typeTo);

		sb.append(",to=" + to);

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

		if (!(obj instanceof ActionMoveN))
			return false;

		final ActionMoveN other = (ActionMoveN) obj;
		return (count == other.count &&
				decision == other.decision &&
				from == other.from &&
				to == other.to && typeFrom == other.typeFrom && typeTo == other.typeTo);
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

		if (count > 1)
			sb.append("x" + count);

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

		if (count > 1)
			sb.append("x" + count);

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
	public int count()
	{
		return count;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.MoveN;
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
