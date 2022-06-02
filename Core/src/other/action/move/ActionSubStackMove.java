package other.action.move;

import java.util.BitSet;
import java.util.List;

import game.Game;
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
import other.state.State;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Moves a part of a stack.
 *
 * @author Eric.Piette
 */
public final class ActionSubStackMove extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type of the from site. */
	private final SiteType typeFrom;

	/** From index. */
	private final int from;

	/** The level of the origin. */
	private int levelFrom = 0;

	/** The graph element type of the to site. */
	private final SiteType typeTo;

	/** To index. */
	private final int to;

	/** The level of the target. */
	private int levelTo = 0;

	/** The number of level to move. */
	private final int numLevel;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;

	//-- from data

	/** Previous What value of the from site. */
	private int[] previousWhatFrom;
	
	/** Previous Who value of the from site. */
	private int[] previousWhoFrom;
	
	/** Previous Site state value of the from site. */
	private int[] previousStateFrom;

	/** Previous Rotation value of the from site. */
	private int[] previousRotationFrom;

	/** Previous Piece value of the from site. */
	private int[] previousValueFrom;

	/** The previous hidden info values of the from site before to be removed. */
	private boolean[][] previousHiddenFrom;
	
	/** The previous hidden what info values of the from site before to be removed. */
	private boolean[][] previousHiddenWhatFrom;
	
	/** The previous hidden who info values of the from site before to be removed. */
	private boolean[][] previousHiddenWhoFrom;

	/** The previous hidden count info values of the from site before to be removed. */
	private boolean[][] previousHiddenCountFrom;

	/** The previous hidden rotation info values of the from site before to be removed. */
	private boolean[][] previousHiddenRotationFrom;

	/** The previous hidden State info values of the from site before to be removed. */
	private boolean[][] previousHiddenStateFrom;

	/** The previous hidden Value info values of the from site before to be removed. */
	private boolean[][] previousHiddenValueFrom;
	
	//--- to data
	
	/** Previous What of the to site. */
	private int[] previousWhatTo;
	
	/** Previous Who of the to site. */
	private int[] previousWhoTo;
	
	/** Previous Site state value of the to site. */
	private int[] previousStateTo;

	/** Previous Rotation value of the to site. */
	private int[] previousRotationTo;

	/** Previous Piece value of the to site. */
	private int[] previousValueTo;

	/** The previous hidden info values of the to site before to be removed. */
	private boolean[][] previousHiddenTo;
	
	/** The previous hidden what info values of the to site before to be removed. */
	private boolean[][] previousHiddenWhatTo;
	
	/** The previous hidden who info values of the to site before to be removed. */
	private boolean[][] previousHiddenWhoTo;

	/** The previous hidden count info values of the to site before to be removed. */
	private boolean[][] previousHiddenCountTo;

	/** The previous hidden rotation info values of the to site before to be removed. */
	private boolean[][] previousHiddenRotationTo;

	/** The previous hidden State info values of the to site before to be removed. */
	private boolean[][] previousHiddenStateTo;

	/** The previous hidden Value info values of the to site before to be removed. */
	private boolean[][] previousHiddenValueTo;
	
	//-------------------------------------------------------------------------

	/**
	 * @param from     The origin.
	 * @param to       The target.
	 * @param numLevel The number of pieces in the stack.
	 * @param typeFrom The type of the origin.
	 * @param typeTo   The type of the target.
	 */
	public ActionSubStackMove
	(
		final SiteType typeFrom,
		final int from, 
		final SiteType typeTo,
		final int to, 
		final int numLevel
	)
	{
		this.from = from;
		this.to = to;
		this.numLevel = numLevel;
		this.typeFrom = typeFrom;
		this.typeTo = typeTo;
	}

	/**
	 * Reconstructs an ActionStackMove object from a detailed String (generated
	 * using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSubStackMove(final String detailedString)
	{
		assert(detailedString.startsWith("[StackMove:"));

		final String strTypeFrom = Action.extractData(detailedString, "typeFrom");
		typeFrom = (strTypeFrom.isEmpty()) ? null : SiteType.valueOf(strTypeFrom);

		final String strFrom = Action.extractData(detailedString, "from");
		from = Integer.parseInt(strFrom);

		final String strTypeTo = Action.extractData(detailedString, "typeTo");
		typeTo = (strTypeTo.isEmpty()) ? null : SiteType.valueOf(strTypeTo);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strNumLevel = Action.extractData(detailedString, "numLevel");
		numLevel = Integer.parseInt(strNumLevel);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		final int contIdA = context.containerId()[from];
		final int contIdB = context.containerId()[to];
		final ContainerState csFrom = context.state().containerStates()[contIdA];
		final ContainerState csTo = context.state().containerStates()[contIdB];
		final int sizeStackA = csFrom.sizeStack(from, typeFrom);
		final int what = csFrom.what(from, typeFrom);

		// Keep in memory the data of the site from and to (for undo method)
		if(!alreadyApplied)
		{
			final int sizeStackFrom = csFrom.sizeStack(from, typeFrom);

			previousWhatFrom = new int[sizeStackFrom];
			previousWhoFrom = new int[sizeStackFrom];
			previousStateFrom = new int[sizeStackFrom];
			previousRotationFrom = new int[sizeStackFrom];
			previousValueFrom = new int[sizeStackFrom];
				
			for(int lvl = 0 ; lvl < sizeStackFrom; lvl++)
			{
				previousWhatFrom[lvl] = csFrom.what(from, lvl, typeFrom);
				previousWhoFrom[lvl] = csFrom.who(from, lvl, typeFrom);
				previousStateFrom[lvl] = csFrom.state(from, lvl, typeFrom);
				previousRotationFrom[lvl] = csFrom.rotation(from, lvl, typeFrom);
				previousValueFrom[lvl] = csFrom.value(from, lvl, typeFrom);
					
				if(context.game().hiddenInformation())
				{
					previousHiddenFrom = new boolean[sizeStackFrom][context.players().size()];
					previousHiddenWhatFrom = new boolean[sizeStackFrom][context.players().size()];
					previousHiddenWhoFrom = new boolean[sizeStackFrom][context.players().size()];
					previousHiddenCountFrom = new boolean[sizeStackFrom][context.players().size()];
					previousHiddenRotationFrom = new boolean[sizeStackFrom][context.players().size()];
					previousHiddenStateFrom = new boolean[sizeStackFrom][context.players().size()];
					previousHiddenValueFrom = new boolean[sizeStackFrom][context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenFrom[lvl][pid] = csFrom.isHidden(pid, from, lvl, typeFrom);
						previousHiddenWhatFrom[lvl][pid] = csFrom.isHiddenWhat(pid, from, lvl, typeFrom);
						previousHiddenWhoFrom[lvl][pid] = csFrom.isHiddenWho(pid, from, lvl, typeFrom);
						previousHiddenCountFrom[lvl][pid] = csFrom.isHiddenCount(pid, from, lvl, typeFrom);
						previousHiddenStateFrom[lvl][pid] = csFrom.isHiddenState(pid, from, lvl, typeFrom);
						previousHiddenRotationFrom[lvl][pid] = csFrom.isHiddenRotation(pid, from, lvl, typeFrom);
						previousHiddenValueFrom[lvl][pid] = csFrom.isHiddenValue(pid, from, lvl, typeFrom);
					}
				}
			}
				
			final int sizeStackTo = csTo.sizeStack(to, typeTo);
			previousWhatTo = new int[sizeStackTo];
			previousWhoTo = new int[sizeStackTo];
			previousStateTo = new int[sizeStackTo];
			previousRotationTo = new int[sizeStackTo];
			previousValueTo = new int[sizeStackTo];
			for(int lvl = 0 ; lvl < sizeStackTo; lvl++)
			{
				previousWhatTo[lvl] = csTo.what(to, lvl, typeTo);
				previousWhoTo[lvl] = csTo.who(to, lvl, typeTo);
				previousStateTo[lvl] = csTo.state(to, lvl, typeTo);
				previousRotationTo[lvl] = csTo.rotation(to, lvl, typeTo);
				previousValueTo[lvl] = csTo.value(to, lvl, typeTo);
					
				if(context.game().hiddenInformation())
				{
					previousHiddenTo = new boolean[sizeStackTo][context.players().size()];
					previousHiddenWhatTo = new boolean[sizeStackTo][context.players().size()];
					previousHiddenWhoTo = new boolean[sizeStackTo][context.players().size()];
					previousHiddenCountTo = new boolean[sizeStackTo][context.players().size()];
					previousHiddenRotationTo = new boolean[sizeStackTo][context.players().size()];
					previousHiddenStateTo = new boolean[sizeStackTo][context.players().size()];
					previousHiddenValueTo = new boolean[sizeStackTo][context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenTo[lvl][pid] = csTo.isHidden(pid, to, lvl, typeTo);
						previousHiddenWhatTo[lvl][pid] = csTo.isHiddenWhat(pid, to, lvl, typeTo);
						previousHiddenWhoTo[lvl][pid] = csTo.isHiddenWho(pid, to, lvl, typeTo);
						previousHiddenCountTo[lvl][pid] = csTo.isHiddenCount(pid, to, lvl, typeTo);
						previousHiddenStateTo[lvl][pid] = csTo.isHiddenState(pid, to, lvl, typeTo);
						previousHiddenRotationTo[lvl][pid] = csTo.isHiddenRotation(pid, to, lvl, typeTo);
						previousHiddenValueTo[lvl][pid] = csTo.isHiddenValue(pid, to, lvl, typeTo);
					}
				}
			}
		
			alreadyApplied = true;
		}
		
		if (what == 0 || sizeStackA < numLevel)
			return this;

		final int[] movedElement = new int[numLevel];
		final int[] ownerElement = new int[numLevel];
		final int[] stateElement = new int[numLevel];
		final int[] rotationElement = new int[numLevel];
		final int[] valueElement = new int[numLevel];

		for (int i = 0; i < numLevel; i++)
		{
			final int whatTop = csFrom.what(from, typeFrom);
			movedElement[i] = whatTop;
			final int whoTop = csFrom.who(from, typeFrom);
			ownerElement[i] = whoTop;
			final int stateTop = csFrom.state(from, typeFrom);
			stateElement[i] = stateTop;
			final int rotationTop = csFrom.rotation(from, typeFrom);
			rotationElement[i] = rotationTop;
			final int valueTop = csFrom.value(from, typeFrom);
			valueElement[i] = valueTop;
			final int topLevel = csFrom.sizeStack(from, typeFrom) - 1;
			context.state().owned().remove(whoTop, whatTop, from, topLevel, typeFrom);
			csFrom.remove(context.state(), from, typeFrom);
		}

		if (csFrom.sizeStack(from, typeFrom) == 0)
			csFrom.addToEmpty(from, typeFrom);

		boolean wasEmpty = (csTo.sizeStack(to, typeTo) == 0);
		
		for (int i = movedElement.length - 1; i >= 0; i--)
		{
			csTo.addItemGeneric(context.state(), to, movedElement[i], ownerElement[i], stateElement[i], rotationElement[i], valueElement[i], context.game(), typeTo);
			context.state().owned().add(ownerElement[i], movedElement[i], to, csTo.sizeStack(to, typeTo) - 1, typeTo);
		}
		
		if (wasEmpty && csTo.sizeStack(to, typeTo) != 0)
			csTo.removeFromEmpty(to, typeTo);
		
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState csFrom = context.state().containerStates()[contIdFrom];
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		final Game game = context.game();
		final State gameState = context.state();
		
		final int sizeStackFrom = csFrom.sizeStack(from, typeFrom);
		final int sizeStackTo = csTo.sizeStack(to, typeTo);
		
		// We restore the from site
		for(int lvl = sizeStackFrom -1 ; lvl >= 0; lvl--)
			csFrom.remove(context.state(), from, lvl, typeFrom);
	
		for(int lvl = 0 ; lvl < previousWhatFrom.length; lvl++)
		{
			csFrom.addItemGeneric(gameState, from, previousWhatFrom[lvl], previousWhoFrom[lvl], previousStateFrom[lvl], previousRotationFrom[lvl], previousValueFrom[lvl], game, typeFrom);
			if(context.game().hiddenInformation())
			{
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					csFrom.setHidden(gameState, pid, from, lvl, typeFrom, previousHiddenFrom[lvl][pid]);
					csFrom.setHiddenWhat(gameState, pid, from, lvl, typeFrom, previousHiddenWhatFrom[lvl][pid]);
					csFrom.setHiddenWho(gameState, pid, from, lvl, typeFrom, previousHiddenWhoFrom[lvl][pid]);
					csFrom.setHiddenCount(gameState, pid, from, lvl, typeFrom, previousHiddenCountFrom[lvl][pid]);
					csFrom.setHiddenState(gameState, pid, from, lvl, typeFrom, previousHiddenStateFrom[lvl][pid]);
					csFrom.setHiddenRotation(gameState, pid, from, lvl, typeFrom, previousHiddenRotationFrom[lvl][pid]);
					csFrom.setHiddenValue(gameState, pid, from, lvl, typeFrom, previousHiddenValueFrom[lvl][pid]);
				}
			}
		}
			
		// We restore the to site
		for(int lvl = sizeStackTo -1 ; lvl >= 0; lvl--)
			csTo.remove(context.state(), to, lvl, typeTo);
			
		for(int lvl = 0 ; lvl < previousWhatTo.length; lvl++)
		{
			csTo.addItemGeneric(gameState, to, previousWhatTo[lvl], previousWhoTo[lvl], previousStateTo[lvl], previousRotationTo[lvl], previousValueTo[lvl], game, typeTo);
			if(context.game().hiddenInformation())
			{
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					csTo.setHidden(gameState, pid, to, lvl, typeTo, previousHiddenTo[lvl][pid]);
					csTo.setHiddenWhat(gameState, pid, to, lvl, typeTo, previousHiddenWhatTo[lvl][pid]);
					csTo.setHiddenWho(gameState, pid, to, lvl, typeTo, previousHiddenWhoTo[lvl][pid]);
					csTo.setHiddenCount(gameState, pid, to, lvl, typeTo, previousHiddenCountTo[lvl][pid]);
					csTo.setHiddenState(gameState, pid, to, lvl, typeTo, previousHiddenStateTo[lvl][pid]);
					csTo.setHiddenRotation(gameState, pid, to, lvl, typeTo, previousHiddenRotationTo[lvl][pid]);
					csTo.setHiddenValue(gameState, pid, to, lvl, typeTo, previousHiddenValueTo[lvl][pid]);
				}
			}
		}
		
		if (csTo.sizeStack(to, typeTo) == 0)
			csTo.addToEmpty(to, typeTo);
		
		if (csFrom.sizeStack(from, typeFrom) != 0)
			csFrom.removeFromEmpty(from, typeFrom);
		
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[StackMove:");

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

		sb.append(",numLevel=" + numLevel);
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
		result = prime * result + numLevel;
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

		if (!(obj instanceof ActionSubStackMove))
			return false;

		final ActionSubStackMove other = (ActionSubStackMove) obj;
		return (numLevel == other.numLevel &&
				decision == other.decision &&
				from == other.from && to == other.to && typeFrom == other.typeFrom && typeTo == other.typeTo);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "StackMove";
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

		if (levelFrom != Constants.UNDEFINED)
			sb.append(":" + levelFrom);

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
			sb.append(":" + levelTo);

		sb.append("^" + numLevel);

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

		sb.append(" numLevel=" + numLevel);

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
		return levelFrom;
	}

	@Override
	public int levelTo()
	{
		return levelTo;
	}

	/**
	 * To set the level from.
	 */
	@Override
	public void setLevelFrom(final int levelA)
	{
		this.levelFrom = levelA;
	}

	/**
	 * To set the level to.
	 */
	@Override
	public void setLevelTo(final int levelB)
	{
		this.levelTo = levelB;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.StackMove;
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

				final List<DirectionFacing> directionsSupported = topology.supportedDirections(RelationType.All, typeFrom);
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
									
									if(distance > 1)
									{
										concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
										concepts.set(Concept.HopCaptureMoreThanOne.id(), true);
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
									if(distance > 1)
									{
										concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
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

		// ---- Sow concepts

		if (ludemeConcept.get(Concept.SowCapture.id()))
			concepts.set(Concept.SowCapture.id(), true);

		if (ludemeConcept.get(Concept.Sow.id()))
			concepts.set(Concept.Sow.id(), true);

		if (ludemeConcept.get(Concept.SowRemove.id()))
			concepts.set(Concept.SowRemove.id(), true);

		if (ludemeConcept.get(Concept.SowBacktracking.id()))
			concepts.set(Concept.SowBacktracking.id(), true);

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
