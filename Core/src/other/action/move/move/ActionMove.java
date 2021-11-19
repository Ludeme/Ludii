package other.action.move.move;

import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

/**
 * Moves a piece from a site to another (only one piece or one full stack).
 *
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class ActionMove extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @param typeFrom   The graph element type of the from site.
	 * @param from       From site index.
	 * @param levelFrom  From level index.
	 * @param typeTo     The graph element type of the to site.
	 * @param to         To site index.
	 * @param levelTo    To level index.
	 * @param state      The state site of the to site.
	 * @param rotation   The rotation value of the to site.
	 * @param value      The piece value of the to site.
	 * @param onStacking True if we move a full stack.
	 */
	public static BaseAction construct
	(
		final SiteType typeFrom,
		final int from,
		final int levelFrom,
		final SiteType typeTo,
		final int to,
		final int levelTo,
		final int state,
		final int rotation,
		final int value,
		final boolean onStacking
	)
	{
		if(onStacking)
			return new ActionMoveStacking(typeFrom, from, levelFrom, typeTo, to, levelTo, state, rotation, value, onStacking);
		else
			return new ActionMoveTopPiece(typeFrom, from, levelFrom, typeTo, to, levelTo, state, rotation, value, onStacking);
	}

	/**
	 * Reconstructs an ActionMove object from a detailed String (generated using toDetailedString())
	 * @param detailedString
	 */
	public static BaseAction construct(final String detailedString)
	{ 
		assert (detailedString.startsWith("[Move:"));

		final String strTypeFrom = Action.extractData(detailedString, "typeFrom");
		final SiteType typeFrom = (strTypeFrom.isEmpty()) ? null : SiteType.valueOf(strTypeFrom);

		final String strFrom = Action.extractData(detailedString, "from");
		final int from = Integer.parseInt(strFrom);

		final String strLevelFrom = Action.extractData(detailedString, "levelFrom");
		final int levelFrom = (strLevelFrom.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevelFrom);
		
		final String strTypeTo = Action.extractData(detailedString, "typeTo");
		final SiteType typeTo = (strTypeTo.isEmpty()) ? null : SiteType.valueOf(strTypeTo);

		final String strTo = Action.extractData(detailedString, "to");
		final int to = Integer.parseInt(strTo);
		
		final String strLevelTo = Action.extractData(detailedString, "levelTo");
		final int levelTo = (strLevelTo.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevelTo);

		final String strState = Action.extractData(detailedString, "state");
		final int state = (strState.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strState);

		final String strRotation = Action.extractData(detailedString, "rotation");
		final int rotation = (strRotation.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strRotation);

		final String strValue = Action.extractData(detailedString, "value");
		final int value = (strValue.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strValue);

		final String strStack = Action.extractData(detailedString, "stack");
		final boolean onStacking = (strStack.isEmpty()) ? false : Boolean.parseBoolean(strStack);

		final String strDecision = Action.extractData(detailedString, "decision");
		final boolean decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
		
		BaseAction action = null;
		
		if(onStacking)
			action = new ActionMoveStacking(typeFrom, from, levelFrom, typeTo, to, levelTo, state, rotation, value, onStacking);
		else
			action =  new ActionMoveTopPiece(typeFrom, from, levelFrom, typeTo, to, levelTo, state, rotation, value, onStacking);
		
		action.setDecision(decision);
		
		return action;
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply
	(
		final Context context,
		final boolean store
	)
	{
		throw new UnsupportedOperationException("ActionMove.eval(): Should never be called directly.");
	}

	@Override
	public Action undo(final Context context)
	{
		throw new UnsupportedOperationException("ActionMove.undo(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------
	@Override
	public String toTrialFormat(final Context context)
	{

		// Should never be there
		return null;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		// Should never be there
		return null;
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		// Should never be there
		return null;
	}

	@Override
	public ActionType actionType()
	{
		return ActionType.Move;
	}

	//-------------------------------------------------------------------------
}
