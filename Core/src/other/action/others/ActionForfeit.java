package other.action.others;

import game.functions.booleans.BooleanConstant;
import game.rules.end.End;
import game.rules.end.If;
import game.rules.end.Result;
import game.types.play.ResultType;
import game.types.play.RoleType;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Forfeits a player.
 *
 * @author Eric.Piette
 */
public final class ActionForfeit extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player. */
	private final RoleType player;

	/**
	 * 
	 * @param player The player who forfeits.
	 */
	public ActionForfeit(final RoleType player)
	{
		this.player = player;
	}

	/**
	 * Reconstructs an ActionForfeit object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionForfeit(final String detailedString)
	{
		assert (detailedString.startsWith("[Forfeit:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = RoleType.valueOf(strPlayer);

		decision = true;
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		// To forfeit a player has to lose directly.
		new End(new If(new BooleanConstant(true), null, null, new Result(player, ResultType.Loss)), null).eval(context);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Forfeit:");
		sb.append("player=" + player);
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
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionForfeit))
			return false;

		final ActionForfeit other = (ActionForfeit) obj;
		return player.equals(other.player);
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Forfeit " + player;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Forfeit " + player + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "Forfeit";
	}

	//-------------------------------------------------------------------------

	@Override
	public ActionType actionType()
	{
		return ActionType.Forfeit;
	}

	@Override
	public boolean isForfeit()
	{
		return true;
	}
}
