package other.action.puzzle;

import game.types.board.SiteType;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.puzzle.ContainerDeductionPuzzleState;

/**
 * Resets all the values of a variable to not set in a deduction puzzle.
 *
 * @author Matthew.Stephenson and Eric.piette
 */
public class ActionReset extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;

	/** Index of the site (the variable) to reset. */
	private final int var;

	/** Maximum number of values. */
	private final int max;
	
	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous value. */
	private boolean previousValues[];

	//-------------------------------------------------------------------------

	/**
	 * @param var     The index of the site.
	 * @param whatMax The max variable.
	 * @param type    The graph element type.
	 */
	public ActionReset
	(
		final SiteType type,
		final int var,
		final int whatMax
	)
	{
		this.type = type;
		this.var = var;
		this.max = whatMax;
	}

	/**
	 * Reconstructs an ActionReset object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionReset(final String detailedString)
	{
		assert (detailedString.startsWith("[Reset:"));

		final String strType = Action.extractData(detailedString, "type");
		type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "var");
		var = Integer.parseInt(strTo);

		final String strMax = Action.extractData(detailedString, "max");
		max = Integer.parseInt(strMax);

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
			final int maxValue = context.board().getRange(type).max(context);
			final int minValue = context.board().getRange(type).min(context);
			previousValues = new boolean[maxValue - minValue + 1]; 
			for(int i = 0; i < previousValues.length; i++)
				previousValues[i] = ps.bit(var, i, type);
			alreadyApplied = true;
		}
		
		if (type.equals(SiteType.Vertex))
			ps.resetVariable(SiteType.Vertex, var, max);
		if (type.equals(SiteType.Edge))
			ps.resetVariable(SiteType.Edge, var, max);
		if (type.equals(SiteType.Cell))
			ps.resetVariable(SiteType.Cell, var, max);

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
		{
			for(int i = 0; i < previousValues.length; i++)
				if(ps.bit(var, i, type) != previousValues[i])
					ps.toggleVerts(var, i);
		}
		else if(type.equals(SiteType.Edge))
		{
			for(int i = 0; i < previousValues.length; i++)
				if(ps.bit(var, i, type) != previousValues[i])
					ps.toggleEdges(var, i);
		}
		else
		{
			for(int i = 0; i < previousValues.length; i++)
				if(ps.bit(var, i, type) != previousValues[i])
					ps.toggleCells(var, i);
		}

		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Reset:");

		if (type != null || (context != null && type != context.board().defaultSite()))
		{
			sb.append("type=" + type);
			sb.append(",var=" + var);
		}
		else
			sb.append("var=" + var);

		sb.append(",max=" + max);
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
		result = prime * result + var;
		result = prime * result + max;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionReset))
			return false;

		final ActionReset other = (ActionReset) obj;

		return (decision == other.decision &&
				var == other.var &&
				max == other.max && type == other.type);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "Reset";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("Reset ");

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


		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Reset ");

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

		sb.append(')');

		return sb.toString();
	}
		
	//-------------------------------------------------------------------------

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
	public boolean isAlwaysGUILegal()
	{
		return true;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.Reset;
	}

}
