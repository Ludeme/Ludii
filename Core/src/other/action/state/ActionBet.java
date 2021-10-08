package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Makes a bet for a player.
 *
 * @author Eric.Piette
 */
public final class ActionBet extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player who bet. */
	private final int player;

	/** The bet. */
	private final int bet;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param bet    The bet.
	 */
	public ActionBet
	(
		final int player, 
		final int bet
	)
	{
		this.player = player;
		this.bet = bet;
	}

	/**
	 * Reconstructs an ActionBet object from a detailed String (generated using
	 * toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionBet(final String detailedString)
	{
		assert (detailedString.startsWith("[Bet:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strBet = Action.extractData(detailedString, "bet");
		bet = Integer.parseInt(strBet);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setAmount(player, bet);
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
		result = prime * result + player;
		result = prime * result + bet;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionBet))
			return false;

		final ActionBet other = (ActionBet) obj;
		return (decision == other.decision && bet == other.bet && player == other.player);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Bet:");
		sb.append("player=" + player);
		sb.append(",bet=" + bet);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "Bet";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Bet P" + player + " $" + bet;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(P" + player + " Bet = " + bet + ")";
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isOtherMove()
	{
		return true;
	}

	@Override
	public int who()
	{
		return player;
	}

	@Override
	public int count()
	{
		return bet;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Bet;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		
		if (decision)
			concepts.set(Concept.BetDecision.id(), true);
		else
			concepts.set(Concept.BetEffect.id(), true);
		
		return concepts;
	}
}
