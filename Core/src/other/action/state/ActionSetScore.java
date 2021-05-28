package other.action.state;

import other.action.Action;
import other.action.BaseAction;
import other.context.Context;

/**
 * Sets the score of a player.
 *
 * @author Eric.Piette
 */
public final class ActionSetScore extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The score's player to update. */
	private final int player;

	/** The new score. */
	private final int score;

	/** True if the score has to be add to the current score. */
	private final boolean add;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param score  The score.
	 * @param add    True if the score has to be added.
	 */
	public ActionSetScore
	(
		final int player, 
		final int score,
		final Boolean add
	)
	{
		this.player = player;
		this.score = score;
		this.add = (add == null) ? false : add.booleanValue();
	}

	/**
	 * Reconstructs an ActionSetScore object from a detailed String (generated using
	 * toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionSetScore(final String detailedString)
	{
		assert (detailedString.startsWith("[SetScore:"));

		final String strPlayer = Action.extractData(detailedString, "player");
		player = Integer.parseInt(strPlayer);

		final String strScore = Action.extractData(detailedString, "score");
		score = Integer.parseInt(strScore);

		final String strAddScore = Action.extractData(detailedString, "add");
		add = (strAddScore.isEmpty()) ? false : Boolean.parseBoolean(strAddScore);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if (add)
			context.setScore(player, context.score(player) + score);
		else
			context.setScore(player, score);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetScore:");
		sb.append("player=" + player);
		sb.append(",score=" + score);
		if (add)
			sb.append(",add=" + add);
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

		if (!(obj instanceof ActionSetScore))
			return false;

		final ActionSetScore other = (ActionSetScore) obj;
		return (decision == other.decision && score == other.score && player == other.player);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return "SetScore";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		if (!add)
			return "P" + player + "=" + score;

		return "P" + player + "+=" + score;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		if (!add)
			return "(Score P" + player + " = " + score + ")";

		return "(Score P" + player + " += " + score + ")";
	}
		
	//-------------------------------------------------------------------------

	@Override
	public int who()
	{
		return player;
	}

}
