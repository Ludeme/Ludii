package app.display.views.tabs.pages;

import java.awt.Rectangle;

import app.PlayerApp;
import app.display.views.tabs.TabPage;
import app.display.views.tabs.TabView;
import app.utils.TrialUtil;
import game.types.play.ModeType;
import other.action.Action;
import other.context.Context;
import other.location.Location;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;
import util.ContainerUtil;
import util.HiddenUtil;

/**
 * Tab for displaying all turns that have been made.
 * 
 * @author Matthew.Stephenson
 */
public class TurnsPage extends TabPage
{
	
	//-------------------------------------------------------------------------

	public static int turnNumber = 0;
	public static int lastMover = -100;
	
	//-------------------------------------------------------------------------
	
	public TurnsPage(final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent)
	{
		super(app, rect, title, text, pageIndex, parent);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void updatePage(final Context context)
	{	
		// Don't display turns if playing an online game with hidden information.
		if (app.manager().settingsNetwork().getActiveGameId() != 0 && app.contextSnapshot().getContext(app).game().hiddenInformation())
			return;
		
		lastMover = -100;
		turnNumber = 0;
		
		clear();
		final int trialStartPoint = TrialUtil.getInstanceStartIndex(context);
		final int trialEndPoint = TrialUtil.getInstanceEndIndex(app.manager(), context);
		
		addText("");
		addFadedText("");
		
		for (int i = trialStartPoint; i < context.trial().numMoves(); i++)
		{
			addText(getTurnStringToDisplay(context, i));
		}
		
		if (app.manager().savedTrial() != null)
		{
			for (int i = context.trial().numMoves(); i < trialEndPoint; i++)
			{
				addFadedText(getTurnStringToDisplay(context, i));
			}
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Gets the turn string for a specified move number in the current trial.
	 */
	private String getTurnStringToDisplay(final Context context, final int moveNumber)
	{
		Trial longestTrial = context.trial();
		if (app.manager().savedTrial() != null)
			longestTrial = app.manager().savedTrial();
		
		final Move lastMove = longestTrial.getMove(moveNumber);
		
		// If the move's from or to is hidden or masked then don't show the move.
		boolean keepSecret = false;
		final int moverToPrint = lastMove.mover();
		final int playerMoverId = context.state().mover();
		if (longestTrial.lastMove() != null && playerMoverId != moverToPrint && !longestTrial.lastMove().isPass() && !longestTrial.lastMove().isSwap())
		{
			final State state = context.state();
			final Location locationFrom = lastMove.getFromLocation();
			final int containerIdFrom = ContainerUtil.getContainerId(context, locationFrom.site(), locationFrom.siteType());
			final Location locationTo = lastMove.getToLocation();
			final int containerIdTo = ContainerUtil.getContainerId(context, locationTo.site(), locationTo.siteType());
			
			if (containerIdFrom != -1 && containerIdTo != -1)
			{
				final ContainerState csFrom = state.containerStates()[containerIdFrom];
				final ContainerState csTo = state.containerStates()[containerIdTo];
				if (HiddenUtil.siteHidden(context, csFrom, locationFrom.site(), locationFrom.level(), playerMoverId, locationFrom.siteType()) 
						|| HiddenUtil.siteHidden(context, csTo, locationTo.site(), locationTo.level(), playerMoverId, locationTo.siteType()))
				{
					keepSecret = true;
				}
			}
		}

		String stringMove = ". ";
		
		final boolean useCoords = app.settingsPlayer().isMoveCoord();
		
		if (context.game().mode().mode() == ModeType.Simultaneous)
		{
			for (final Action action : lastMove.actions())
				if (action.isDecision())
					stringMove += action.toTurnFormat(context.currentInstanceContext(), useCoords) + ", ";

			if (stringMove.length() > 0)
				stringMove = stringMove.substring(0, stringMove.length() - 2);
		}
		else if (context.game().mode().mode() == ModeType.Simulation)
		{
			for (final Action action : lastMove.actions())
				stringMove += action.toTurnFormat(context.currentInstanceContext(), useCoords) + ", ";

			if (stringMove.length() > 0)
				stringMove = stringMove.substring(0, stringMove.length() - 2);
		}
		else
		{
			for (final Action action : lastMove.actions())
				if (action.isDecision())
				{
					stringMove = action.toTurnFormat(context.currentInstanceContext(), useCoords);
					break;
				}
		}
		String textToAdd = "";

		if (lastMove.mover() != lastMover)
		{
			turnNumber++;

			if (turnNumber != 1)
				textToAdd += "\n";

			if (keepSecret)
				textToAdd += "Turn " + turnNumber + ". -";
			else
				textToAdd += "Turn " + turnNumber + ". " + stringMove;
		}
		else
		{
			textToAdd += ", " + stringMove;
		}

		lastMover = lastMove.mover();
		return textToAdd;
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
