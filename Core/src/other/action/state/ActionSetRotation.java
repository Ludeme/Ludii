package other.action.state;

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
 * Sets the rotation value of a site.
 *
 * @author Eric.Piette
 */
public final class ActionSetRotation extends BaseAction
{
	//-------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Loc index. */
	private final int to;

	/** The new rotation value. */
	private final int rotation;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous rotation. */
	private int previousRotation;
	
	/** The previous site type. */
	private SiteType previousType;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type     The graph element type.
	 * @param to       The index of the site.
	 * @param rotation The new rotation.
	 */
	public ActionSetRotation
	( 
		final SiteType type,
		final int to, 
		final int rotation
	)
	{
		this.to = to;
		this.rotation = rotation;
		this.type = type;
	}

	/**
	 * Reconstructs an ActionSetRotation object from a detailed String (generated
	 * using toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionSetRotation(final String detailedString)
	{
		assert (detailedString.startsWith("[SetRotation:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);

		final String strRotation = Action.extractData(detailedString, "rotation");
		rotation = Integer.parseInt(strRotation);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		final int cid = to >= context.containerId().length ? 0 : context.containerId()[to];
		final ContainerState cs = context.state().containerStates()[cid];
		
//		if (context.game().isStacking()) // Level has to be implemented
//		{
//			if (level != Constants.UNDEFINED)
//			{
//				final int stackSize = cs.sizeStack(to, type);
//				if (level < stackSize)
//				{
//					if(!alreadyApplied)
//					{
//						previousValue = cs.value(to, level, type);
//						previousType = type;
//						alreadyApplied = true;
//					}
//					
//					final int what = cs.what(to, level, type);
//					final int who = cs.who(to, level, type);
//					final int value = cs.value(to, level, type);
//					final int state = cs.state(to, level, type);
//					cs.remove(context.state(), to, level);
//					cs.insert(context.state(), type, to, level, what, who, state, rotation, value, context.game());
//				}
//			}
//			else
//			{
//				if(!alreadyApplied)
//				{
//					previousValue = cs.value(to, type);
//					previousType = type;
//					alreadyApplied = true;
//				}
//				
//				context.containerState(context.containerId()[to]).setSite(context.state(), to, Constants.UNDEFINED,
//						Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, rotation, Constants.UNDEFINED,
//						type);
//			}
//		}
//		else
//		{
			if(!alreadyApplied)
			{
				previousRotation = cs.rotation(to, type);
				previousType = type;
				alreadyApplied = true;
			}
			
			cs.setSite(context.state(), to, Constants.UNDEFINED,
				Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, rotation, Constants.UNDEFINED, type);
		//}
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		final int cid = to >= context.containerId().length ? 0 : context.containerId()[to];
		final ContainerState cs = context.state().containerStates()[cid];
		
//		if (context.game().isStacking()) // Level has to be implemented
//		{
//			final int stackSize = cs.sizeStack(to, type);
//			
//			if (level != Constants.UNDEFINED)
//			{
//				if (level < stackSize)
//				{
//					final int what = cs.what(to, level, type);
//					final int who = cs.who(to, level, type);
//					final int value = cs.value(to, level, type);
//					final int state = cs.state(to, level, type);
//					cs.remove(context.state(), to, level);
//					cs.insert(context.state(), previousType, to, level, what, who, state, previousValue, value, context.game());
//				}
//			}
//			else
//			{
//				cs.setSite(context.state(), to, Constants.UNDEFINED,
//						Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousValue, Constants.UNDEFINED,
//						previousType);
//			}
//		}
//		else
//		{
			cs.setSite(context.state(), to, Constants.UNDEFINED,
				Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousRotation, Constants.UNDEFINED, previousType);
//		}
		
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
		result = prime * result + rotation;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetRotation))
			return false;

		final ActionSetRotation other = (ActionSetRotation) obj;

		return (decision == other.decision && to == other.to && rotation == other.rotation
				&& type == other.type);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetRotation:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",to=" + to);
		}
		else
			sb.append("to=" + to);
		sb.append(",rotation=" + rotation);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	

	@Override
	public String getDescription()
	{
		return "SetRotation";
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

		sb.append(" r" + rotation);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Rotation ");

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

		sb.append(" = " + rotation);

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
	public int rotation()
	{
		return rotation;
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
	public int state()
	{
		return rotation;
	}

	@Override
	public int who()
	{
		return rotation;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.SetRotation;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		
		if(decision)
			concepts.set(Concept.RotationDecision.id(), true);
		else
			concepts.set(Concept.SetRotation.id(), true);
		
		return concepts;
	}
}
