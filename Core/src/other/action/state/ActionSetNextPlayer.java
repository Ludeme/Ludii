package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the next player.
 *
 * @author Eric.Piette
 */
public final class ActionSetNextPlayer extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Index of player. */
	private final int player;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous value. */
	private int previousValue;
	
	//-------------------------------------------------------------------------

	/**
	 * @param player The new next player.
	 */
	public ActionSetNextPlayer
	(
		final int player
	)
	{
		this.player = player;
	}

	/**
	 * Reconstructs a ActionSetNextPlayer object from a detailed String (generated
	 * using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetNextPlayer(final String detailedString)
	{
		assert (detailedString.startsWith("[SetNextPlayer:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if(!alreadyApplied)
		{
			previousValue = context.state().next();
			alreadyApplied = true;
		}
		
		context.state().setNext(player);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		context.state().setNext(previousValue);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetNextPlayer:");
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
		result = prime * result + player;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetNextPlayer))
			return false;

		final ActionSetNextPlayer other = (ActionSetNextPlayer) obj;
		return (decision == other.decision &&
				player == other.player);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "SetNextPlayer";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Next P" + player;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Next Player = P" + player + ")";
	}
		
	//-------------------------------------------------------------------------

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
	public int what()
	{
		return player;
	}

	@Override
	public int playerSelected()
	{
		return player;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.SetNextPlayer;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final int mover = context.state().mover();
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SetNextPlayer.id(), true);
		if (mover == player)
			concepts.set(Concept.MoveAgain.id(), true);
		return concepts;
	}
}
