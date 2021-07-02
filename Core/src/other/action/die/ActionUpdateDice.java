package other.action.die;

import java.util.BitSet;

import game.equipment.container.other.Dice;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Sets the state of the site where is the die and update the value of the
 * die.
 *
 * @author Eric.Piette
 */
public final class ActionUpdateDice extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Site index. */
	private final int site;

	/** The new state value. */
	private final int newState;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param site     The index of the site.
	 * @param newState The new state value.
	 */
	public ActionUpdateDice
	(
		final int site,
		final int newState
	)
	{
		this.site = site;
		this.newState = newState;
	}

	/**
	 * Reconstructs an ActionSetStateAndUpdateDice object from a detailed String
	 * (generated using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public ActionUpdateDice(final String detailedString)
	{
		assert (detailedString.startsWith("[SetStateAndUpdateDice:"));

		final String strSite = Action.extractData(detailedString, "site");
		site = Integer.parseInt(strSite);

		final String strState = Action.extractData(detailedString, "state");
		newState = Integer.parseInt(strState);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if(newState < 0)
			return this;
		
		final int cid = context.containerId()[site];
		final ContainerState state = context.state().containerStates()[cid];
		state.setSite(context.state(), site, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, newState,
				Constants.UNDEFINED, Constants.UNDEFINED, SiteType.Cell);

		if (context.containers()[cid].isDice())
		{
			final Dice dice = (Dice) context.containers()[cid];
			int indexDice = 0;
			for (int i = 0; i < context.handDice().size(); i++)
			{
				final Dice d = context.handDice().get(i);
				if (d.index() == dice.index())
				{
					indexDice = i;
					break;
				}
			}
			final int from = context.sitesFrom()[cid];
			final int what = state.whatCell(site);
			final int dieIndex = site - from;
			context.state().currentDice()[indexDice]
					[dieIndex] =
					context.components()[what]
							.getFaces()[newState];
		}

		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + site;
		result = prime * result + newState;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionUpdateDice))
			return false;

		final ActionUpdateDice other = (ActionUpdateDice) obj;

		return (decision == other.decision &&
				site == other.site &&
				newState == other.newState);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetStateAndUpdateDice:");
		sb.append("site=" + site);
		sb.append(",state=" + newState);
		if (decision)
			sb.append(",decision=" + decision);
		sb.append(']');

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "SetStateAndUpdateDice";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		return "Die " + site + "=" + newState;
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return "(Die at " + site + " state=" + newState + ")";
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

	@Override
	public int state()
	{
		return newState;
	}

	@Override
	public int who()
	{
		return newState;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.SetStateAndUpdateDice;
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Roll.id(), true);
		return concepts;
	}
}
