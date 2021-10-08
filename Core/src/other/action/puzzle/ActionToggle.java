package other.action.puzzle;

import game.types.board.SiteType;
import other.action.Action;
import other.action.BaseAction;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.puzzle.ContainerDeductionPuzzleState;

/**
 * Excludes a value from the possible values of a variable in a deduction
 * puzzle.
 *
 * @author Eric.Piette
 */
public class ActionToggle extends BaseAction  //implements ActionAtomic
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the site (the variable). */
	private final int var;

	/** The value to toggle to the variable. */
	private final int value;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param to    The index of the site (the variable).
	 * @param value The value to toggle.
	 * @param type  The graph element type.
	 */
	public ActionToggle
	(
		final SiteType type,
		final int to,
		final int value
	)
	{
		this.var = to;
		this.value = value;
		this.type = type;
	}

	/**
	 * Reconstructs an ActionToggle object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionToggle(final String detailedString)
	{
		assert (detailedString.startsWith("[Toggle:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strVar = Action.extractData(detailedString, "var");
		var = Integer.parseInt(strVar);

		final String strValue = Action.extractData(detailedString, "value");
		value = Integer.parseInt(strValue);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = context.containerId()[0];
		final ContainerState sc = context.state().containerStates()[contID];
		final ContainerDeductionPuzzleState ps = (ContainerDeductionPuzzleState) sc;
		if(type.equals(SiteType.Vertex))
			ps.toggleVerts(var, value);
		else if(type.equals(SiteType.Edge))
			ps.toggleEdges(var, value);
		else // Cell
			ps.toggleCells(var, value);

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
		result = prime * result + var;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionToggle))
			return false;

		final ActionToggle other = (ActionToggle) obj;
		return (decision == other.decision &&
				var == other.var &&
				value == other.value && type == other.type);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Toggle:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",var=" + var);
		}
		else
			sb.append("var=" + var);

		sb.append(",value=" + value);
		if (decision)
			sb.append(",decision=" + decision);

		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Toggle";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		String newTo = var + "";
		if (useCoords)
		{
			final int cid = (type == SiteType.Cell || type == null && context.board().defaultSite() == SiteType.Cell)
					? context.containerId()[var]
					: 0;
			if (cid == 0)
			{
				final SiteType realType = (type != null) ? type : context.board().defaultSite();
				newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(var)
						.label();
			}
		}

		if (type != null && !type.equals(context.board().defaultSite()))
			sb.append(type + " " + newTo);
		else
			sb.append(newTo);

		sb.append("^=" + value);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Toggle ");

		String newTo = var + "";
		if (useCoords)
		{
			final int cid = (type == SiteType.Cell || type == null && context.board().defaultSite() == SiteType.Cell)
					? context.containerId()[var]
					: 0;
			if (cid == 0)
			{
				final SiteType realType = (type != null) ? type : context.board().defaultSite();
				newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(var)
						.label();
			}
		}

		if (type != null && !type.equals(context.board().defaultSite()))
			sb.append(type + " " + newTo);
		else
			sb.append(newTo);

		sb.append(" on " + value);

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
		return var;
	}

	@Override
	public int to()
	{
		return var;
	}

	@Override
	public int count()
	{
		return 1;
	}

}
