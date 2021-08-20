package app.utils;

import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import app.move.MoveFormat;
import game.types.play.ModeType;
import other.action.Action;
import other.context.Context;
import other.move.Move;

public class MoveUtil 
{

	//-------------------------------------------------------------------------
	
	/** 
	 * Gets the action string for a specified Action object.
	 */
	public static String getActionFormat(final Action action, final Context context, final boolean shortMoveFormat, final boolean useCoords)
	{
		if (shortMoveFormat)
			return action.toTurnFormat(context.currentInstanceContext(), useCoords);
		else
			return action.toMoveFormat(context.currentInstanceContext(), useCoords);
	}
	
	//-------------------------------------------------------------------------
	
	public static String getMoveFormat(final PlayerApp app, final Move move, final Context context)
	{
		final MoveFormat settingMoveFormat = app.settingsPlayer().moveFormat();
		final boolean useCoords = app.settingsPlayer().isMoveCoord();
		
		// Full move format
		if (settingMoveFormat.equals(MoveFormat.Full))
		{
			final List<Action> actionsToPrint = new ArrayList<>(move.actions());
			final StringBuilder completeActionLastMove = new StringBuilder();

			for (final Action a : actionsToPrint)
			{
				if (completeActionLastMove.length() > 0)
					completeActionLastMove.append(", ");
				completeActionLastMove.append(a.toMoveFormat(context.currentInstanceContext(), useCoords));
			}
			if (actionsToPrint.size() > 1)
			{
				completeActionLastMove.insert(0, '[');
				completeActionLastMove.append(']');
			}
			
			return completeActionLastMove.toString();
		}
		
		// Default/Short move format
		else
		{
			final boolean shortMoveFormat = settingMoveFormat.equals(MoveFormat.Short);
			
			if (context.game().mode().mode() == ModeType.Simultaneous)
			{
				String moveToPrint = "";
				for (final Action action : move.actions())
					if (action.isDecision())
						moveToPrint += getActionFormat(action, context, shortMoveFormat, useCoords) + ", ";

				if (moveToPrint.length() > 0)
					return moveToPrint.substring(0, moveToPrint.length() - 2);
				
				return ".\n";
			}
			else if (context.game().mode().mode() == ModeType.Simulation)
			{
				String moveToPrint = "";
				for (final Action action : move.actions())
					moveToPrint += getActionFormat(action, context, shortMoveFormat, useCoords) + ", ";

				if (moveToPrint.length() > 0)
					return moveToPrint.substring(0, moveToPrint.length() - 2);
				
				return ".\n";
			}
			else
			{
				for (final Action action : move.actions())
					if (action.isDecision())
						return getActionFormat(action, context, shortMoveFormat, useCoords);
			}

			return ".\n";
		}
	}
	
	//-------------------------------------------------------------------------
	
}
