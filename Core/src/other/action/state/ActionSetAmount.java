package other.action.state;

import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Sets the amount of a player.
 *
 * @author Eric.Piette
 */
public final class ActionSetAmount extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player index. */
	private final int player;

	/** The new amount. */
	private final int amount;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous amount. */
	private int previousAmount;
	
	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param amount The amount.
	 */
	public ActionSetAmount
	(
		final int player,
		final int amount
	)
	{
		this.player = player;
		this.amount = amount;
	}

	/**
	 * Reconstructs an ActionSetAmount object from a detailed String (generated
	 * using toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionSetAmount(final String detailedString)
	{
		assert (detailedString.startsWith("[SetAmount:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strAmount = Action.extractData(detailedString, "amount");
		amount = Integer.parseInt(strAmount);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if(!alreadyApplied)
		{
			previousAmount = context.state().amount(player);
			alreadyApplied = true;
		}
		
		context.state().setAmount(player, amount);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		context.state().setAmount(player, previousAmount);
		return this;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + player;
		result = prime * result + amount;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetAmount))
			return false;

		final ActionSetAmount other = (ActionSetAmount) obj;
		return (decision == other.decision && amount == other.amount && player == other.player);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetAmount:");
		sb.append("player=" + player);
		sb.append(",amount=" + amount);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Amount";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "P" + player + "=$" + amount;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Amount P" + player + " = " + amount + ")";
	}

	@Override
	public int who()
	{
		return player;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.SetAmount;
	}
		
}
