package app.move;

import java.awt.Point;

import app.PlayerApp;
import app.utils.sandbox.SandboxUtil;
import main.Constants;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import util.LocationUtil;

/**
 * Functions that handle any "mouse" actions.
 * 
 * @author Matthew.Stephenson
 */
public class MouseHandler 
{

	//-------------------------------------------------------------------------
	
	/**
	 * Code that is applied when a mouse is pressed.
	 */
	public static void mousePressedCode(final PlayerApp app, final Point pressedPoint)
	{
		if (!mouseChecks(app))
			return;
		
		final Context context = app.contextSnapshot().getContext(app);	

		if (app.bridge().settingsVC().selectedFromLocation().equals(new FullLocation(Constants.UNDEFINED)))
			app.settingsPlayer().setComponentIsSelected(false);
		
		// Can't select pieces if the AI is moving
		if (app.manager().aiSelected()[context.state().playerToAgent(context.state().mover())].ai() != null && !app.manager().settingsManager().agentsPaused())
			return;

		// Get the nearest valid from location to the pressed point.
		if (app.settingsPlayer().sandboxMode())
			app.bridge().settingsVC().setSelectedFromLocation(LocationUtil.calculateNearestLocation(context, app.bridge(), pressedPoint, LocationUtil.getAllLocations(context, app.bridge())));
		else if (!app.settingsPlayer().componentIsSelected())
			app.bridge().settingsVC().setSelectedFromLocation(LocationUtil.calculateNearestLocation(context, app.bridge(), pressedPoint, LocationUtil.getLegalFromLocations(context)));
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Code that is applied when a mouse is released.
	 */
	public static void mouseReleasedCode(final PlayerApp app, final Point releasedPoint) 
	{
		if (!mouseChecks(app))
			return;
		
		final Context context = app.contextSnapshot().getContext(app);	
		final Location selectedFromLocation = app.bridge().settingsVC().selectedFromLocation();
		Location selectedToLocation;

		if (app.bridge().settingsVC().selectingConsequenceMove() || app.settingsPlayer().sandboxMode())
			selectedToLocation = LocationUtil.calculateNearestLocation(context, app.bridge(), releasedPoint, LocationUtil.getAllLocations(context, app.bridge()));	
		else
			selectedToLocation = LocationUtil.calculateNearestLocation(context, app.bridge(), releasedPoint, LocationUtil.getLegalToLocations(app.bridge(), context));
		
		// Account for any large component offsets
		if 
		(
			app.settingsPlayer().dragComponent() != null 
			&& 
			app.bridge().getComponentStyle(app.settingsPlayer().dragComponent().index()).getLargeOffsets().size() 
			> 
			app.settingsPlayer().dragComponentState()
		)
		{
			final Point newPoint = releasedPoint;
			newPoint.x = (int) (newPoint.x - app.bridge().getComponentStyle(app.settingsPlayer().dragComponent().index()).getLargeOffsets().get(app.settingsPlayer().dragComponentState()).getX());
			newPoint.y = (int) (newPoint.y + app.bridge().getComponentStyle(app.settingsPlayer().dragComponent().index()).getLargeOffsets().get(app.settingsPlayer().dragComponentState()).getY());
			selectedToLocation = LocationUtil.calculateNearestLocation(context, app.bridge(), newPoint, LocationUtil.getLegalToLocations(app.bridge(), context));
		}
		
		if (context.game().isDeductionPuzzle())
		{
			MoveHandler.tryPuzzleMove(app, selectedFromLocation, selectedToLocation);
		}
		else
		{
			if (app.settingsPlayer().sandboxMode())
			{
				SandboxUtil.makeSandboxDragMove(app, selectedFromLocation, selectedToLocation);
			}
			else if (MoveHandler.tryGameMove(app, selectedFromLocation, selectedToLocation))
			{
				// Need to do this to allow click moves to still be applied if the dialog is closed. Mostly for web player functionality.
				app.settingsPlayer().setComponentIsSelected(true);
			}
			else
			{
				// Remember the selected From location for next time.
				if (!app.settingsPlayer().componentIsSelected() && app.bridge().settingsVC().lastClickedSite().equals(selectedFromLocation))
				{
					app.settingsPlayer().setComponentIsSelected(true);
				}
				else
				{
					app.settingsPlayer().setComponentIsSelected(false);
					app.setVolatileMessage("That is not a valid move.");
				}
			}
		}

		if (!app.settingsPlayer().componentIsSelected())
			app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
			
		app.bridge().settingsVC().setPieceBeingDragged(false);
		app.settingsPlayer().setCurrentWalkExtra(0);
		app.repaint();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Code that is applied when a mouse is clicked.
	 */
	public static void mouseClickedCode(final PlayerApp app, final Point point) 
	{
		if (!mouseChecks(app))
			return;
		
		// Store the last clicked location, used for dev display and selecting pieces.
		final Context context = app.contextSnapshot().getContext(app);
		final Location clickedLocation = LocationUtil.calculateNearestLocation(context, app.bridge(), point, LocationUtil.getLegalFromLocations(context));		// Change this back to all locations if any problems.
		app.bridge().settingsVC().setLastClickedSite(clickedLocation);
		app.repaint();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Code that is applied when a mouse is dragged.
	 */
	public static void mouseDraggedCode(final PlayerApp app, final Point point) 
	{
		if (!mouseChecks(app))
			return;
		
		final Context context = app.contextSnapshot().getContext(app);
		app.bridge().settingsVC().setSelectedFromLocation(app.bridge().settingsVC().selectedFromLocation());
		
		// Can't drag pieces in a deduction puzzle.
		if (context.game().isDeductionPuzzle())
			return;
		
		// repaint the whole view when a piece starts to be dragged.
		if (!app.bridge().settingsVC().pieceBeingDragged())
			app.repaint();

		// Repaint between the dragged points and update location of dragged piece.
		app.repaintComponentBetweenPoints
		(
			context, 
			app.bridge().settingsVC().selectedFromLocation(), 
			app.settingsPlayer().oldMousePoint(), 
			point
		);
		app.bridge().settingsVC().setPieceBeingDragged(true);
		app.settingsPlayer().setOldMousePoint(point);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Checks that need to be made before any code specific to mouse actions is performed.
	 */
	private static boolean mouseChecks(final PlayerApp app)
	{
		if (app.manager().settingsNetwork().getActiveGameId() != 0)
		{
			if (app.contextSnapshot().getContext(app).state().mover() != app.manager().settingsNetwork().getNetworkPlayerNumber())
			{
				app.setVolatileMessage("Wait your turn!");
				return false;
			}
			for (int i = 1; i <= app.contextSnapshot().getContext(app).game().players().count(); i++)
			{
				if (app.manager().aiSelected()[i].name().trim().equals(""))
				{
					app.setVolatileMessage("Not all players have joined yet.");
					return false;
				}
			}
		}

		return true;
	}
	
	//-------------------------------------------------------------------------
	
}
