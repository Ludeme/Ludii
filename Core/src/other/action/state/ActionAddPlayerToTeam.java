package other.action.state;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Adds a player to a team.
 *
 * @author Eric.Piette
 */
public final class ActionAddPlayerToTeam extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The team. */
	private final int team;

	/** The player. */
	private final int player;

	/**
	 * Constructor.
	 * 
	 * @param team   The index of the team.
	 * @param player The index of the player.
	 */
	public ActionAddPlayerToTeam
	(
		final int team, 
		final int player
	)
	{
		this.team = team;
		this.player = player;
	}

	/**
	 * Reconstructs an ActionAddPlayerToTeam object from a detailed String
	 * (generated using toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionAddPlayerToTeam(final String detailedString)
	{
		assert (detailedString.startsWith("[AddPlayerToTeam:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strTeam = Action.extractData(detailedString, "team");
		team = Integer.parseInt(strTeam);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().setPlayerToTeam(player, team);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[AddPlayerToTeam:");
		sb.append("team=" + team);
		sb.append(",player=" + player);

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
		result = prime * result + team;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionAddPlayerToTeam))
			return false;

		final ActionAddPlayerToTeam other = (ActionAddPlayerToTeam) obj;
		return team == other.team && player == other.player;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "AddPlayerToTeam";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Team" + team + " + P" + player;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Add P" + player + " to Team" + team + ")";
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.AddPlayerToTeam;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Coalition.id(), true);
		return concepts;
	}
}
