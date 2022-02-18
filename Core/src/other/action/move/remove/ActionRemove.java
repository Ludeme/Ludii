package other.action.move.remove;

import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.context.Context;

@SuppressWarnings("javadoc")
public class ActionRemove extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type    The graph element type.
	 * @param to      Location to remove the component(s).
	 * @param level   Level to remove the component(s).
	 * @param applied True if the action has to be applied immediately.
	 */
	public static BaseAction construct
	(
		final SiteType type,
		final int to, 
		final int level, 
		final boolean applied
	)
	{
		if(!applied)
			return new ActionRemoveNonApplied(type, to);
		else if(level != Constants.UNDEFINED)
			return new ActionRemoveLevel(type, to, level);
		else
			return new ActionRemoveTopPiece(type, to);
	}

	/**
	 * Reconstructs a ActionRemove object from a detailed String (generated using toDetailedString())
	 * 
	 * @param detailedString
	 */
	public static BaseAction construct (final String detailedString) 
	{
		assert (detailedString.startsWith("[Remove:"));

		final String strType = Action.extractData(detailedString, "type");
		final SiteType type = (strType.isEmpty()) ? null : SiteType.valueOf(strType);

		final String strTo = Action.extractData(detailedString, "to");
		final int to = Integer.parseInt(strTo);

		final String strLevel = Action.extractData(detailedString, "level");
		final int level = (strLevel.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevel);

		final String strApplied = Action.extractData(detailedString, "applied");
		final boolean applied = (strApplied.isEmpty()) ? true : Boolean.parseBoolean(strApplied);

		final String strDecision = Action.extractData(detailedString, "decision");
		final boolean decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
		
		BaseAction action = null;
		
		if(!applied)
			action = new ActionRemoveNonApplied(type, to);
		else if(level != Constants.UNDEFINED)
			action = new ActionRemoveLevel(type, to, level);
		else
			action=  new ActionRemoveTopPiece(type, to);
		
		action.setDecision(decision);
		return action;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Action apply(Context context, boolean store)
	{
		throw new UnsupportedOperationException("ActionRemove.eval(): Should never be called directly.");
	}

	@Override
	public Action undo(Context context, boolean discard)
	{
		throw new UnsupportedOperationException("ActionRemove.undo(): Should never be called directly.");
	}

	@Override
	public String toTrialFormat(Context context)
	{
		// Should never be there
		return null;
	}

	@Override
	public String getDescription()
	{
		// Should never be there
		return null;
	}

	@Override
	public String toTurnFormat(Context context, boolean useCoords)
	{
		// Should never be there
		return null;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.Remove;
	}

	//-------------------------------------------------------------------------
	
}
