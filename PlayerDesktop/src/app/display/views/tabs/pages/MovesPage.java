package app.display.views.tabs.pages;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import app.display.views.tabs.TabPage;
import app.display.views.tabs.TabView;
import app.utils.TrialUtil;
import game.types.play.ModeType;
import other.action.Action;
import other.context.Context;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import util.ContainerUtil;
import util.HiddenUtil;

/**
 * Tab for displaying all moves that have been made.
 * 
 * @author Matthew.Stephenson
 */
public class MovesPage extends TabPage
{
	
	//-------------------------------------------------------------------------

	public MovesPage(final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent)
	{
		super(app, rect, title, text, pageIndex, parent);
	}

	@Override
	public void updatePage(final Context context)
	{		
		// Don't display moves if playing an online game with hidden information.
		if (app.manager().settingsNetwork().getActiveGameId() != 0 && app.contextSnapshot().getContext(app).game().hiddenInformation())
			return;
		
		String newSolidText = "";
		String newFadedText = "";
		
		for (int i = TrialUtil.getInstanceStartIndex(context); i < context.trial().numMoves(); i++)
			newSolidText += getMoveStringToDisplay(context, context.trial().getMove(i), i);
		
		if (app.manager().undoneMoves().size() > 0)
			for (int i = 0; i < app.manager().undoneMoves().size(); i++)
				newFadedText += getMoveStringToDisplay(context, app.manager().undoneMoves().get(i), context.trial().numMoves() + i);
		
		if (!newSolidText.equals(solidText) || !newFadedText.equals(fadedText))
		{
			clear();
			addText(newSolidText);
			addFadedText(newFadedText);
		}
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Gets the move string for a specified move number in the current trial.
	 */
	private String getMoveStringToDisplay(final Context context, final Move move, final int moveNumber)
	{
		final String settingMoveFormat = app.settingsPlayer().moveFormat();
		final boolean useCoords = app.settingsPlayer().isMoveCoord();
		
		// If the move's from or to is hidden then don't show the move.
		final int moverToPrint = move.mover();
		final int playerMoverId = app.contextSnapshot().getContext(app).pointofView();
		final Location locationFrom = move.getFromLocation();
		final int containerIdFrom = ContainerUtil.getContainerId(context, locationFrom.site(), locationFrom.siteType());
		final Location locationTo = move.getToLocation();
		final int containerIdTo = ContainerUtil.getContainerId(context, locationTo.site(), locationTo.siteType());
		if (containerIdFrom != -1 && containerIdTo != -1)
		{
			final ContainerState csFrom = context.state().containerStates()[containerIdFrom];
			final ContainerState csTo = context.state().containerStates()[containerIdTo];
			if (HiddenUtil.siteHiddenBitsetInteger(context, csFrom, locationFrom.site(), locationFrom.level(), playerMoverId, locationFrom.siteType()) > 0
					|| HiddenUtil.siteHiddenBitsetInteger(context, csTo, locationTo.site(), locationTo.level(), playerMoverId, locationTo.siteType()) > 0)
			{
				final int moveNumberToPrint = moveNumber - TrialUtil.getInstanceStartIndex(context) + 1;
				if (moverToPrint > 0)
					return	(moveNumberToPrint) + ". (" + moverToPrint + ") \n";
				else
					return (moveNumberToPrint) + ". \n";
			}
		}

		// Full move format
		if (settingMoveFormat.equals("Full"))
		{
			final List<Action> actionsToPrint = new ArrayList<>(move.actions());
			final StringBuilder completeActionLastMove = new StringBuilder();
			
			final int moveNumberToPrint = moveNumber - TrialUtil.getInstanceStartIndex(context) + 1;

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
			completeActionLastMove.append('\n');
			completeActionLastMove.insert(0, moveNumberToPrint + ". ");
			
			return completeActionLastMove.toString();
		}
		// Default/Short move format
		else
		{
			boolean shortMoveFormat = true;
			if (settingMoveFormat.equals("Move"))
				shortMoveFormat = false;
			
			if (context.game().mode().mode() == ModeType.Simultaneous)
			{
				String moveToPrint = "";
				for (final Action action : move.actions())
					if (action.isDecision())
						moveToPrint += getActionFormat(action, context, shortMoveFormat, useCoords) + ", ";

				if (moveToPrint.length() > 0)
				{
					final int moveNumberToPrint = moveNumber - TrialUtil.getInstanceStartIndex(context) + 1;
					return (moveNumberToPrint) + ". " + moveToPrint.substring(0, moveToPrint.length() - 2) + "\n";
				}
				return ".\n";
			}
			else if (context.game().mode().mode() == ModeType.Simulation)
			{
				String moveToPrint = "";
				for (final Action action : move.actions())
					moveToPrint += getActionFormat(action, context, shortMoveFormat, useCoords) + ", ";

				if (moveToPrint.length() > 0)
				{
					final int moveNumberToPrint = moveNumber - TrialUtil.getInstanceStartIndex(context) + 1;
					return (moveNumberToPrint) + ". " + moveToPrint.substring(0, moveToPrint.length() - 2) + "\n";
				}
				return ".\n";
			}
			else
			{
				for (final Action action : move.actions())
					if (action.isDecision())
					{
						final int moveNumberToPrint = moveNumber - TrialUtil.getInstanceStartIndex(context) + 1;
						return (moveNumberToPrint) + ". " + getActionFormat(action, context, shortMoveFormat, useCoords) + "\n";
					}
			}

			return ".\n";
		}
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Gets the action string for a specified Action object.
	 */
	private static String getActionFormat(final Action action, final Context context, final boolean shortMoveFormat, final boolean useCoords)
	{
		if (shortMoveFormat)
			return action.toTurnFormat(context.currentInstanceContext(), useCoords);
		else
			return action.toMoveFormat(context.currentInstanceContext(), useCoords);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset()
	{
		clear();
		updatePage(app.contextSnapshot().getContext(app));
	}
	
	//-------------------------------------------------------------------------

}
