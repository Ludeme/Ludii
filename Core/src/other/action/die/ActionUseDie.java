package other.action.die;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Uses a die and remove it from the current dice of the context.
 *
 * @author Eric.Piette
 */
public class ActionUseDie extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Hand Dice index. */
	 private final int indexHandDice;

	/** Index Die. */
	 private final int indexDie;

	/** Index of the site. */
	 private final int site;

	//-------------------------------------------------------------------------

	/**
	 * @param indexHandDice The index of the hand dice.
	 * @param indexDie      The index of the die in this hand.
	 * @param toIndex       The index of the die.
	 */
	public ActionUseDie
	(
		final int indexHandDice,
		final int indexDie,
		final int toIndex
	)
	{
		this.indexHandDice = indexHandDice;
		this.indexDie = indexDie;
		this.site = toIndex;
	}

	/**
	 * Reconstructs an ActionUseDie object from a detailed String (generated using
	 * toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionUseDie(final String detailedString)
	{
		assert (detailedString.startsWith("[UseDie:"));

		final String strIndexDie = Action.extractData(detailedString, "indexDie");
		indexDie = Integer.parseInt(strIndexDie);

		final String strIndexHandDice = Action.extractData(detailedString, "indexHandDice");
		indexHandDice = Integer.parseInt(strIndexHandDice);

		final String strSite = Action.extractData(detailedString, "site");
		site = Integer.parseInt(strSite);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		context.state().updateCurrentDice(0, indexDie, indexHandDice);
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
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[UseDie:");
		sb.append("indexHandDice=" + indexHandDice);
		sb.append(",indexDie=" + indexDie);
		sb.append(",site=" + site);
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
		result = prime * result + indexHandDice;
		result = prime * result + indexDie;
		result = prime * result + site;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionUseDie))
			return false;

		final ActionUseDie other = (ActionUseDie) obj;

		return (indexHandDice == other.indexHandDice &&
				indexDie == other.indexDie &&
				site == other.site &&
				decision == other.decision);
	}

	//-------------------------------------------------------------------------

	@Override
	public String getDescription()
	{
		return "UseDie";
	}
	
	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Die " + site;
	}
	
	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Die at " + site + " is used)";
	}

	//-------------------------------------------------------------------------

	@Override
	public int from()
	{
		return site;
	}

	@Override
	public int to()
	{
		return site;
	}

	/**
	 * @return The index of the hand of dice.
	 */
	public int indexHandDice()
	{
		return this.indexHandDice;
	}

	/**
	 * @return The index of the die.
	 */
	public int indexDie()
	{
		return this.indexDie;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.UseDie;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.ByDieMove.id(), true);
		return concepts;
	}
}
