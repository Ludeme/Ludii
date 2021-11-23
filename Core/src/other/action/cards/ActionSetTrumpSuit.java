package other.action.cards;

import java.util.BitSet;

import game.rules.play.moves.Moves;
import game.types.component.SuitType;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;

/**
 * Sets the trump suit for card games.
 *
 * @author Eric.Piette
 */
public class ActionSetTrumpSuit extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The new trump suit. */
	private final int trumpSuit;

	//-------------------------------------------------------------------------
	
	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;
	
	/** The previous trump suit. */
	private int previousTrumpSuit;
	
	//-------------------------------------------------------------------------

	/**
	 * @param trumpSuit The trump suit.
	 */
	public ActionSetTrumpSuit
	(
		final int trumpSuit
	)
	{
		this.trumpSuit = trumpSuit;
	}

	/**
	 * Reconstructs an ActionSetTrumpSuit object from a detailed String (generated
	 * using toDetailedString())
	 *
	 * @param detailedString
	 */
	public ActionSetTrumpSuit(final String detailedString)
	{
		assert (detailedString.startsWith("[SetTrumpSuit:"));

		final String strTrumpSuit = Action.extractData(detailedString, "trumpSuit");
		trumpSuit = Integer.parseInt(strTrumpSuit);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply(final Context context, final boolean store)
	{
		if(!alreadyApplied)
		{
			previousTrumpSuit = context.state().trumpSuit();
			alreadyApplied = true;
		}
		
		context.state().setTrumpSuit(trumpSuit);
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		context.state().setTrumpSuit(previousTrumpSuit);
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[SetTrumpSuit:");
		sb.append("trumpSuit=" + trumpSuit);
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
		result = prime * result + trumpSuit;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionSetTrumpSuit))
			return false;

		final ActionSetTrumpSuit other = (ActionSetTrumpSuit) obj;

		return (trumpSuit == other.trumpSuit
				&& decision == other.decision);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("TrumpSuit = " + SuitType.values()[trumpSuit]);
		return sb.toString();
	}

	@Override
	public String getDescription()
	{
		return "SetTrumpSuit";
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("(TrumpSuit = " + SuitType.values()[trumpSuit] + ")");
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isOtherMove()
	{
		return true;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.SetTrumpSuit;
	}

	@Override
	public int what()
	{
		return trumpSuit;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet concepts = new BitSet();
		
		if(decision)
			concepts.set(Concept.ChooseTrumpSuitDecision.id(), true);
		else
			concepts.set(Concept.SetTrumpSuit.id(), true);
		
		return concepts;
	}

}