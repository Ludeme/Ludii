package other.action.state;

import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.BaseAction;
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
		final ContainerState state = context.state().containerStates()[context.containerId()[to]];
		state.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
				rotation, Constants.UNDEFINED, type);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
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
}
