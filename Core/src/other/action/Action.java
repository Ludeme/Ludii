package other.action;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Hide;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Action (or actions) making up a player move.
 *
 * @author cambolbro and Eric.Piette
 */
@Hide
public interface Action extends Serializable
{
	/**
	 * Apply this instruction to the specified state.
	 *
	 * @param context The context.
	 * @param store   To store directly the action in the trial.
	 * @return Action as applied (possibly with additional Actions from consequents
	 *         in Moves)
	 */
	public Action apply(final Context context, final boolean store);

	/**
	 * @return Whether this action is a pass.
	 */
	public boolean isPass();

	/**
	 * @return Whether this action is a forfeit.
	 */
	public boolean isForfeit();

	/**
	 * @return Whether this action is a swap.
	 */
	public boolean isSwap();
	
	/**
	 * @return Whether this action is a vote.
	 */
	public boolean isVote();
	
	/**
	 * @return Whether this action is forced.
	 */
	public boolean isForced();
	
	/**
	 * @return Whether this action is a propose.
	 */
	public boolean isPropose();
	
	/**
	 * @return Whether this action is always legal in the GUI.
	 */
	public boolean isAlwaysGUILegal();

	/**
	 * @return The player selected by the action.
	 */
	public int playerSelected();

	/** 
	 * @return Whether this contains a NextInstance action 
	 */
	public boolean containsNextInstance();

	/**
	 * @param siteA             The site A.
	 * @param levelA            The level A.
	 * @param graphElementTypeA The graph element type A.
	 * @param siteB             The site B.
	 * @param levelB            The site B.
	 * @param graphElementTypeB The graph element type B.
	 * @return Whether this action matches a user move in the GUI.
	 */
	public boolean matchesUserMove(final int siteA, final int levelA, final SiteType graphElementTypeA, final int siteB,
			final int levelB, final SiteType graphElementTypeB);

	//-------------------------------------------------------------------------

	/**
	 * @return The from location of the action. For Move.java, the from location of
	 *         the first decision action in the list.
	 */
	public int from();

	/**
	 * @return The level of the from location of the action (typically 0). For
	 *         Move.java, the level of the from location of the first decision
	 *         action in the list.
	 */
	public int levelFrom();

	/**
	 * @return The to location of the action. For Move.java, the to location of the
	 *         first decision action in the list.
	 */
	public int to();

	/**
	 * @return The level of the to location of the action (typically 0). For
	 *         Move.java, the level of the to location of the first decision action
	 *         in the list.
	 */
	public int levelTo();

	/**
	 * @return The player index of the action. For Move.java, the player index of
	 *         the first decision action in the list.
	 */
	public int who();

	/**
	 * @return The piece index of the action. For Move.java, the piece index of the
	 *         first decision action in the list.
	 */
	public int what();

	/**
	 * @return The state value of the action. For Move.java, the state value of the
	 *         first decision action in the list.
	 */
	public int state();

	/**
	 * @return The rotation value of the action. For Move.java, the rotation value
	 *         of the first decision action in the list.
	 */
	public int rotation();

	/**
	 * @return The piece value of the action. For Move.java, the piece value of the
	 *         first decision action in the list.
	 */
	public int value();

	/**
	 * @return The count value of the action. For Move.java, the count value of the
	 *         first decision action in the list.
	 */
	public int count();

	/**
	 * @return The proposition of the action. For Move.java, the proposition of the
	 *         first decision action in the list.
	 */
	public String proposition();

	/**
	 * @return The vote of the action. For Move.java, the vote of the first decision
	 *         action in the list.
	 */
	public String vote();

	/**
	 * @return The message of the action. For Move.java, the message of the first
	 *         decision action in the list.
	 */
	public String message();

	/**
	 * @return Whether the action is in a stacking state. For Move.java, Whether the
	 *         first decision action in the list is in a stacking state.
	 */
	public boolean isStacking();

	/**
	 * @return Record of hidden information for the action. For Move.java, the
	 *         hidden information of the first decision action in the list.
	 */
	boolean[] hidden();

	/**
	 * @return True if this is a decision.
	 */
	public boolean isDecision();

	/**
	 * @return The action Type.
	 */
	public ActionType actionType();

	/**
	 * @return The type of kind of graph element involved in the action by the from.
	 *         In Move.java, the graph element type of the from location of the
	 *         first decision action in the list.
	 */
	public abstract SiteType fromType();

	/**
	 * @return The type of kind of graph element involved in the action by the to.
	 *         In Move.java, the graph element type of the to location of the first
	 *         decision action in the list.
	 */
	public abstract SiteType toType();

	/**
	 * Set whether this action is a decision.
	 * 
	 * @param decision The decision to set.
	 */
	public void setDecision(final boolean decision);

	/**
	 * Set whether this action is a decision, and return the action object.
	 * 
	 * @param decision
	 * @return The same action object we're calling the method on.
	 */
	public Action withDecision(final boolean decision);

	/**
	 * @param context
	 * @return A detailed string description of this action, with sufficient
	 * detail to allow an equivalent reconstruction (in the given context,
	 * with consequents already resolved for that context)
	 */
	public String toTrialFormat(final Context context);
	
	/**
	 * @param context
	 * @param useCoords Use coordinates for display.
	 * @return A less detailed string description of the move for the move tab.
	 */
	public String toMoveFormat(final Context context, final boolean useCoords);

	/**
	 * @param context
	 * @return A plain English string description of this action.
	 */
	public String toEnglishString(final Context context);
	
	/**
	 * @return A short string name/description for this action.
	 */
	public String getDescription();

	/**
	 * Need the context in order to display the coordinates if needed.
	 * 
	 * @param context The context.
	 * @param useCoords Use coordinates for display.
	 * @return The string.
	 */
	public String toTurnFormat(final Context context, final boolean useCoords);

	/**
	 * To set the level of the from location of the action.
	 * 
	 * @param levelA The level to set.
	 */
	public void setLevelFrom(final int levelA);

	/**
	 * To set the level of the to location of the action.
	 * 
	 * @param levelB The level to set.
	 */
	public void setLevelTo(final int levelB);

	/**
	 * @return True if the move has to be used in the . . . in the GUI.
	 */
	public boolean isOtherMove();

	/**
	 * @param detailedString The full detailed string of the action
	 * @param data           The data to extract
	 * @return The extracted data in a string. If not found, an empty String.
	 */
	public static String extractData(final String detailedString, final String data)
	{
		final int fromIndex = detailedString.indexOf(data + "=");

		if (fromIndex < 0)
			return "";

		final String beginData = detailedString.substring(fromIndex);

		int toIndex = beginData.indexOf(",");

		// Special case for masked and invisible (which are array of data)
		if (data.equals("masked") || data.equals("invisible"))
		{
			final String afterData = beginData.substring(beginData.indexOf("=") + 1);
			int toSpecialIndex = afterData.indexOf("=");

			if (toSpecialIndex < 0)
				return afterData.substring(0, afterData.length() - 1);

			while (afterData.charAt(toSpecialIndex) != ',')
				toSpecialIndex--;

			return afterData.substring(0, toSpecialIndex);
		}

		if (toIndex < 0)
			toIndex = beginData.indexOf("]");

		if (toIndex < 0)
			return "";

		return beginData.substring(beginData.indexOf("=") + 1, toIndex);
	}

	/**
	 * @param context     The context.
	 * @param movesLudeme The Moves ludeme where come from the move.
	 * @return Accumulated flags corresponding to the action/move concepts.
	 */
	public BitSet concepts(final Context context, final Moves movesLudeme);
}
