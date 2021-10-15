package other.action.puzzle;

import game.types.board.SiteType;
import other.action.Action;
import other.action.BaseAction;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.puzzle.ContainerDeductionPuzzleState;

/**
 * Sets a value to a variable in a deduction puzzle.
 *
 * @author Eric.Piette
 */
public class ActionSet extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Graph element type. */
	private SiteType type;

	/** Index of the site (the variable) to set. */
	private final int var;

	/** Value to set to the variable. */
	private final int value;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous value. */
	private int previousValue;
	
	//-------------------------------------------------------------------------

	/**
	 * @param var  The index of the site.
	 * @param what The value to set to the variable.
	 * @param type The graph element type.
	 */
	public ActionSet
	(
		final SiteType type,
		final int var,
		final int what
	)
	{
		this.var = var;
		this.value = what;
		this.type = type;
	}

	/**
	 * Reconstructs an ActionSet object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionSet(final String detailedString)
	{
		assert (detailedString.startsWith("[Set:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "var");
		var = Integer.parseInt(strTo);

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
		
		if(!alreadyApplied)
		{
			if (type.equals(SiteType.Vertex))
				previousValue = ps.whatVertex(var);
			else if(type.equals(SiteType.Edge))
				previousValue = ps.whatEdge(var);
			else
				previousValue = ps.whatCell(var);
			alreadyApplied = true;
		}
		
		if (type.equals(SiteType.Vertex))
			ps.setVert(var, value);
		else if(type.equals(SiteType.Edge))
			ps.setEdge(var, value);
		else 
			ps.setCell(var, value);

		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		type = (type == null) ? context.board().defaultSite() : type;
		final int contID = context.containerId()[0];
		final ContainerState sc = context.state().containerStates()[contID];
		final ContainerDeductionPuzzleState ps = (ContainerDeductionPuzzleState) sc;
		
		if (type.equals(SiteType.Vertex))
			ps.setVert(var, previousValue);
		else if(type.equals(SiteType.Edge))
			ps.setEdge(var, previousValue);
		else
			ps.setCell(var, previousValue);

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

		if (!(obj instanceof ActionSet))
			return false;

		final ActionSet other = (ActionSet) obj;

		// Eric: Decision member is not checked, all the atomic action for puzzle are
		// decision.
//		return (decision == other.decision &&
//				loc == other.loc &&
//				what == other.what && type == other.type);
		return (var == other.var &&
				value == other.value && type == other.type);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Set:");
		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",var=" + var);
		}
		else
			sb.append("var" + var);

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
		return "Set";
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

		sb.append("=" + value);

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(");

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

		sb.append(" = " + value);

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
	public int what()
	{
		return value;
	}

	@Override
	public int count()
	{
		return 1;
	}

}
