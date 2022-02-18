package other.action.move;

import java.util.BitSet;

import game.Game;
import game.equipment.component.Component;
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
 * Promotes a piece to another piece.
 *
 * @author Eric.Piette
 */
public final class ActionPromote extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Site index. */
	private final int to;

	/** Level. */
	private int level = Constants.UNDEFINED;

	/** New index of the piece after promotion. */
	private final int newWhat;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous index of the piece before to be removed. */
	private int previousWhat;
	
	/** The previous state value of the piece before to be removed. */
	private int previousState;

	/** The previous rotation value of the piece before to be removed. */
	private int previousRotation;

	/** The previous value of the piece before to be removed. */
	private int previousValue;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type The graph element type.
	 * @param to   Location to promote the component.
	 * @param what The new index of the piece.
	 */
	public ActionPromote
	(
		final SiteType type,
		final int to, 
		final int what
	)
	{
		this.to = to;
		this.newWhat = what;
		this.type = type;
	}

	/**
	 * Reconstructs an ActionPromote object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionPromote(final String detailedString)
	{
		assert (detailedString.startsWith("[Promote:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		level = (strLevel.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevel);

		final String strWhat = Action.extractData(detailedString, "what");
		newWhat = Integer.parseInt(strWhat);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = (type == SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state().containerStates()[contID];
		final Game game = context.game();
		final int oldWhat = (level == Constants.UNDEFINED) ? cs.what(to, type) : cs.what(to, level, type);
		
		if(!alreadyApplied)
		{
			previousWhat = oldWhat;
			previousState = cs.state(to, type);
			previousRotation = cs.rotation(to, type);
			previousValue = cs.value(to, type);
			alreadyApplied = true;
		}
		
		if (!game.isStacking())
		{
			Component piece = null;
			// To keep the site of the item in cache for each player.
			if (oldWhat != 0)
			{
				piece = context.components()[oldWhat];
				final int owner = piece.owner();
				if (owner != 0)
				{
					context.state().owned().remove(owner, oldWhat, to, type);
				}
			}
			cs.remove(context.state(), to, type);

			final int who = (oldWhat < 1) ? 0 : context.components()[newWhat].owner();
			cs.setSite(context.state(), to, who, newWhat, 1, Constants.UNDEFINED, Constants.UNDEFINED,
					Constants.UNDEFINED, type);

			// to keep the site of the item in cache for each player
			if (newWhat != 0)
			{
				piece = context.components()[newWhat];
				final int owner = piece.owner();
				if (owner != 0)
					context.state().owned().add(owner, newWhat, to, type);
			}
		}
		else
		{
			Component piece = context.components()[oldWhat];
			final int previousOwner = piece.owner();

			if (level == Constants.UNDEFINED) // remove the item on the top of the stack
				cs.remove(context.state(), to, type);
			else
				cs.remove(context.state(), to, level);

			final int sizeStack = cs.sizeStack(to, type);

			if (cs.sizeStack(to, type) == 0)
				cs.addToEmptyCell(to);

			// To keep the site of the item in cache for each player.
			if (cs.sizeStack(to, type) != 0)
			{
				if (previousOwner != 0)
				{
					if (level == Constants.UNDEFINED)
						context.state().owned().remove(previousOwner, oldWhat, to, sizeStack, type);
					else
						context.state().owned().remove(previousOwner, oldWhat, to, level, type);
				}
			}

			final int who = (newWhat < 1) ? 0 : context.components()[newWhat].owner();
			cs.addItemGeneric(context.state(), to, newWhat, who, context.game(), type);
			cs.removeFromEmptyCell(to);

			if (newWhat != 0)
			{
				piece = context.components()[newWhat];
				final int owner = piece.owner();
				if (owner != 0)
				{
					if (level == Constants.UNDEFINED)
						context.state().owned().add(owner, newWhat, to, sizeStack, type);
					else
						context.state().owned().add(owner, newWhat, to, level, type);
				}
			}
		}

		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = (type == SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState cs = context.state().containerStates()[contID];
		final Game game = context.game();
		final int oldWhat = (level == Constants.UNDEFINED) ? cs.what(to, type) : cs.what(to, level, type);
		
		if (!game.isStacking())
		{
			cs.remove(context.state(), to, type);

			final int who = (oldWhat < 1) ? 0 : context.components()[previousWhat].owner();
			cs.setSite(context.state(), to, who, previousWhat, 1, previousState, previousRotation,
					previousValue, type);
		}
		else
		{
			if (level == Constants.UNDEFINED) // remove the item on the top of the stack
				cs.remove(context.state(), to, type);
			else
				cs.remove(context.state(), to, level);

			if (cs.sizeStack(to, type) == 0)
				cs.addToEmptyCell(to);

			final int who = (previousWhat < 1) ? 0 : context.components()[previousWhat].owner();
			cs.addItemGeneric(context.state(), to, previousWhat, who, context.game(), type);
			cs.removeFromEmptyCell(to);
		}
		
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Promote:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);

		if (level != Constants.UNDEFINED)
			sb.append(",level=" + level);

		sb.append(",what=" + newWhat);
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
		result = prime * result + level;
		result = prime * result + to;
		result = prime * result + newWhat;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionPromote))
			return false;

		final ActionPromote other = (ActionPromote) obj;
		return (decision == other.decision &&
				level == other.level &&
				to == other.to &&
				newWhat == other.newWhat && type == other.type);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Promote";
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

		if (newWhat > 0 && newWhat < context.components().length)
			sb.append(" => " + context.components()[newWhat].name());

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Promote ");

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

		if (newWhat > 0 && newWhat < context.components().length)
			sb.append(" to " + context.components()[newWhat].name());

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
	public int count()
	{
		return 1;
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
		return newWhat;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Promote;
	}

	/**
	 * To set the level in the JUnit test. To not use in other code !
	 * 
	 * @param level The new level.
	 */
	public void setLevel(final int level)
	{
		this.level = level;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		if(decision)
			concepts.set(Concept.PromotionDecision.id(), true);
		else
			concepts.set(Concept.PromotionEffect.id(), true);
			
		return concepts;
	}

}
