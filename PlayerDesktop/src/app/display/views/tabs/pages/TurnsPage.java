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
import other.state.container.ContainerState;
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
		
		String newSolidText = "";
		String newFadedText = "";
		
		for (int i = TrialUtil.getInstanceStartIndex(context); i < context.trial().numMoves(); i++)
			newSolidText += getTurnStringToDisplay(context, context.trial().getMove(i));
		
		if (app.manager().undoneMoves() != null)
			for (int i = 0; i < app.manager().undoneMoves().size(); i++)
				newFadedText += getTurnStringToDisplay(context, app.manager().undoneMoves().get(i));
		
		if (!newSolidText.equals(solidText) || !newFadedText.equals(fadedText))
		{
			clear();
			addText(newSolidText);
			addFadedText(newFadedText);
		}		
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Gets the turn string for a specified move number in the current trial.
	 */
	private String getTurnStringToDisplay(final Context context, final Move move)
	{
		// If the move's from or to is hidden then don't show the move.
		boolean keepSecret = false;
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
				keepSecret = true;
			}
		}

		String stringMove = ". ";
		
		final boolean useCoords = app.settingsPlayer().isMoveCoord();
		
		if (context.game().mode().mode() == ModeType.Simultaneous)
		{
			for (final Action action : move.actions())
				if (action.isDecision())
					stringMove += action.toTurnFormat(context.currentInstanceContext(), useCoords) + ", ";

			if (stringMove.length() > 0)
				stringMove = stringMove.substring(0, stringMove.length() - 2);
		}
		else if (context.game().mode().mode() == ModeType.Simulation)
		{
			for (final Action action : move.actions())
				stringMove += action.toTurnFormat(context.currentInstanceContext(), useCoords) + ", ";

			if (stringMove.length() > 0)
				stringMove = stringMove.substring(0, stringMove.length() - 2);
		}
		else
		{
			for (final Action action : move.actions())
				if (action.isDecision())
				{
					stringMove = action.toTurnFormat(context.currentInstanceContext(), useCoords);
					break;
				}
		}
		String textToAdd = "";

		if (move.mover() != lastMover)
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

		lastMover = move.mover();
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
