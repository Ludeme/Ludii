package other.action.others;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Swap two players.
 *
 * @author Eric.Piette
 */
public final class ActionSwap extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first player to swap.*/
	private final int player1;
	
	/** The second player to swap.*/
	private final int player2;
	
	/**
	 * @param player1 The index of the first player.
	 * @param player2 The index of the second player.
	 */
	public ActionSwap
	(
		final int player1,
		final int player2
	)
	{
		this.player1 = player1;
		this.player2 = player2;
	}

	/**
	 * Reconstructs an ActionSwap object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionSwap(final String detailedString)
	{
		assert (detailedString.startsWith("[Swap:"));

		final String strPlayer1 = Action.extractData(detailedString, "player1");
		player1 = Integer.parseInt(strPlayer1);

		final String strPlayer2 = Action.extractData(detailedString, "player2");
		player2 = Integer.parseInt(strPlayer2);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().swapPlayerOrder(player1, player2);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		context.state().swapPlayerOrder(player2, player1);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Swap:");
		sb.append("player1=" + player1);
		sb.append(",player2=" + player2);
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
		result = prime * result + player1;
		result = prime * result + player2;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSwap))
			return false;

		final ActionSwap other = (ActionSwap) obj;
		return (decision == other.decision && player1 == other.player1 && player2 == other.player2);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "Swap";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Swap " + "P" + player1 + " P" + player2;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Swap " + "P" + player1 + " P" + player2 + ")";
	}
		
	//-------------------------------------------------------------------------

	@Override
	public boolean isOtherMove()
	{
		return true;
	}

	@Override
	public boolean isSwap()
	{
		return true;
	}
	
	/**
	 * @return player1
	 */
	public int player1()
	{
		return player1;
	}
	
	/**
	 * @return player2
	 */
	public int player2()
	{
		return player2;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Swap;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		if (decision)
			concepts.set(Concept.SwapPlayersDecision.id(), true);
		else
			concepts.set(Concept.SwapPlayersEffect.id(), true);
		return concepts;
	}
}